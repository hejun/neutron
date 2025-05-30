package io.github.hejun.neutron.entity;

import io.github.hejun.neutron.annotations.GeneratedUUID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;
import java.util.List;

/**
 * 客户端
 *
 * @author HeJun
 */
@Getter
@Setter
@Entity
@Table(name = "t_client", uniqueConstraints = {
	@UniqueConstraint(name = "uk_client_client_id_tenant_id", columnNames = {"client_id", "tenant_id"})
})
public class Client {

	@Id
	@GeneratedUUID
	private String id;

	@Column(nullable = false)
	private String clientId;

	private String name;

	private String authenticationMethods;

	private String authorizationGrantTypes;

	private String redirectUris;

	private String scopes;

	private Boolean requireProofKey;

	private Boolean requireAuthorizationConsent;

	private Integer accessTokenTimeToLive;

	private Integer refreshTokenTimeToLive;

	@Column(nullable = false)
	private Boolean enabled;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private Tenant tenant;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "t_client_user",
		joinColumns = {@JoinColumn(name = "user_id")},
		inverseJoinColumns = {@JoinColumn(name = "client_id")},
		indexes = {@Index(name = "uk_client_user_client_id_user_id", columnList = "client_id,user_id", unique = true)},
		foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT),
		inverseForeignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
	)
	private List<User> users;

	@CreatedDate
	@Column(nullable = false)
	private Date createDate;

	@LastModifiedDate
	private Date lastModifiedDate;

}
