package io.github.hejun.neutron.auth.vo.user;

import io.github.hejun.neutron.auth.vo.tenant.TenantListVO;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.util.Date;

/**
 * 用户 DetailVO
 *
 * @author HeJun
 */
@Getter
@Setter
public class UserDetailVO {

    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 账户
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 邮箱是否已验证, 0: 否, 1: 是. 默认: 0
     */
    private Boolean emailVerified;

    /**
     * 手机号
     */
    private String phoneNumber;

    /**
     * 手机号是否已验证, 0: 否, 1: 是. 默认: 0
     */
    private Boolean phoneNumberVerified;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 性别, 1: 男, 2: 女. 默认: 1
     */
    private Byte gender;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 生日
     */
    private Date birthdate;

    /**
     * 是否启用, 0: 否, 1: 是. 默认: 1
     */
    private Boolean enabled;

    /**
     * 所属租户
     */
    private TenantListVO tenant;

    /**
     * 创建时间
     */
    private Date createdDate;

    /**
     * 最后更新时间
     */
    private Date lastModifiedDate;

}
