package org.feynix.domain.embedding.repository;

import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.embedding.model.FileDetailEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

/**
 * 文件详情仓库接口
 * @author zang
 */
@Mapper
public interface FileDetailRepository extends MyBatisPlusExtRepository<FileDetailEntity> {
    
} 