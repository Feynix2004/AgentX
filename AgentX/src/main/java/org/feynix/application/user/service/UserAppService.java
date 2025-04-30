package org.feynix.application.user.service;


import org.feynix.application.user.assembler.UserAssembler;
import org.feynix.application.user.dto.UserDTO;
import org.feynix.domain.user.model.UserEntity;
import org.feynix.domain.user.service.UserDomainService;

public class UserAppService {


    private final UserDomainService userDomainService;

    public UserAppService(UserDomainService userDomainService) {
        this.userDomainService = userDomainService;
    }

    public UserDTO getUserInfo(String id){
        UserEntity userEntity = userDomainService.getInfo(id);
        return UserAssembler.toDTO(userEntity);
    }
}
