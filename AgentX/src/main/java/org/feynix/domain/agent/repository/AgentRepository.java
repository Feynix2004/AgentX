package org.feynix.domain.agent.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.agent.model.AgentEntity;

/**
 * Agent仓库接口
 */
@Mapper
public interface AgentRepository extends BaseMapper<AgentEntity> {
} 