package com.monghwa.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GeminiImageService
 * Google AI Studio의 Gemini 이미지 생성 API를 호출하여
 * 사용자의 꿈 내용을 시각화한 이미지를 생성하는 서비스 클래스입니다.
 *
 * 사용 모델: gemini-2.5-flash-image
 * API 방식: REST POST (https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent)
 */
@Service
public class GeminiImageService {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("GOOGLE_API_KEY");

    private static final String IMAGE_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent?key=" + API_KEY;

    /**
     * 꿈 이미지를 생성하는 메서드
     *
     * @param text  사용자가 입력한 꿈 내용
     * @param style 선택된 이미지 화풍 (예: 수채화, 일러스트, 애니 등)
     * @return Base64 인코딩된 PNG 이미지 데이터 URI 또는 오류 메시지
     */
    public String generateDreamImage(String text, String style) {
        try {
            // ✅ 프롬프트 구성
            String prompt = String.format(
                    "%s 스타일로 '%s' 장면을 몽환적이고 예술적인 일러스트로 시각화해줘. " +
                            "텍스트 설명은 절대 포함하지 말고, 반드시 이미지만 생성해.",
                    style, text
            );

            // 요청 본문 (responseModalities 형식으로 변경)
            String requestBody = """
            {
              "contents": [
                {
                  "role": "user",
                  "parts": [{ "text": "%s" }]
                }
              ],
              "generationConfig": {
                "responseModalities": ["Image"]
              }
            }
            """.formatted(prompt.replace("\"", "\\\""));

            // HTTP 요청 설정
            URL url = new URL(IMAGE_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            // 요청 본문 전송
            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            System.out.println("📡 Google 응답 코드: " + code);

            InputStream stream =
                    (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            if (stream == null) return "이미지 생성 실패 (응답 없음)";

            // 응답 읽기
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }

            String response = sb.toString();
            System.out.println("Gemini 응답 본문: " + response);

            // inline_data 또는 inlineData 경로에서 Base64 이미지 추출
            Pattern pattern1 = Pattern.compile("\"inline_data\"\\s*:\\s*\\{[^}]*\"data\"\\s*:\\s*\"([^\"]+)\"");
            Pattern pattern2 = Pattern.compile("\"inlineData\"\\s*:\\s*\\{[^}]*\"data\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher1 = pattern1.matcher(response);
            Matcher matcher2 = pattern2.matcher(response);

            String base64 = null;
            if (matcher1.find()) {
                base64 = matcher1.group(1);
            } else if (matcher2.find()) {
                base64 = matcher2.group(1);
            }

            if (base64 != null) {
                return "data:image/png;base64," + base64;
            } else {
                return "이미지 응답 오류: " + response;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "예외 발생: " + e.getMessage();
        }
    }
}
