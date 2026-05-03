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
 * 用户表
 *
 * @author HeJun
 */
@Getter
@Setter
@Entity
@Table(name = "t_user", indexes = {
	@Index(name = "uk_user_username_tenant_id", columnList = "username,tenant_id", unique = true),
	@Index(name = "idx_user_tenant_id", columnList = "tenant_id")
})
@EntityListeners(AuditingEntityListener.class)
public class User implements Serializable {

	/**
	 * 主键
	 */
	@Id
	@SnowflakeGenerator
	private Long id;

	/**
	 * 账户
	 */
	@Column(nullable = false)
	private String username;

	/**
	 * 密码
	 */
	@Column(nullable = false)
	private String password;

	/**
	 * 昵称
	 */
	private String nickname;

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
	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Consent> consent;

}
