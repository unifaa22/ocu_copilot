package com.example.diagnoseillusion.config;

import com.example.diagnoseillusion.service.dify.DifyClient;
import com.example.diagnoseillusion.service.dify.RealDifyClient;
import com.example.diagnoseillusion.service.dify.StubDifyClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DifyClientConfig {

    private final AppProperties appProperties;

    @Bean
    public DifyClient difyClient(StubDifyClient stubDifyClient, RealDifyClient realDifyClient) {
        if (appProperties.getDify().isStubEnabled()) {
            return stubDifyClient;
        }
        return realDifyClient;
    }
}
