package io.github.hejun.auth.dto.tenant;

import lombok.Getter;
import lombok.Setter;

/**
 * 租户 ListDTO
 * @author HeJun
 */
@Getter
@Setter
public class TenantSaveDTO {

	private String name;

	private String issuer;

	private Boolean enabled;

}
