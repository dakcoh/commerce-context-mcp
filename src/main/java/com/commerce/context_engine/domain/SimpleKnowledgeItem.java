package com.commerce.context_engine.domain;

import lombok.Data;

import java.util.List;

/**
 * inventory / payment / settlement / coupon 4개 단순 도메인의 공통 아이템 모델.
 * 각 도메인 @ConfigurationProperties 가 이 클래스를 List 원소 타입으로 공유한다.
 */
@Data
public class SimpleKnowledgeItem {
    private String id;
    private String category;
    private String title;
    private String summary;
    private List<String> guidance;
    private List<String> avoidPatterns;
    private List<String> checklist;
    private List<String> tags;
}
