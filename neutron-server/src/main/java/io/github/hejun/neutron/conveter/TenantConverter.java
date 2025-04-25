package io.github.hejun.neutron.conveter;

import io.github.hejun.neutron.dto.TenantDTO;
import io.github.hejun.neutron.entity.Tenant;
import org.mapstruct.Mapper;

/**
 * Tenant Entity与DTO转换
 *
 * @author HeJun
 */
@Mapper(componentModel = "spring")
public interface TenantConverter {

	TenantDTO convert(Tenant tenant);

	Tenant convert(TenantDTO tenantDTO);

}
