package io.github.hejun.neutron.notify.service.impl;

import io.github.hejun.neutron.common.core.dto.Result;
import io.github.hejun.neutron.notify.dto.MsgDTO;
import io.github.hejun.neutron.notify.service.IMsgService;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.incubator.propagation.ExtendedContextPropagators;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息服务的 Redis 实现
 *
 * @author HeJun
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMsgServiceImpl implements IMsgService, MessageListener {

    private static final Map<String, SseEmitter> emitters = new HashMap<>();
    private static final String CHANNEL = "neutron.notify.msg";

    private final RedisTemplate<String, RedisMsgDTO> redisTemplate;
    private final OpenTelemetry openTelemetry;
    private final Tracer tracer;

    @Override
    public SseEmitter connect() throws Exception {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            throw new AccessDeniedException("Unauthenticated");
        }

        String sender = SecurityContextHolder.getContext().getAuthentication().getName();

        SseEmitter emitter = new SseEmitter(Duration.ofMinutes(5).toMillis());
        emitter.send(SseEmitter.event().name("connected").data(Result.SUCCESS()).build());
        emitter.onCompletion(() -> emitters.remove(sender));
        emitter.onTimeout(() -> emitters.remove(sender));
        emitters.put(sender, emitter);

        log.info("Connected from {}", sender);
        return emitter;
    }

    @Override
    public void send(MsgDTO msg) throws IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            throw new AccessDeniedException("Unauthenticated");
        }

        String sender = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("{} send message, receiver: {}", sender, msg.getReceiver());

        RedisMsgDTO redisMsg = new RedisMsgDTO();
        redisMsg.setSender(sender);
        redisMsg.setReceiver(msg.getReceiver());
        redisMsg.setContent(msg.getContent());

        Map<String, String> context = ExtendedContextPropagators
            .getTextMapPropagationContext(openTelemetry.getPropagators());
        redisMsg.setTraceContext(context);

        redisTemplate.convertAndSend(CHANNEL, redisMsg);
    }

    @Override
    public void onMessage(Message message, byte @Nullable [] pattern) {
        RedisMsgDTO msg = (RedisMsgDTO) redisTemplate.getValueSerializer().deserialize(message.getBody());
        if (msg == null) {
            log.error("msg is empty");
            return;
        }

        Context context = ExtendedContextPropagators
            .extractTextMapPropagationContext(msg.getTraceContext(), openTelemetry.getPropagators());
        Span span = tracer
            .spanBuilder("handleMsg")
            .setParent(context)
            .setSpanKind(SpanKind.CONSUMER)
            .startSpan();

        try (Scope ignored = span.makeCurrent()) {
            log.info("hand msg, sender: {}, receiver: {}", msg.getSender(), msg.getReceiver());
            SseEmitter emitter = emitters.get(msg.getReceiver());
            if (emitter != null) {
                String format = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
                emitter.send(Result.SUCCESS("Send from %s, Content %s, Now: %s".formatted(msg.getSender(), msg.getContent(), format)));
            } else if ("*".equals(msg.getReceiver())) {
                for (SseEmitter e : emitters.values()) {
                    String format = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
                    e.send(Result.SUCCESS("Send from %s, Content %s, Now: %s".formatted(msg.getSender(), msg.getContent(), format)));
                }
            } else {
                log.error("send from {}, no receiver found", msg.getSender());
            }
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
        } finally {
            span.end();
        }
    }

    @Bean
    public RedisMessageListenerContainer messageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(this, new PatternTopic(CHANNEL));
        return container;
    }

    @Getter
    @Setter
    public static final class RedisMsgDTO {

        private String sender;
        private String receiver;
        private String content;

        // 用于存放 OTel 的追踪上下文, 不参与实际业务
        private Map<String, String> traceContext;
    }

}
