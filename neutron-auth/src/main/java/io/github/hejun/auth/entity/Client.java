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
import java.util.List;

/**
 * 客户端表
 *
 * @author HeJun
 */
@Getter
@Setter
@Entity
@Table(name = "t_client", indexes = {
	@Index(name = "uk_client_client_id_tenant_id", columnList = "client_id,tenant_id", unique = true),
	@Index(name = "idx_client_tenant_id", columnList = "tenant_id")
})
@EntityListeners(AuditingEntityListener.class)
public class Client implements Serializable {

	/**
	 * 主键
	 */
	@Id
	@SnowflakeGenerator
	private Long id;

	/**
	 * 客户端ID
	 */
	@Column(nullable = false)
	private String clientId;

	/**
	 * 客户端密钥
	 */
	private String clientSecret;

	/**
	 * 客户端名称
	 */
	private String clientName;

	/**
	 * 客户端Logo链接
	 */
	private String logoUrl;

	/**
	 * 认证方式
	 */
	private String clientAuthenticationMethods;

	/**
	 * 授权方式
	 */
	private String authorizationGrantTypes;

	/**
	 * 跳转链接
	 */
	private String redirectUris;

	/**
	 * 登出跳转链接
	 */
	private String postLogoutRedirectUris;

	/**
	 * 授权域
	 */
	private String scopes;

	/**
	 * 是否开启密码验证(PKCE模式), 0: 否, 1: 是
	 */
	private Boolean requireProofKey;

	/**
	 * 是否需要确认授权, 0: 否, 1: 是
	 */
	private Boolean requireAuthorizationConsent;

	/**
	 * AccessToken存活时间,单位: 秒
	 */
	private Integer accessTokenTimeToLive;

	/**
	 * RefreshToken存活时间,单位: 秒
	 */
	private Integer refreshTokenTimeToLive;

	/**
	 * 是否启用, 0: 否, 1: 是. 默认: 1
	 */
	@Column(nullable = false)
	private Boolean enabled;

	/**
	 * 所属租户
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

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

	/**
	 * 关联的授权
	 */
	@OneToMany(mappedBy = "client", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Consent> consent;

}
