package com.monghwa.controller;

import com.monghwa.service.GeminiService;
import com.monghwa.service.GeminiImageService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dream")
@CrossOrigin("*")
public class DreamController {
    private final GeminiService geminiService;
    private final GeminiImageService geminiImageService;

    public DreamController(GeminiService geminiService, GeminiImageService geminiImageService) {
        this.geminiService = geminiService;
        this.geminiImageService = geminiImageService;
    }

    /**
     * AI 꿈 해몽 (텍스트 해석)
     * @param dreamText 사용자가 입력한 꿈 내용
     * @return Gemini 모델이 생성한 해몽 결과 (Markdown 형식 텍스트)
     */
    @PostMapping
    public String interpretDream(@RequestBody String dreamText) {
        return geminiService.interpretDream(dreamText);
    }

    /**
     * AI 이미지 생성
     * @param payload { text: "꿈 내용", style: "화풍" }
     * @return Base64 인코딩된 PNG 이미지 데이터 URI
     */
    @PostMapping("/image")
    public String generateDreamImage(@RequestBody Map<String, String> payload) {
        String text = payload.get("text");
        String style = payload.get("style");
        return geminiImageService.generateDreamImage(text, style);
    }
}
