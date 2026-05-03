package io.github.hejun.auth.security.authorization;

import io.github.hejun.auth.entity.Tenant;
import io.github.hejun.auth.entity.User;
import io.github.hejun.auth.service.ITenantService;
import io.github.hejun.auth.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
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
public class UserDetailsServiceImpl implements UserDetailsService {

    private final ITenantService tenantService;
    private final IUserService userService;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        String issuer = Optional.ofNullable(AuthorizationServerContextHolder.getContext()).map(AuthorizationServerContext::getIssuer).orElse(null);
        if (log.isDebugEnabled()) {
            log.debug("loadUserByUsername, current issuer: {}", issuer);
        }
        Tenant tenant = tenantService.findByIssuer(issuer);
        if (tenant == null) {
            throw new AuthorizationDeniedException("Tenant not found");
        }
        if (Boolean.FALSE.equals(tenant.getEnabled())) {
            throw new DisabledException("Tenant is locked");
        }

        User user = userService.findByUsername(username, tenant.getId());
        if (user == null) {
            throw new UsernameNotFoundException(username + " not found");
        }

        return this.convertUserToUserDetails(user);
    }

    private UserDetails convertUserToUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .disabled(Boolean.FALSE.equals(user.getEnabled()))
			.build();
    }

}
