package io.github.hejun.neutron.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 客户端
 *
 * @author HeJun
 */
@Getter
@Setter
@TableName("t_client")
public class Client {

	@TableId(type = IdType.ASSIGN_ID)
	private Long id;

	private String clientId;

	private String clientSecret;

	private String name;

	private String clientAuthenticationMethods;

	private String authorizationGrantTypes;

	private String redirectUris;

	private String postLogoutRedirectUris;

	private String scopes;

	private Boolean requireProofKey;

	private Boolean requireAuthorizationConsent;

	private Integer accessTokenTimeToLive;

	private Integer refreshTokenTimeToLive;

	private Boolean enabled;

	private Long tenantId;

	private Date createDate;

	private Date lastModifiedDate;

}
