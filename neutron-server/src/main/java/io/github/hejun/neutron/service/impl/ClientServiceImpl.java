package io.github.hejun.neutron.service.impl;

import io.github.hejun.neutron.entity.Client;
import io.github.hejun.neutron.repository.ClientRepository;
import io.github.hejun.neutron.service.IClientService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 客户端Service
 *
 * @author HeJun
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ClientServiceImpl implements IClientService {

	private final ClientRepository clientRepository;

	@Override
	public Page<Client> findPage(Client client, Pageable pageable) {
		Specification<Client> spec = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (client != null) {
				if (StringUtils.isNotBlank(client.getClientId())) {
					predicates.add(criteriaBuilder.like(root.get("clientId"), "%" + client.getClientId() + "%"));
				}
				if (StringUtils.isNotBlank(client.getName())) {
					predicates.add(criteriaBuilder.like(root.get("name"), "%" + client.getName() + "%"));
				}
				if (client.getEnabled() != null) {
					predicates.add(criteriaBuilder.equal(root.get("enabled"), client.getEnabled()));
				}
			}
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
		pageable = PageRequest
			.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.Direction.DESC, "createDate");
		return clientRepository.findAll(spec, pageable);
	}

	@Override
	public Client findById(String id) {
		if (StringUtils.isBlank(id)) {
			return null;
		}
		return clientRepository.findById(id).orElse(null);
	}

	@Override
	public Client findByClientId(String clientId) {
		if (StringUtils.isBlank(clientId)) {
			return null;
		}
		return clientRepository.findByClientId(clientId).orElse(null);
	}

	@Override
	public Client save(Client client) {
		if (client == null) {
			return null;
		}
		if (client.getEnabled() == null) {
			client.setEnabled(true);
		}
		client.setCreateDate(new Date());
		client.setLastModifiedDate(null);
		return clientRepository.save(client);
	}

	@Override
	public Client update(Client client) {
		Client exists;
		if (client == null || StringUtils.isBlank(client.getId()) || (exists = this.findById(client.getId())) == null) {
			return null;
		}
		if (StringUtils.isNotBlank(client.getName())) {
			exists.setName(client.getName());
		}
		if (client.getEnabled() != null) {
			exists.setEnabled(client.getEnabled());
		}
		if (StringUtils.isNotBlank(client.getAuthenticationMethods())) {
			exists.setAuthenticationMethods(client.getAuthenticationMethods());
		}
		if (StringUtils.isNotBlank(client.getAuthorizationGrantTypes())) {
			exists.setAuthorizationGrantTypes(client.getAuthorizationGrantTypes());
		}
		if (StringUtils.isNotBlank(client.getRedirectUris())) {
			exists.setRedirectUris(client.getRedirectUris());
		}
		if (StringUtils.isNotBlank(client.getScopes())) {
			exists.setScopes(client.getScopes());
		}
		if (client.getRequireProofKey() != null) {
			exists.setRequireProofKey(client.getRequireProofKey());
		}
		if (client.getRequireAuthorizationConsent() != null) {
			exists.setRequireAuthorizationConsent(client.getRequireAuthorizationConsent());
		}
		if (client.getAccessTokenTimeToLive() != null) {
			exists.setAccessTokenTimeToLive(client.getAccessTokenTimeToLive());
		}
		if (client.getRefreshTokenTimeToLive() != null) {
			exists.setRefreshTokenTimeToLive(client.getRefreshTokenTimeToLive());
		}
		if (client.getEnabled() != null){
			exists.setEnabled(client.getEnabled());
		}
		exists.setLastModifiedDate(new Date());
		return clientRepository.save(exists);
	}

	@Override
	public void deleteById(String id) {
		if (StringUtils.isNotBlank(id)) {
			clientRepository.deleteById(id);
		}
	}

}
