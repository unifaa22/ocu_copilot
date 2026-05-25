package com.example.diagnoseillusion.service.dify;

import com.example.diagnoseillusion.common.CustomException;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Component
public class StubDifyClient implements DifyClient {

    @Override
    public String createDataset(String name) {
        return "ds-" + UUID.randomUUID();
    }

    @Override
    public String uploadDocument(String datasetId, String fileName, InputStream content, long size, String contentType) {
        if (fileName != null && fileName.contains("损坏")) {
            throw new CustomException(500, "Dify 解析失败");
        }
        return "doc-" + UUID.randomUUID();
    }

    @Override
    public void deleteDocument(String datasetId, String documentId) {
        // stub no-op
    }

    @Override
    public void deleteDataset(String datasetId) {
        // stub no-op
    }

    @Override
    public DifyChatResult chat(String question, String conversationId, List<String> datasetIds) {
        String convId = conversationId != null && !conversationId.isBlank()
                ? conversationId
                : "conv-" + System.currentTimeMillis();
        String answer = "这是基于 Dify 知识库（Mock）生成的回答：关于「" + question + "」的智能答复。";
        return new DifyChatResult(convId, answer);
    }
}
