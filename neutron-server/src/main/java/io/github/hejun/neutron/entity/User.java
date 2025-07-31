package io.github.hejun.neutron.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 用户
 *
 * @author HeJun
 */
@Getter
@Setter
@TableName("t_user")
public class User {

	@TableId(type = IdType.ASSIGN_ID)
	private Long id;

	private String username;

	private String password;

	private Boolean enabled;

	private Long tenantId;

	private Date createDate;

	private Date lastModifiedDate;

}
