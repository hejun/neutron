package io.github.hejun.neutron.annotations.impl;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;
import org.hibernate.id.uuid.StandardRandomStrategy;
import org.hibernate.id.uuid.UuidValueGenerator;
import org.hibernate.type.descriptor.java.UUIDJavaType;

import java.util.EnumSet;
import java.util.UUID;

/**
 * UUID 主键生成策略
 * <p>
 * 默认的有中划线,这里去掉
 *
 * @author HeJun
 */
public class GeneratedUUIDImpl implements BeforeExecutionGenerator {

	private final UuidValueGenerator uuidValueGenerator = StandardRandomStrategy.INSTANCE;

	@Override
	public Object generate(SharedSessionContractImplementor session, Object owner,
						   Object currentValue, EventType eventType) {
		UUID uuid = uuidValueGenerator.generateUuid(session);
		String transformedUUID = UUIDJavaType.ToStringTransformer.INSTANCE.transform(uuid);
		transformedUUID = transformedUUID.replaceAll("-", "");
		return transformedUUID;
	}

	@Override
	public EnumSet<EventType> getEventTypes() {
		return EventTypeSets.INSERT_ONLY;
	}

}
