package io.github.hejun.neutron.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 租户
 *
 * @author HeJun
 */
@Getter
@Setter
@TableName("t_tenant")
public class Tenant {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String issuer;

    private String publicKey;

    private String privateKey;

    private Boolean enabled;

    private Date createDate;

    private Date lastModifiedDate;

}
