package io.github.hejun.auth.converter;

import io.github.hejun.auth.entity.Client;
import io.github.hejun.auth.vo.client.ClientListVO;
import org.mapstruct.*;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * 客户端转换类
 *
 * @author HeJun
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientConverter {

	@Mappings({
		@Mapping(target = "authorizationGrantTypes", qualifiedByName = "strToSet"),
	})
	ClientListVO toClientList(Client client);

	@Named("strToSet")
	default Set<String> strToSet(String str) {
		return StringUtils.commaDelimitedListToSet(str);
	}

	@Named("listToStr")
	default String listToStr(List<String> list) {
		return StringUtils.collectionToCommaDelimitedString(list);
	}

}
