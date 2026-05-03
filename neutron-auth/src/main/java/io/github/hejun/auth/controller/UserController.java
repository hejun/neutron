package io.github.hejun.auth.controller;

import io.github.hejun.auth.converter.UserConverter;
import io.github.hejun.auth.dto.user.UserListDTO;
import io.github.hejun.auth.entity.User;
import io.github.hejun.auth.service.IUserService;
import io.github.hejun.auth.vo.user.UserListVO;
import io.github.hejun.neutron.common.core.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	public Result<Page<UserListVO>> findPage(UserListDTO dto, @PageableDefault Pageable pageable) {
		dto = dto == null ? new UserListDTO() : dto;
		Page<User> page = userService.findPage(dto.getUsername(), dto.getEnabled(), dto.getTenantId(), pageable);
		Page<UserListVO> convertedPage = page.map(userConverter::toUserList);
		return Result.SUCCESS(convertedPage);
	}

}
