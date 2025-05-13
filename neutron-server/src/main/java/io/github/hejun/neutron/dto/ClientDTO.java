package io.github.hejun.neutron.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 客户端 DTO
 *
 * @author HeJun
 */
@Getter
@Setter
public class ClientDTO {

	private String id;

	@NotBlank(message = "客户端ID不可为空")
	@Size(min = 2, max = 50, message = "客户端ID应在2-50个字之间")
	private String clientId;

	private String name;

	private String authenticationMethods;

	private String authorizationGrantTypes;

	private String redirectUris;

	private String scopes;

	private Boolean requireProofKey;

	private Boolean requireAuthorizationConsent;

	private Integer accessTokenTimeToLive;

	private Integer refreshTokenTimeToLive;

	private Boolean enabled;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private Date createDate;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private Date lastModifiedDate;

}
