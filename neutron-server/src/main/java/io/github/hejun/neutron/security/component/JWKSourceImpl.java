package io.github.hejun.neutron.security.component;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import io.github.hejun.neutron.entity.Tenant;
import io.github.hejun.neutron.service.ITenantService;
import io.github.hejun.neutron.util.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多租户 JWKSource
 *
 * @author HeJun
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class JWKSourceImpl implements JWKSource<SecurityContext> {

	private final ITenantService tenantService;

	private final Map<String, List<JWK>> jwkCache = new HashMap<>();

	@Override
	public List<JWK> get(JWKSelector jwkSelector, SecurityContext context) throws KeySourceException {
		String issuer = ContextUtil.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("get, current issuer: {}", issuer);
		}
		if (jwkCache.containsKey(issuer)) {
			return jwkCache.get(issuer);
		}

		try {
			Tenant tenant = tenantService.findByIssuer(issuer);

			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(tenant.getPrivateKey())));
			PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(tenant.getPublicKey())));

			RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) publicKey).privateKey(privateKey)
				.keyID(String.valueOf(tenant.getId()))
				.build();
			JWKSet jwkSet = new JWKSet(rsaKey);
			JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(jwkSet);

			this.jwkCache.put(issuer, jwkSource.get(jwkSelector, context));
			return jwkSource.get(jwkSelector, context);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new AuthenticationServiceException(e.getMessage());
		}
	}

}
