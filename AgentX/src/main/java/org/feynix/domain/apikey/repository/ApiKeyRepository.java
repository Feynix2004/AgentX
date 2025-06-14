package org.feynix.domain.apikey.repository;

import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.apikey.model.ApiKeyEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

/** API密钥仓储接口 */
@Mapper
public interface ApiKeyRepository extends MyBatisPlusExtRepository<ApiKeyEntity> {
}