package org.feynix.domain.user.repository;

import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.user.model.UsageRecordEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

/**
 * 用量记录仓储接口
 */
@Mapper
public interface UsageRecordRepository extends MyBatisPlusExtRepository<UsageRecordEntity> {
}