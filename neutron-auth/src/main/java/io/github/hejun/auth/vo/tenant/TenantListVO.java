package io.github.hejun.auth.vo.tenant;

import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.util.Date;

/**
 * 租户 ListVO
 *
 * @author HeJun
 */
@Getter
@Setter
public class TenantListVO {

	/**
	 * 主键
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;

	/**
	 * 租户名
	 */
	private String name;

	/**
	 * 发行域名
	 */
	private String issuer;

	/**
	 * 是否启用, 0: 否, 1: 是. 默认: 1
	 */
	private Boolean enabled;

	/**
	 * 创建时间
	 */
	private Date createdDate;

}
