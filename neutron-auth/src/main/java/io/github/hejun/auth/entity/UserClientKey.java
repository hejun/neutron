package io.github.hejun.auth.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 用户-客户端-联合主键
 *
 * @author HeJun
 */
@Getter
@Setter
@Embeddable
public class UserClientKey implements Serializable {

	/**
	 * 用户ID
	 */
	private Long userId;

	/**
	 * 客户端ID
	 */
	private Long clientId;

}
