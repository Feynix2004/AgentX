import { Model } from './model';
import { Provider } from './provider';

/** Agent嵌入配置 */
export interface AgentEmbed {
  id: string;
  agentId: string;
  userId: string;
  publicId: string;
  embedName: string;
  embedDescription?: string;
  model: Model;
  provider?: Provider;
  allowedDomains: string[];
  dailyLimit: number;
  enabled: boolean;
  embedCode: string;
  createdAt: string;
  updatedAt: string;
}

/** 创建嵌入配置请求 */
export interface CreateEmbedRequest {
  embedName: string;
  embedDescription?: string;
  modelId: string;
  providerId?: string;
  allowedDomains: string[];
  dailyLimit: number;
}

/** 更新嵌入配置请求 */
export interface UpdateEmbedRequest {
  embedName: string;
  embedDescription?: string;
  modelId: string;
  providerId?: string;
  allowedDomains: string[];
  dailyLimit: number;
  enabled: boolean;
}

/** 嵌入聊天请求 */
export interface EmbedChatRequest {
  message: string;
  sessionId: string;
  fileUrls: string[];
}