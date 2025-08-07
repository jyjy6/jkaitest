package test.test.Interview;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * WebClient 설정 클래스
 * Gemini API 호출을 위한 WebClient 빈 설정
 */
@Configuration
public class WebClientConfig {
    
    /**
     * Gemini API 호출용 WebClient 빈 생성
     * 타임아웃 및 기본 설정 적용
     * 
     * @return 설정된 WebClient 인스턴스
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }
}