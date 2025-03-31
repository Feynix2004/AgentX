package org.feynix.infrastructure.llm;


import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import org.springframework.stereotype.Service;
import org.feynix.infrastructure.llm.config.ProviderConfig;
import org.feynix.infrastructure.llm.factory.LLMProviderFactory;
import org.feynix.infrastructure.llm.protocol.enums.ProviderProtocol;

@Service
public class LLMProviderService {


    public ChatLanguageModel getNormal(ProviderProtocol protocol, ProviderConfig providerConfig){
        return LLMProviderFactory.getLLMProvider(protocol, providerConfig);
    }


    public StreamingChatLanguageModel getStream(ProviderProtocol protocol, ProviderConfig providerConfig){
        return LLMProviderFactory.getLLMProviderByStream(protocol, providerConfig);
    }
}
