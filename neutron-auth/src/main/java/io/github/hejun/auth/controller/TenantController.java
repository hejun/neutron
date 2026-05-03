package io.github.hejun.auth.controller;

import io.github.hejun.auth.converter.TenantConverter;
import io.github.hejun.auth.dto.tenant.TenantListDTO;
import io.github.hejun.auth.dto.tenant.TenantSaveDTO;
import io.github.hejun.auth.entity.Tenant;
import io.github.hejun.auth.service.ITenantService;
import io.github.hejun.auth.vo.tenant.TenantListVO;
import io.github.hejun.neutron.common.core.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 租户 Controller
 *
 * @author HeJun
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/tenant")
public class TenantController {

	private final ITenantService tenantService;

	private final TenantConverter tenantConverter;

	@GetMapping
	public Result<Page<TenantListVO>> findPage(TenantListDTO dto, @PageableDefault Pageable pageable) {
		dto = dto == null ? new TenantListDTO() : dto;
		Page<Tenant> page = tenantService.findPage(dto.getName(), dto.getEnabled(), pageable);
		Page<TenantListVO> convertedPage = page.map(tenantConverter::toTenantList);
		return Result.SUCCESS(convertedPage);
	}

	@PostMapping
	public Result<Map<String, String>> save(@RequestBody TenantSaveDTO dto) {
		Tenant saved = tenantService.save(tenantConverter.toTenant(dto));
		return Result.SUCCESS(Map.of("id", String.valueOf(saved.getId())));
	}

}
