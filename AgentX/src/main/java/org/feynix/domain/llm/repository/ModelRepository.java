package org.feynix.domain.llm.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.llm.model.ModelEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

/** 模型仓储接口 */
@Mapper
public interface ModelRepository extends MyBatisPlusExtRepository<ModelEntity> {

}