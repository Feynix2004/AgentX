package org.feynix.interfaces.dto.product.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * 更新商品请求
 */
public class UpdateProductRequest {
    
    @NotBlank(message = "商品ID不能为空")
    private String id;
    
    private String name;
    
    private String type;
    
    private String serviceId;
    
    private String ruleId;
    
    private Map<String, Object> pricingConfig;
    
    private Integer status;
    
    public UpdateProductRequest() {
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getServiceId() {
        return serviceId;
    }
    
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }
    
    public String getRuleId() {
        return ruleId;
    }
    
    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }
    
    public Map<String, Object> getPricingConfig() {
        return pricingConfig;
    }
    
    public void setPricingConfig(Map<String, Object> pricingConfig) {
        this.pricingConfig = pricingConfig;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
}