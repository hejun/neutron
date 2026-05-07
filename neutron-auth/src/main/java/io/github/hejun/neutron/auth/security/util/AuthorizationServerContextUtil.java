package io.github.hejun.neutron.auth.security.util;

import io.github.hejun.neutron.auth.entity.Tenant;
import io.github.hejun.neutron.auth.service.ITenantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 获取 Tenant 的 Util
 *
 * @author HeJun
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizationServerContextUtil {

    private final RequestCache requestCache = new HttpSessionRequestCache();

    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final ObjectProvider<OAuth2AuthorizationService> authorizationServiceProvider;
    private final IssuerResolver issuerResolver;
    private final ITenantService tenantService;

    @Nullable
    public String getCurrentIssuer() {
        AuthorizationServerContext context = AuthorizationServerContextHolder.getContext();
        if (context != null) {
            log.debug("get current issuer from context: {}", context.getIssuer());
            return context.getIssuer();
        }

        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest instanceof DefaultSavedRequest defaultSavedRequest) {
            String issuer = issuerResolver.resolve(defaultSavedRequest.getRequestURL());

            log.debug("get current issuer from saved request: {}", issuer);
            return issuer;
        }

        String state = request.getParameter(OAuth2ParameterNames.STATE);
        if (StringUtils.hasText(state)) {
            OAuth2Authorization authorization = authorizationServiceProvider.getObject().findByToken(state, null);

            if (authorization != null) {
                OAuth2AuthorizationRequest authorizationRequest = authorization.getAttribute(OAuth2AuthorizationRequest.class.getName());
                if (authorizationRequest != null) {
                    String issuer = issuerResolver.resolve(authorizationRequest.getAuthorizationUri());

                    log.debug("get current issuer from state: {}", state);
                    return issuer;
                }
            }
        }

        log.error("current issuer not set!");
        return null;
    }

    public Optional<Tenant> getCurrentTenant() {
        String issuer = this.getCurrentIssuer();
        if (StringUtils.hasText(issuer)) {
            return tenantService.findByIssuer(issuer);
        }
        return Optional.empty();
    }

}
