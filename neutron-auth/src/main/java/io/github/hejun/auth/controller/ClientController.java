package io.github.hejun.auth.controller;

import io.github.hejun.auth.converter.ClientConverter;
import io.github.hejun.auth.dto.client.ClientListDTO;
import io.github.hejun.auth.entity.Client;
import io.github.hejun.auth.service.IClientService;
import io.github.hejun.auth.vo.client.ClientListVO;
import io.github.hejun.neutron.common.core.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 客户端 Controller
 *
 * @author HeJun
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/client")
public class ClientController {

	private final IClientService clientService;

	private final ClientConverter clientConverter;

	@GetMapping
	public Result<Page<ClientListVO>> findPage(ClientListDTO dto, @PageableDefault Pageable pageable) {
		dto = dto == null ? new ClientListDTO() : dto;
		Page<Client> page = clientService.findPage(dto.getName(), dto.getEnabled(), dto.getTenantId(), pageable);
		Page<ClientListVO> convertedPage = page.map(clientConverter::toClientList);
		return Result.SUCCESS(convertedPage);
	}

}
