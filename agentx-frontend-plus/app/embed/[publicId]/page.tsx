"use client";

import React, { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Skeleton } from "@/components/ui/skeleton";
import { AlertCircle, MessageCircle } from "lucide-react";
import { EmbedChatInterface } from './components/EmbedChatInterface';

interface EmbedInfo {
  embedName: string;
  embedDescription?: string;
  agentName: string;
  agentAvatar?: string;
  welcomeMessage?: string;
  enabled: boolean;
  dailyLimit: number;
  dailyCalls: number;
}

export default function EmbedChatPage() {
  const params = useParams();
  const publicId = params.publicId as string;
  
  const [embedInfo, setEmbedInfo] = useState<EmbedInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 获取嵌入信息
  useEffect(() => {
    const fetchEmbedInfo = async () => {
      try {
        setLoading(true);
        setError(null);

        const response = await fetch(`/api/embed/${publicId}/info`, {
          headers: {
            'Referer': window.location.origin
          }
        });

        if (!response.ok) {
          throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        const data = await response.json();
        
        if (data.code !== 200) {
          throw new Error(data.message || '获取嵌入信息失败');
        }

        setEmbedInfo(data.data);
      } catch (error) {
        console.error('获取嵌入信息失败:', error);
        setError(error instanceof Error ? error.message : '获取嵌入信息失败');
      } finally {
        setLoading(false);
      }
    };

    if (publicId) {
      fetchEmbedInfo();
    }
  }, [publicId]);

  // 加载状态
  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 p-4">
        <div className="max-w-4xl mx-auto">
          <Card>
            <CardHeader>
              <div className="flex items-center space-x-3">
                <Skeleton className="h-12 w-12 rounded-full" />
                <div>
                  <Skeleton className="h-6 w-32 mb-2" />
                  <Skeleton className="h-4 w-48" />
                </div>
              </div>
            </CardHeader>
            <CardContent>
              <Skeleton className="h-[500px] w-full" />
            </CardContent>
          </Card>
        </div>
      </div>
    );
  }

  // 错误状态
  if (error || !embedInfo) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
        <Card className="max-w-md w-full">
          <CardContent className="pt-6">
            <div className="flex items-center space-x-3 mb-4">
              <AlertCircle className="h-8 w-8 text-destructive" />
              <div>
                <h3 className="text-lg font-semibold">无法访问</h3>
                <p className="text-sm text-muted-foreground">
                  {error || '嵌入配置不存在或已被禁用'}
                </p>
              </div>
            </div>
            <Alert>
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>
                请检查链接是否正确，或联系网站管理员。
              </AlertDescription>
            </Alert>
          </CardContent>
        </Card>
      </div>
    );
  }

  // 检查配置是否启用
  if (!embedInfo.enabled) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
        <Card className="max-w-md w-full">
          <CardContent className="pt-6">
            <div className="flex items-center space-x-3 mb-4">
              <AlertCircle className="h-8 w-8 text-amber-500" />
              <div>
                <h3 className="text-lg font-semibold">暂时不可用</h3>
                <p className="text-sm text-muted-foreground">
                  此聊天服务暂时不可用
                </p>
              </div>
            </div>
            <Alert>
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>
                服务已被暂时禁用，请稍后再试或联系网站管理员。
              </AlertDescription>
            </Alert>
          </CardContent>
        </Card>
      </div>
    );
  }

  // 检查每日调用限制
  const hasReachedLimit = embedInfo.dailyLimit !== -1 && embedInfo.dailyCalls >= embedInfo.dailyLimit;

  if (hasReachedLimit) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
        <Card className="max-w-md w-full">
          <CardContent className="pt-6">
            <div className="flex items-center space-x-3 mb-4">
              <AlertCircle className="h-8 w-8 text-amber-500" />
              <div>
                <h3 className="text-lg font-semibold">今日调用已达上限</h3>
                <p className="text-sm text-muted-foreground">
                  今日调用次数已达到 {embedInfo.dailyLimit} 次上限
                </p>
              </div>
            </div>
            <Alert>
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>
                请明天再来，或联系网站管理员增加调用额度。
              </AlertDescription>
            </Alert>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 p-4">
      <div className="max-w-4xl mx-auto">
        <Card>
          <CardHeader>
            <div className="flex items-center space-x-3">
              {embedInfo.agentAvatar ? (
                <img 
                  src={embedInfo.agentAvatar} 
                  alt={embedInfo.agentName}
                  className="h-12 w-12 rounded-full object-cover"
                />
              ) : (
                <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center">
                  <MessageCircle className="h-6 w-6 text-primary" />
                </div>
              )}
              <div>
                <CardTitle className="text-xl">{embedInfo.embedName}</CardTitle>
                {embedInfo.embedDescription && (
                  <p className="text-sm text-muted-foreground mt-1">
                    {embedInfo.embedDescription}
                  </p>
                )}
              </div>
            </div>
            
            {/* 调用次数显示 */}
            {embedInfo.dailyLimit !== -1 && (
              <div className="mt-4 text-xs text-muted-foreground">
                今日调用: {embedInfo.dailyCalls} / {embedInfo.dailyLimit}
              </div>
            )}
          </CardHeader>
          
          <CardContent>
            <EmbedChatInterface 
              publicId={publicId}
              agentName={embedInfo.agentName}
              agentAvatar={embedInfo.agentAvatar}
              welcomeMessage={embedInfo.welcomeMessage}
            />
          </CardContent>
        </Card>
      </div>
    </div>
  );
}