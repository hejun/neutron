package io.github.hejun.neutron.auth.entity;

import io.github.hejun.neutron.auth.constant.Gender;
import io.github.hejun.neutron.common.persist.annotation.SnowflakeGenerator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 用户表
 *
 * @author HeJun
 */
@Getter
@Setter
@Entity
@Table(name = "t_user", indexes = {
    @Index(name = "uk_user_tenant_id_username", columnList = "tenant_id,username", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
public class User implements Serializable {

    /**
     * 主键
     */
    @Id
    @SnowflakeGenerator
    @Column(nullable = false, comment = "主键")
    private Long id;

    /**
     * 账户
     */
    @Column(nullable = false, comment = "账户")
    private String username;

    /**
     * 密码
     */
    @Column(nullable = false, comment = "密码")
    private String password;

    /**
     * 邮箱
     */
    @Column(comment = "邮箱")
    private String email;

    /**
     * 邮箱是否已验证, 0: 否, 1: 是. 默认: 0
     */
    @Column(nullable = false, comment = "邮箱是否已验证, 0: 否, 1: 是. 默认: 0")
    private Boolean emailVerified;

    /**
     * 手机号
     */
    @Column(comment = "手机号")
    private String phoneNumber;

    /**
     * 手机号是否已验证, 0: 否, 1: 是. 默认: 0
     */
    @Column(nullable = false, comment = "手机号是否已验证, 0: 否, 1: 是. 默认: 0")
    private Boolean phoneNumberVerified;

    /**
     * 昵称
     */
    @Column(comment = "昵称")
    private String nickname;

    /**
     * 性别, 1: 男, 2: 女. 默认: 1
     */
    @Column(nullable = false, comment = "性别, 1: 男, 2: 女. 默认: 1")
    private Gender gender;

    /**
     * 头像
     */
    @Column(comment = "头像")
    private String avatar;

    /**
     * 生日
     */
    @Column(comment = "生日")
    private Date birthdate;

    /**
     * 是否启用, 0: 否, 1: 是. 默认: 1
     */
    @Column(nullable = false, comment = "是否启用, 0: 否, 1: 是. 默认: 1")
    private Boolean enabled;

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
     * 关联的授权
     */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Consent> consent = new HashSet<>();

    /**
     * 关联的角色
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "t_user_role",
        // 这个索引是为了通过 roleId 反向查询的时候使用
        indexes = {@Index(name = "idx_user_role_role_id", columnList = "role_id")},
        joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Set<Role> roles = new HashSet<>();


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User user)) return false;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
