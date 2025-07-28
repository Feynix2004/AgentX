// 商品相关类型定义

import { BillingType, ProductStatus } from './billing';

// 商品接口
export interface Product {
  id: string;
  name: string;
  type: string; // BillingType枚举值的字符串形式
  serviceId: string;
  ruleId: string;
  pricingConfig: Record<string, any>;
  status: ProductStatus;
  createdAt: string;
  updatedAt: string;
}

// 创建商品请求
export interface CreateProductRequest {
  name: string;
  type: string;
  serviceId: string;
  ruleId: string;
  pricingConfig: Record<string, any>;
  status?: ProductStatus;
}

// 更新商品请求
export interface UpdateProductRequest {
  name?: string;
  type?: string;
  serviceId?: string;
  ruleId?: string;
  pricingConfig?: Record<string, any>;
  status?: ProductStatus;
}

// 查询商品请求
export interface QueryProductRequest {
  keyword?: string;
  type?: string;
  status?: ProductStatus;
  page?: number;
  pageSize?: number;
}

// 价格配置接口（针对不同的计费策略）
export interface ModelTokenPricingConfig {
  input_cost_per_million: number;
  output_cost_per_million: number;
}

export interface PerUnitPricingConfig {
  cost_per_unit: number;
}

export interface TieredPricingConfig {
  tiers: Array<{
    min_quantity: number;
    max_quantity?: number;
    unit_price: number;
  }>;
}

// 商品表单数据接口
export interface ProductFormData {
  name: string;
  type: BillingType;
  serviceId: string;
  ruleId: string;
  pricingConfig: Record<string, any>;
  status: ProductStatus;
}