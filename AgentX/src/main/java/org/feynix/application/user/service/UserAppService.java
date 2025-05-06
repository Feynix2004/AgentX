package org.feynix.application.user.service;

import org.springframework.stereotype.Service;
import org.feynix.application.user.assembler.UserAssembler;
import org.feynix.application.user.dto.UserDTO;
import org.feynix.domain.user.model.UserEntity;
import org.feynix.domain.user.service.UserDomainService;

@Service
public class UserAppService {

    private final UserDomainService userDomainService;

    public UserAppService(UserDomainService userDomainService) {
        this.userDomainService = userDomainService;
    }

    /** 获取用户信息 */
    public UserDTO getUserInfo(String id) {
        UserEntity userEntity = userDomainService.getUserInfo(id);
        return UserAssembler.toDTO(userEntity);
    }
}
