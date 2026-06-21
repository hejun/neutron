package io.github.hejun.neutron.auth.converter;

import io.github.hejun.neutron.auth.dto.client.ClientSaveDTO;
import io.github.hejun.neutron.auth.entity.Client;
import io.github.hejun.neutron.auth.vo.client.ClientDetailVO;
import io.github.hejun.neutron.auth.vo.client.ClientListVO;
import org.mapstruct.*;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 客户端转换类
 *
 * @author HeJun
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientConverter {

    @Mappings({
        @Mapping(target = "authorizationGrantTypes", qualifiedByName = "strToList"),
    })
    ClientListVO toClientList(Client client);

    @Mappings({
        @Mapping(target = "clientAuthenticationMethods", qualifiedByName = "strToList"),
        @Mapping(target = "authorizationGrantTypes", qualifiedByName = "strToList"),
        @Mapping(target = "redirectUris", qualifiedByName = "strToList"),
        @Mapping(target = "postLogoutRedirectUris", qualifiedByName = "strToList"),
        @Mapping(target = "scopes", qualifiedByName = "strToList"),
    })
    ClientDetailVO toClientDetail(Client client);

    @Mappings({
        @Mapping(target = "clientAuthenticationMethods", qualifiedByName = "listToStr"),
        @Mapping(target = "authorizationGrantTypes", qualifiedByName = "listToStr"),
        @Mapping(target = "redirectUris", qualifiedByName = "listToStr"),
        @Mapping(target = "postLogoutRedirectUris", qualifiedByName = "listToStr"),
        @Mapping(target = "scopes", qualifiedByName = "listToStr"),
    })
    Client toClient(ClientSaveDTO dto);

    @Named("strToList")
    default List<String> strToSet(String str) {
        return List.of(StringUtils.commaDelimitedListToStringArray(str));
    }

    @Named("listToStr")
    default String listToStr(List<String> list) {
        return StringUtils.collectionToCommaDelimitedString(list);
    }

}
