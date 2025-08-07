package test.test.Interview;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 요청/응답 로깅 필터
 * UTF-8 인코딩 문제 디버깅을 위한 로깅
 */
@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Interview API 요청만 로깅
        if (!request.getRequestURI().startsWith("/api/interview")) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            logRequestDetails(requestWrapper);
            logResponseDetails(responseWrapper);
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequestDetails(ContentCachingRequestWrapper request) {
        try {
            String requestBody = new String(request.getContentAsByteArray(), StandardCharsets.UTF_8);
            log.debug("=== 요청 정보 ===");
            log.debug("Method: {}", request.getMethod());
            log.debug("URI: {}", request.getRequestURI());
            log.debug("Content-Type: {}", request.getContentType());
            log.debug("Character Encoding: {}", request.getCharacterEncoding());
            log.debug("Content Length: {}", request.getContentLength());
            
            if (!requestBody.isEmpty()) {
                log.debug("Request Body: {}", requestBody);
                // 바이트 배열로 인코딩 체크
                byte[] bytes = requestBody.getBytes(StandardCharsets.UTF_8);
                log.debug("Body bytes length: {}", bytes.length);
            }
            
        } catch (Exception e) {
            log.error("요청 로깅 중 오류 발생", e);
        }
    }

    private void logResponseDetails(ContentCachingResponseWrapper response) {
        try {
            String responseBody = new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);
            log.debug("=== 응답 정보 ===");
            log.debug("Status: {}", response.getStatus());
            log.debug("Content-Type: {}", response.getContentType());
            log.debug("Character Encoding: {}", response.getCharacterEncoding());
            
            if (!responseBody.isEmpty() && responseBody.length() < 1000) { // 너무 긴 응답은 제외
                log.debug("Response Body: {}", responseBody);
            }
            
        } catch (Exception e) {
            log.error("응답 로깅 중 오류 발생", e);
        }
    }
}