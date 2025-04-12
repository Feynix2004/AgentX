package org.feynix.domain.rag.repository;

/**
 * @author zang
 * @date 18:01 <br/>
 */

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.llm.model.ModelEntity;
import org.feynix.domain.rag.model.DocumentChunkEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

/**
 * 文档块仓储接口
 * @author zang
 */
@Mapper
public interface DocumentChunkRepository extends MyBatisPlusExtRepository<ModelEntity> {

}
