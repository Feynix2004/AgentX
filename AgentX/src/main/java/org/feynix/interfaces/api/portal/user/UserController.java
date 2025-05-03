package org.feynix.interfaces.api.portal.user;


import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.feynix.application.user.dto.UserDTO;
import org.feynix.application.user.service.LoginAppService;
import org.feynix.application.user.service.UserAppService;
import org.feynix.interfaces.api.common.Result;
import org.feynix.interfaces.dto.user.request.LoginRequest;
import org.feynix.interfaces.dto.user.request.RegisterRequest;

import java.util.Map;

/**
 * 用户
 */
@RestController
@RequestMapping("/users")
public class UserController {

   private final UserAppService userAppService;


    public UserController(UserAppService userAppService) {
        this.userAppService = userAppService;
    }

    /**
     * 获取用户信息
     * @param userId
     * @return
     */
    @GetMapping("/{userId}")
    public Result<UserDTO> getUserInfo(@PathVariable String userId){
        return Result.success(userAppService.getUserInfo(userId));
    }
}
