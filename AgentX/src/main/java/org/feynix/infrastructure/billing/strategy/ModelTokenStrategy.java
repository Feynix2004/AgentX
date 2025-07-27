package org.feynix.infrastructure.billing.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 模型Token计费策略
 * 基于输入输出Token数量分别计费
 */
@Component
public class ModelTokenStrategy implements BillingStrategy {
    
    private static final String INPUT_TOKENS_KEY = "input";
    private static final String OUTPUT_TOKENS_KEY = "output";
    private static final String INPUT_COST_PER_MILLION_KEY = "input_cost_per_million";
    private static final String OUTPUT_COST_PER_MILLION_KEY = "output_cost_per_million";
    private static final BigDecimal MILLION = new BigDecimal("1000000");
    
    @Override
    public BigDecimal calculate(Map<String, Object> usageData, Map<String, Object> pricingConfig) {
        if (!validateUsageData(usageData) || !validatePricingConfig(pricingConfig)) {
            throw new IllegalArgumentException("无效的用量数据或价格配置");
        }
        
        // 获取Token数量
        Integer inputTokens = (Integer) usageData.get(INPUT_TOKENS_KEY);
        Integer outputTokens = (Integer) usageData.get(OUTPUT_TOKENS_KEY);
        
        // 获取价格配置
        BigDecimal inputCostPerMillion = getBigDecimalValue(pricingConfig, INPUT_COST_PER_MILLION_KEY);
        BigDecimal outputCostPerMillion = getBigDecimalValue(pricingConfig, OUTPUT_COST_PER_MILLION_KEY);
        
        // 计算输入Token费用：(inputTokens / 1000000) * inputCostPerMillion
        BigDecimal inputCost = new BigDecimal(inputTokens)
                .divide(MILLION, 8, RoundingMode.HALF_UP)
                .multiply(inputCostPerMillion);
        
        // 计算输出Token费用：(outputTokens / 1000000) * outputCostPerMillion  
        BigDecimal outputCost = new BigDecimal(outputTokens)
                .divide(MILLION, 8, RoundingMode.HALF_UP)
                .multiply(outputCostPerMillion);
        
        // 总费用
        return inputCost.add(outputCost).setScale(8, RoundingMode.HALF_UP);
    }
    
    @Override
    public String getStrategyName() {
        return "MODEL_TOKEN_STRATEGY";
    }
    
    @Override
    public boolean validateUsageData(Map<String, Object> usageData) {
        if (usageData == null || usageData.isEmpty()) {
            return false;
        }
        
        // 检查必需字段
        Object inputTokens = usageData.get(INPUT_TOKENS_KEY);
        Object outputTokens = usageData.get(OUTPUT_TOKENS_KEY);
        
        if (!(inputTokens instanceof Integer) || !(outputTokens instanceof Integer)) {
            return false;
        }
        
        // 检查数值有效性
        return (Integer) inputTokens >= 0 && (Integer) outputTokens >= 0;
    }
    
    @Override
    public boolean validatePricingConfig(Map<String, Object> pricingConfig) {
        if (pricingConfig == null || pricingConfig.isEmpty()) {
            return false;
        }
        
        // 检查必需字段
        Object inputCost = pricingConfig.get(INPUT_COST_PER_MILLION_KEY);
        Object outputCost = pricingConfig.get(OUTPUT_COST_PER_MILLION_KEY);
        
        if (inputCost == null || outputCost == null) {
            return false;
        }
        
        try {
            BigDecimal inputCostDecimal = getBigDecimalValue(pricingConfig, INPUT_COST_PER_MILLION_KEY);
            BigDecimal outputCostDecimal = getBigDecimalValue(pricingConfig, OUTPUT_COST_PER_MILLION_KEY);
            
            // 检查价格非负
            return inputCostDecimal.compareTo(BigDecimal.ZERO) >= 0 
                && outputCostDecimal.compareTo(BigDecimal.ZERO) >= 0;
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