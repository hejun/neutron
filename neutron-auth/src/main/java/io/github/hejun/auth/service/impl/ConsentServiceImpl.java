package io.github.hejun.auth.service.impl;

import io.github.hejun.auth.entity.Consent;
import io.github.hejun.auth.entity.UserClientKey;
import io.github.hejun.auth.repository.ConsentRepository;
import io.github.hejun.auth.service.IConsentService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.UpdateSpecification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端-用户授权 Service
 *
 * @author HeJun
 */
@Service
@RequiredArgsConstructor
public class ConsentServiceImpl implements IConsentService {

	private final ConsentRepository consentRepository;

	@Override
	public Consent findByUserAndClient(Long userId, Long clientId) {
		UserClientKey key = new UserClientKey();
		key.setUserId(userId);
		key.setClientId(clientId);
		return consentRepository.findById(key).orElse(null);
	}

	@Override
	public Consent save(Consent consent) {
		return consentRepository.save(consent);
	}

	@Override
	public long update(Consent consent) {
		if (consent == null ||
			consent.getUser() == null || consent.getUser().getId() == null ||
			consent.getClient() == null || consent.getClient().getId() == null
		) {
			return 0;
		}
		UpdateSpecification<Consent> updateSpecification = UpdateSpecification
			.<Consent>update((root, update, criteriaBuilder) -> update
                .set("authorities", consent.getAuthorities())
            )
			.where((root, update, criteriaBuilder) -> {
				List<Predicate> predicates = new ArrayList<>();
				predicates.add(criteriaBuilder.equal(root.get("user").get("id"), consent.getUser().getId()));
				predicates.add(criteriaBuilder.equal(root.get("client").get("id"), consent.getClient().getId()));
				return criteriaBuilder.and(predicates);
			});
		return consentRepository.update(updateSpecification);
	}

	@Override
	public void delete(Long userId, Long clientId) {
		if (userId == null || clientId == null) {
			return;
		}

		UserClientKey key = new UserClientKey();
		key.setUserId(userId);
		key.setClientId(clientId);
		consentRepository.deleteById(key);
	}

}
