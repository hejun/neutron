package io.github.hejun.neutron.conveter;

import io.github.hejun.neutron.dto.UserDTO;
import io.github.hejun.neutron.entity.User;
import org.mapstruct.Mapper;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * User Entity与DTO转换
 *
 * @author HeJun
 */
@Mapper(componentModel = "spring")
public interface UserConverter {

	UserDTO convert(User user);

	User convert(UserDTO userDTO);

	default UserDetails toUserDetails(User user) {
		if (user == null) {
			return null;
		}
		return org.springframework.security.core.userdetails.User.builder()
			.username(user.getUsername())
			.password(user.getPassword())
			.roles("USER")
			.build();
	}

}
