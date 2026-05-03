package io.github.hejun.auth.security.authorization;

import io.github.hejun.auth.entity.*;
import io.github.hejun.auth.service.IConsentService;
import io.github.hejun.auth.service.ITenantService;
import io.github.hejun.auth.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 多租户 OAuth2AuthorizationConsentService
 *
 * @author HeJun
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthorizationConsentServiceImpl implements OAuth2AuthorizationConsentService {

	private final ITenantService tenantService;
	private final IUserService userService;
	private final IConsentService consentService;

	@Override
	public void save(OAuth2AuthorizationConsent authorizationConsent) {
		String issuer = AuthorizationServerContextHolder.getContext().getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("save, current issuer: {}", issuer);
		}

		Tenant tenant = tenantService.findByIssuer(issuer);
		User user = userService.findByUsername(authorizationConsent.getPrincipalName(), tenant.getId());

		Set<String> authoritieSet = authorizationConsent.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.toSet());
		String authorities = StringUtils.collectionToCommaDelimitedString(authoritieSet);

		Long registeredClientId = Long.valueOf(authorizationConsent.getRegisteredClientId());
		Consent consent = consentService.findByUserAndClient(user.getId(), registeredClientId);
		if (consent == null) {
			UserClientKey key = new UserClientKey();
			key.setUserId(user.getId());
			key.setClientId(registeredClientId);

			Client client = new Client();
			client.setId(registeredClientId);

			consent = new Consent();
			consent.setUserClientKey(key);
			consent.setUser(user);
			consent.setClient(client);
			consent.setAuthorities(authorities);

			consentService.save(consent);
		} else {
			consent.setAuthorities(authorities);
			consentService.update(consent);
		}
	}

	@Override
	public void remove(OAuth2AuthorizationConsent authorizationConsent) {
		String issuer = AuthorizationServerContextHolder.getContext().getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("remove, current issuer: {}", issuer);
		}

		Tenant tenant = tenantService.findByIssuer(issuer);
		User user = userService.findByUsername(authorizationConsent.getPrincipalName(), tenant.getId());

		consentService.delete(user.getId(), Long.valueOf(authorizationConsent.getRegisteredClientId()));
	}

	@Override
	public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
		String issuer = AuthorizationServerContextHolder.getContext().getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("findById, current issuer: {}", issuer);
		}

		Tenant tenant = tenantService.findByIssuer(issuer);
		User user = userService.findByUsername(principalName, tenant.getId());
		Consent consent = consentService.findByUserAndClient(user.getId(), Long.valueOf(registeredClientId));

		if (consent == null) {
			return null;
		}

		OAuth2AuthorizationConsent.Builder consentBuilder = OAuth2AuthorizationConsent.withId(registeredClientId, principalName);
		if (StringUtils.hasText(consent.getAuthorities())) {
			for (String authority : StringUtils.commaDelimitedListToStringArray(consent.getAuthorities())) {
				consentBuilder.authority(new SimpleGrantedAuthority(authority));
			}
		}

		return consentBuilder.build();
	}

}
