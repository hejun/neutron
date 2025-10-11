package io.github.hejun.neutron.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 租户 DTO
 *
 * @author HeJun
 */
@Getter
@Setter
public class TenantSaveDTO {

	@NotBlank(message = "名称不可为空")
	@Size(min = 2, max = 50, message = "名称应在2-50个字之间")
	private String name;

	@NotBlank(message = "Issuer不可为空")
	@Size(min = 2, max = 100, message = "Issuer应在2-100个字之间")
	private String issuer;

	private Boolean enabled;

}
