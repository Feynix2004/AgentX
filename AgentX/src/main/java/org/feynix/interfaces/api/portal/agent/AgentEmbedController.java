package org.feynix.interfaces.api.portal.agent;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.feynix.application.agent.dto.AgentEmbedDTO;
import org.feynix.application.agent.service.AgentEmbedAppService;
import org.feynix.infrastructure.auth.UserContext;
import org.feynix.interfaces.api.common.Result;
import org.feynix.interfaces.dto.agent.request.CreateEmbedRequest;
import org.feynix.interfaces.dto.agent.request.UpdateEmbedRequest;

import java.util.List;

/** Agent嵌入配置控制器 */
@RestController
@RequestMapping("/agents/{agentId}/embeds")
public class AgentEmbedController {

    private final AgentEmbedAppService agentEmbedAppService;

    public AgentEmbedController(AgentEmbedAppService agentEmbedAppService) {
        this.agentEmbedAppService = agentEmbedAppService;
    }

    /** 创建嵌入配置
     *
     * @param agentId Agent ID
     * @param request 创建请求
     * @return 创建的嵌入配置 */
    @PostMapping
    public Result<AgentEmbedDTO> createEmbed(@PathVariable String agentId,
                                           @RequestBody @Validated CreateEmbedRequest request) {
        String userId = UserContext.getCurrentUserId();
        AgentEmbedDTO embed = agentEmbedAppService.createEmbed(agentId, request, userId);
        return Result.success(embed);
    }

    /** 获取Agent的所有嵌入配置
     *
     * @param agentId Agent ID
     * @return 嵌入配置列表 */
    @GetMapping
    public Result<List<AgentEmbedDTO>> getEmbeds(@PathVariable String agentId) {
        String userId = UserContext.getCurrentUserId();
        List<AgentEmbedDTO> embeds = agentEmbedAppService.getEmbedsByAgent(agentId, userId);
        return Result.success(embeds);
    }

    /** 获取嵌入配置详情
     *
     * @param agentId Agent ID
     * @param embedId 嵌入配置ID
     * @return 嵌入配置详情 */
    @GetMapping("/{embedId}")
    public Result<AgentEmbedDTO> getEmbedDetail(@PathVariable String agentId,
                                              @PathVariable String embedId) {
        String userId = UserContext.getCurrentUserId();
        AgentEmbedDTO embed = agentEmbedAppService.getEmbedDetail(embedId, userId);
        return Result.success(embed);
    }

    /** 更新嵌入配置
     *
     * @param agentId Agent ID
     * @param embedId 嵌入配置ID
     * @param request 更新请求
     * @return 更新后的嵌入配置 */
    @PutMapping("/{embedId}")
    public Result<AgentEmbedDTO> updateEmbed(@PathVariable String agentId,
                                           @PathVariable String embedId,
                                           @RequestBody @Validated UpdateEmbedRequest request) {
        String userId = UserContext.getCurrentUserId();
        AgentEmbedDTO embed = agentEmbedAppService.updateEmbed(embedId, request, userId);
        return Result.success(embed);
    }

    /** 切换嵌入配置启用状态
     *
     * @param agentId Agent ID
     * @param embedId 嵌入配置ID
     * @return 更新后的嵌入配置 */
    @PostMapping("/{embedId}/status")
    public Result<AgentEmbedDTO> toggleEmbedStatus(@PathVariable String agentId,
                                                 @PathVariable String embedId) {
        String userId = UserContext.getCurrentUserId();
        AgentEmbedDTO embed = agentEmbedAppService.toggleEmbedStatus(embedId, userId);
        return Result.success(embed);
    }

    /** 删除嵌入配置
     *
     * @param agentId Agent ID
     * @param embedId 嵌入配置ID
     * @return 删除结果 */
    @DeleteMapping("/{embedId}")
    public Result<Void> deleteEmbed(@PathVariable String agentId,
                                  @PathVariable String embedId) {
        String userId = UserContext.getCurrentUserId();
        agentEmbedAppService.deleteEmbed(embedId, userId);
        return Result.success();
    }
}

/** 用户嵌入配置控制器 */
@RestController
@RequestMapping("/user/embeds")
class UserEmbedController {

    private final AgentEmbedAppService agentEmbedAppService;

    public UserEmbedController(AgentEmbedAppService agentEmbedAppService) {
        this.agentEmbedAppService = agentEmbedAppService;
    }

    /** 获取用户的所有嵌入配置
     *
     * @return 嵌入配置列表 */
    @GetMapping
    public Result<List<AgentEmbedDTO>> getUserEmbeds() {
        String userId = UserContext.getCurrentUserId();
        List<AgentEmbedDTO> embeds = agentEmbedAppService.getEmbedsByUser(userId);
        return Result.success(embeds);
    }
}