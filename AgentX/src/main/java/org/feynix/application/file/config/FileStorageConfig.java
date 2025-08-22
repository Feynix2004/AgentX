package org.feynix.application.file.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.feynix.application.file.strategy.AvatarFileStorageStrategy;
import org.feynix.application.file.strategy.FileStorageStrategy;
import org.feynix.application.file.strategy.GeneralFileStorageStrategy;
import org.feynix.application.file.strategy.RagFileStorageStrategy;
import org.feynix.domain.file.constant.FileTypeEnum;

/** 文件存储配置类
 * 
 * 配置策略映射关系，供策略工厂使用
 * 
 * @author shilong.zang
 * @date 2024-12-09 */
@Configuration
public class FileStorageConfig {

    /** 配置文件类型与策略的映射关系
     * 
     * @param ragFileStorageStrategy RAG文件策略
     * @param avatarFileStorageStrategy 头像文件策略
     * @param generalFileStorageStrategy 通用文件策略
     * @return 策略映射Map */
    @Bean
    public Map<FileTypeEnum, FileStorageStrategy> fileStorageStrategyMap(RagFileStorageStrategy ragFileStorageStrategy,
            AvatarFileStorageStrategy avatarFileStorageStrategy,
            GeneralFileStorageStrategy generalFileStorageStrategy) {

        Map<FileTypeEnum, FileStorageStrategy> strategyMap = new HashMap<>();

        strategyMap.put(FileTypeEnum.RAG, ragFileStorageStrategy);
        strategyMap.put(FileTypeEnum.AVATAR, avatarFileStorageStrategy);
        strategyMap.put(FileTypeEnum.GENERAL, generalFileStorageStrategy);

        return strategyMap;
    }
}