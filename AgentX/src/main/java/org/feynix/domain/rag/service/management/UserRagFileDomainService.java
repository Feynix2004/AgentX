package org.feynix.domain.rag.service.management;

import org.springframework.stereotype.Service;
import org.feynix.domain.rag.model.UserRagFileEntity;
import org.feynix.domain.rag.repository.UserRagFileRepository;

@Service
public class UserRagFileDomainService {

    private final UserRagFileRepository userRagFileRepository;

    public UserRagFileDomainService(UserRagFileRepository userRagFileRepository) {
        this.userRagFileRepository = userRagFileRepository;
    }

    public UserRagFileEntity getById(String id) {
        return userRagFileRepository.selectById(id);
    }
}
