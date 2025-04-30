package org.feynix.domain.user.repository;

import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.user.model.UserEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

/**
 * 模型仓储接口
 */
@Mapper
public interface UserRepository extends MyBatisPlusExtRepository<UserEntity> {
    
   
} 