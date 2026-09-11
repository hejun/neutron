package io.github.hejun.neutron.auth.service.impl;

import io.github.hejun.neutron.auth.entity.Role;
import io.github.hejun.neutron.auth.exception.OccupiedException;
import io.github.hejun.neutron.auth.repository.RoleRepository;
import io.github.hejun.neutron.auth.service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 角色 Service
 *
 * @author HeJun
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements IRoleService {

    private final RoleRepository roleRepository;

    @Override
    public Role save(Role role) {
        if (role == null || role.getTenant() == null || role.getTenant().getId() == null || !StringUtils.hasText(role.getCode())) {
            return null;
        }
        if (roleRepository.findByCodeAndTenantId(role.getCode(), role.getTenant().getId()).isPresent()) {
            throw new OccupiedException("角色代码：" + role.getCode() + " 已被使用");
        }
        return roleRepository.save(role);
    }

}
