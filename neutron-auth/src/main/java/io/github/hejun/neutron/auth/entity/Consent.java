package io.github.hejun.neutron.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;

/**
 * 授权表
 *
 * @author HeJun
 */
@Getter
@Setter
@Entity
@Table(name = "t_consent")
@EntityListeners(AuditingEntityListener.class)
public class Consent implements Serializable {

    /**
     * 关联主键
     */
    @EmbeddedId
    private UserClientKey userClientKey;

    /**
     * 关联用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", comment = "关联用户")
    private User user;

    /**
     * 关联租户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("clientId")
    @JoinColumn(name = "client_id", comment = "关联租户")
    private Client client;

    /**
     * 授权
     */
    @Column(comment = "授权")
    private String authorities;

}
