package io.github.hejun.auth.service.impl;

import io.github.hejun.auth.entity.Client;
import io.github.hejun.auth.exception.OccupiedException;
import io.github.hejun.auth.repository.ClientRepository;
import io.github.hejun.auth.service.IClientService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.UpdateSpecification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端 Service
 *
 * @author HeJun
 */
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements IClientService {

	private final ClientRepository clientRepository;

	@Override
	public Page<Client> findPage(String name, Boolean enabled, Long tenantId, Pageable pageable) {
		Specification<Client> specification = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (StringUtils.hasText(name)) {
				predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
			}
			if (enabled != null) {
				predicates.add(criteriaBuilder.equal(root.get("enabled"), enabled));
			}
			if (tenantId != null) {
				predicates.add(criteriaBuilder.equal(root.get("tenant").get("id"), tenantId));
			}
			return criteriaBuilder.and(predicates);
		};
		return clientRepository.findAll(specification, pageable);
	}

	@Override
	public Client findById(Long id) {
		if (id == null) {
			return null;
		}
		return clientRepository.findById(id).orElse(null);
	}

	@Override
	public Client findByClientId(String clientId, Long tenantId) {
		if (tenantId == null || !StringUtils.hasText(clientId)) {
			return null;
		}
		return clientRepository.findByClientIdAndTenantId(clientId, tenantId).orElse(null);
	}

	@Override
	public Client save(Client client) {
		if (client == null || client.getTenant() == null || client.getTenant().getId() == null || !StringUtils.hasText(client.getClientId())) {
			return null;
		}
		if (clientRepository.findByClientIdAndTenantId(client.getClientId(), client.getTenant().getId()).isPresent()) {
			throw new OccupiedException("客户端：" + client.getClientId() + " 已被使用");
		}
		return clientRepository.save(client);
	}

	@Override
	public long update(Client client) {
		if (client == null || client.getId() == null) {
			return 0;
		}
		UpdateSpecification<Client> updateSpecification = UpdateSpecification
			.<Client>update((root, update, criteriaBuilder) -> {
				if (StringUtils.hasText(client.getClientId())) {
					update.set("clientId", client.getClientId());
				}
				if (client.getTenant() != null) {
					update.set("tenant.id", client.getTenant().getId());
				}
			})
			.where((root, update, criteriaBuilder) ->
				criteriaBuilder.equal(root.get("id"), client.getId())
			);
		return clientRepository.update(updateSpecification);
	}

	@Override
	public void delete(Long id) {
		if (id == null) {
			return;
		}
		clientRepository.deleteById(id);
	}

}
