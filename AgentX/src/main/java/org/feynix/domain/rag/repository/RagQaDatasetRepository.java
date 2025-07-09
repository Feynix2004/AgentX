package org.feynix.domain.rag.repository;

import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.rag.model.FileDetailEntity;
import org.feynix.domain.rag.model.RagQaDatasetEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

/**
 * @author shilong.zang
 * @date 17:44 <br/>
 */
@Mapper
public interface RagQaDatasetRepository extends MyBatisPlusExtRepository<RagQaDatasetEntity> {

}
