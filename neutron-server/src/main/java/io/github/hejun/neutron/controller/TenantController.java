package io.github.hejun.neutron.controller;

import io.github.hejun.neutron.conveter.TenantConverter;
import io.github.hejun.neutron.dto.TenantDTO;
import io.github.hejun.neutron.entity.Tenant;
import io.github.hejun.neutron.service.ITenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 租户 Controller
 *
 * @author HeJun
 */
@RestController
@RequestMapping("/tenant")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TenantController {

	private final ITenantService tenantService;
	private final TenantConverter tenantConverter;

	@GetMapping
	public PagedModel<TenantDTO> findPage(TenantDTO tenant, @PageableDefault Pageable pageable) {
		Tenant query = tenantConverter.convert(tenant);
		Page<Tenant> page = tenantService.findPage(query, pageable);
		Page<TenantDTO> convertedPage = page.map(tenantConverter::convert);
		return new PagedModel<>(convertedPage);
	}

	@GetMapping("/{id}")
	public TenantDTO findPage(@PathVariable String id) {
		Tenant tenant = tenantService.findById(id);
		return tenantConverter.convert(tenant);
	}

	@PostMapping
	public TenantDTO save(@RequestBody @Validated TenantDTO tenantDTO) {
		Tenant entity = tenantConverter.convert(tenantDTO);
		entity = tenantService.save(entity);
		return tenantConverter.convert(entity);
	}

	@PutMapping
	public TenantDTO update(@RequestBody @Validated TenantDTO tenantDTO) {
		Tenant entity = tenantConverter.convert(tenantDTO);
		entity = tenantService.update(entity);
		return tenantConverter.convert(entity);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable String id) {
		tenantService.deleteById(id);
	}

}
