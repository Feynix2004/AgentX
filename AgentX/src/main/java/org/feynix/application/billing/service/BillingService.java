package org.feynix.application.billing.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.feynix.application.billing.dto.BillingContext;
import org.feynix.domain.product.model.ProductEntity;
import org.feynix.domain.product.service.ProductDomainService;
import org.feynix.domain.rule.model.RuleEntity;
import org.feynix.domain.rule.service.RuleDomainService;
import org.feynix.domain.user.model.AccountEntity;
import org.feynix.domain.user.model.UsageRecordEntity;
import org.feynix.domain.user.service.AccountDomainService;
import org.feynix.domain.user.service.UsageRecordDomainService;
import org.feynix.infrastructure.billing.strategy.BillingStrategy;
import org.feynix.infrastructure.billing.strategy.BillingStrategyFactory;
import org.feynix.infrastructure.exception.BusinessException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/** 计费服务 协调整个计费流程的核心服务 */
@Service
public class BillingService {

    private final ProductDomainService productDomainService;
    private final RuleDomainService ruleDomainService;
    private final AccountDomainService accountDomainService;
    private final UsageRecordDomainService usageRecordDomainService;
    private final BillingStrategyFactory billingStrategyFactory;

    public BillingService(ProductDomainService productDomainService, RuleDomainService ruleDomainService,
            AccountDomainService accountDomainService, UsageRecordDomainService usageRecordDomainService,
            BillingStrategyFactory billingStrategyFactory) {
        this.productDomainService = productDomainService;
        this.ruleDomainService = ruleDomainService;
        this.accountDomainService = accountDomainService;
        this.usageRecordDomainService = usageRecordDomainService;
        this.billingStrategyFactory = billingStrategyFactory;
    }

    /** 执行计费
     * 
     * @param context 计费上下文
     * @throws BusinessException 余额不足或其他业务异常 */
    @Transactional
    public void charge(BillingContext context) {
        // 1. 验证上下文
        if (!context.isValid()) {
            throw new BusinessException("无效的计费上下文");
        }

        // 2. 查找商品
        ProductEntity product = productDomainService.findProductByBusinessKey(context.getType(),
                context.getServiceId());

        if (product == null) {
            // 没有配置计费规则，直接放行
            return;
        }

        if (!product.isActive()) {
            throw new BusinessException("商品已被禁用，无法计费");
        }

        // 3. 检查幂等性
        if (context.getRequestId() != null && usageRecordDomainService.existsByRequestId(context.getRequestId())) {
            // 请求已处理，直接返回
            return;
        }

        // 4. 获取规则和策略
        RuleEntity rule = ruleDomainService.getRuleById(product.getRuleId());
        if (rule == null) {
            throw new BusinessException("关联的计费规则不存在");
        }

        BillingStrategy strategy = billingStrategyFactory.getStrategy(rule.getHandlerKey());

        // 5. 计算费用
        BigDecimal cost = strategy.calculate(context.getUsageData(), product.getPricingConfig());

        if (cost.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("计算出的费用不能为负数");
        }

        // 如果费用为0，也需要记录用量，但不扣费
        if (cost.compareTo(BigDecimal.ZERO) == 0) {
            recordUsage(context, product, cost);
            return;
        }

        // 6. 检查余额并扣费
        accountDomainService.deduct(context.getUserId(), cost);

        // 7. 记录用量
        recordUsage(context, product, cost);
    }

    /** 检查余额是否充足（不实际扣费）
     * 
     * @param context 计费上下文
     * @return 是否余额充足 */
    public boolean checkBalance(BillingContext context) {
        try {
            // 查找商品
            ProductEntity product = productDomainService.findProductByBusinessKey(context.getType(),
                    context.getServiceId());

            if (product == null || !product.isActive()) {
                return true; // 无需计费
            }

            // 获取规则和策略
            RuleEntity rule = ruleDomainService.getRuleById(product.getRuleId());
            if (rule == null) {
                return false;
            }

            BillingStrategy strategy = billingStrategyFactory.getStrategy(rule.getHandlerKey());

            // 计算费用
            BigDecimal cost = strategy.calculate(context.getUsageData(), product.getPricingConfig());

            if (cost.compareTo(BigDecimal.ZERO) <= 0) {
                return true; // 无需扣费
            }

            // 检查余额
            return accountDomainService.checkSufficientBalance(context.getUserId(), cost);

        } catch (Exception e) {
            return false;
        }
    }

    /** 记录用量 */
    private void recordUsage(BillingContext context, ProductEntity product, BigDecimal cost) {
        UsageRecordEntity usageRecord = new UsageRecordEntity();
        usageRecord.setId(UUID.randomUUID().toString());
        usageRecord.setUserId(context.getUserId());
        usageRecord.setProductId(product.getId());
        usageRecord.setQuantityData(context.getUsageData());
        usageRecord.setCost(cost);
        usageRecord.setRequestId(context.getRequestId());

        usageRecordDomainService.createUsageRecord(usageRecord);
    }

    /** 估算费用（不实际扣费）
     * 
     * @param context 计费上下文
     * @return 估算费用，如果无需计费返回null */
    public BigDecimal estimateCost(BillingContext context) {
        try {
            // 查找商品
            ProductEntity product = productDomainService.findProductByBusinessKey(context.getType(),
                    context.getServiceId());

            if (product == null || !product.isActive()) {
                return null; // 无需计费
            }

            // 获取规则和策略
            RuleEntity rule = ruleDomainService.getRuleById(product.getRuleId());
            if (rule == null) {
                throw new BusinessException("关联的计费规则不存在");
            }

            BillingStrategy strategy = billingStrategyFactory.getStrategy(rule.getHandlerKey());

            // 计算费用
            return strategy.calculate(context.getUsageData(), product.getPricingConfig());

        } catch (Exception e) {
            throw new BusinessException("费用估算失败: " + e.getMessage());
        }
    }
}