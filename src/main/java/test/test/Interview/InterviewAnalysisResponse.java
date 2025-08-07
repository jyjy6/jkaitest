package test.test.Interview;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * 면접 분석 응답 DTO
 * AI 분석 결과를 클라이언트에게 전달하기 위한 데이터 전송 객체
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewAnalysisResponse {
    
    /**
     * 맞춤형 면접 질문 리스트 (최대 5개)
     */
    private List<String> interviewQuestions;
    
    /**
     * 개인 맞춤형 학습 경로 (HTML 형식 지원)
     */
    private String learningPath;
    
    /**
     * 분석 성공 여부
     */
    private boolean success;
    
    /**
     * 오류 메시지 (분석 실패 시)
     */
    private String errorMessage;
    
    /**
     * 추가 분석 정보 (선택사항)
     */
    private AnalysisMetadata metadata;
    
    /**
     * 성공 응답 생성을 위한 정적 메서드
     * @param questions 면접 질문 리스트
     * @param learningPath 학습 경로
     * @return 성공 응답 객체
     */
    public static InterviewAnalysisResponse success(List<String> questions, String learningPath) {
        return InterviewAnalysisResponse.builder()
                .interviewQuestions(questions)
                .learningPath(learningPath)
                .success(true)
                .build();
    }
    
    /**
     * 성공 응답 생성 (메타데이터 포함)
     * @param questions 면접 질문 리스트
     * @param learningPath 학습 경로
     * @param metadata 분석 메타데이터
     * @return 성공 응답 객체
     */
    public static InterviewAnalysisResponse success(List<String> questions, String learningPath, AnalysisMetadata metadata) {
        return InterviewAnalysisResponse.builder()
                .interviewQuestions(questions)
                .learningPath(learningPath)
                .success(true)
                .metadata(metadata)
                .build();
    }
    
    /**
     * 실패 응답 생성을 위한 정적 메서드
     * @param errorMessage 오류 메시지
     * @return 실패 응답 객체
     */
    public static InterviewAnalysisResponse failure(String errorMessage) {
        return InterviewAnalysisResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
    
    /**
     * 분석 메타데이터 내부 클래스
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnalysisMetadata {
        
        /**
         * 분석 처리 시간 (밀리초)
         */
        private Long processingTimeMs;
        
        /**
         * 사용된 AI 모델 정보
         */
        private String aiModel;
        
        /**
         * 분석 품질 점수 (1-10)
         */
        private Integer qualityScore;
        
        /**
         * 분석 일시 (ISO 8601 형식)
         */
        private String analysisTimestamp;
        
        /**
         * 추천 우선순위 (HIGH, MEDIUM, LOW)
         */
        private String priority;
        
        /**
         * 분석에 사용된 키워드들
         */
        private List<String> extractedKeywords;
    }
}