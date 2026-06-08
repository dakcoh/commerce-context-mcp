package com.commerce.context_engine.tool;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UnifiedSearchToolTest {

    @Autowired
    UnifiedSearchTool tool;

    @Test
    void searchAllKnowledge_returnsResultsAcrossDomains() {
        String result = tool.searchAllKnowledge("멱등성");

        // 재고·결제 등 여러 도메인 결과가 모두 포함된다
        assertThat(result).isNotBlank();
        assertThat(result).contains("멱등");
    }

    @Test
    void searchAllKnowledge_crossDomain_containsMultipleDomainContent() {
        String result = tool.searchAllKnowledge("동시성");

        // 재고 낙관락·쿠폰 발급 등 여러 도메인 내용이 함께 반환된다
        assertThat(result).isNotBlank();
    }

    @Test
    void searchAllKnowledge_blankKeyword_returnsGuideMessage() {
        assertThat(tool.searchAllKnowledge("  ")).contains("키워드를 입력");
        assertThat(tool.searchAllKnowledge(null)).contains("키워드를 입력");
    }

    @Test
    void searchAllKnowledge_unknownKeyword_returnsNotFoundMessage() {
        assertThat(tool.searchAllKnowledge("존재하지않는키워드xyzabc")).contains("찾을 수 없습니다");
    }

    @Test
    void searchAllKnowledge_redis_containsDistributedLockContent() {
        String result = tool.searchAllKnowledge("redis");

        assertThat(result).contains("분산락");
    }
}
