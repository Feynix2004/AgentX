package org.feynix.domain.agent.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.agent.model.AgentVersionEntity;

/**
 * Agent版本仓库接口
 */
@Mapper
public interface AgentVersionRepository extends BaseMapper<AgentVersionEntity> {
} 