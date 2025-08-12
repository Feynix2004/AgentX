import { Model } from '@/lib/user-settings-service';
import { Provider } from './provider';

/** Agent小组件配置 */
export interface AgentWidget {
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

/** 创建小组件配置请求 */
export interface CreateWidgetRequest {
  embedName: string;
  embedDescription?: string;
  modelId: string;
  providerId?: string;
  allowedDomains: string[];
  dailyLimit: number;
}

/** 更新小组件配置请求 */
export interface UpdateWidgetRequest {
  embedName: string;
  embedDescription?: string;
  modelId: string;
  providerId?: string;
  allowedDomains: string[];
  dailyLimit: number;
  enabled: boolean;
}

/** 小组件聊天请求 */
export interface WidgetChatRequest {
  message: string;
  sessionId: string;
  fileUrls: string[];
}