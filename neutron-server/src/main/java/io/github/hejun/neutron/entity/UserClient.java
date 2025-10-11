package io.github.hejun.neutron.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户-客户端 关系表
 *
 * @author HeJun
 */
@Getter
@Setter
@TableName("t_user_client")
public class UserClient {

	private Long userId;
	private Long clientId;

}
