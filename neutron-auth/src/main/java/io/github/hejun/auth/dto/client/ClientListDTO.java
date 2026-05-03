package io.github.hejun.auth.dto.client;

import lombok.Getter;
import lombok.Setter;

/**
 * 客户端 ListDTO
 * @author HeJun
 */
@Getter
@Setter
public class ClientListDTO {

	private String name;

	private Boolean enabled;

	private Long tenantId;

}
