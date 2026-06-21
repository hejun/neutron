package io.github.hejun.neutron.auth.dto.tenant;

import lombok.Getter;
import lombok.Setter;

/**
 * 租户 ListDTO
 *
 * @author HeJun
 */
@Getter
@Setter
public class TenantSaveDTO {

    private Long id;

    private String name;

    private String issuer;

    private String copyright;

    private Boolean enabled;

}
