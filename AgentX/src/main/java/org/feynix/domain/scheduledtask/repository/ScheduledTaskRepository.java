package org.feynix.domain.scheduledtask.repository;

import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.scheduledtask.model.ScheduledTaskEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

/** 定时任务仓储接口 */
@Mapper
public interface ScheduledTaskRepository extends MyBatisPlusExtRepository<ScheduledTaskEntity> {
}