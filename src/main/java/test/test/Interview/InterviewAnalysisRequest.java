package test.test.Interview;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 면접 분석 요청 DTO
 * 사용자의 이력서 정보를 받아 AI 분석을 위한 데이터 전송 객체
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewAnalysisRequest {
    
    /**
     * 경력 정보 (예: "3년차", "신입", "5년 이상")
     */
    private String experience;
    
    /**
     * 희망 직무 (예: "백엔드 개발자", "프론트엔드 개발자")
     */
    private String position;
    
    /**
     * 프론트엔드 기술 스킬 (쉼표로 구분된 문자열)
     */
    private String front;
    
    /**
     * 백엔드 기술 스킬 (쉼표로 구분된 문자열)
     * 이런 스킬들 나중에 리스트로하면 좋을거같기도하고 아닌가.. 아님말고
     */
    private String back;
    
    /**
     * DevOps/인프라 기술 스킬 (쉼표로 구분된 문자열)
     */
    private String devops;
    
    /**
     * 기타 기술 스킬 (쉼표로 구분된 문자열)
     */
    private String etc;
    
    /**
     * 주요 프로젝트 경험
     */
    private String projectExperience;
    
    /**
     * 학습 목표 및 관심 분야
     */
    private String learningGoals;
    
    /**
     * 선호 회사 규모
     */
    private String companySize;
    
    /**
     * 관심 업계
     */
    private String industry;
    
    /**
     * 모든 기술 스킬을 하나의 문자열로 결합
     * @return 결합된 기술 스킬 문자열
     */
    public String getAllSkills() {
        StringBuilder skills = new StringBuilder();
        
        if (front != null && !front.trim().isEmpty()) {
            skills.append("프론트엔드: ").append(front).append(". ");
        }
        if (back != null && !back.trim().isEmpty()) {
            skills.append("백엔드: ").append(back).append(". ");
        }
        if (devops != null && !devops.trim().isEmpty()) {
            skills.append("DevOps/인프라: ").append(devops).append(". ");
        }
        if (etc != null && !etc.trim().isEmpty()) {
            skills.append("기타 기술: ").append(etc).append(". ");
        }
        
        return skills.toString().trim();
    }
    
    /**
     * 완전한 프로필 요약 생성
     * @return AI 분석을 위한 완전한 프로필 문자열
     */
    public String getFullProfile() {
        StringBuilder profile = new StringBuilder();
        
        // 기본 정보
        profile.append("경력: ").append(experience != null ? experience : "정보 없음").append("\n");
        profile.append("희망 직무: ").append(position != null ? position : "정보 없음").append("\n");
        
        // 기술 스킬
        String skills = getAllSkills();
        if (!skills.isEmpty()) {
            profile.append("기술 스킬: ").append(skills).append("\n");
        }
        
        // 프로젝트 경험
        if (projectExperience != null && !projectExperience.trim().isEmpty()) {
            profile.append("주요 프로젝트 경험: ").append(projectExperience).append("\n");
        }
        
        // 학습 목표
        if (learningGoals != null && !learningGoals.trim().isEmpty()) {
            profile.append("학습 목표 및 관심 분야: ").append(learningGoals).append("\n");
        }
        
        // 선호사항
        if (companySize != null && !companySize.trim().isEmpty()) {
            profile.append("선호 회사 규모: ").append(companySize).append("\n");
        }
        if (industry != null && !industry.trim().isEmpty()) {
            profile.append("관심 업계: ").append(industry).append("\n");
        }
        
        return profile.toString();
    }
}