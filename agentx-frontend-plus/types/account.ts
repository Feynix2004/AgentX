// 账户相关类型定义

// 账户接口
export interface Account {
  id: string;
  userId: string;
  balance: number;
  credit: number;
  totalConsumed: number;
  lastTransactionAt?: string;
  createdAt: string;
  updatedAt: string;
  availableBalance: number; // 可用余额（余额+信用额度）
}

// 充值请求
export interface RechargeRequest {
  amount: number;
  paymentMethod?: string;
}

// 添加信用额度请求
export interface AddCreditRequest {
  amount: number;
  reason?: string;
}

// 账户统计接口
export interface AccountStats {
  totalUsers: number;
  totalBalance: number;
  totalCredit: number;
  totalConsumed: number;
  averageBalance: number;
}

// 账户余额变动记录
export interface BalanceTransaction {
  id: string;
  userId: string;
  type: 'recharge' | 'consume' | 'credit' | 'refund';
  amount: number;
  balance: number;
  description: string;
  createdAt: string;
}