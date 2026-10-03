package io.github.hejun.neutron.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 登录 Controller
 *
 * @author HeJun
 */
@Controller
@RequiredArgsConstructor
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

}
