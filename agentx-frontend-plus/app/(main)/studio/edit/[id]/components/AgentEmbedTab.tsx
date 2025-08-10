"use client";

import React, { useState, useEffect } from 'react';
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Label } from "@/components/ui/label";
import { MoreHorizontal, Plus, ExternalLink, Code, Copy, Eye, Settings, Trash } from "lucide-react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { toast } from "@/hooks/use-toast";
import { AgentEmbed } from "@/types/embed";
import { Model } from "@/lib/user-settings-service";
import { getEmbedsWithToast, toggleEmbedStatusWithToast, deleteEmbedWithToast } from "@/lib/agent-embed-service";
import { getAllModelsWithToast } from "@/lib/user-settings-service";
import { CreateEmbedDialog } from "./CreateEmbedDialog";
import { UpdateEmbedDialog } from "./UpdateEmbedDialog";
import { EmbedCodeDialog } from "./EmbedCodeDialog";

interface AgentEmbedTabProps {
  agentId: string;
}

export function AgentEmbedTab({ agentId }: AgentEmbedTabProps) {
  const [embeds, setEmbeds] = useState<AgentEmbed[]>([]);
  const [models, setModels] = useState<Model[]>([]);
  const [loading, setLoading] = useState(true);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [updateDialogOpen, setUpdateDialogOpen] = useState(false);
  const [embedCodeDialogOpen, setEmbedCodeDialogOpen] = useState(false);
  const [selectedEmbed, setSelectedEmbed] = useState<AgentEmbed | null>(null);

  // 加载嵌入配置列表
  const loadEmbeds = async () => {
    setLoading(true);
    try {
      const response = await getEmbedsWithToast(agentId);
      if (response.code === 200) {
        setEmbeds(response.data || []);
      }
    } catch (error) {
      console.error('Failed to load embeds:', error);
    } finally {
      setLoading(false);
    }
  };

  // 加载模型列表
  const loadModels = async () => {
    try {
      const response = await getAllModelsWithToast();
      if (response.code === 200) {
        setModels(response.data || []);
      }
    } catch (error) {
      console.error('Failed to load models:', error);
    }
  };

  useEffect(() => {
    loadEmbeds();
    loadModels();
  }, [agentId]);

  // 切换启用状态
  const handleToggleStatus = async (embed: AgentEmbed) => {
    try {
      const response = await toggleEmbedStatusWithToast(agentId, embed.id);
      if (response.code === 200) {
        loadEmbeds(); // 重新加载数据
      }
    } catch (error) {
      console.error('Failed to toggle embed status:', error);
    }
  };

  // 删除嵌入配置
  const handleDelete = async (embed: AgentEmbed) => {
    if (!confirm(`确定要删除嵌入配置 "${embed.embedName}" 吗？此操作不可撤销。`)) {
      return;
    }

    try {
      const response = await deleteEmbedWithToast(agentId, embed.id);
      if (response.code === 200) {
        loadEmbeds(); // 重新加载数据
      }
    } catch (error) {
      console.error('Failed to delete embed:', error);
    }
  };

  // 复制嵌入代码
  const handleCopyEmbedCode = async (embedCode: string) => {
    try {
      await navigator.clipboard.writeText(embedCode);
      toast({
        title: "复制成功",
        description: "嵌入代码已复制到剪贴板",
      });
    } catch (error) {
      toast({
        title: "复制失败",
        description: "无法复制到剪贴板",
        variant: "destructive",
      });
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-8">
        <div className="text-muted-foreground">加载中...</div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* 头部说明 */}
      <div className="flex items-start justify-between">
        <div>
          <h3 className="text-lg font-semibold">网站嵌入</h3>
          <p className="text-sm text-muted-foreground">
            创建嵌入配置，让你的Agent可以嵌入到任何网站中使用
          </p>
        </div>
        <Button onClick={() => setCreateDialogOpen(true)}>
          <Plus className="h-4 w-4 mr-2" />
          创建嵌入配置
        </Button>
      </div>

      {/* 嵌入配置列表 */}
      {embeds.length === 0 ? (
        <Card>
          <CardContent className="py-8">
            <div className="text-center">
              <Code className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
              <h3 className="text-lg font-semibold mb-2">还没有嵌入配置</h3>
              <p className="text-muted-foreground mb-4">
                创建你的第一个嵌入配置，让网站访客可以直接与你的Agent对话
              </p>
              <Button onClick={() => setCreateDialogOpen(true)}>
                <Plus className="h-4 w-4 mr-2" />
                创建嵌入配置
              </Button>
            </div>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4">
          {embeds.map(embed => (
            <EmbedConfigCard 
              key={embed.id}
              embed={embed}
              onToggleStatus={handleToggleStatus}
              onEdit={(embed) => {
                setSelectedEmbed(embed);
                setUpdateDialogOpen(true);
              }}
              onViewCode={(embed) => {
                setSelectedEmbed(embed);
                setEmbedCodeDialogOpen(true);
              }}
              onCopyCode={handleCopyEmbedCode}
              onDelete={handleDelete}
            />
          ))}
        </div>
      )}

      {/* 对话框 */}
      <CreateEmbedDialog
        open={createDialogOpen}
        onClose={() => setCreateDialogOpen(false)}
        agentId={agentId}
        models={models}
        onSuccess={loadEmbeds}
      />

      {selectedEmbed && (
        <>
          <UpdateEmbedDialog
            open={updateDialogOpen}
            onClose={() => {
              setUpdateDialogOpen(false);
              setSelectedEmbed(null);
            }}
            agentId={agentId}
            embed={selectedEmbed}
            models={models}
            onSuccess={loadEmbeds}
          />

          <EmbedCodeDialog
            open={embedCodeDialogOpen}
            onClose={() => {
              setEmbedCodeDialogOpen(false);
              setSelectedEmbed(null);
            }}
            embed={selectedEmbed}
            onCopy={handleCopyEmbedCode}
          />
        </>
      )}
    </div>
  );
}

// 嵌入配置卡片组件
interface EmbedConfigCardProps {
  embed: AgentEmbed;
  onToggleStatus: (embed: AgentEmbed) => void;
  onEdit: (embed: AgentEmbed) => void;
  onViewCode: (embed: AgentEmbed) => void;
  onCopyCode: (embedCode: string) => void;
  onDelete: (embed: AgentEmbed) => void;
}

function EmbedConfigCard({ embed, onToggleStatus, onEdit, onViewCode, onCopyCode, onDelete }: EmbedConfigCardProps) {
  const embedUrl = `${window.location.origin}/embed/${embed.publicId}`;

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div className="flex-1">
            <CardTitle className="text-lg">{embed.embedName}</CardTitle>
            {embed.embedDescription && (
              <CardDescription>{embed.embedDescription}</CardDescription>
            )}
          </div>
          <div className="flex items-center gap-2">
            <Badge variant={embed.enabled ? "default" : "secondary"}>
              {embed.enabled ? "已启用" : "已禁用"}
            </Badge>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="outline" size="sm">
                  <MoreHorizontal className="h-4 w-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem onClick={() => onEdit(embed)}>
                  <Settings className="h-4 w-4 mr-2" />
                  编辑配置
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => onViewCode(embed)}>
                  <Code className="h-4 w-4 mr-2" />
                  查看嵌入代码
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => onCopyCode(embed.embedCode)}>
                  <Copy className="h-4 w-4 mr-2" />
                  复制嵌入代码
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => window.open(embedUrl, '_blank')}>
                  <ExternalLink className="h-4 w-4 mr-2" />
                  预览页面
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={() => onToggleStatus(embed)}>
                  {embed.enabled ? "禁用" : "启用"}
                </DropdownMenuItem>
                <DropdownMenuItem 
                  onClick={() => onDelete(embed)}
                  className="text-destructive"
                >
                  <Trash className="h-4 w-4 mr-2" />
                  删除
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <Label className="text-muted-foreground">使用模型</Label>
            <p>{embed.model?.name}</p>
          </div>
          <div>
            <Label className="text-muted-foreground">每日限制</Label>
            <p>{embed.dailyLimit === -1 ? "无限制" : `${embed.dailyLimit} 次`}</p>
          </div>
          <div className="col-span-2">
            <Label className="text-muted-foreground">访问链接</Label>
            <p className="font-mono text-xs break-all text-blue-600">
              {embedUrl}
            </p>
          </div>
          <div className="col-span-2">
            <Label className="text-muted-foreground">允许域名</Label>
            <p>{embed.allowedDomains.length > 0 ? embed.allowedDomains.join(", ") : "所有域名"}</p>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}