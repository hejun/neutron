package io.github.hejun.neutron.service.impl;

import io.github.hejun.neutron.entity.Tenant;
import io.github.hejun.neutron.repository.TenantRepository;
import io.github.hejun.neutron.service.ITenantService;
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
 * 租户Service
 *
 * @author HeJun
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TenantServiceImpl implements ITenantService {

	private final TenantRepository tenantRepository;

	@Override
	public Page<Tenant> findPage(Tenant tenant, Pageable pageable) {
		Specification<Tenant> spec = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (tenant != null) {
				if (StringUtils.isNotBlank(tenant.getCode())) {
					predicates.add(criteriaBuilder.like(root.get("code"), "%" + tenant.getCode() + "%"));
				}
				if (StringUtils.isNotBlank(tenant.getName())) {
					predicates.add(criteriaBuilder.like(root.get("name"), "%" + tenant.getName() + "%"));
				}
				if (tenant.getEnabled() != null) {
					predicates.add(criteriaBuilder.equal(root.get("enabled"), tenant.getEnabled()));
				}
			}
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
		pageable = PageRequest
			.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.Direction.DESC, "createDate");
		return tenantRepository.findAll(spec, pageable);
	}

	@Override
	public Tenant findByCode(String code) {
		if (StringUtils.isBlank(code)) {
			return null;
		}
		return tenantRepository.findByCode(code).orElse(null);
	}

	@Override
	public Tenant findById(String id) {
		if (StringUtils.isBlank(id)) {
			return null;
		}
		return tenantRepository.findById(id).orElse(null);
	}

	@Override
	public Tenant save(Tenant tenant) {
		if (tenant == null) {
			return null;
		}
		if (tenant.getEnabled() == null) {
			tenant.setEnabled(true);
		}
		tenant.setCreateDate(new Date());
		tenant.setLastModifiedDate(null);
		return tenantRepository.save(tenant);
	}

	@Override
	public Tenant update(Tenant tenant) {
		Tenant exists;
		if (tenant == null || StringUtils.isBlank(tenant.getId()) || (exists = this.findById(tenant.getId())) == null) {
			return null;
		}
		if (StringUtils.isNotBlank(tenant.getName())) {
			exists.setName(tenant.getName());
		}
		if (tenant.getEnabled() != null) {
			exists.setEnabled(tenant.getEnabled());
		}
		exists.setLastModifiedDate(new Date());
		return tenantRepository.save(exists);
	}

	@Override
	public void deleteById(String id) {
		if (StringUtils.isNotBlank(id)) {
			tenantRepository.deleteById(id);
		}
	}

}
