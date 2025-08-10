"use client";

import React, { useState } from 'react';
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Copy, ExternalLink } from "lucide-react";
import { toast } from "@/hooks/use-toast";
import { AgentEmbed } from "@/types/embed";

interface EmbedCodeDialogProps {
  open: boolean;
  onClose: () => void;
  embed: AgentEmbed;
  onCopy: (code: string) => void;
}

export function EmbedCodeDialog({ open, onClose, embed, onCopy }: EmbedCodeDialogProps) {
  const [activeTab, setActiveTab] = useState("iframe");
  
  const embedUrl = `${window.location.origin}/embed/${embed.publicId}`;
  
  // 简单的iframe嵌入代码
  const iframeCode = `<iframe 
  src="${embedUrl}"
  width="400" 
  height="600"
  frameborder="0"
  style="border: 1px solid #e2e8f0; border-radius: 8px;"
  allow="microphone">
</iframe>`;

  // 悬浮窗嵌入代码
  const floatingCode = `<script>
  (function() {
    const agentButton = document.createElement('div');
    agentButton.innerHTML = '💬 ${embed.embedName}';
    agentButton.style.cssText = 'position:fixed;bottom:20px;right:20px;z-index:9999;background:#007bff;color:white;padding:12px 20px;border-radius:25px;cursor:pointer;box-shadow:0 4px 12px rgba(0,0,0,0.15);font-family:sans-serif;';
    
    agentButton.onclick = function() {
      const iframe = document.createElement('iframe');
      iframe.src = '${embedUrl}';
      iframe.style.cssText = 'position:fixed;bottom:80px;right:20px;width:400px;height:600px;border:none;border-radius:8px;z-index:10000;box-shadow:0 8px 32px rgba(0,0,0,0.1);';
      
      const closeBtn = document.createElement('div');
      closeBtn.innerHTML = '×';
      closeBtn.style.cssText = 'position:fixed;bottom:685px;right:25px;width:20px;height:20px;background:#ff4757;color:white;border-radius:50%;text-align:center;line-height:20px;cursor:pointer;z-index:10001;font-family:sans-serif;';
      closeBtn.onclick = function() {
        document.body.removeChild(iframe);
        document.body.removeChild(closeBtn);
        agentButton.style.display = 'block';
      };
      
      document.body.appendChild(iframe);
      document.body.appendChild(closeBtn);
      agentButton.style.display = 'none';
    };
    
    document.body.appendChild(agentButton);
  })();
</script>`;

  // 响应式嵌入代码
  const responsiveCode = `<div id="agentx-chat-container" style="width: 100%; height: 600px; min-height: 400px;">
  <iframe 
    src="${embedUrl}"
    width="100%" 
    height="100%"
    frameborder="0"
    style="border: 1px solid #e2e8f0; border-radius: 8px;"
    allow="microphone">
  </iframe>
</div>

<style>
  @media (max-width: 768px) {
    #agentx-chat-container {
      height: 500px;
    }
  }
  
  @media (max-width: 480px) {
    #agentx-chat-container {
      height: 400px;
    }
  }
</style>`;

  const handleCopy = (code: string) => {
    onCopy(code);
  };

  const handlePreview = () => {
    window.open(embedUrl, '_blank');
  };

  const getCurrentCode = () => {
    switch (activeTab) {
      case "iframe":
        return iframeCode;
      case "floating":
        return floatingCode;
      case "responsive":
        return responsiveCode;
      default:
        return iframeCode;
    }
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-4xl max-h-[80vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>嵌入代码 - {embed.embedName}</DialogTitle>
          <DialogDescription>
            选择适合的嵌入方式，复制代码到你的网站中
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4">
          {/* 预览链接 */}
          <div className="flex items-center gap-2 p-3 bg-muted rounded-lg">
            <div className="flex-1">
              <Label className="text-sm font-medium">预览链接</Label>
              <p className="text-sm font-mono text-blue-600 break-all">{embedUrl}</p>
            </div>
            <Button variant="outline" size="sm" onClick={handlePreview}>
              <ExternalLink className="h-4 w-4 mr-1" />
              预览
            </Button>
          </div>

          {/* 嵌入代码选项卡 */}
          <Tabs value={activeTab} onValueChange={setActiveTab}>
            <TabsList className="grid w-full grid-cols-3">
              <TabsTrigger value="iframe">固定尺寸</TabsTrigger>
              <TabsTrigger value="floating">悬浮窗口</TabsTrigger>
              <TabsTrigger value="responsive">响应式</TabsTrigger>
            </TabsList>

            <TabsContent value="iframe" className="space-y-3">
              <div>
                <Label>固定尺寸 iframe</Label>
                <p className="text-sm text-muted-foreground mb-2">
                  简单的固定尺寸嵌入，适合在页面特定位置展示聊天界面
                </p>
                <div className="relative">
                  <Textarea
                    value={iframeCode}
                    readOnly
                    className="font-mono text-xs"
                    rows={8}
                  />
                  <Button
                    size="sm"
                    className="absolute top-2 right-2"
                    onClick={() => handleCopy(iframeCode)}
                  >
                    <Copy className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="floating" className="space-y-3">
              <div>
                <Label>悬浮窗口</Label>
                <p className="text-sm text-muted-foreground mb-2">
                  在页面右下角显示一个悬浮按钮，点击后弹出聊天窗口
                </p>
                <div className="relative">
                  <Textarea
                    value={floatingCode}
                    readOnly
                    className="font-mono text-xs"
                    rows={12}
                  />
                  <Button
                    size="sm"
                    className="absolute top-2 right-2"
                    onClick={() => handleCopy(floatingCode)}
                  >
                    <Copy className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="responsive" className="space-y-3">
              <div>
                <Label>响应式布局</Label>
                <p className="text-sm text-muted-foreground mb-2">
                  自适应不同屏幕尺寸，在移动设备上也能良好显示
                </p>
                <div className="relative">
                  <Textarea
                    value={responsiveCode}
                    readOnly
                    className="font-mono text-xs"
                    rows={18}
                  />
                  <Button
                    size="sm"
                    className="absolute top-2 right-2"
                    onClick={() => handleCopy(responsiveCode)}
                  >
                    <Copy className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            </TabsContent>
          </Tabs>

          {/* 使用说明 */}
          <div className="bg-blue-50 p-4 rounded-lg">
            <h4 className="font-medium text-blue-900 mb-2">使用说明</h4>
            <ul className="text-sm text-blue-800 space-y-1">
              <li>• 将代码复制并粘贴到你的网站 HTML 中</li>
              <li>• 确保网站域名在允许列表中（如果有设置域名限制）</li>
              <li>• 聊天记录会自动保存在用户浏览器中</li>
              <li>• 支持文本对话，具体功能取决于你的 Agent 配置</li>
              {embed.dailyLimit > 0 && (
                <li>• 当前每日调用限制：{embed.dailyLimit} 次</li>
              )}
            </ul>
          </div>

          {/* 底部操作按钮 */}
          <div className="flex justify-between pt-4">
            <Button variant="outline" onClick={onClose}>
              关闭
            </Button>
            <div className="flex gap-2">
              <Button variant="outline" onClick={handlePreview}>
                <ExternalLink className="h-4 w-4 mr-2" />
                预览页面
              </Button>
              <Button onClick={() => handleCopy(getCurrentCode())}>
                <Copy className="h-4 w-4 mr-2" />
                复制代码
              </Button>
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}