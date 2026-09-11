package io.github.hejun.neutron.auth.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.hejun.neutron.auth.constant.Gender;
import io.github.hejun.neutron.auth.dto.tenant.TenantSaveDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 用户 SaveDTO
 *
 * @author HeJun
 */
@Getter
@Setter
public class UserSaveDTO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 账户
     */
    private String username;

    /**
     * 密码
     */
    private String password;

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
    private Gender gender;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 生日
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date birthdate;

    /**
     * 是否启用, 0: 否, 1: 是. 默认: 1
     */
    private Boolean enabled;

    /**
     * 所属租户
     */
    private TenantSaveDTO tenant;

}
