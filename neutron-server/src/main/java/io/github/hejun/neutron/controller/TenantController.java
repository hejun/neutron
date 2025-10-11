package io.github.hejun.neutron.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.hejun.neutron.converter.TenantConverter;
import io.github.hejun.neutron.dto.TenantSaveDTO;
import io.github.hejun.neutron.entity.Tenant;
import io.github.hejun.neutron.service.ITenantService;
import io.github.hejun.neutron.vo.TenantListVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
	public IPage<TenantListVO> findPage(@Valid @Min(value = 1, message = "页码不可小于1") @RequestParam(defaultValue = "1") Long current,
										@Valid @Max(value = 200, message = "每页不可大于200") @RequestParam(defaultValue = "15") Long size,
										String name, Boolean enabled) {
		return tenantService.findPage(current, size, name, enabled).convert(tenantConverter::convertToList);
	}

	@PostMapping
	public Map<String, Object> save(@Valid @RequestBody TenantSaveDTO tenantSaveDTO) {
		Tenant tenant = tenantConverter.convert(tenantSaveDTO);
		tenant = tenantService.save(tenant);
		return Map.of("id", String.valueOf(tenant.getId()));
	}

}
