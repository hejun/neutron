package io.github.hejun.neutron.auth.controller;

import io.github.hejun.neutron.auth.converter.TenantConverter;
import io.github.hejun.neutron.auth.dto.tenant.TenantListDTO;
import io.github.hejun.neutron.auth.dto.tenant.TenantSaveDTO;
import io.github.hejun.neutron.auth.entity.Tenant;
import io.github.hejun.neutron.auth.service.ITenantService;
import io.github.hejun.neutron.auth.vo.tenant.TenantDetailVO;
import io.github.hejun.neutron.auth.vo.tenant.TenantListVO;
import io.github.hejun.neutron.common.core.dto.Result;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
    public Result<PagedModel<TenantListVO>> findPage(TenantListDTO dto,
                                                     @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        dto = ObjectUtils.getIfNull(dto, new TenantListDTO());
        Page<Tenant> page = tenantService.findPage(dto.getName(), dto.getEnabled(), pageable);
        Page<TenantListVO> convertedPage = page.map(tenantConverter::toTenantList);
        return Result.SUCCESS(new PagedModel<>(convertedPage));
    }

    @GetMapping("/{id:\\d+}")
    public Result<TenantDetailVO> findById(@PathVariable Long id) {
        Optional<TenantDetailVO> optional = tenantService.findById(id).map(tenantConverter::toTenantDetail);
        return optional.map(Result::SUCCESS).orElseGet(() -> Result.ERROR(400, "租户不存在"));
    }

    @PostMapping
    public Result<Void> save(@RequestBody TenantSaveDTO dto) {
        tenantService.save(tenantConverter.toTenant(dto));
        return Result.SUCCESS();
    }

    @PutMapping
    public Result<Void> update(@RequestBody TenantSaveDTO dto) {
        tenantService.update(tenantConverter.toTenant(dto));
        return Result.SUCCESS();
    }

    @DeleteMapping("/{id:\\d+}")
    public Result<Void> delete(@PathVariable Long id) {
        tenantService.delete(id);
        return Result.SUCCESS();
    }

}
