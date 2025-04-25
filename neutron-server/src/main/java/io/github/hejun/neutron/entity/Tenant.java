package io.github.hejun.neutron.entity;

import io.github.hejun.neutron.annotations.GeneratedUUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "t_tenant")
public class Tenant {

	@Id
	@GeneratedUUID
	private String id;

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
