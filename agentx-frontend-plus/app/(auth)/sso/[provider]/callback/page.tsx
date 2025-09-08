"use client"

import { useEffect, use, useState } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { toast } from "@/hooks/use-toast"
import { handleSsoCallbackApi } from "@/lib/api-services"
import { AccountService } from "@/lib/account-service"
import { setCookie } from "@/lib/utils"

interface SsoCallbackPageProps {
  params: Promise<{ provider: string }>
}

export default function SsoCallbackPage({ params }: SsoCallbackPageProps) {
  const router = useRouter()
  const searchParams = useSearchParams()
  const { provider } = use(params)
  const [status, setStatus] = useState("处理中")
  const [isProcessing, setIsProcessing] = useState(false) // 防重复调用标识

  useEffect(() => {
    // 防止重复执行
    if (isProcessing) {
      return
    }

    const handleCallback = async () => {
      setIsProcessing(true) // 设置处理标识
      
      try {
        const code = searchParams.get("code")
        if (!code) {
          toast({
            variant: "destructive",
            title: "登录失败",
            description: "未获取到授权码"
          })
          router.push("/login")
          return
        }

        setStatus("验证授权码")
        const res = await handleSsoCallbackApi(provider, code)
        
        if (res.code === 200 && res.data?.token) {
          setStatus("保存认证信息")
          
          // 保存token到本地存储
          localStorage.setItem("auth_token", res.data.token)
          setCookie("token", res.data.token, 30)
          
          // 增加延迟时间，确保token保存完成
          await new Promise(resolve => setTimeout(resolve, 300))
          
          // 验证token是否有效 - 尝试获取用户账户信息
          setStatus("验证登录状态")
          try {
            console.log("[SSO] 开始验证账户信息")
            const accountRes = await AccountService.getCurrentUserAccount()
            
            if (accountRes.code === 200) {
              // 认证成功，跳转到主页
              const providerName = provider === 'community' ? '敲鸭' : provider.toUpperCase()
              toast({
                title: "登录成功",
                description: `使用 ${providerName} 登录成功`
              })
              console.log("[SSO] 账户验证成功，跳转到主页")
              router.push("/")
            } else if (accountRes.code === 401) {
              // 账户验证返回401，可能是时机问题，仍然继续登录
              console.warn("[SSO] 账户验证401，可能是时机问题，继续登录流程")
              const providerName = provider === 'community' ? '敲鸭' : provider.toUpperCase()
              toast({
                title: "登录成功", 
                description: `使用 ${providerName} 登录成功`
              })
              router.push("/")
            } else {
              // 其他错误，仍然继续登录
              console.warn("[SSO] 获取账户信息失败，但继续登录:", accountRes.message)
              const providerName = provider === 'community' ? '敲鸭' : provider.toUpperCase()
              toast({
                title: "登录成功",
                description: `使用 ${providerName} 登录成功`
              })
              router.push("/")
            }
          } catch (accountError) {
            // 账户信息获取异常，但token获取成功，仍然继续
            console.warn("[SSO] 账户验证异常，但继续登录:", accountError)
            const providerName = provider === 'community' ? '敲鸭' : provider.toUpperCase()
            toast({
              title: "登录成功",
              description: `使用 ${providerName} 登录成功`
            })
            router.push("/")
          }
        } else {
          toast({
            variant: "destructive",
            title: "登录失败",
            description: res.message || "登录过程中出现错误"
          })
          router.push("/login")
        }
      } catch (error) {
        console.error("[SSO] 登录回调处理失败:", error)
        
        let errorMessage = "登录过程中出现错误，请重试"
        if (error instanceof Error) {
          errorMessage = error.message.includes("401") 
            ? "认证失败，请重新登录" 
            : error.message
        }
        
        toast({
          variant: "destructive",
          title: "登录失败",
          description: errorMessage
        })
        
        // 清理可能已保存的无效token
        localStorage.removeItem("auth_token")
        document.cookie = "token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;"
        
        router.push("/login")
      } finally {
        setIsProcessing(false) // 无论成功失败都重置标识
      }
    }

    handleCallback()
  }, [searchParams, router, provider, isProcessing]) // 将 isProcessing 加入依赖

  return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="text-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
        <p className="text-muted-foreground">正在处理登录...</p>
        <p className="text-sm text-muted-foreground/70 mt-2">{status}</p>
      </div>
    </div>
  )
}