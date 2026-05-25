package com.example.diagnoseillusion.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "")
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Minio minio = new Minio();
    private final Dify dify = new Dify();
    private final Upload upload = new Upload();

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private int expiryHours = 24;
    }

    @Getter
    @Setter
    public static class Minio {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucket;
    }

    @Getter
    @Setter
    public static class Dify {
        private String baseUrl;
        private String apiKey;
        private String datasetApiKey;
        private boolean stubEnabled = true;
        private final Upload upload = new Upload();

        @Getter
        @Setter
        public static class Upload {
            private int maxFileSizeMb = 15;
        }
    }

    @Getter
    @Setter
    public static class Upload {
        private int avatarMaxSizeMb = 2;
    }
}
