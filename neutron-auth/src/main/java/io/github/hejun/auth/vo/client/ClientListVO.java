package io.github.hejun.auth.vo.client;

import io.github.hejun.auth.vo.tenant.TenantListVO;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.util.Date;
import java.util.Set;

/**
 * 客户端 ListVO
 *
 * @author HeJun
 */
@Getter
@Setter
public class ClientListVO {

	/**
	 * 主键
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;

	/**
	 * 客户端ID
	 */
	private String clientId;

	/**
	 * 客户端名称
	 */
	private String clientName;

	/**
	 * 授权方式
	 */
	private Set<String> authorizationGrantTypes;

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
