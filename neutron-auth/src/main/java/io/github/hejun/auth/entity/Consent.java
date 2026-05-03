package io.github.hejun.auth.entity;

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
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 关联租户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("clientId")
    @JoinColumn(name = "client_id")
    private Client client;

    /**
     * 授权
     */
    private String authorities;

}
