package io.github.hejun.neutron.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新密码 DTO
 *
 * @author HeJun
 */
@Getter
@Setter
public class UpdateUserPasswordDTO {

	@NotBlank(message = "主键不可为空")
	private String id;

	@NotBlank(message = "原密码为空")
	@Size(min = 2, max = 50, message = "原密码应在2-50个字之间")
	private String oldPassword;

	@NotBlank(message = "新密码不可为空")
	@Size(min = 2, max = 50, message = "新密码应在2-50个字之间")
	private String newPassword;

}
