# 🌙 몽화 (Monghwa)
> **AI 꿈 해몽 + 이미지 생성 서비스**  
> 잊혀진 밤의 풍경을, AI가 다시 그려드립니다.



## 🪄 프로젝트 개요
몽화(Monghwa)는 사용자가 꾼 꿈의 내용을 입력하면  
Google **Gemini 2.5 Flash** 모델이 그 의미를 해석하고,  
**Gemini 2.5 Flash Image** 모델이 그 장면을 시각화해주는  
AI 기반 꿈 해몽 서비스입니다.  

> “AI가 당신의 무의식을 읽고, 잃어버린 꿈의 세계를 그림으로 되살립니다.”



## ✨ 주요 기능
| 기능 | 설명 |
|------|------|
| 🧠 **AI 꿈 해석** | Gemini 2.5 Flash 모델이 꿈의 의미를 간결하게 요약 및 분석 |
| 🎨 **AI 이미지 생성** | 선택한 화풍(예: 수채화, 픽셀, 몽환적 등)에 맞춰 꿈의 장면 시각화 |
| 💬 **실시간 출력** | 해몽 결과와 이미지가 한 화면에 카드 형태로 표시 |
| 🪶 **프롬프트 기반 제어** | 해몽의 구조(제목 → 종류 → 해석)를 명확히 제시하여 일관된 결과 도출 |



## ⚙️ 기술 스택
| 구분 | 사용 기술 |
|------|------------|
| **Frontend** | HTML5, CSS3, JavaScript (Vanilla JS) |
| **Backend** | Spring Boot (Java 17) |
| **API 연동** | Google Gemini 2.5 Flash / Flash Image (REST API) |
| **환경 관리** | Dotenv (API Key 관리) |



## 🧩 시스템 구조
```plaintext
[사용자]
   ↓ 꿈 내용 입력
[Static HTML (몽화.html)]
   ↓ fetch() 요청 (POST)
[Spring Boot Controller]
   ↓
[GeminiService] → 꿈 해몽 텍스트 생성
   ↓
[GeminiImageService] → Base64 이미지 생성
   ↓
[결과 페이지에 출력]
