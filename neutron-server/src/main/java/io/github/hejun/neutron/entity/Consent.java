package io.github.hejun.neutron.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 客户端-用户授权
 *
 * @author HeJun
 */
@Getter
@Setter
@TableName("t_consent")
public class Consent {

	private Long clientId;

	private Long userId;

	private String authorities;

}
