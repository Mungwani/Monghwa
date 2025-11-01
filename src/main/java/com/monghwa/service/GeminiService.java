package com.monghwa.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * GeminiService
 * 사용자의 꿈을 텍스트로 해석하는 서비스 클래스
 * Google AI Studio의 Gemini 2.5 Flash 모델을 호출하여
 * 꿈 내용을 간결하고 의미 있게 요약해주는 기능 수행
 *
 * ✅ 사용 모델: gemini-2.5-flash
 * ✅ 엔드포인트: https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent
 */
@Service
public class GeminiService {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("GOOGLE_API_KEY");

    // ✅ 최신 AI Studio REST API 엔드포인트 (2025년 기준)
    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    /**
     * 꿈 해몽 요청 메서드
     * 사용자의 꿈 내용을 Gemini API로 전송하고 간결한 해석 결과를 반환
     *
     * @param dreamText 사용자가 입력한 꿈 내용
     * @return AI가 생성한 5줄 이내의 꿈 해석 요약문
     */
    public String interpretDream(String dreamText) {
        try {
            String requestBody = """
        {
          "contents": [{
            "parts": [{
              "text": "이 꿈을 간결하게 해석해줘. 핵심 의미만 5줄 이내로 요약해서 알려줘. 이 꿈이 길몽인지 흉몽인지 태몽인지 확실하다면 그걸 맨 앞에 언급해줘: %s"
            }]
          }]
        }
        """.formatted(dreamText);

            // HTTP 연결 설정
            URL url = new URL(GEMINI_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            // 요청 전송
            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes("UTF-8"));
            }

            int code = conn.getResponseCode();

            // 응답 스트림 처리
            InputStream responseStream = (code >= 200 && code < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            if (responseStream == null) {
                return "🌙 오류: 서버로부터 응답이 없습니다. (HTTP " + code + ")";
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(responseStream, "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) response.append(line);
            }

            // 응답 파싱 (text 필드 추출)
            if (code != 200) {
                return "🌙 오류 (" + code + "): " + response;
            }

            int start = response.indexOf("\"text\":");
            if (start == -1) {
                return "🌙 응답 파싱 실패: " + response;
            }

            int quote1 = response.indexOf("\"", start + 7);
            int quote2 = response.indexOf("\"", quote1 + 1);

            if (quote1 == -1 || quote2 == -1) {
                return "🌙 응답 형식 오류: " + response;
            }

            String resultText = response.substring(quote1 + 1, quote2)
                    .replace("\\n", "\n")
                    .replace("\\u0026", "&");

            return "🌙 " + resultText.trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "🌙 예외 발생: " + e.getMessage();
        }
    }
}
