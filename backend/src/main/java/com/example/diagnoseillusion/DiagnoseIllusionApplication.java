package com.example.diagnoseillusion;

import com.example.diagnoseillusion.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class DiagnoseIllusionApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiagnoseIllusionApplication.class, args);
    }

}
