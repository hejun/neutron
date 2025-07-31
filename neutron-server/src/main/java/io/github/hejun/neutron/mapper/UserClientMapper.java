package io.github.hejun.neutron.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.hejun.neutron.entity.UserClient;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户-客户端 关系表 Mapper
 *
 * @author HeJun
 */
@Mapper
public interface UserClientMapper extends BaseMapper<UserClient> {
}
