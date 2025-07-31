package io.github.hejun.neutron.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.hejun.neutron.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 *
 * @author HeJun
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
