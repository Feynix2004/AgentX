package org.feynix.domain.user.repository;

import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.user.model.UserSettingsEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

/** 用户设置仓储接口 */
@Mapper
public interface UserSettingsRepository extends MyBatisPlusExtRepository<UserSettingsEntity> {

} 