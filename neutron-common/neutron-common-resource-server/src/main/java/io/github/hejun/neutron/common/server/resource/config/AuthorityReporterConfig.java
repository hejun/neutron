package io.github.hejun.neutron.common.server.resource.config;

import io.github.hejun.neutron.common.server.resource.service.AuthorityReportService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 资源上报配置
 *
 * @author HeJun
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class AuthorityReporterConfig {

    private static final Pattern AUTHORITY_PATTERN = Pattern.compile("has(?:Any)?Authority\\(([^)]+)\\)");
    private static final Pattern CODE_PATTERN = Pattern.compile("'([^']+)'");

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class ServletEnvAuthorityReporterConfig {

        public ApplicationListener<@NonNull ContextRefreshedEvent> reportAuthorityApplicationListener() {
            return event -> {
                log.debug("web env reporting authorities");

                ApplicationContext context = event.getApplicationContext();
                org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping handlerMapping = context
                    .getBean("requestMappingHandlerMapping", org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping.class);
                Map<org.springframework.web.servlet.mvc.method.RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

                List<String> permissionList = new ArrayList<>();

                for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
                    RequestMappingInfo mappingInfo = entry.getKey();
                    HandlerMethod handlerMethod = entry.getValue();
                    Method method = handlerMethod.getMethod();

                    PreAuthorize preAuthorize = AnnotationUtils.findAnnotation(method, PreAuthorize.class);
                    if (preAuthorize == null) {
                        preAuthorize = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), PreAuthorize.class);
                    }

                    if (preAuthorize == null || preAuthorize.value().isBlank()) {
                        continue;
                    }

                    List<String> rawCodes = extractAuthorityCodes(preAuthorize.value());

                    Set<String> patterns = mappingInfo.getPatternValues();
                    String path = patterns.isEmpty() ? "" : patterns.iterator().next();

                    Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();
                    String httpMethod = methods.isEmpty() ? "ALL" : methods.iterator().next().name();

                    for (String rawCode : rawCodes) {
                        String finalCode = context.getApplicationName() + ":" + rawCode;

                        finalCode += ":" + method.getName() + ":" + path + ":" + httpMethod;
                        permissionList.add(finalCode);
                    }
                }

                log.debug("permission list: {}", permissionList);
            };
        }

    }

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class ReactiveEnvAuthorityReporterConfig {

        public ApplicationListener<@NonNull ContextRefreshedEvent> reportAuthorityApplicationListener() {
            return event -> {
                log.debug("reactive env reporting authorities");

                ApplicationContext context = event.getApplicationContext();
                org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping handlerMapping = context
                    .getBean("requestMappingHandlerMapping", org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping.class);
                Map<org.springframework.web.reactive.result.method.RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

                List<String> permissionList = new ArrayList<>();

                for (Map.Entry<org.springframework.web.reactive.result.method.RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
                    org.springframework.web.reactive.result.method.RequestMappingInfo mappingInfo = entry.getKey();
                    HandlerMethod handlerMethod = entry.getValue();
                    Method method = handlerMethod.getMethod();

                    PreAuthorize preAuthorize = AnnotationUtils.findAnnotation(method, PreAuthorize.class);
                    if (preAuthorize == null) {
                        preAuthorize = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), PreAuthorize.class);
                    }

                    if (preAuthorize == null || preAuthorize.value().isBlank()) {
                        continue;
                    }

                    List<String> rawCodes = extractAuthorityCodes(preAuthorize.value());

                    Set<String> patterns = mappingInfo.getPatternCondition().getPatterns();
                    String path = patterns.isEmpty() ? "" : patterns.iterator().next();

                    Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();
                    String httpMethod = methods.isEmpty() ? "ALL" : methods.iterator().next().name();

                    for (String rawCode : rawCodes) {
                        String finalCode = context.getApplicationName() + ":" + rawCode;

                        finalCode += ":" + method.getName() + ":" + path + ":" + httpMethod;
                        permissionList.add(finalCode);
                    }
                }

                log.debug("permission list: {}", permissionList);
            };
        }

    }

    private static List<String> extractAuthorityCodes(String spEl) {
        List<String> codes = new ArrayList<>();
        Matcher matcher = AUTHORITY_PATTERN.matcher(spEl);

        while (matcher.find()) {
            String rawArgs = matcher.group(1);
            Matcher codeMatcher = CODE_PATTERN.matcher(rawArgs);
            while (codeMatcher.find()) {
                codes.add(codeMatcher.group(1));
            }
        }

        if (codes.isEmpty()) {
            Matcher fallbackMatcher = CODE_PATTERN.matcher(spEl);
            while (fallbackMatcher.find()) {
                codes.add(fallbackMatcher.group(1));
            }
        }

        return codes;
    }

}
