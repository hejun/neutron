package io.github.hejun.neutron.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.hejun.neutron.entity.Consent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户端-用户授权 Mapper
 *
 * @author HeJun
 */
@Mapper
public interface ConsentMapper extends BaseMapper<Consent> {
}
