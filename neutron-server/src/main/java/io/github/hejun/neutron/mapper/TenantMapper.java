package io.github.hejun.neutron.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.hejun.neutron.entity.Tenant;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户 Mapper
 *
 * @author HeJun
 */
@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {
}
