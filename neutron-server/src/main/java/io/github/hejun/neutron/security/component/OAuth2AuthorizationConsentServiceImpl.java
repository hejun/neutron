package io.github.hejun.neutron.security.component;

import io.github.hejun.neutron.entity.Consent;
import io.github.hejun.neutron.entity.Tenant;
import io.github.hejun.neutron.entity.User;
import io.github.hejun.neutron.service.IConsentService;
import io.github.hejun.neutron.service.ITenantService;
import io.github.hejun.neutron.service.IUserService;
import io.github.hejun.neutron.util.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
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
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OAuth2AuthorizationConsentServiceImpl implements OAuth2AuthorizationConsentService {

	private final ITenantService tenantService;
	private final IUserService userService;
	private final IConsentService consentService;

	@Override
	public void save(OAuth2AuthorizationConsent authorizationConsent) {
		String issuer = ContextUtil.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("save, current issuer: {}", issuer);
		}

		Tenant tenant = tenantService.findByIssuer(issuer);
		User user = userService.findByUsername(tenant.getId(), authorizationConsent.getPrincipalName());

		Set<String> authoritieSet = authorizationConsent.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.toSet());
		String authorities = StringUtils.collectionToCommaDelimitedString(authoritieSet);

		Consent consent = consentService.findByClientAndUser(Long.valueOf(authorizationConsent.getRegisteredClientId()), user.getId());
		if (consent == null) {
			consent = new Consent();
			consent.setClientId(Long.valueOf(authorizationConsent.getRegisteredClientId()));
			consent.setUserId(user.getId());
			consent.setAuthorities(authorities);
			consentService.save(consent);
		} else {
			consent.setAuthorities(authorities);
			consentService.update(consent);
		}
	}

	@Override
	public void remove(OAuth2AuthorizationConsent authorizationConsent) {
		String issuer = ContextUtil.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("remove, current issuer: {}", issuer);
		}

		Tenant tenant = tenantService.findByIssuer(issuer);
		User user = userService.findByUsername(tenant.getId(), authorizationConsent.getPrincipalName());

		Consent consent = new Consent();
		consent.setClientId(Long.valueOf(authorizationConsent.getRegisteredClientId()));
		consent.setUserId(user.getId());
		consentService.delete(consent);
	}

	@Override
	public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
		String issuer = ContextUtil.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("findById, current issuer: {}", issuer);
		}

		Tenant tenant = tenantService.findByIssuer(issuer);
		User user = userService.findByUsername(tenant.getId(), principalName);
		Consent consent = consentService.findByClientAndUser(Long.valueOf(registeredClientId), user.getId());

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
