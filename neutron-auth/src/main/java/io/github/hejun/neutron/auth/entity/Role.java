package io.github.hejun.neutron.auth.entity;

import io.github.hejun.neutron.common.persist.annotation.SnowflakeGenerator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 角色表
 *
 * @author HeJun
 */
@Getter
@Setter
@Entity
@Table(name = "t_role", indexes = {
    @Index(name = "uk_role_tenant_id_code", columnList = "tenant_id,code", unique = true),
    @Index(name = "idx_role_tenant_parent", columnList = "tenant_id,parent_role_id"),
})
@EntityListeners(AuditingEntityListener.class)
public class Role {

    /**
     * 主键
     */
    @Id
    @SnowflakeGenerator
    @Column(nullable = false, comment = "主键")
    private Long id;

    /**
     * 角色代码
     */
    @Column(nullable = false, comment = "角色代码")
    private String code;

    /**
     * 角色名
     */
    @Column(nullable = false, comment = "角色名")
    private String name;

    /**
     * 父级角色
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_role_id", comment = "父级角色")
    private Role parentRole;

    /**
     * 所属租户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, comment = "所属租户")
    private Tenant tenant;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(nullable = false, updatable = false, comment = "创建时间")
    private Date createdDate;

    /**
     * 最后更新时间
     */
    @LastModifiedDate
    @Column(comment = "最后更新时间")
    private Date lastModifiedDate;

    /**
     * 关联的子角色
     */
    @OneToMany(mappedBy = "parentRole", fetch = FetchType.LAZY)
    private Set<Role> childRoles = new HashSet<>();

    /**
     * 关联的用户
     */
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Role role)) return false;
        return id != null && Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
