package io.github.hejun.auth.entity;

import io.github.hejun.neutron.common.persist.annotation.SnowflakeGenerator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.Date;

/**
 * 租户表
 *
 * @author HeJun
 */
@Getter
@Setter
@Entity
@Table(name = "t_tenant", indexes = {
	@Index(name = "uk_tenant_issuer", columnList = "issuer", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
public class Tenant implements Serializable {

	/**
	 * 主键
	 */
	@Id
	@SnowflakeGenerator
	private Long id;

	/**
	 * 租户名
	 */
	@Column(nullable = false)
	private String name;

	/**
	 * 发行域名
	 */
	@Column(nullable = false)
	private String issuer;

	/**
	 * 公钥
	 */
	@Column(nullable = false)
	private String publicKey;

	/**
	 * 私钥
	 */
	@Column(nullable = false)
	private String privateKey;

	/**
	 * 是否启用, 0: 否, 1: 是. 默认: 1
	 */
	@Column(nullable = false)
	private Boolean enabled;

	/**
	 * 创建时间
	 */
	@CreatedDate
	@Column(nullable = false)
	private Date createdDate;

	/**
	 * 最后更新时间
	 */
	@LastModifiedDate
	private Date lastModifiedDate;

}
