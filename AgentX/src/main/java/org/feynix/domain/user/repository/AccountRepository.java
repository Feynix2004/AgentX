package org.feynix.domain.user.repository;

import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.user.model.AccountEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

/**
 * 账户仓储接口
 */
@Mapper
public interface AccountRepository extends MyBatisPlusExtRepository<AccountEntity> {
}