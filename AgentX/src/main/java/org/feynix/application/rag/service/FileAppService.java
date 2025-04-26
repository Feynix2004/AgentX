package org.feynix.application.rag.service;


import org.springframework.stereotype.Service;
import org.feynix.application.rag.assembler.FileAssembler;
import org.feynix.domain.rag.model.FileDetailEntity;
import org.feynix.domain.rag.service.FileDetailDomainService;
import org.feynix.interfaces.dto.rag.RagUploadRequest;

/**
 * @author shilong.zang
 * @date 15:43 <br/>
 */

@Service
public class FileAppService {

    private final FileDetailDomainService fileDetailService;

    public FileAppService(FileDetailDomainService fileDetailService) {
        this.fileDetailService = fileDetailService;
    }


    public void upload(RagUploadRequest request) {

        final FileDetailEntity fileDetailEntity = FileAssembler.toEntity(request);

        fileDetailService.upload(fileDetailEntity);
    }
}
