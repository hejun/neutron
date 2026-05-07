package io.github.hejun.neutron.notify.service;

import io.github.hejun.neutron.notify.dto.MsgDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * 通知服务
 *
 * @author HeJun
 */
public interface IMsgService {

    SseEmitter connect() throws Exception;

    void send(MsgDTO dto) throws IOException;

}
