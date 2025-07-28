// 用量记录相关类型定义

// 用量记录接口
export interface UsageRecord {
  id: string;
  userId: string;
  productId: string;
  quantityData: Record<string, any>;
  cost: number;
  requestId: string;
  billedAt: string;
  createdAt: string;
  updatedAt: string;
}

// 查询用量记录请求
export interface QueryUsageRecordRequest {
  userId?: string;
  productId?: string;
  startDate?: string;
  endDate?: string;
  minCost?: number;
  maxCost?: number;
  page?: number;
  pageSize?: number;
}

// 用量记录统计接口
export interface UsageStats {
  totalRecords: number;
  totalCost: number;
  averageCost: number;
  periodCost: number; // 指定时间段内的费用
}

// 用量记录详情（包含关联的商品信息）
export interface UsageRecordDetail extends UsageRecord {
  productName?: string;
  productType?: string;
  serviceName?: string;
}

// 用量数据类型（针对不同计费类型）
export interface ModelUsageData {
  input: number;
  output: number;
  model?: string;
}

export interface AgentUsageData {
  calls: number;
  agentId?: string;
}

export interface ApiCallUsageData {
  calls: number;
  endpoint?: string;
}

export interface StorageUsageData {
  bytes: number;
  files?: number;
}

// 用量图表数据
export interface UsageChartData {
  date: string;
  cost: number;
  records: number;
}