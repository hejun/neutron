package io.github.hejun.neutron.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 租户 DTO
 *
 * @author HeJun
 */
@Getter
@Setter
public class UserDTO {

	private String id;

	@NotBlank(message = "账户不可为空")
	@Size(min = 2, max = 20, message = "账户应在2-20个字之间")
	private String username;

	@NotBlank(message = "密码不可为空")
	@Size(min = 2, max = 20, message = "名称应在2-20个字之间")
	private String password;

	private Boolean enabled;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private Date createDate;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private Date lastModifiedDate;

}
