package org.feynix.domain.auth.repository;

import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.auth.model.AuthSettingEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

/** 认证配置Repository接口 */
@Mapper
public interface AuthSettingRepository extends MyBatisPlusExtRepository<AuthSettingEntity> {
}