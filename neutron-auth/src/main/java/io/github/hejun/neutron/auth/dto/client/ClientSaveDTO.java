package io.github.hejun.neutron.auth.dto.client;

import io.github.hejun.neutron.auth.dto.tenant.TenantSaveDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 客户端 SaveDTO
 *
 * @author HeJun
 */
@Getter
@Setter
public class ClientSaveDTO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 客户端密钥
     */
    private String clientSecret;

    /**
     * 客户端名称
     */
    private String clientName;

    /**
     * 认证方式
     */
    private List<String> clientAuthenticationMethods;

    /**
     * 授权方式
     */
    private List<String> authorizationGrantTypes;

    /**
     * 跳转链接
     */
    private List<String> redirectUris;

    /**
     * 登出跳转链接
     */
    private List<String> postLogoutRedirectUris;

    /**
     * 授权域
     */
    private List<String> scopes;

    /**
     * 是否开启密码验证(PKCE模式), 0: 否, 1: 是
     */
    private Boolean requireProofKey;

    /**
     * 是否需要确认授权, 0: 否, 1: 是
     */
    private Boolean requireAuthorizationConsent;

    /**
     * AccessToken存活时间,单位: 秒
     */
    private Integer accessTokenTimeToLive;

    /**
     * RefreshToken存活时间,单位: 秒
     */
    private Integer refreshTokenTimeToLive;

    /**
     * 是否启用, 0: 否, 1: 是. 默认: 1
     */
    private Boolean enabled;

    /**
     * 所属租户
     */
    private TenantSaveDTO tenant;

}
