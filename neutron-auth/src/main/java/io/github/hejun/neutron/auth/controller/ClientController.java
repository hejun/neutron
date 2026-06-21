package io.github.hejun.neutron.auth.controller;

import io.github.hejun.neutron.auth.converter.ClientConverter;
import io.github.hejun.neutron.auth.dto.client.ClientListDTO;
import io.github.hejun.neutron.auth.dto.client.ClientSaveDTO;
import io.github.hejun.neutron.auth.entity.Client;
import io.github.hejun.neutron.auth.service.IClientService;
import io.github.hejun.neutron.auth.vo.client.ClientDetailVO;
import io.github.hejun.neutron.auth.vo.client.ClientListVO;
import io.github.hejun.neutron.common.core.dto.Result;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
    public Result<PagedModel<ClientListVO>> findPage(ClientListDTO dto,
                                                     @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        dto = ObjectUtils.getIfNull(dto, new ClientListDTO());
        Page<Client> page = clientService.findPage(dto.getClientId(), dto.getClientName(), dto.getEnabled(), dto.getTenantId(), pageable);
        Page<ClientListVO> convertedPage = page.map(clientConverter::toClientList);
        return Result.SUCCESS(new PagedModel<>(convertedPage));
    }

    @GetMapping("/{id:\\d+}")
    public Result<ClientDetailVO> findById(@PathVariable Long id) {
        Optional<ClientDetailVO> optional = clientService.findById(id).map(clientConverter::toClientDetail);
        return optional.map(Result::SUCCESS).orElseGet(() -> Result.ERROR(400, "客户端不存在"));
    }

    @PostMapping
    public Result<Void> save(@RequestBody ClientSaveDTO dto) {
        clientService.save(clientConverter.toClient(dto));
        return Result.SUCCESS();
    }

    @PutMapping
    public Result<Void> update(@RequestBody ClientSaveDTO dto) {
        clientService.update(clientConverter.toClient(dto));
        return Result.SUCCESS();
    }

    @DeleteMapping("/{id:\\d+}")
    public Result<Void> delete(@PathVariable Long id) {
        clientService.delete(id);
        return Result.SUCCESS();
    }

}
