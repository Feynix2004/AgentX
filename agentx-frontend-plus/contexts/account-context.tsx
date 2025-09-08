"use client";

import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from "react";
import { Account } from "@/types/account";
import { AccountService } from "@/lib/account-service";

interface AccountContextType {
  // 账户数据
  account: Account | null;
  loading: boolean;
  error: string | null;
  
  // 操作方法
  refreshAccount: () => Promise<void>;
  updateAccountData: (accountData: Account) => void;
  clearAccount: () => void;
  
  // 余额相关便捷方法
  isLowBalance: boolean;
  formatAmount: (amount: number) => string;
}

const AccountContext = createContext<AccountContextType | undefined>(undefined);

export function AccountProvider({ children }: { children: ReactNode }) {
  const [account, setAccount] = useState<Account | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 刷新账户数据
  const refreshAccount = useCallback(async () => {
 
    setLoading(true);
    setError(null);
    
    try {
      const response = await AccountService.getCurrentUserAccount();
      
      if (response.code === 200) {
 
        setAccount(response.data);
      } else {
 
        setError(response.message);
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : '网络错误，请稍后重试';
 
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  }, []);

  // 直接更新账户数据（用于支付成功后的即时更新）
  const updateAccountData = useCallback((accountData: Account) => {
 
    setAccount(accountData);
    setError(null);
  }, []);

  // 清除账户数据
  const clearAccount = useCallback(() => {
 
    setAccount(null);
    setError(null);
    setLoading(false);
  }, []);

  // 判断余额是否不足
  const isLowBalance = account ? account.balance < 10 : false;

  // 格式化金额
  const formatAmount = useCallback((amount: number) => {
    return `¥${amount.toFixed(2)}`;
  }, []);

  // 检查是否在SSO登录过程中
  const isSsoLogin = useCallback(() => {
    if (typeof window === "undefined") return false;
    const currentPath = window.location.pathname;
    return currentPath.includes('/sso/') || currentPath.includes('/login');
  }, []);

  // 智能初始化时机控制
  useEffect(() => {
    // 如果正在SSO登录过程中，延迟初始化避免竞态条件
    if (isSsoLogin()) {
      console.log('[AccountContext] 检测到SSO登录，延迟初始化账户数据');
      
      // 延迟2秒后再初始化，给SSO登录过程足够时间
      const timer = setTimeout(() => {
        if (typeof window !== "undefined") {
          const token = localStorage.getItem("auth_token");
          if (token) {
            console.log('[AccountContext] SSO登录完成，开始获取账户数据');
            refreshAccount();
          }
        }
      }, 2000);
      
      return () => clearTimeout(timer);
    } else {
      // 非SSO登录情况，立即初始化
      refreshAccount();
    }
  }, [refreshAccount, isSsoLogin]);

  const value: AccountContextType = {
    account,
    loading,
    error,
    refreshAccount,
    updateAccountData,
    clearAccount,
    isLowBalance,
    formatAmount,
  };

  return (
    <AccountContext.Provider value={value}>
      {children}
    </AccountContext.Provider>
  );
}

export function useAccount() {
  const context = useContext(AccountContext);
  if (context === undefined) {
    throw new Error("useAccount must be used within an AccountProvider");
  }
  return context;
}

// 便捷的 Hook：只获取余额信息
export function useBalance() {
  const { account, loading, formatAmount } = useAccount();
  
  return {
    balance: account?.balance || 0,
    credit: account?.credit || 0,
    availableBalance: account?.availableBalance || 0,
    loading,
    formatAmount,
  };
}

// 便捷的 Hook：只获取刷新方法
export function useAccountRefresh() {
  const { refreshAccount, loading } = useAccount();
  
  return {
    refreshAccount,
    loading,
  };
}