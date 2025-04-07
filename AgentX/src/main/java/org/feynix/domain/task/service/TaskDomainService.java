package org.feynix.domain.task.service;

import org.springframework.stereotype.Service;
import org.feynix.domain.task.constant.TaskStatus;
import org.feynix.domain.task.model.TaskEntity;
import org.feynix.domain.task.repository.TaskRepository;
import org.feynix.infrastructure.exception.BusinessException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务领域服务
 */
@Service
public class TaskDomainService {
    
    private final TaskRepository taskRepository;
    
    public TaskDomainService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }


    public TaskEntity addTask(TaskEntity taskEntity) {
        taskEntity.setStartTime(LocalDateTime.now());
        taskRepository.checkInsert(taskEntity);
        return taskEntity;
    }
    
    /**
     * 更新任务
     *
     * @param taskEntity 任务实体
     * @return 更新后的任务实体
     */
    public TaskEntity updateTask(TaskEntity taskEntity) {
        taskRepository.checkedUpdateById(taskEntity);
        return taskEntity;
    }
}