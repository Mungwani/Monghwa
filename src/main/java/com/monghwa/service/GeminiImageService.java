package com.monghwa.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
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
            // 프롬프트 구성
            String prompt = "다음 꿈을 " + style + " 스타일에 딱 맞춰서 시각화해줘: " + text;

            // 요청 본문 (AI Studio 형식)
            String requestBody = """
            {
              "contents": [
                {
                  "role": "user",
                  "parts": [
                    { "text": "%s" }
                  ]
                }
              ]
            }
            """.formatted(prompt.replace("\"", "\\\""));

            // HTTP 요청
            URL url = new URL(IMAGE_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            // 요청 본문 전송
            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes("UTF-8"));
            }

            int code = conn.getResponseCode();
            System.out.println("📡 Google 응답 코드: " + code);

            // 응답 스트림 읽기
            InputStream stream =
                    (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            if (stream == null) return "이미지 생성 실패 (응답 없음)";

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }

            String response = sb.toString();
            System.out.println("Gemini 응답 본문: " + response);

            // inlineData.data 에서 Base64 이미지 추출
            Pattern pattern = Pattern.compile("\"data\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(response);

            if (matcher.find()) {
                String base64 = matcher.group(1);
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
