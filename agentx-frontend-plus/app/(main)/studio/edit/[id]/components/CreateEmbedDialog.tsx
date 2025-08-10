"use client";

import React, { useState } from 'react';
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Model } from "@/lib/user-settings-service";
import { CreateEmbedRequest } from "@/types/embed";
import { createEmbedWithToast } from "@/lib/agent-embed-service";

interface CreateEmbedDialogProps {
  open: boolean;
  onClose: () => void;
  agentId: string;
  models: Model[];
  onSuccess: () => void;
}

export function CreateEmbedDialog({ open, onClose, agentId, models, onSuccess }: CreateEmbedDialogProps) {
  const [formData, setFormData] = useState<CreateEmbedRequest>({
    embedName: '',
    embedDescription: '',
    modelId: '',
    providerId: undefined,
    allowedDomains: [],
    dailyLimit: -1,
  });
  const [domainsText, setDomainsText] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async () => {
    if (!formData.embedName.trim()) {
      return;
    }
    if (!formData.modelId) {
      return;
    }

    setSubmitting(true);
    try {
      // 处理域名列表
      const domains = domainsText
        .split('\n')
        .map(d => d.trim())
        .filter(d => d.length > 0);

      const requestData: CreateEmbedRequest = {
        ...formData,
        allowedDomains: domains,
      };

      const response = await createEmbedWithToast(agentId, requestData);
      
      if (response.code === 200) {
        onSuccess();
        handleClose();
      }
    } catch (error) {
      console.error('Failed to create embed:', error);
    } finally {
      setSubmitting(false);
    }
  };

  const handleClose = () => {
    setFormData({
      embedName: '',
      embedDescription: '',
      modelId: '',
      providerId: undefined,
      allowedDomains: [],
      dailyLimit: -1,
    });
    setDomainsText('');
    onClose();
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>创建嵌入配置</DialogTitle>
          <DialogDescription>
            为你的Agent创建一个嵌入配置，让它可以嵌入到任何网站中使用
          </DialogDescription>
        </DialogHeader>

        <div className="grid gap-4 py-4">
          <div className="grid gap-2">
            <Label htmlFor="embedName">配置名称 *</Label>
            <Input
              id="embedName"
              placeholder="例如：官网客服助手"
              value={formData.embedName}
              onChange={(e) => setFormData({ ...formData, embedName: e.target.value })}
            />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="embedDescription">配置描述</Label>
            <Textarea
              id="embedDescription"
              placeholder="简单描述这个嵌入配置的用途"
              value={formData.embedDescription}
              onChange={(e) => setFormData({ ...formData, embedDescription: e.target.value })}
            />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="model">选择模型 *</Label>
            <Select
              value={formData.modelId}
              onValueChange={(value) => setFormData({ ...formData, modelId: value })}
            >
              <SelectTrigger>
                <SelectValue placeholder="选择要使用的模型" />
              </SelectTrigger>
              <SelectContent>
                {models.map(model => (
                  <SelectItem key={model.id} value={model.id}>
                    {model.name} - {model.providerName}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="grid gap-2">
            <Label htmlFor="dailyLimit">每日调用限制</Label>
            <Select
              value={formData.dailyLimit.toString()}
              onValueChange={(value) => setFormData({ ...formData, dailyLimit: parseInt(value) })}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="-1">无限制</SelectItem>
                <SelectItem value="100">100次/天</SelectItem>
                <SelectItem value="500">500次/天</SelectItem>
                <SelectItem value="1000">1000次/天</SelectItem>
                <SelectItem value="5000">5000次/天</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="grid gap-2">
            <Label htmlFor="allowedDomains">允许的域名（可选）</Label>
            <Textarea
              id="allowedDomains"
              placeholder="每行一个域名，例如：&#10;example.com&#10;www.example.com&#10;留空表示允许所有域名"
              value={domainsText}
              onChange={(e) => setDomainsText(e.target.value)}
              rows={4}
            />
            <p className="text-xs text-muted-foreground">
              设置后，只有这些域名的网站可以嵌入使用。支持通配符，如 *.example.com
            </p>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={handleClose}>
            取消
          </Button>
          <Button 
            onClick={handleSubmit}
            disabled={!formData.embedName.trim() || !formData.modelId || submitting}
          >
            {submitting ? "创建中..." : "创建配置"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}