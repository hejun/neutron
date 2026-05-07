package io.github.hejun.neutron.notify.controller;

import io.github.hejun.neutron.common.core.dto.Result;
import io.github.hejun.neutron.notify.dto.MsgDTO;
import io.github.hejun.neutron.notify.service.IMsgService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * 消息 Controller
 *
 * @author HeJun
 */
@Slf4j
@RestController
@RequestMapping("/msg")
@RequiredArgsConstructor
public class MessageController {

    private final IMsgService msgService;

    @GetMapping("/connect")
    public SseEmitter connect() throws Exception {
        return msgService.connect();
    }

    @PostMapping("/send")
    public Result<Void> send(@RequestBody @Valid MsgDTO dto) throws IOException {
        msgService.send(dto);
        return Result.SUCCESS();
    }

}
