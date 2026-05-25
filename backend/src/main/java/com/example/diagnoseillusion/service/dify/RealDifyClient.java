package com.example.diagnoseillusion.service.dify;

import com.example.diagnoseillusion.common.CustomException;
import com.example.diagnoseillusion.config.AppProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RealDifyClient implements DifyClient {

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    @Override
    public String createDataset(String name) {
        Map<String, Object> body = Map.of("name", name);
        JsonNode node = postJson("/datasets", body, true);
        return node.path("id").asText();
    }

    @Override
    public String uploadDocument(String datasetId, String fileName, InputStream content, long size, String contentType) {
        try {
            byte[] bytes = content.readAllBytes();
            MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
            form.add("file", new org.springframework.core.io.ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            });
            JsonNode node = restClient.post()
                    .uri(baseUrl() + "/datasets/" + datasetId + "/document/create_by_file")
                    .header("Authorization", "Bearer " + datasetApiKey())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(form)
                    .retrieve()
                    .body(JsonNode.class);
            if (node == null) {
                throw new CustomException(500, "Dify 上传文档失败");
            }
            return node.path("document").path("id").asText();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(500, "Dify 上传文档失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteDocument(String datasetId, String documentId) {
        restClient.method(org.springframework.http.HttpMethod.DELETE)
                .uri(baseUrl() + "/datasets/" + datasetId + "/documents/" + documentId)
                .header("Authorization", "Bearer " + datasetApiKey())
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void deleteDataset(String datasetId) {
        restClient.method(org.springframework.http.HttpMethod.DELETE)
                .uri(baseUrl() + "/datasets/" + datasetId)
                .header("Authorization", "Bearer " + datasetApiKey())
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public DifyChatResult chat(String question, String conversationId, List<String> datasetIds) {
        Map<String, Object> body = new HashMap<>();
        body.put("query", question);
        body.put("response_mode", "blocking");
        body.put("user", "kw-user");
        if (conversationId != null && !conversationId.isBlank()) {
            body.put("conversation_id", conversationId);
        }
        JsonNode node = postJson("/chat-messages", body, false);
        String answer = node.path("answer").asText("暂无回答");
        String convId = node.path("conversation_id").asText(
                conversationId != null ? conversationId : "conv-" + UUID.randomUUID());
        return new DifyChatResult(convId, answer);
    }

    private JsonNode postJson(String path, Map<String, Object> body, boolean datasetApi) {
        try {
            String json = objectMapper.writeValueAsString(body);
            JsonNode node = restClient.post()
                    .uri(baseUrl() + path)
                    .header("Authorization", "Bearer " + (datasetApi ? datasetApiKey() : appApiKey()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .retrieve()
                    .body(JsonNode.class);
            if (node == null) {
                throw new CustomException(500, "Dify 调用失败");
            }
            return node;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(500, "Dify 调用失败: " + e.getMessage());
        }
    }

    private String baseUrl() {
        return appProperties.getDify().getBaseUrl();
    }

    private String appApiKey() {
        return appProperties.getDify().getApiKey();
    }

    private String datasetApiKey() {
        return appProperties.getDify().getDatasetApiKey();
    }
}
