package io.github.hejun.auth.converter;

import io.github.hejun.auth.entity.User;
import io.github.hejun.auth.vo.user.UserListVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 用户转换类
 *
 * @author HeJun
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserConverter {

	UserListVO toUserList(User user);

}
