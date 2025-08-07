package test.test.Interview;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * 면접 분석 API 컨트롤러
 * AI 기반 맞춤형 면접 질문 생성 및 학습 경로 추천 API 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {
    
    private final InterviewService interviewService;
    
    /**
     * AI 기반 면접 분석 엔드포인트
     * 사용자의 이력서 정보를 바탕으로 맞춤형 면접 질문과 학습 경로를 생성
     * 
     * @param request 사용자 이력서 정보
     * @return 면접 질문 및 학습 경로 응답
     */
    @PostMapping("/analyze")
    public ResponseEntity<InterviewAnalysisResponse> analyzeProfile(
            @Valid @RequestBody InterviewAnalysisRequest request) {
        
        log.info("면접 분석 요청 수신: 직무={}, 경력={}", request.getPosition(), request.getExperience());
        
        try {
            // Mono를 block()으로 동기 처리
            InterviewAnalysisResponse response = interviewService.analyzeProfile(request).block();
            
            if (response != null && response.isSuccess()) {
                log.info("=== 면접 분석 성공 ===");
                log.info("질문 개수: {}", response.getInterviewQuestions() != null ? response.getInterviewQuestions().size() : 0);
                log.info("학습 경로 길이: {}자", response.getLearningPath() != null ? response.getLearningPath().length() : 0);
                log.info("응답 객체: {}", response);
                
                if (response.getInterviewQuestions() != null) {
                    for (int i = 0; i < response.getInterviewQuestions().size(); i++) {
                        log.info("질문 {}: {}", i+1, response.getInterviewQuestions().get(i));
                    }
                }
                
                log.info("=== ResponseEntity 생성 전 마지막 체크 ===");
                log.info("response 객체 존재: {}", response != null);
                log.info("JSON 직렬화 테스트: {}", response.toString());
                
                return ResponseEntity.ok()
                        .header("Content-Type", "application/json; charset=UTF-8")
                        .body(response);
            } else {
                String errorMsg = response != null ? response.getErrorMessage() : "응답이 null입니다";
                log.error("면접 분석 실패: {}", errorMsg);
                InterviewAnalysisResponse errorResponse = InterviewAnalysisResponse.failure(errorMsg);
                return ResponseEntity.badRequest().body(errorResponse);
            }
        } catch (Exception e) {
            log.error("면접 분석 중 예외 발생", e);
            InterviewAnalysisResponse errorResponse = InterviewAnalysisResponse.failure(
                    "서버 내부 오류가 발생했습니다: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 서비스 상태 확인 엔드포인트
     * 
     * @return 서비스 상태 정보
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.debug("면접 서비스 상태 확인 요청");
        return ResponseEntity.ok("면접 분석 서비스가 정상적으로 작동 중입니다.");
    }
    
    /**
     * AI 모델 정보 확인 엔드포인트
     * 
     * @return 사용 중인 AI 모델 정보
     */
    @GetMapping("/model-info")
    public ResponseEntity<String> getModelInfo() {
        try {
            String modelInfo = interviewService.getModelInfo().block();
            return ResponseEntity.ok(modelInfo);
        } catch (Exception e) {
            log.error("모델 정보 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body("모델 정보 조회에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 샘플 면접 질문 생성 엔드포인트 (테스트용)
     * 
     * @param position 직무명
     * @param experience 경력
     * @return 샘플 면접 질문 리스트
     */
    @GetMapping("/sample-questions")
    public ResponseEntity<InterviewAnalysisResponse> getSampleQuestions(
            @RequestParam String position,
            @RequestParam(defaultValue = "신입") String experience) {
        
        log.info("샘플 면접 질문 요청: 직무={}, 경력={}", position, experience);
        
        try {
            InterviewAnalysisResponse response = interviewService.generateSampleQuestions(position, experience).block();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("샘플 질문 생성 실패", e);
            InterviewAnalysisResponse errorResponse = InterviewAnalysisResponse.failure(
                    "샘플 질문 생성에 실패했습니다: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    

}