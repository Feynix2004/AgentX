package org.feynix.application.usage.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.feynix.application.usage.assembler.UsageRecordAssembler;
import org.feynix.application.usage.dto.UsageRecordDTO;
import org.feynix.domain.user.model.UsageRecordEntity;
import org.feynix.domain.user.service.UsageRecordDomainService;
import org.feynix.infrastructure.exception.BusinessException;
import org.feynix.interfaces.dto.usage.request.QueryUsageRecordRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 使用记录应用服务 处理使用记录相关的业务流程编排 */
@Service
public class UsageRecordAppService {

    private final UsageRecordDomainService usageRecordDomainService;
    private final UsageRecordBusinessInfoService businessInfoService;

    public UsageRecordAppService(UsageRecordDomainService usageRecordDomainService,
            UsageRecordBusinessInfoService businessInfoService) {
        this.usageRecordDomainService = usageRecordDomainService;
        this.businessInfoService = businessInfoService;
    }

    /** 根据ID获取使用记录
     * @param recordId 记录ID
     * @return 使用记录DTO */
    public UsageRecordDTO getUsageRecordById(String recordId) {
        UsageRecordEntity entity = usageRecordDomainService.getUsageRecordById(recordId);
        if (entity == null) {
            throw new BusinessException("使用记录不存在");
        }
        UsageRecordDTO dto = UsageRecordAssembler.toDTO(entity);

        // 填充业务信息
        fillBusinessInfo(List.of(dto));

        return dto;
    }

    /** 获取用户的使用记录（分页）
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 使用记录分页结果 */
    public Page<UsageRecordDTO> getUserUsageRecords(String userId, int page, int size) {
        Page<UsageRecordEntity> entityPage = usageRecordDomainService.getUserUsageHistory(userId, page, size);

        List<UsageRecordDTO> dtoList = UsageRecordAssembler.toDTOs(entityPage.getRecords());

        // 填充业务信息
        fillBusinessInfo(dtoList);

        Page<UsageRecordDTO> resultPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(),
                entityPage.getTotal());
        resultPage.setRecords(dtoList);

        return resultPage;
    }

    /** 按条件查询使用记录
     * @param request 查询请求
     * @return 使用记录分页结果 */
    public Page<UsageRecordDTO> queryUsageRecords(QueryUsageRecordRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<UsageRecordEntity> wrapper = Wrappers.<UsageRecordEntity>lambdaQuery()
                .eq(StringUtils.isNotBlank(request.getUserId()), UsageRecordEntity::getUserId, request.getUserId())
                .eq(StringUtils.isNotBlank(request.getProductId()), UsageRecordEntity::getProductId,
                        request.getProductId())
                .eq(StringUtils.isNotBlank(request.getRequestId()), UsageRecordEntity::getRequestId,
                        request.getRequestId())
                .ge(request.getStartTime() != null, UsageRecordEntity::getBilledAt, request.getStartTime())
                .le(request.getEndTime() != null, UsageRecordEntity::getBilledAt, request.getEndTime())
                .orderByDesc(UsageRecordEntity::getBilledAt);

        // 分页查询
        Page<UsageRecordEntity> entityPage = new Page<>(request.getPage(), request.getPageSize());
        entityPage = usageRecordDomainService.getUsageRecordRepository().selectPage(entityPage, wrapper);

        // 转换结果
        List<UsageRecordDTO> dtoList = UsageRecordAssembler.toDTOs(entityPage.getRecords());

        // 填充业务信息
        fillBusinessInfo(dtoList);

        Page<UsageRecordDTO> resultPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(),
                entityPage.getTotal());
        resultPage.setRecords(dtoList);

        return resultPage;
    }

    /** 获取用户某个商品的使用记录
     * @param userId 用户ID
     * @param productId 商品ID
     * @param page 页码
     * @param size 每页大小
     * @return 使用记录分页结果 */
    public Page<UsageRecordDTO> getUserProductUsageRecords(String userId, String productId, int page, int size) {
        Page<UsageRecordEntity> entityPage = usageRecordDomainService.getUserProductUsageHistory(userId, productId,
                page, size);

        List<UsageRecordDTO> dtoList = UsageRecordAssembler.toDTOs(entityPage.getRecords());

        // 填充业务信息
        fillBusinessInfo(dtoList);

        Page<UsageRecordDTO> resultPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(),
                entityPage.getTotal());
        resultPage.setRecords(dtoList);

        return resultPage;
    }

    /** 获取用户在指定时间范围内的使用记录
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 使用记录列表 */
    public List<UsageRecordDTO> getUserUsageByTimeRange(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        List<UsageRecordEntity> entities = usageRecordDomainService.getUserUsageByTimeRange(userId, startTime, endTime);
        List<UsageRecordDTO> dtoList = UsageRecordAssembler.toDTOs(entities);

        // 填充业务信息
        fillBusinessInfo(dtoList);

        return dtoList;
    }

    /** 获取商品的使用记录（分页）
     * @param productId 商品ID
     * @param page 页码
     * @param size 每页大小
     * @return 使用记录分页结果 */
    public Page<UsageRecordDTO> getProductUsageRecords(String productId, int page, int size) {
        Page<UsageRecordEntity> entityPage = usageRecordDomainService.getProductUsageHistory(productId, page, size);

        List<UsageRecordDTO> dtoList = UsageRecordAssembler.toDTOs(entityPage.getRecords());

        // 填充业务信息
        fillBusinessInfo(dtoList);

        Page<UsageRecordDTO> resultPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(),
                entityPage.getTotal());
        resultPage.setRecords(dtoList);

        return resultPage;
    }

    /** 检查请求ID是否已存在
     * @param requestId 请求ID
     * @return 是否存在 */
    public boolean existsByRequestId(String requestId) {
        return usageRecordDomainService.existsByRequestId(requestId);
    }

    /** 统计用户的总消费金额
     * @param userId 用户ID
     * @return 总消费金额 */
    public BigDecimal getUserTotalCost(String userId) {
        Page<UsageRecordEntity> entityPage = usageRecordDomainService.getUserUsageHistory(userId, 1, Integer.MAX_VALUE);

        return entityPage.getRecords().stream().map(UsageRecordEntity::getCost).reduce(BigDecimal.ZERO,
                BigDecimal::add);
    }

    /** 统计用户在指定时间范围内的消费金额
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消费金额 */
    public BigDecimal getUserCostByTimeRange(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        List<UsageRecordEntity> entities = usageRecordDomainService.getUserUsageByTimeRange(userId, startTime, endTime);

        return entities.stream().map(UsageRecordEntity::getCost).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** 批量填充用量记录的业务信息
     * 
     * @param dtoList 用量记录DTO列表 */
    private void fillBusinessInfo(List<UsageRecordDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }

        // 1. 收集所有商品ID
        Set<String> productIds = dtoList.stream().map(UsageRecordDTO::getProductId).collect(Collectors.toSet());

        // 2. 批量获取业务信息映射
        Map<String, UsageRecordBusinessInfoService.BusinessInfo> businessInfoMap = businessInfoService
                .getBatchBusinessInfo(productIds);

        // 3. 填充业务信息到DTO
        for (UsageRecordDTO dto : dtoList) {
            UsageRecordBusinessInfoService.BusinessInfo businessInfo = businessInfoMap.get(dto.getProductId());
            if (businessInfo != null) {
                dto.setServiceName(businessInfo.getServiceName());
                dto.setServiceType(businessInfo.getServiceType());
                dto.setServiceDescription(businessInfo.getServiceDescription());
                dto.setPricingRule(businessInfo.getPricingRule());
                dto.setRelatedEntityName(businessInfo.getRelatedEntityName());
            }
        }
    }
}