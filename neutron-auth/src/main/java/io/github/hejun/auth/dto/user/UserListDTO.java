package io.github.hejun.auth.dto.user;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户 ListDTO
 *
 * @author HeJun
 */
@Getter
@Setter
public class UserListDTO {

	private String username;

	private Boolean enabled;

	private Long tenantId;

}
