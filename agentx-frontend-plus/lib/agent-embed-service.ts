import { httpClient } from './http-client';
import { withToast } from './toast-utils';
import { ApiResponse } from './user-settings-service';
import { AgentEmbed, CreateEmbedRequest, UpdateEmbedRequest } from '@/types/embed';

/** Agent嵌入配置API服务 */
export class AgentEmbedService {
  /** 创建嵌入配置 */
  static async createEmbed(agentId: string, data: CreateEmbedRequest): Promise<ApiResponse<AgentEmbed>> {
    return httpClient.post(`/agents/${agentId}/embeds`, data);
  }

  /** 获取Agent的所有嵌入配置 */
  static async getEmbeds(agentId: string): Promise<ApiResponse<AgentEmbed[]>> {
    return httpClient.get(`/agents/${agentId}/embeds`);
  }

  /** 获取嵌入配置详情 */
  static async getEmbedDetail(agentId: string, embedId: string): Promise<ApiResponse<AgentEmbed>> {
    return httpClient.get(`/agents/${agentId}/embeds/${embedId}`);
  }

  /** 更新嵌入配置 */
  static async updateEmbed(agentId: string, embedId: string, data: UpdateEmbedRequest): Promise<ApiResponse<AgentEmbed>> {
    return httpClient.put(`/agents/${agentId}/embeds/${embedId}`, data);
  }

  /** 切换嵌入配置启用状态 */
  static async toggleEmbedStatus(agentId: string, embedId: string): Promise<ApiResponse<AgentEmbed>> {
    return httpClient.post(`/agents/${agentId}/embeds/${embedId}/status`);
  }

  /** 删除嵌入配置 */
  static async deleteEmbed(agentId: string, embedId: string): Promise<ApiResponse<void>> {
    return httpClient.delete(`/agents/${agentId}/embeds/${embedId}`);
  }

  /** 获取用户的所有嵌入配置 */
  static async getUserEmbeds(): Promise<ApiResponse<AgentEmbed[]>> {
    return httpClient.get('/user/embeds');
  }
}

// 带Toast的API方法
export const createEmbedWithToast = withToast(AgentEmbedService.createEmbed, {
  showSuccessToast: true,
  showErrorToast: true,
  successTitle: "创建嵌入配置成功",
  errorTitle: "创建嵌入配置失败"
});

export const getEmbedsWithToast = withToast(AgentEmbedService.getEmbeds, {
  showErrorToast: true,
  errorTitle: "获取嵌入配置失败"
});

export const updateEmbedWithToast = withToast(AgentEmbedService.updateEmbed, {
  showSuccessToast: true,
  showErrorToast: true,
  successTitle: "更新嵌入配置成功",
  errorTitle: "更新嵌入配置失败"
});

export const toggleEmbedStatusWithToast = withToast(AgentEmbedService.toggleEmbedStatus, {
  showSuccessToast: true,
  showErrorToast: true,
  successTitle: "状态切换成功",
  errorTitle: "状态切换失败"
});

export const deleteEmbedWithToast = withToast(AgentEmbedService.deleteEmbed, {
  showSuccessToast: true,
  showErrorToast: true,
  successTitle: "删除嵌入配置成功",
  errorTitle: "删除嵌入配置失败"
});

export const getUserEmbedsWithToast = withToast(AgentEmbedService.getUserEmbeds, {
  showErrorToast: true,
  errorTitle: "获取嵌入配置失败"
});