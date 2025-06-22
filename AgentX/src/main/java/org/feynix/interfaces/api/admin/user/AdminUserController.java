package org.feynix.interfaces.api.admin.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.feynix.application.user.dto.UserDTO;
import org.feynix.application.user.service.UserAppService;
import org.feynix.interfaces.api.common.Result;
import org.feynix.interfaces.dto.user.request.QueryUserRequest;

/** 管理员用户管理接口 */
@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserAppService userAppService;

    public AdminUserController(UserAppService userAppService) {
        this.userAppService = userAppService;
    }

    /** 分页获取用户列表
     * 
     * @param queryUserRequest 查询参数
     * @return 用户分页列表 */
    @GetMapping
    public Result<Page<UserDTO>> getUsers(QueryUserRequest queryUserRequest) {
        return Result.success(userAppService.getUsers(queryUserRequest));
    }
}