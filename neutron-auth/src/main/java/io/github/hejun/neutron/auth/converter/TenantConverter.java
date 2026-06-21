package io.github.hejun.neutron.auth.converter;

import io.github.hejun.neutron.auth.dto.tenant.TenantSaveDTO;
import io.github.hejun.neutron.auth.entity.Tenant;
import io.github.hejun.neutron.auth.vo.tenant.TenantDetailVO;
import io.github.hejun.neutron.auth.vo.tenant.TenantListVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 租户转换类
 *
 * @author HeJun
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TenantConverter {

	TenantListVO toTenantList(Tenant tenant);

	TenantDetailVO toTenantDetail(Tenant tenant);

	Tenant toTenant(TenantSaveDTO dto);

}
