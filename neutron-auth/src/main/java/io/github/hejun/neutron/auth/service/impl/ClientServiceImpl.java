package io.github.hejun.neutron.auth.service.impl;

import io.github.hejun.neutron.auth.entity.Client;
import io.github.hejun.neutron.auth.exception.OccupiedException;
import io.github.hejun.neutron.auth.repository.ClientRepository;
import io.github.hejun.neutron.auth.service.IClientService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.UpdateSpecification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 客户端 Service
 *
 * @author HeJun
 */
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements IClientService {

    private final PasswordEncoder passwordEncoder;
    private final ClientRepository clientRepository;

    @Override
    public Page<Client> findPage(String clientId, String clientName, Boolean enabled, Long tenantId, Pageable pageable) {
        Specification<Client> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(clientId)) {
                predicates.add(cb.like(root.get("clientId"), "%" + clientId + "%"));
            }
            if (StringUtils.hasText(clientName)) {
                predicates.add(cb.like(root.get("clientName"), "%" + clientName + "%"));
            }
            if (enabled != null) {
                predicates.add(cb.equal(root.get("enabled"), enabled));
            }
            if (tenantId != null) {
                predicates.add(cb.equal(root.get("tenant").get("id"), tenantId));
            }
            return cb.and(predicates);
        };
        return clientRepository.findAll(specification, pageable);
    }

    @Override
    public Optional<Client> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return clientRepository.findById(id);
    }

    @Override
    public Optional<Client> findByClientId(String clientId, Long tenantId) {
        if (tenantId == null || !StringUtils.hasText(clientId)) {
            return Optional.empty();
        }
        return clientRepository.findByClientIdAndTenantId(clientId, tenantId);
    }

    @Override
    public Client save(Client client) {
        if (client == null || client.getTenant() == null || client.getTenant().getId() == null || !StringUtils.hasText(client.getClientId())) {
            return null;
        }
        if (clientRepository.findByClientIdAndTenantId(client.getClientId(), client.getTenant().getId()).isPresent()) {
            throw new OccupiedException("客户端：" + client.getClientId() + " 已被使用");
        }
        // 密码需要加密
        if (StringUtils.hasText(client.getClientSecret())) {
            client.setClientSecret(passwordEncoder.encode(client.getClientSecret()));
        }
        if (client.getEnabled() == null) {
            client.setEnabled(true);
        }
        return clientRepository.save(client);
    }

    @Override
    public void update(Client client) {
        if (client == null || client.getId() == null) {
            return;
        }
        Optional<Client> checkerOptional = clientRepository.findByClientIdAndTenantId(client.getClientId(), client.getTenant().getId());
        if (checkerOptional.isPresent() && !checkerOptional.get().getId().equals(client.getId())) {
            throw new OccupiedException("客户端：" + client.getClientId() + " 已被使用");
        }
        UpdateSpecification<Client> updateSpecification = UpdateSpecification
            .<Client>update((root, update, criteriaBuilder) -> {
                if (StringUtils.hasText(client.getClientId())) {
                    update.set("clientId", client.getClientId());
                }
                if (StringUtils.hasText(client.getClientSecret())) {
                    update.set("clientSecret", passwordEncoder.encode(client.getClientSecret()));
                }
                if (StringUtils.hasText(client.getClientName())) {
                    update.set("clientName", client.getClientName());
                }
                if (StringUtils.hasText(client.getClientAuthenticationMethods())) {
                    update.set("clientAuthenticationMethods", client.getClientAuthenticationMethods());
                }
                if (StringUtils.hasText(client.getAuthorizationGrantTypes())) {
                    update.set("authorizationGrantTypes", client.getAuthorizationGrantTypes());
                }
                if (StringUtils.hasText(client.getRedirectUris())) {
                    update.set("redirectUris", client.getRedirectUris());
                }
                if (StringUtils.hasText(client.getPostLogoutRedirectUris())) {
                    update.set("postLogoutRedirectUris", client.getPostLogoutRedirectUris());
                }
                if (StringUtils.hasText(client.getScopes())) {
                    update.set("scopes", client.getScopes());
                }
                if (client.getRequireProofKey() != null) {
                    update.set("requireProofKey", client.getRequireProofKey());
                }
                if (client.getRequireAuthorizationConsent() != null) {
                    update.set("requireAuthorizationConsent", client.getRequireAuthorizationConsent());
                }
                if (client.getAccessTokenTimeToLive() != null) {
                    update.set("accessTokenTimeToLive", client.getAccessTokenTimeToLive());
                }
                if (client.getRefreshTokenTimeToLive() != null) {
                    update.set("refreshTokenTimeToLive", client.getRefreshTokenTimeToLive());
                }
                if (client.getEnabled() != null) {
                    update.set("enabled", client.getEnabled());
                }
                if (client.getTenant() != null && client.getTenant().getId() != null) {
                    update.set("tenant", client.getTenant());
                }
            })
            .where((root, update, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id"), client.getId())
            );
        clientRepository.update(updateSpecification);
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            return;
        }
        clientRepository.deleteById(id);
    }

}
