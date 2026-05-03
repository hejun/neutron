package io.github.hejun.auth.converter;

import io.github.hejun.auth.dto.tenant.TenantSaveDTO;
import io.github.hejun.auth.entity.Tenant;
import io.github.hejun.auth.vo.tenant.TenantListVO;
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

	Tenant toTenant(TenantSaveDTO dto);

}
