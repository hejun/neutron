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
 * 用户
 *
 * @author HeJun
 */
@Getter
@Setter
@Entity
@Table(name = "t_user", uniqueConstraints = {
	@UniqueConstraint(name = "uk_user_tenant_id_username", columnNames = {"username", "tenant_id"})
})
public class User {

	@Id
	@GeneratedUUID
	private String id;

	private String username;

	private String password;

	@Column(nullable = false)
	private Boolean enabled;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private Tenant tenant;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "t_client_user",
		joinColumns = {@JoinColumn(name = "client_id")},
		inverseJoinColumns = {@JoinColumn(name = "user_id")},
		indexes = {@Index(name = "uk_client_user_client_id_user_id", columnList = "client_id,user_id", unique = true)},
		foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT),
		inverseForeignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
	)
	private List<Client> clients;

	@CreatedDate
	@Column(nullable = false)
	private Date createDate;

	@LastModifiedDate
	private Date lastModifiedDate;

}
