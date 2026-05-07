package io.github.hejun.neutron.auth.security;

import io.github.hejun.neutron.auth.entity.*;
import io.github.hejun.neutron.auth.security.util.AuthorizationServerContextUtil;
import io.github.hejun.neutron.auth.service.IConsentService;
import io.github.hejun.neutron.auth.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OAuth2授权同意管理器
 *
 * @author HeJun
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthorizationConsentManager implements OAuth2AuthorizationConsentService {

    private final AuthorizationServerContextUtil contextUtil;
    private final IUserService userService;
    private final IConsentService consentService;

    @Override
    public void save(OAuth2AuthorizationConsent authorizationConsent) {
        Optional<User> userOptional = userService.findById(Long.valueOf(authorizationConsent.getPrincipalName()));
        if (userOptional.isEmpty()) {
            throw new AuthenticationServiceException("User not found");
        }
        User user = userOptional.get();

        Set<String> authoritieSet = authorizationConsent.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
        String authorities = StringUtils.collectionToCommaDelimitedString(authoritieSet);

        Long registeredClientId = Long.valueOf(authorizationConsent.getRegisteredClientId());
        Optional<Consent> optional = consentService.findByUserAndClient(user.getId(), registeredClientId);
        if (optional.isEmpty()) {
            UserClientKey key = new UserClientKey();
            key.setUserId(user.getId());
            key.setClientId(registeredClientId);

            Client client = new Client();
            client.setId(registeredClientId);

            Consent consent = new Consent();
            consent.setUserClientKey(key);
            consent.setUser(user);
            consent.setClient(client);
            consent.setAuthorities(authorities);

            consentService.save(consent);
        } else {
            Consent consent = optional.get();
            consent.setAuthorities(authorities);
            consentService.update(consent);
        }
    }

    @Override
    public void remove(OAuth2AuthorizationConsent authorizationConsent) {
        Optional<Tenant> tenantOptional = contextUtil.getCurrentTenant();

        Long userId = Long.valueOf(authorizationConsent.getPrincipalName());

        tenantOptional.ifPresent(tenant -> consentService.delete(userId, tenant.getId()));
    }

    @Override
    public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
        Optional<User> userOptional = userService.findById(Long.valueOf(principalName));
        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();
        Optional<Consent> optional = consentService.findByUserAndClient(user.getId(), Long.valueOf(registeredClientId));

        if (optional.isEmpty()) {
            return null;
        }

        Consent consent = optional.get();
        String username = StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
        OAuth2AuthorizationConsent.Builder builder = OAuth2AuthorizationConsent.withId(registeredClientId, username);
        if (StringUtils.hasText(consent.getAuthorities())) {
            for (String authority : StringUtils.commaDelimitedListToStringArray(consent.getAuthorities())) {
                builder.authority(new SimpleGrantedAuthority(authority));
            }
        }

        return builder.build();
    }

    @NonNull
    public String translateScope(String scope) {
        if (scope == null) {
            return "";
        }

        return switch (scope.toLowerCase()) {
            case OidcScopes.OPENID -> "身份标识";
            case OidcScopes.PROFILE -> "基础信息";
            case OidcScopes.EMAIL -> "邮箱";
            case OidcScopes.ADDRESS -> "地址";
            case OidcScopes.PHONE -> "电话";
            default -> scope;
        };
    }
}
