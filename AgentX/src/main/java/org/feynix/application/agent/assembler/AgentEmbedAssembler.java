package org.feynix.application.agent.assembler;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.feynix.application.agent.dto.AgentEmbedDTO;
import org.feynix.application.llm.dto.ModelDTO;
import org.feynix.application.llm.dto.ProviderDTO;
import org.feynix.domain.agent.model.AgentEmbedEntity;
import org.feynix.interfaces.dto.agent.request.CreateEmbedRequest;
import org.feynix.interfaces.dto.agent.request.UpdateEmbedRequest;

import java.util.Collections;
import java.util.List;

/** Agent嵌入配置转换器 */
@Component
public class AgentEmbedAssembler {

    @Value("${agentx.frontend.base-url:http://localhost:3000}")
    public String frontendBaseUrl;

    /** 将Entity转换为DTO
     *
     * @param entity 嵌入配置实体
     * @param model 模型DTO
     * @param provider 服务商DTO
     * @return 嵌入配置DTO */
    public static AgentEmbedDTO toDTO(AgentEmbedEntity entity, ModelDTO model, ProviderDTO provider) {
        if (entity == null) {
            return null;
        }

        AgentEmbedDTO dto = new AgentEmbedDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setModel(model);
        dto.setProvider(provider);
        
        return dto;
    }

    /** 将Entity转换为DTO（带嵌入代码）
     *
     * @param entity 嵌入配置实体
     * @param model 模型DTO
     * @param provider 服务商DTO
     * @param frontendBaseUrl 前端基础URL
     * @return 嵌入配置DTO */
    public static AgentEmbedDTO toDTOWithEmbedCode(AgentEmbedEntity entity, ModelDTO model, 
                                                   ProviderDTO provider, String frontendBaseUrl) {
        AgentEmbedDTO dto = toDTO(entity, model, provider);
        if (dto != null) {
            dto.setEmbedCode(generateEmbedCode(entity.getPublicId(), frontendBaseUrl));
        }
        return dto;
    }

    /** 将CreateEmbedRequest转换为Entity
     *
     * @param request 创建嵌入请求
     * @param agentId Agent ID
     * @param userId 用户ID
     * @return 嵌入配置实体 */
    public static AgentEmbedEntity toEntity(CreateEmbedRequest request, String agentId, String userId) {
        return AgentEmbedEntity.createNew(
                agentId,
                userId,
                request.getEmbedName(),
                request.getEmbedDescription(),
                request.getModelId(),
                request.getProviderId(),
                request.getAllowedDomains(),
                request.getDailyLimit()
        );
    }

    /** 根据UpdateEmbedRequest更新Entity
     *
     * @param entity 要更新的实体
     * @param request 更新请求 */
    public static void updateEntity(AgentEmbedEntity entity, UpdateEmbedRequest request) {
        entity.updateConfig(
                request.getEmbedName(),
                request.getEmbedDescription(),
                request.getModelId(),
                request.getProviderId(),
                request.getAllowedDomains(),
                request.getDailyLimit()
        );
        entity.setEnabled(request.getEnabled());
    }

    /** 将Entity列表转换为DTO列表
     *
     * @param entities 实体列表
     * @param models 模型列表（按实体顺序）
     * @param providers 服务商列表（按实体顺序）
     * @return DTO列表 */
    public static List<AgentEmbedDTO> toDTOs(List<AgentEmbedEntity> entities, List<ModelDTO> models, 
                                           List<ProviderDTO> providers) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(entity -> {
                    int index = entities.indexOf(entity);
                    ModelDTO model = models != null && index < models.size() ? models.get(index) : null;
                    ProviderDTO provider = providers != null && index < providers.size() ? providers.get(index) : null;
                    return toDTO(entity, model, provider);
                })
                .toList();
    }

    /** 将Entity列表转换为带嵌入代码的DTO列表
     *
     * @param entities 实体列表
     * @param models 模型列表
     * @param providers 服务商列表
     * @param frontendBaseUrl 前端基础URL
     * @return DTO列表 */
    public static List<AgentEmbedDTO> toDTOsWithEmbedCode(List<AgentEmbedEntity> entities, 
                                                        List<ModelDTO> models, 
                                                        List<ProviderDTO> providers,
                                                        String frontendBaseUrl) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(entity -> {
                    int index = entities.indexOf(entity);
                    ModelDTO model = models != null && index < models.size() ? models.get(index) : null;
                    ProviderDTO provider = providers != null && index < providers.size() ? providers.get(index) : null;
                    return toDTOWithEmbedCode(entity, model, provider, frontendBaseUrl);
                })
                .toList();
    }

    /** 生成嵌入代码
     *
     * @param publicId 公开访问ID
     * @param baseUrl 基础URL
     * @return 嵌入HTML代码 */
    private static String generateEmbedCode(String publicId, String baseUrl) {
        return String.format("""
            <!-- AgentX 智能助手嵌入代码 -->
            <iframe 
              src="%s/embed/%s"
              width="400" 
              height="600"
              frameborder="0"
              style="border: 1px solid #e2e8f0; border-radius: 8px;"
              allow="microphone">
            </iframe>
            
            <!-- 或者使用悬浮窗模式 -->
            <script>
              (function() {
                const agentButton = document.createElement('div');
                agentButton.innerHTML = '💬 智能助手';
                agentButton.style.cssText = 'position:fixed;bottom:20px;right:20px;z-index:9999;background:#007bff;color:white;padding:12px 20px;border-radius:25px;cursor:pointer;box-shadow:0 4px 12px rgba(0,0,0,0.15);font-family:sans-serif;';
                
                agentButton.onclick = function() {
                  const iframe = document.createElement('iframe');
                  iframe.src = '%s/embed/%s';
                  iframe.style.cssText = 'position:fixed;bottom:80px;right:20px;width:400px;height:600px;border:none;border-radius:8px;z-index:10000;box-shadow:0 8px 32px rgba(0,0,0,0.1);';
                  
                  const closeBtn = document.createElement('div');
                  closeBtn.innerHTML = '×';
                  closeBtn.style.cssText = 'position:fixed;bottom:685px;right:25px;width:20px;height:20px;background:#ff4757;color:white;border-radius:50%%;text-align:center;line-height:20px;cursor:pointer;z-index:10001;font-family:sans-serif;';
                  closeBtn.onclick = function() {
                    document.body.removeChild(iframe);
                    document.body.removeChild(closeBtn);
                    agentButton.style.display = 'block';
                  };
                  
                  document.body.appendChild(iframe);
                  document.body.appendChild(closeBtn);
                  agentButton.style.display = 'none';
                };
                
                document.body.appendChild(agentButton);
              })();
            </script>
            """, baseUrl, publicId, baseUrl, publicId);
    }

    /** 实例方法：使用配置的baseUrl生成嵌入代码 */
    public String generateEmbedCodeWithConfig(String publicId) {
        return generateEmbedCode(publicId, frontendBaseUrl);
    }

    /** 实例方法：转换为带嵌入代码的DTO */
    public AgentEmbedDTO toDTOWithEmbedCode(AgentEmbedEntity entity, ModelDTO model, ProviderDTO provider) {
        return toDTOWithEmbedCode(entity, model, provider, frontendBaseUrl);
    }
}