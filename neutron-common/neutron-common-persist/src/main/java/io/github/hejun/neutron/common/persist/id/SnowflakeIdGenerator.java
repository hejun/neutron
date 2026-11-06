package io.github.hejun.neutron.common.persist.id;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongSupplier;

/**
 * 雪花 ID 生成器
 * <p>
 * 64 位结构：1 位符号位 + 41 位时间戳 + 5 位数据中心 + 5 位机器 + 12 位序列号
 * <p>
 * 默认的 10 位机器标识由主机名与进程号哈希生成，可通过 neutron.persist.data-center-id、
 * neutron.persist.worker-id 覆盖
 *
 * @author HeJun
 */
public class SnowflakeIdGenerator implements IdentifierGenerator {

    private static final long EPOCH = Instant.parse("2026-01-01T00:00:00Z").toEpochMilli();

    private static final long SEQUENCE_MASK = (1L << 12) - 1;
    private static final long WORKER_ID_SHIFT = 12L;
    private static final long DATA_CENTER_ID_SHIFT = 17L;
    private static final long TIMESTAMP_SHIFT = 22L;
    private static final long MAX_MACHINE_ID = 31L;
    private static final long MACHINE_ID_SPACE = 1024L;
    private static final long MAX_CLOCK_BACKWARD_MS = 5L;

    private static final Object LOCK = new Object();

    private static final long MACHINE_ID = detectMachineId();
    private static final long DATA_CENTER_ID = resolve("neutron.persist.data-center-id",
        () -> MACHINE_ID >>> 5);
    private static final long WORKER_ID = resolve("neutron.persist.worker-id",
        () -> MACHINE_ID & MAX_MACHINE_ID);

    private static long sequence = 0L;
    private static long lastTimestamp = -1L;

    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) {
        synchronized (LOCK) {
            long timestamp = System.currentTimeMillis();
            if (timestamp < EPOCH) {
                throw new IllegalStateException("系统时间早于雪花 ID 起始时间: " + Instant.ofEpochMilli(EPOCH));
            }
            if (timestamp < lastTimestamp) {
                if (lastTimestamp - timestamp > MAX_CLOCK_BACKWARD_MS) {
                    throw new IllegalStateException("系统时间回拨 " + (lastTimestamp - timestamp) + " ms，无法生成雪花 ID");
                }
                timestamp = lastTimestamp;
            }
            if (timestamp == lastTimestamp) {
                sequence = (sequence + 1) & SEQUENCE_MASK;
                if (sequence == 0L) {
                    timestamp = waitNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0L;
            }
            lastTimestamp = timestamp;
            return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (DATA_CENTER_ID << DATA_CENTER_ID_SHIFT)
                | (WORKER_ID << WORKER_ID_SHIFT)
                | sequence;
        }
    }

    private static long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            Thread.onSpinWait();
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    /**
     * 优先取显式配置，未配置时回退到自动推导
     */
    private static long resolve(String key, LongSupplier detector) {
        Long value = read(key);
        if (value != null) {
            if (value < 0 || value > MAX_MACHINE_ID) {
                throw new IllegalStateException(key + " 必须在 0~" + MAX_MACHINE_ID + " 之间: " + value);
            }
            return value;
        }
        return Math.floorMod(detector.getAsLong(), MAX_MACHINE_ID + 1);
    }

    /**
     * 支持系统属性或环境变量，如 neutron.persist.worker-id / NEUTRON_PERSIST_WORKER_ID
     */
    private static Long read(String key) {
        String property = System.getProperty(key);
        if (property != null) {
            return parse(key, property);
        }
        String env = System.getenv(key.replace('.', '_').replace('-', '_').toUpperCase(Locale.ROOT));
        if (env == null || env.isBlank()) {
            return null;
        }
        return parse(key, env);
    }

    private static long parse(String key, String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException(key + " 不是合法的数字: " + value, e);
        }
    }

    private static long detectMachineId() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            long pid = ProcessHandle.current().pid();
            return Integer.toUnsignedLong((hostName + "@" + pid).hashCode()) % MACHINE_ID_SPACE;
        } catch (Exception e) {
            return ThreadLocalRandom.current().nextLong(MACHINE_ID_SPACE);
        }
    }

}
