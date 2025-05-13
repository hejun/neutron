package io.github.hejun.neutron.entity;

import io.github.hejun.neutron.annotations.GeneratedUUID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;

/**
 * 租户
 *
 * @author HeJun
 */
@Getter
@Setter
@Entity
@Table(name = "t_tenant", uniqueConstraints = {
	@UniqueConstraint(name = "uk_tenant_code", columnNames = {"code"})
})
public class Tenant {

	@Id
	@GeneratedUUID
	private String id;

	@Column(nullable = false)
	private String code;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Boolean enabled;

	@CreatedDate
	@Column(nullable = false)
	private Date createDate;

	@LastModifiedDate
	private Date lastModifiedDate;

}
