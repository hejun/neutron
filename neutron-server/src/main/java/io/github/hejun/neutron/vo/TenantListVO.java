package io.github.hejun.neutron.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
public class TenantListVO {

	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;

	private String name;

	private String issuer;

	private Boolean enabled;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private Date createDate;

}
