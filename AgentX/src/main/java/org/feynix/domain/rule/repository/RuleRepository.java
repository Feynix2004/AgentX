package org.feynix.domain.rule.repository;

import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.rule.model.RuleEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

/**
 * 规则仓储接口
 */
@Mapper
public interface RuleRepository extends MyBatisPlusExtRepository<RuleEntity> {
}