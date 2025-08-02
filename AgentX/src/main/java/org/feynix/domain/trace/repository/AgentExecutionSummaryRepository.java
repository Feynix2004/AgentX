package org.feynix.domain.trace.repository;

import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.trace.model.AgentExecutionSummaryEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

/** Agent执行链路汇总仓库接口 */
@Mapper
public interface AgentExecutionSummaryRepository extends MyBatisPlusExtRepository<AgentExecutionSummaryEntity> {
}