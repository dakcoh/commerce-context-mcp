package com.commerce.context_engine.tool;

import com.commerce.context_engine.service.SpringCommerceKnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringCommerceContextTool {

    private final SpringCommerceKnowledgeService knowledgeService;

    @Tool(name = "get_spring_commerce_implementation_context",
          description = """
                  Java Spring 기반 이커머스 웹 백엔드 구현 가이드 전체를 반환합니다.
                  모듈형 모놀리스, 트랜잭션, JPA, 동시성, API 오류, Outbox, Redis, 배치, 테스트와 관측성,
                  Java 값 객체, null/예외, Stream, Spring Security, 설정, 스키마 마이그레이션을 포함합니다.
                  Spring Boot 쇼핑몰 신규 구축, 리팩터링, 코드 리뷰 요청 시 호출하세요.
                  """)
    public String getSpringCommerceImplementationContext() {
        return knowledgeService.getSpringImplementationContext();
    }

    @Tool(name = "get_spring_commerce_checklist",
          description = """
                  Java Spring 이커머스 구현 검토 체크리스트를 반환합니다.
                  출시 전 검토, 아키텍처 리뷰, Spring 백엔드 누락 확인 요청 시 호출하세요.
                  """)
    public String getSpringCommerceChecklist() {
        return knowledgeService.getChecklist();
    }

    @Tool(name = "search_spring_commerce_knowledge",
          description = """
                  키워드로 Java Spring 이커머스 구현 지식을 검색합니다.
                  Transactional, JPA, Redis, Outbox, Scheduler, Testcontainers, Optional, Stream, Security, Flyway 등 Java/Spring 구현 질문에 사용하세요.
                  """)
    public String searchSpringCommerceKnowledge(
            @ToolParam(description = "검색 키워드 (예: '@Transactional', 'JPA', 'Redis', 'Outbox', 'Testcontainers', 'Optional', 'Stream', 'Security', 'Flyway')") String keyword) {
        return knowledgeService.search(keyword);
    }
}
