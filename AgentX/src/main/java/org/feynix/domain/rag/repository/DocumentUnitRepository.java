package org.feynix.domain.rag.repository;

import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.rag.model.DocumentUnitEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

/**
 * @author shilong.zang
 * @date 21:07 <br/>
 */
@Mapper
public interface DocumentUnitRepository extends MyBatisPlusExtRepository<DocumentUnitEntity> {

}
