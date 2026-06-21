package io.github.hejun.neutron.auth.controller;

import io.github.hejun.neutron.auth.converter.UserConverter;
import io.github.hejun.neutron.auth.dto.user.UserListDTO;
import io.github.hejun.neutron.auth.dto.user.UserSaveDTO;
import io.github.hejun.neutron.auth.entity.User;
import io.github.hejun.neutron.auth.service.IUserService;
import io.github.hejun.neutron.auth.vo.user.UserDetailVO;
import io.github.hejun.neutron.auth.vo.user.UserListVO;
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
 * 用户 Controller
 *
 * @author HeJun
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final IUserService userService;
    private final UserConverter userConverter;

    @GetMapping
    public Result<PagedModel<UserListVO>> findPage(UserListDTO dto,
                                                   @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        dto = ObjectUtils.getIfNull(dto, new UserListDTO());
        Page<User> page = userService.findPage(dto.getUsername(), dto.getEnabled(), dto.getTenantId(), pageable);
        Page<UserListVO> convertedPage = page.map(userConverter::toUserList);
        return Result.SUCCESS(new PagedModel<>(convertedPage));
    }

    @GetMapping("/{id:\\d+}")
    public Result<UserDetailVO> findById(@PathVariable Long id) {
        Optional<UserDetailVO> optional = userService.findById(id).map(userConverter::toUserDetail);
        return optional.map(Result::SUCCESS).orElseGet(() -> Result.ERROR(400, "客户端不存在"));
    }

    @PostMapping
    public Result<Void> save(@RequestBody UserSaveDTO dto) {
        userService.save(userConverter.toUser(dto));
        return Result.SUCCESS();
    }

    @PutMapping
    public Result<Void> update(@RequestBody UserSaveDTO dto) {
        userService.update(userConverter.toUser(dto));
        return Result.SUCCESS();
    }

    @DeleteMapping("/{id:\\d+}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.SUCCESS();
    }

}
