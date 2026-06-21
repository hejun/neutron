package io.github.hejun.neutron.auth.converter;

import io.github.hejun.neutron.auth.dto.user.UserSaveDTO;
import io.github.hejun.neutron.auth.entity.User;
import io.github.hejun.neutron.auth.vo.user.UserDetailVO;
import io.github.hejun.neutron.auth.vo.user.UserListVO;
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

    UserDetailVO toUserDetail(User user);

    User toUser(UserSaveDTO dto);

}
