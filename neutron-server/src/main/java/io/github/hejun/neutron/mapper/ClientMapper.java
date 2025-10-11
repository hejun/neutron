package io.github.hejun.neutron.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.hejun.neutron.entity.Client;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户端 Mapper
 *
 * @author HeJun
 */
@Mapper
public interface ClientMapper extends BaseMapper<Client> {
}
