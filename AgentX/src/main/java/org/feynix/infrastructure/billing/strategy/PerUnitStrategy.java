package org.feynix.infrastructure.billing.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 按次计费策略
 * 按使用次数进行固定计费
 */
@Component
public class PerUnitStrategy implements BillingStrategy {
    
    private static final String QUANTITY_KEY = "quantity";
    private static final String COST_PER_UNIT_KEY = "cost_per_unit";
    
    @Override
    public BigDecimal calculate(Map<String, Object> usageData, Map<String, Object> pricingConfig) {
        if (!validateUsageData(usageData) || !validatePricingConfig(pricingConfig)) {
            throw new IllegalArgumentException("无效的用量数据或价格配置");
        }
        
        // 获取使用数量
        Integer quantity = (Integer) usageData.get(QUANTITY_KEY);
        
        // 获取单价
        BigDecimal costPerUnit = getBigDecimalValue(pricingConfig, COST_PER_UNIT_KEY);
        
        // 计算总费用：quantity * costPerUnit
        return new BigDecimal(quantity)
                .multiply(costPerUnit)
                .setScale(8, RoundingMode.HALF_UP);
    }
    
    @Override
    public String getStrategyName() {
        return "PER_UNIT_STRATEGY";
    }
    
    @Override
    public boolean validateUsageData(Map<String, Object> usageData) {
        if (usageData == null || usageData.isEmpty()) {
            return false;
        }
        
        // 检查必需字段
        Object quantity = usageData.get(QUANTITY_KEY);
        
        if (!(quantity instanceof Integer)) {
            return false;
        }
        
        // 检查数值有效性
        return (Integer) quantity > 0;
    }
    
    @Override
    public boolean validatePricingConfig(Map<String, Object> pricingConfig) {
        if (pricingConfig == null || pricingConfig.isEmpty()) {
            return false;
        }
        
        // 检查必需字段
        Object costPerUnit = pricingConfig.get(COST_PER_UNIT_KEY);
        
        if (costPerUnit == null) {
            return false;
        }
        
        try {
            BigDecimal costDecimal = getBigDecimalValue(pricingConfig, COST_PER_UNIT_KEY);
            
            // 检查价格非负
            return costDecimal.compareTo(BigDecimal.ZERO) >= 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 从配置中获取BigDecimal值，支持多种数值类型转换
     */
    private BigDecimal getBigDecimalValue(Map<String, Object> config, String key) {
        Object value = config.get(key);
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof Double) {
            return BigDecimal.valueOf((Double) value);
        } else if (value instanceof Float) {
            return BigDecimal.valueOf((Float) value);
        } else if (value instanceof Integer) {
            return new BigDecimal((Integer) value);
        } else if (value instanceof Long) {
            return new BigDecimal((Long) value);
        } else if (value instanceof String) {
            return new BigDecimal((String) value);
        } else {
            throw new IllegalArgumentException("无法转换为BigDecimal: " + key + " = " + value);
        }
    }
}