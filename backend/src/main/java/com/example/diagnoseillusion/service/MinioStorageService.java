package com.example.diagnoseillusion.service;

import com.example.diagnoseillusion.common.CustomException;
import com.example.diagnoseillusion.config.AppProperties;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minioClient;
    private final AppProperties appProperties;

    public void upload(String objectKey, MultipartFile file, String contentType) {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket())
                    .object(objectKey)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            throw new CustomException(500, "文件上传失败: " + e.getMessage());
        }
    }

    public void delete(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket())
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            throw new CustomException(500, "文件删除失败: " + e.getMessage());
        }
    }

    public byte[] readObjectBytes(String objectKey) {
        try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket())
                .object(objectKey)
                .build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new CustomException(500, "读取文件失败: " + e.getMessage());
        }
    }

    public String presignedGetUrl(String objectKey, int expireSeconds) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket())
                    .object(objectKey)
                    .expiry(expireSeconds, TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            throw new CustomException(500, "生成预览链接失败: " + e.getMessage());
        }
    }

    private String bucket() {
        return appProperties.getMinio().getBucket();
    }
}
