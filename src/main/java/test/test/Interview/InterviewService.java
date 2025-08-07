package test.test.Interview;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

/**
 * 면접 분석 서비스
 * Google Gemini API를 활용한 AI 기반 면접 질문 생성 및 학습 경로 추천 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewService {
    
    private final WebClient webClient;
    
    @Value("${google.gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    
    /**
     * 사용자 프로필 분석 및 맞춤형 면접 콘텐츠 생성
     * 
     * @param request 사용자 이력서 정보
     * @return 면접 질문 및 학습 경로 응답
     */
    public Mono<InterviewAnalysisResponse> analyzeProfile(InterviewAnalysisRequest request) {
        long startTime = System.currentTimeMillis();
        
        return generateInterviewQuestions(request)
                .flatMap(questions -> {
                    log.info("=== 면접 질문 생성 완료 ===");
                    log.info("생성된 질문 수: {}", questions.size());
                    questions.forEach(q -> log.info("질문: {}", q));
                    
                    return generateLearningPath(request)
                            .map(learningPath -> {
                                log.info("=== 학습 경로 생성 완료 ===");
                                log.info("학습 경로 길이: {}자", learningPath.length());
                                log.info("학습 경로 내용: {}", learningPath.substring(0, Math.min(200, learningPath.length())) + "...");
                                
                                long processingTime = System.currentTimeMillis() - startTime;
                                
                                InterviewAnalysisResponse.AnalysisMetadata metadata = 
                                        InterviewAnalysisResponse.AnalysisMetadata.builder()
                                                .processingTimeMs(processingTime)
                                                .aiModel("Google Gemini 2.5 Flash")
                                                .qualityScore(calculateQualityScore(request))
                                                .analysisTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                                .priority(determinePriority(request))
                                                .extractedKeywords(extractKeywords(request))
                                                .build();
                                
                                InterviewAnalysisResponse response = InterviewAnalysisResponse.success(questions, learningPath, metadata);
                                log.info("=== 최종 응답 객체 생성 완료 ===");
                                log.info("Success: {}", response.isSuccess());
                                log.info("질문 리스트 크기: {}", response.getInterviewQuestions().size());
                                log.info("학습 경로 존재 여부: {}", response.getLearningPath() != null);
                                
                                return response;
                            });
                })
                .onErrorResume(throwable -> {
                    log.error("프로필 분석 중 오류 발생", throwable);
                    return Mono.just(InterviewAnalysisResponse.failure("분석 처리 중 오류가 발생했습니다: " + throwable.getMessage()));
                });
    }
    
    /**
     * Gemini API를 사용하여 맞춤형 면접 질문 생성
     * 
     * @param request 사용자 이력서 정보
     * @return 면접 질문 리스트
     */
    private Mono<List<String>> generateInterviewQuestions(InterviewAnalysisRequest request) {
        String prompt = buildInterviewQuestionPrompt(request);
        
        return callGeminiAPI(prompt)
                .map(this::parseInterviewQuestions)
                .onErrorResume(throwable -> {
                    log.error("면접 질문 생성 실패", throwable);
                    return Mono.just(getDefaultQuestions(request.getPosition()));
                });
    }
    
    /**
     * Gemini API를 사용하여 개인 맞춤형 학습 경로 생성
     * 
     * @param request 사용자 이력서 정보
     * @return 학습 경로 HTML 문자열
     */
    private Mono<String> generateLearningPath(InterviewAnalysisRequest request) {
        String prompt = buildLearningPathPrompt(request);
        
        return callGeminiAPI(prompt)
                .map(this::formatLearningPathAsHTML)
                .onErrorResume(throwable -> {
                    log.error("학습 경로 생성 실패", throwable);
                    return Mono.just(getDefaultLearningPath(request.getPosition()));
                });
    }
    
    /**
     * Gemini API 호출
     * 
     * @param prompt AI에게 전달할 프롬프트
     * @return API 응답 텍스트
     */
    private Mono<String> callGeminiAPI(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );
        
        return webClient.post()
                .uri(GEMINI_API_URL + "?key=" + geminiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    try {
                        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        return (String) parts.get(0).get("text");
                    } catch (Exception e) {
                        log.error("Gemini API 응답 파싱 오류", e);
                        throw new RuntimeException("AI 응답 처리 중 오류가 발생했습니다", e);
                    }
                });
    }
    
    /**
     * 면접 질문 생성을 위한 프롬프트 구성
     */
    private String buildInterviewQuestionPrompt(InterviewAnalysisRequest request) {
        return String.format("""
                당신은 전문 면접관입니다. 다음 구직자 정보를 바탕으로 실제 면접에서 나올 법한 심층적인 질문 5개를 생성해주세요.
                
                구직자 정보:
                %s
                
                요구사항:
                1. 각 질문은 구직자의 경험과 기술 스택에 특화되어야 합니다
                2. 기술적 깊이와 실무 적용 능력을 평가할 수 있는 질문이어야 합니다
                3. 상황 기반 질문(STAR 방식)을 포함해주세요
                4. 질문은 번호와 함께 명확하게 구분해주세요
                5. 각 질문은 구체적이고 답변하기에 적절한 난이도여야 합니다
                
                응답 형식:
                1. [질문 내용]
                2. [질문 내용]
                3. [질문 내용]
                4. [질문 내용]
                5. [질문 내용]
                """, request.getFullProfile());
    }
    
    /**
     * 학습 경로 생성을 위한 프롬프트 구성
     */
    private String buildLearningPathPrompt(InterviewAnalysisRequest request) {
        return String.format("""
                당신은 전문 커리어 컨설턴트입니다. 다음 구직자 정보를 바탕으로 개인 맞춤형 학습 경로를 제안해주세요.
                
                구직자 정보:
                %s
                
                요구사항:
                1. 현재 보유 기술을 바탕으로 한 발전 방향 제시
                2. 희망 직무에 필요한 추가 기술 스택 추천
                3. 구체적인 학습 단계별 로드맵 제공
                4. 실무 프로젝트 경험 쌓기 방안
                5. 업계 트렌드를 반영한 최신 기술 포함
                6. 학습 우선순위와 예상 소요 시간 제시
                
                응답 형식:
                ## 단기 목표 (1-3개월)
                - 학습 항목과 구체적인 방법
                
                ## 중기 목표 (3-6개월)
                - 심화 학습 및 프로젝트 경험
                
                ## 장기 목표 (6개월 이상)
                - 전문성 강화 및 리더십 개발
                
                ## 추천 리소스
                - 온라인 강의, 책, 실습 프로젝트 등
                """, request.getFullProfile());
    }
    
    /**
     * AI 응답에서 면접 질문 파싱
     */
    private List<String> parseInterviewQuestions(String response) {
        log.info("=== 질문 파싱 시작 ===");
        log.info("원본 응답 길이: {}자", response.length());
        log.info("원본 응답 내용 (처음 500자): {}", response.substring(0, Math.min(500, response.length())));
        
        // 질문을 더 유연하게 파싱
        List<String> questions = Arrays.stream(response.split("\\n\\n|\\n(?=\\d+\\.)"))
                .filter(line -> line.trim().matches("^\\d+\\..*"))
                .map(line -> {
                    // 번호 제거 후 정리
                    String cleaned = line.replaceFirst("^\\d+\\.", "").trim();
                    // 여러 줄로 된 질문 합치기
                    cleaned = cleaned.replaceAll("\\n+", " ").trim();
                    return cleaned;
                })
                .filter(question -> !question.isEmpty())
                .limit(5)
                .toList();
        
        log.info("파싱된 질문 수: {}", questions.size());
        questions.forEach(q -> log.info("파싱된 질문: {}", q));
        
        return questions;
    }
    
    /**
     * 학습 경로를 HTML 형식으로 포맷팅
     */
    private String formatLearningPathAsHTML(String response) {
        log.info("=== HTML 포맷팅 시작 ===");
        log.info("원본 응답 길이: {}자", response.length());
        log.info("원본 응답 내용 (처음 300자): {}", response.substring(0, Math.min(300, response.length())));
        
        // 더 정확한 HTML 포맷팅
        StringBuilder htmlBuilder = new StringBuilder();
        String[] lines = response.split("\n");
        boolean inList = false;
        
        for (String line : lines) {
            line = line.trim();
            
            if (line.isEmpty()) {
                continue;
            }
            
            // 헤딩 처리 (## 로 시작)
            if (line.startsWith("## ")) {
                // 이전 리스트가 열려있으면 닫기
                if (inList) {
                    htmlBuilder.append("</ul>\n");
                    inList = false;
                }
                String heading = line.substring(3).trim();
                htmlBuilder.append("<h3>").append(heading).append("</h3>\n");
            }
            // 리스트 아이템 처리 (- 로 시작)
            else if (line.startsWith("- ")) {
                // 리스트가 시작되지 않았으면 시작
                if (!inList) {
                    htmlBuilder.append("<ul>\n");
                    inList = true;
                }
                String listItem = line.substring(2).trim();
                htmlBuilder.append("<li>").append(listItem).append("</li>\n");
            }
            // 일반 텍스트
            else {
                // 이전 리스트가 열려있으면 닫기
                if (inList) {
                    htmlBuilder.append("</ul>\n");
                    inList = false;
                }
                htmlBuilder.append("<p>").append(line).append("</p>\n");
            }
        }
        
        // 마지막에 열린 리스트가 있으면 닫기
        if (inList) {
            htmlBuilder.append("</ul>\n");
        }
        
        String formattedHtml = htmlBuilder.toString();
        log.info("포맷팅된 HTML 길이: {}자", formattedHtml.length());
        log.info("포맷팅된 HTML: {}", formattedHtml);
        
        return formattedHtml;
    }
    
    /**
     * 기본 면접 질문 (AI 호출 실패 시 백업)
     */
    private List<String> getDefaultQuestions(String position) {
        return List.of(
                "자기소개를 간단히 해주세요.",
                "이 직무에 지원한 이유는 무엇인가요?",
                "가장 기억에 남는 프로젝트 경험을 설명해주세요.",
                "어려운 기술적 문제를 해결한 경험이 있나요?",
                "앞으로의 커리어 목표는 무엇인가요?"
        );
    }
    
    /**
     * 기본 학습 경로 (AI 호출 실패 시 백업)
     */
    private String getDefaultLearningPath(String position) {
        return """
                <h3>단기 목표 (1-3개월)</h3>
                <ul>
                <li>기본 기술 스택 복습 및 심화 학습</li>
                <li>포트폴리오 프로젝트 1개 완성</li>
                </ul>
                
                <h3>중기 목표 (3-6개월)</h3>
                <ul>
                <li>실무 프로젝트 경험 쌓기</li>
                <li>새로운 기술 스택 학습</li>
                </ul>
                
                <h3>장기 목표 (6개월 이상)</h3>
                <ul>
                <li>전문성 강화 및 깊이 있는 학습</li>
                <li>커뮤니티 활동 및 지식 공유</li>
                </ul>
                """;
    }
    
    /**
     * 품질 점수 계산
     */
    private Integer calculateQualityScore(InterviewAnalysisRequest request) {
        int score = 5; // 기본 점수
        
        if (request.getProjectExperience() != null && !request.getProjectExperience().trim().isEmpty()) score += 2;
        if (request.getAllSkills().length() > 50) score += 2;
        if (request.getLearningGoals() != null && !request.getLearningGoals().trim().isEmpty()) score += 1;
        
        return Math.min(score, 10);
    }
    
    /**
     * 우선순위 결정
     */
    private String determinePriority(InterviewAnalysisRequest request) {
        if ("신입".equals(request.getExperience())) return "HIGH";
        if (request.getProjectExperience() != null && request.getProjectExperience().length() > 100) return "HIGH";
        return "MEDIUM";
    }
    
    /**
     * 키워드 추출
     */
    private List<String> extractKeywords(InterviewAnalysisRequest request) {
        return Arrays.asList(
                request.getPosition(),
                request.getExperience(),
                "면접 준비",
                "기술 스킬"
        );
    }
    
    /**
     * 샘플 질문 생성 (테스트용)
     */
    public Mono<InterviewAnalysisResponse> generateSampleQuestions(String position, String experience) {
        List<String> sampleQuestions = List.of(
                position + " 직무에 대한 이해도를 설명해주세요.",
                experience + " 경력으로서 가장 도전적이었던 프로젝트는 무엇인가요?",
                "팀워크 경험과 협업 시 중요하게 생각하는 점은 무엇인가요?",
                "기술적 성장을 위해 어떤 노력을 하고 계신가요?",
                "향후 " + position + " 분야에서의 목표는 무엇인가요?"
        );
        
        String samplePath = "<h3>기본 학습 가이드</h3><ul><li>기초 역량 강화</li><li>실무 경험 쌓기</li></ul>";
        
        return Mono.just(InterviewAnalysisResponse.success(sampleQuestions, samplePath));
    }
    
    /**
     * AI 모델 정보 조회
     */
    public Mono<String> getModelInfo() {
        return Mono.just("Google Gemini 2.5 Flash Latest - 면접 질문 생성 및 학습 경로 추천에 최적화된 모델");
    }
}