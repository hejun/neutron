package io.github.hejun.neutron.controller;

import io.github.hejun.neutron.conveter.ClientConverter;
import io.github.hejun.neutron.dto.ClientDTO;
import io.github.hejun.neutron.entity.Client;
import io.github.hejun.neutron.service.IClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 客户端 Controller
 *
 * @author HeJun
 */
@RestController
@RequestMapping("/client")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ClientController {

	private final IClientService clientService;
	private final ClientConverter clientConverter;

	@GetMapping
	public PagedModel<ClientDTO> findPage(ClientDTO client, @PageableDefault Pageable pageable) {
		Client query = clientConverter.convert(client);
		Page<Client> page = clientService.findPage(query, pageable);
		Page<ClientDTO> convertedPage = page.map(clientConverter::convert);
		return new PagedModel<>(convertedPage);
	}

	@GetMapping("/{id}")
	public ClientDTO findPage(@PathVariable String id) {
		Client client = clientService.findById(id);
		return clientConverter.convert(client);
	}

	@PostMapping
	public ClientDTO save(@RequestBody @Validated ClientDTO clientDTO) {
		Client entity = clientConverter.convert(clientDTO);
		entity = clientService.save(entity);
		return clientConverter.convert(entity);
	}

	@PutMapping
	public ClientDTO update(@RequestBody @Validated ClientDTO clientDTO) {
		Client entity = clientConverter.convert(clientDTO);
		entity = clientService.update(entity);
		return clientConverter.convert(entity);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable String id) {
		clientService.deleteById(id);
	}

}
