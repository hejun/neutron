package io.github.hejun.neutron.security.component;

import io.github.hejun.neutron.entity.Client;
import io.github.hejun.neutron.entity.Tenant;
import io.github.hejun.neutron.entity.User;
import io.github.hejun.neutron.exception.ClientNotFoundException;
import io.github.hejun.neutron.exception.TenantNotFoundException;
import io.github.hejun.neutron.service.ITenantService;
import io.github.hejun.neutron.service.IUserService;
import io.github.hejun.neutron.util.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 多租户 UserDetailsService
 *
 * @author HeJun
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserDetailsServiceImpl implements UserDetailsService {

	private final ITenantService tenantService;
	private final IUserService userService;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		String issuer = ContextUtil.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("loadUserByUsername, current issuer: {}", issuer);
		}
		Tenant tenant = tenantService.findByIssuer(issuer);
		if (tenant == null) {
			throw new TenantNotFoundException("Tenant not found");
		}
		if (Boolean.FALSE.equals(tenant.getEnabled())) {
			throw new LockedException("Tenant is locked");
		}

		User user = userService.findByUsername(tenant.getId(), username);
		if (user == null) {
			throw new UsernameNotFoundException(username + " not found");
		}

		String clientId = ContextUtil.getClientId();
		if (clientId == null) {
			throw new ClientNotFoundException("Client not found");
		}
		List<Client> clients = userService.findUserClients(user.getId());
		Set<String> clientIdSet = Optional.ofNullable(clients).orElse(Collections.emptyList()).stream()
			.map(Client::getClientId).collect(Collectors.toSet());
		if (!clientIdSet.contains(clientId)) {
			throw new AccessDeniedException("Client access denied");
		}

		return this.convertUserToUserDetails(user);
	}

	private UserDetails convertUserToUserDetails(User user) {
		return org.springframework.security.core.userdetails.User.builder()
			.username(user.getUsername())
			.password(user.getPassword())
			.accountLocked(Boolean.FALSE.equals(user.getEnabled())).build();
	}

}
