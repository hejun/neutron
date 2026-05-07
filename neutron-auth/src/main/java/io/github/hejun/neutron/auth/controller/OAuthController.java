package io.github.hejun.neutron.auth.controller;

import io.github.hejun.neutron.auth.entity.Tenant;
import io.github.hejun.neutron.auth.security.OAuth2AuthorizationConsentManager;
import io.github.hejun.neutron.auth.security.util.AuthorizationServerContextUtil;
import io.github.hejun.neutron.auth.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * OAuth页面 Controller
 *
 * @author HeJun
 */
@Controller
@RequiredArgsConstructor
public class OAuthController {

    private final RegisteredClientRepository registeredClientRepository;
    private final OAuth2AuthorizationConsentManager authorizationConsentService;
    private final AuthorizationServerContextUtil contextUtil;
    private final IUserService userService;

    @GetMapping("/login")
    public String login(Model model) {
        Optional<Tenant> tenantOptional = this.contextUtil.getCurrentTenant();
        tenantOptional.ifPresent(tenant -> {
            model.addAttribute("tenantName", tenant.getName());
            model.addAttribute("tenantCopyright", tenant.getCopyright());
        });
        return "login";
    }

    @GetMapping("/consent")
    public String consent(Model model, Principal principal,
                          @RequestParam(OAuth2ParameterNames.CLIENT_ID) String clientId,
                          @RequestParam(OAuth2ParameterNames.SCOPE) String scope,
                          @RequestParam(OAuth2ParameterNames.STATE) String state) {

        Optional<Tenant> tenantOptional = this.contextUtil.getCurrentTenant();
        tenantOptional.ifPresent(tenant -> {
            model.addAttribute("tenantName", tenant.getName());
            model.addAttribute("tenantCopyright", tenant.getCopyright());
            model.addAttribute("issuer", tenant.getIssuer());
        });

        RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            throw new OAuth2AuthenticationException("Client not exists");
        }

        userService.findById(Long.valueOf(principal.getName())).ifPresent(user -> {
            String username = StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
            model.addAttribute("principalName", username);
        });

        model.addAttribute("clientId", clientId);
        model.addAttribute("clientName", StringUtils.hasText(clientId) ? registeredClient.getClientName() : clientId);

        List<Map<String, String>> scopeWithDescription = Arrays
            .stream(StringUtils.delimitedListToStringArray(scope, " "))
            .map(s -> Map.of(
                "scope", s,
                "desc", authorizationConsentService.translateScope(s))
            )
            .toList();
        model.addAttribute("scopes", scopeWithDescription);

        model.addAttribute("state", state);
        return "consent";
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
