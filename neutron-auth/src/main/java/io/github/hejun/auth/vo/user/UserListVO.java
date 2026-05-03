package io.github.hejun.auth.vo.user;

import io.github.hejun.auth.vo.tenant.TenantListVO;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.util.Date;

/**
 * 用户 ListVO
 *
 * @author HeJun
 */
@Getter
@Setter
public class UserListVO {

	/**
	 * 主键
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;

	/**
	 * 账户
	 */
	private String username;

	/**
	 * 昵称
	 */
	private String nickname;

	/**
	 * 是否启用, 0: 否, 1: 是. 默认: 1
	 */
	private Boolean enabled;

	/**
	 * 所属租户
	 */
	private TenantListVO tenant;

	/**
	 * 创建时间
	 */
	private Date createdDate;

}
