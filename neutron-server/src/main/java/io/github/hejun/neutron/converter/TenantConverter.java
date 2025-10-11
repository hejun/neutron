package io.github.hejun.neutron.converter;

import io.github.hejun.neutron.dto.TenantSaveDTO;
import io.github.hejun.neutron.entity.Tenant;
import io.github.hejun.neutron.vo.TenantListVO;
import org.mapstruct.Mapper;

/**
 * Tenant Entity与DTO转换
 *
 * @author HeJun
 */
@Mapper(componentModel = "spring")
public interface TenantConverter {

	TenantListVO convertToList(Tenant tenant);

	Tenant convert(TenantSaveDTO tenantSaveDTO);

}
