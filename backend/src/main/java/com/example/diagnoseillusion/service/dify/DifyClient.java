package com.example.diagnoseillusion.service.dify;

import java.io.InputStream;

public interface DifyClient {

    String createDataset(String name);

    String uploadDocument(String datasetId, String fileName, InputStream content, long size, String contentType);

    void deleteDocument(String datasetId, String documentId);

    void deleteDataset(String datasetId);

    DifyChatResult chat(String question, String conversationId, java.util.List<String> datasetIds);
}
