package io.github.hejun.neutron.notify.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 消息
 *
 * @author HeJun
 */
@Getter
@Setter
public class MsgDTO {

    @NotBlank(message = "接收人不可为空")
    private String receiver;

    @NotBlank(message = "内容不可为空")
    private String content;

}
