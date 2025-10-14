package io.github.hejun.neutron.controller;

import io.github.hejun.neutron.entity.Tenant;
import io.github.hejun.neutron.service.ITenantService;
import io.github.hejun.neutron.util.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * OAuth页面 Controller
 *
 * @author HeJun
 */
@Controller
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PageController {

	private final ITenantService tenantService;
	private final RegisteredClientRepository registeredClientRepository;
	private final OAuth2AuthorizationConsentService authorizationConsentService;

	@GetMapping("/login")
	public String login(Model model) {
		String issuer = ContextUtil.getIssuer();
		if (issuer != null) {
			Tenant tenant = tenantService.findByIssuer(issuer);
			if (tenant != null) {
				model.addAttribute("tenantName", tenant.getName());
			}
		}
		return "login";
	}

	@GetMapping("/consent")
	public String consent(Model model, Principal principal,
						  @RequestParam(OAuth2ParameterNames.CLIENT_ID) String clientId,
						  @RequestParam(OAuth2ParameterNames.SCOPE) String scope,
						  @RequestParam(OAuth2ParameterNames.STATE) String state) {
		String issuer = ContextUtil.getIssuer();
		if (issuer != null) {
			model.addAttribute("issuer", issuer);
			Tenant tenant = tenantService.findByIssuer(issuer);
			if (tenant != null) {
				model.addAttribute("tenantName", tenant.getName());
			}
		}

		Set<String> scopesToApprove = new HashSet<>();
		Set<String> previouslyApprovedScopes = new HashSet<>();

		RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
		if (registeredClient == null) {
			throw new OAuth2AuthenticationException("Client not exists");
		}

		OAuth2AuthorizationConsent authorizationConsent =
			this.authorizationConsentService.findById(registeredClient.getId(), principal.getName());

		Set<String> authorizedScopes;
		if (authorizationConsent != null) {
			authorizedScopes = authorizationConsent.getScopes();
		} else {
			authorizedScopes = Collections.emptySet();
		}

		for (String requestedScope : StringUtils.delimitedListToStringArray(scope, " ")) {
			if (OidcScopes.OPENID.equals(requestedScope)) {
				continue;
			}
			if (authorizedScopes.contains(requestedScope)) {
				previouslyApprovedScopes.add(requestedScope);
			} else {
				scopesToApprove.add(requestedScope);
			}
		}

		model.addAttribute("clientId", clientId);
		model.addAttribute("clientName", StringUtils.hasText(clientId) ? registeredClient.getClientName() : clientId);
		model.addAttribute("principalName", principal.getName());
		model.addAttribute("previouslyApprovedScopes", previouslyApprovedScopes);
		model.addAttribute("scopes", scopesToApprove);
		model.addAttribute("state", state);
		return "consent";
	}

	@GetMapping("/terms-of-service")
	public String termsOfService() {
		return "termsOfService";
	}

	@GetMapping("/privacy-policy")
	public String privacyPolicy() {
		return "privacyPolicy";
	}

	/**
	 * 处理浏览器F12开发模式下默认会发出的请求
	 *
	 * @return 空值
	 */
	@ResponseBody
	@GetMapping("/.well-known/appspecific/com.chrome.devtools.json")
	public Map<String, Object> appSpecific() {
		return Map.of();
	}

}
