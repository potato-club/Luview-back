# 💕 Luview - 커플 기록 웹앱

## 📌 프로젝트 소개

**Luview**는 커플들이 함께 방문한 장소를 기록하고,  
직접 작성한 리뷰를 남길 수 있는 **커플 전용 기록 웹 애플리케이션**입니다.  
함께한 순간들을 사진과 후기와 함께 남기며, 소중한 추억을 쌓을 수 있습니다.

---

## 🖼️ 주요 화면

| 페이지 | 설명 |
|:-------|:-----|
| 로그인 / 회원가입 | 간편한 로그인 및 신규 회원가입 기능 제공 |
| 메인 홈 화면 | 나와 내 커플 파트너의 프로필 및 최근 기록 확인 |
| 프로필 수정 페이지 | 닉네임, 프로필 사진 등 개인 정보 수정 |
| 공개리뷰 리스트 | 다른 커플들의 리뷰 게시물 목록 열람 |
| 공개리뷰 게시글 상세 | 공개된 리뷰 상세 조회 |
| 지도 검색 페이지 | 주변 장소 검색 및 위치 기반 리뷰 등록 |
| 지도 리뷰 등록/조회 | 특정 장소에 대한 커플 리뷰 작성 및 열람 |

---

## ✨ 주요 기능

- **회원 관리**  
  - 회원가입 / 로그인 / 프로필 수정

- **커플 기록 기능**  
  - 방문한 장소 기록  
  - 커플 후기(리뷰) 작성 및 수정
  - 장소 기반 지도 리뷰 기능

- **리뷰 기능**  
  - 공개 리뷰 리스트 열람
  - 공개 게시글 댓글 작성 및 좋아요 기능
  - 게시글 조회수 집계
  - 최근 인기글 조회 
  - 가장 많은 좋아요 게시물 순위 표시

- **지도 검색**  
  - 지도에서 장소 검색 후, 해당 장소에 리뷰 남기기

- **즐겨찾기**  
  - 좋아하는 리뷰를 즐겨찾기로 저장

---

## 🔥 기술 스택

| 항목 | 사용 기술 |
|:----|:---------|
| Frontend | React.js, React Router, Tailwind CSS |
| Backend | Spring Boot, JAVA 17 |
| Database | MySQL, Redis (세션/토큰 관리) |
| 기타 | AWS EC2, S3 (파일 업로드) |

---

## 🚀 프로젝트 목표

- 커플 전용 장소 기록 서비스 제공
- 함께한 장소를 기반으로 리뷰/사진 아카이빙
- 사용자 친화적이고 직관적인 UX 디자인 구현
- 지도 기반의 장소 검색 및 커플 데이터 시각화

---

## 🗂️ 관련 API 문서

- `/users` - 회원가입, 로그인, 회원 정보 관리
- `/review` - 커플 리뷰 작성 및 관리
- `/favorites` - 즐겨찾기 기능
- `/uploads` - 파일(사진) 업로드
- `/couple` - 커플 매칭 기능
- `/daily_question` - 오늘의 질문
- `/mainPage` - 메인 홈 데이터 조회
- `/comment` - 댓글 작성 및 관리
- `/like` - 좋아요 기능

---

## 피그마 

![image](https://github.com/user-attachments/assets/3db037cf-a965-4fe8-ac0f-e620697438a2)
