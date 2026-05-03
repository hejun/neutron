package io.github.hejun.auth.repository;

import io.github.hejun.auth.entity.Consent;
import io.github.hejun.auth.entity.UserClientKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 授权 Repository
 *
 * @author HeJun
 */
@Repository
public interface ConsentRepository extends JpaRepository<Consent, UserClientKey>, JpaSpecificationExecutor<Consent> {
}
