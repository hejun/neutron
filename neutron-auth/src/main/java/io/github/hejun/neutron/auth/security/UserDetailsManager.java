package io.github.hejun.neutron.auth.security;

import io.github.hejun.neutron.auth.entity.Tenant;
import io.github.hejun.neutron.auth.entity.User;
import io.github.hejun.neutron.auth.security.util.AuthorizationServerContextUtil;
import io.github.hejun.neutron.auth.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;


/**
 * 多租户 UserDetailsService
 *
 * @author HeJun
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserDetailsManager implements UserDetailsService {

    private final AuthorizationServerContextUtil contextUtil;
    private final IUserService userService;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        Optional<Tenant> tenantOptional = contextUtil.getCurrentTenant();
        if (tenantOptional.isEmpty() || Boolean.FALSE.equals(tenantOptional.get().getEnabled())) {
            throw new DisabledException("Tenant not found");
        }

        Tenant tenant = tenantOptional.get();
        Optional<User> optional = userService.findByUsername(username, tenant.getId());
        if (optional.isEmpty()) {
            throw new UsernameNotFoundException(username + " not found");
        }

        return optional.map(this::convertUserToUserDetails).orElse(null);
    }

    private UserDetails convertUserToUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getId().toString())
            .password(user.getPassword())
            .disabled(Boolean.FALSE.equals(user.getEnabled()))
            .build();
    }

}
