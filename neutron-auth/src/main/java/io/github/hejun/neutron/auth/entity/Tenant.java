package io.github.hejun.neutron.auth.entity;

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
    @Column(nullable = false, comment = "主键")
    private Long id;

    /**
     * 租户名
     */
    @Column(nullable = false, comment = "租户名")
    private String name;

    /**
     * 发行域名
     */
    @Column(nullable = false, comment = "发行域名")
    private String issuer;

    /**
     * 公钥
     */
    @Column(nullable = false, comment = "公钥")
    private String publicKey;

    /**
     * 私钥
     */
    @Column(nullable = false, comment = "私钥")
    private String privateKey;

    /**
     * 版权
     */
    @Column(comment = "版权")
    private String copyright;

    /**
     * 是否启用, 0: 否, 1: 是. 默认: 1
     */
    @Column(nullable = false, comment = "是否启用, 0: 否, 1: 是. 默认: 1")
    private Boolean enabled;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(nullable = false, comment = "创建时间")
    private Date createdDate;

    /**
     * 最后更新时间
     */
    @LastModifiedDate
    @Column(comment = "最后更新时间")
    private Date lastModifiedDate;

}
