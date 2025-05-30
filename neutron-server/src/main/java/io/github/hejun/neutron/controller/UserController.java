package io.github.hejun.neutron.controller;

import io.github.hejun.neutron.conveter.UserConverter;
import io.github.hejun.neutron.dto.UpdateUserPasswordDTO;
import io.github.hejun.neutron.dto.UserDTO;
import io.github.hejun.neutron.entity.User;
import io.github.hejun.neutron.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户 Controller
 *
 * @author HeJun
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserController {

	private final IUserService userService;
	private final UserConverter userConverter;

	@GetMapping
	public PagedModel<UserDTO> findPage(UserDTO user, @PageableDefault Pageable pageable) {
		User query = userConverter.convert(user);
		Page<User> page = userService.findPage(query, pageable);
		Page<UserDTO> convertedPage = page.map(userConverter::convert);
		return new PagedModel<>(convertedPage);
	}

	@GetMapping("/{id}")
	public UserDTO findPage(@PathVariable String id) {
		User user = userService.findById(id);
		return userConverter.convert(user);
	}

	@PostMapping
	public UserDTO save(@RequestBody @Validated UserDTO userDTO) {
		User entity = userConverter.convert(userDTO);
		entity = userService.save(entity);
		return userConverter.convert(entity);
	}

	@PutMapping
	public UserDTO update(@RequestBody @Validated UserDTO userDTO) {
		User entity = userConverter.convert(userDTO);
		entity = userService.update(entity);
		return userConverter.convert(entity);
	}

	@PutMapping("/updatePassword")
	public UserDTO updatePassword(@RequestBody @Validated UpdateUserPasswordDTO dto) {
		User entity = userService.updatePassword(dto.getId(), dto.getOldPassword(), dto.getNewPassword());
		return userConverter.convert(entity);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable String id) {
		userService.deleteById(id);
	}

}
