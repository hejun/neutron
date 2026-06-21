package io.github.hejun.neutron.auth.dto.tenant;

import lombok.Getter;
import lombok.Setter;

/**
 * 租户 ListDTO
 * @author HeJun
 */
@Getter
@Setter
public class TenantListDTO {

	private String name;

	private Boolean enabled;

}
