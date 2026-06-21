package io.github.hejun.neutron.auth.dto.client;

import lombok.Getter;
import lombok.Setter;

/**
 * 客户端 ListDTO
 * @author HeJun
 */
@Getter
@Setter
public class ClientListDTO {

	private String clientId;

	private String clientName;

	private Boolean enabled;

	private Long tenantId;

}
