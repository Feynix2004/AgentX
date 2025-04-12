package org.feynix.application.conversation.service.handler;

import dev.langchain4j.service.TokenStream;

public interface Agent {

    TokenStream chat(String prompt);

}
