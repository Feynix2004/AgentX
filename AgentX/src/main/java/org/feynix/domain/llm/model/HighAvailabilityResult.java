package org.feynix.domain.llm.model;

/** 高可用选择结果
 * 
 * @author feynix
 * @since 1.0.0 */
public class HighAvailabilityResult {

    /** 选择的Provider */
    private ProviderEntity provider;

    /** 选择的Model（可能有不同的部署名称） */
    private ModelEntity model;

    /** 实例ID（用于结果上报） */
    private String instanceId;

    /** 模型是否被切换（降级到备用模型） */
    private boolean switched;

    public HighAvailabilityResult() {
    }

    public HighAvailabilityResult(ProviderEntity provider, ModelEntity model, String instanceId) {
        this.provider = provider;
        this.model = model;
        this.instanceId = instanceId;
        this.switched = false;
    }

    public HighAvailabilityResult(ProviderEntity provider, ModelEntity model, String instanceId, boolean switched) {
        this.provider = provider;
        this.model = model;
        this.instanceId = instanceId;
        this.switched = switched;
    }

    public ProviderEntity getProvider() {
        return provider;
    }

    public void setProvider(ProviderEntity provider) {
        this.provider = provider;
    }

    public ModelEntity getModel() {
        return model;
    }

    public void setModel(ModelEntity model) {
        this.model = model;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public boolean isSwitched() {
        return switched;
    }

    public void setSwitched(boolean switched) {
        this.switched = switched;
    }
}