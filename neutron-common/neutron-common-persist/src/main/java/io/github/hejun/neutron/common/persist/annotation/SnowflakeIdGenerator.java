package io.github.hejun.neutron.common.persist.annotation;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 雪花ID生成算法
 * <p>
 * 雪花ID共64位Bit, 41位时间戳, 10位机器ID(5位机器节点ID,5位数据中心ID,共10位), 12位计数序列号
 *
 * @author HeJun
 */
public class SnowflakeIdGenerator implements IdentifierGenerator {

	// 起始 2026-01-01 00:00:00.000
	private static final long epoch = 1767196800000L;
	private static final long workerIdShift = 12L;
	private static final long datacenterIdShift = workerIdShift + 5;
	private static final long timestampLeftShift = datacenterIdShift + 5;

	// 用于并发控制
	private static long sequence = 0L;
	private static long lastTimestamp = 0L;

	private final long dataCenterId;
	private final long workerId;

	public SnowflakeIdGenerator() {
		this.dataCenterId = getDataCenterId();
		this.workerId = getWorkerId();
	}

	@Override
	public synchronized Object generate(SharedSessionContractImplementor session, Object object) {
		long timestamp = genTimestamp();
		// 时间戳部分 | 数据中心部分 | 机器标识部分 | 序列号部分
		return ((timestamp - epoch) << timestampLeftShift)
			| (dataCenterId << datacenterIdShift)
			| (workerId << workerIdShift)
			| sequence;
	}

	/**
	 * 获取 DataCenterId
	 *
	 * @return DataCenterId
	 */
	private long getDataCenterId() {
		try {
			long dataCenterId;
			InetAddress inetAddress = InetAddress.getLocalHost();
			String hostName = inetAddress.getHostName();

			if (hostName == null || hostName.equals("localhost") || hostName.startsWith("127.")) {
				NetworkInterface network = NetworkInterface.getByInetAddress(inetAddress);
				if (network == null || network.getHardwareAddress() == null) {
					dataCenterId = 1L;
				} else {
					byte[] mac = network.getHardwareAddress();
					dataCenterId = ((0x000000FF & (long) mac[mac.length - 2]) | (0x0000FF00 & (((long) mac[mac.length - 1]) << 8))) >> 6;
				}
				return dataCenterId % 32;
			} else {
				return Math.abs(hostName.hashCode()) % 32;
			}
		} catch (Exception e) {
			// 网络环境异常的兜底处理
			return ThreadLocalRandom.current().nextLong(32);
		}
	}

	/**
	 * 获取 WorkerId
	 *
	 * @return WorkerId
	 */
	private long getWorkerId() {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		int pid = Integer.parseInt(name.split("@")[0]);
		if (pid < 10) { // 疑似容器环境
			pid = Math.abs(name.hashCode());
		}
		return (0xffff & pid) % 32;
	}

	private long genTimestamp() {
		long timestamp = timeGen();
		// 闰秒时, 短时间在允许范围内的话直接等待到正常
		if (timestamp < lastTimestamp) {
			long offset = lastTimestamp - timestamp;
			if (offset <= 5) {
				try {
					Thread.sleep(offset << 1);
					timestamp = timeGen();
					if (timestamp < lastTimestamp) {
						throw new RuntimeException("系统时间异常, 可能有调时!");
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException(e);
				}
			} else {
				throw new RuntimeException("系统时间异常, 可能有调时!");
			}
		}

		if (timestamp == lastTimestamp) {
			// 序列号在当前毫秒内自增，并利用位掩码4095（2的12次方减1）防止超过12位
			sequence = (sequence + 1) & 4095;
			// 变为0说明4095个号在这一毫秒全部用光了（溢出）
			if (sequence == 0) {
				// 强行自旋等待，直到进入下一毫秒
				timestamp = tilNextMillis(lastTimestamp);
			}
		} else {
			sequence = 0L;
		}

		lastTimestamp = timestamp;
		return timestamp;
	}

	private long tilNextMillis(long lastTimestamp) {
		long timestamp = timeGen();
		while (timestamp <= lastTimestamp) {
			timestamp = timeGen(); // 自旋等待
		}
		return timestamp;
	}

	/**
	 * 获取当前时间戳, 高并发下可能需要优化
	 *
	 * @return 当前时间戳
	 */
	private long timeGen() {
		return System.currentTimeMillis();
	}

}
