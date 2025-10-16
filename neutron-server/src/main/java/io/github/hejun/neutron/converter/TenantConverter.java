package io.github.hejun.neutron.converter;

import io.github.hejun.neutron.dto.TenantEditDTO;
import io.github.hejun.neutron.entity.Tenant;
import io.github.hejun.neutron.vo.TenantDetailVO;
import io.github.hejun.neutron.vo.TenantListVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Tenant Entity与DTO转换
 *
 * @author HeJun
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TenantConverter {

	TenantListVO convertToList(Tenant tenant);

	TenantDetailVO convertToDetail(Tenant tenant);

	Tenant convert(TenantEditDTO tenantEditDTO);

}
