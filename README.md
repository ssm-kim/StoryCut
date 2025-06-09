 # 🎬 StoryCut

#### AI 기반 영상 편집 및 공유 플랫폼

> 2025.04.14 ~ 2025.05.22

<br>

---

1. **[웹 서비스 소개](#1-웹-서비스-소개)**
2. **[기술 스택](#2-기술-스택)**
3. **[주요 기능](#3-주요-기능)**
4. **[시스템 아키텍처](#4-시스템-아키텍처)**
5. **[서비스 화면](#5-서비스-화면)**
6. **[개발 팀 소개](#6-개발-팀-소개)**

## 1. 웹 서비스 소개

### ✨ StoryCut: AI 기반 영상 편집 및 공유 플랫폼

영상 편집이 어렵고 시간이 많이 걸리는 경험을 해보셨나요?
StoryCut과 함께 AI 기반의 스마트한 영상 편집을 경험해보세요!

#### 🌟 StoryCut만의 특별함

- 프롬프트 기반 지능형 컷 편집: 사용자의 프롬프트를 분석하여 원하는 영상 구간을 자동으로 추출
- 멀티모달 AI 통합 시스템: mmaction2와 OpenCV를 활용한 고급 영상 분석
- 선택적 프라이버시 보호: 특정 인물만 선택적으로 보존하고 나머지는 자동 모자이크 처리

#### 💡 이런 분들에게 완벽해요

- 영상 편집에 익숙하지 않은 초보자
- 빠른 시간 내에 고품질 영상을 제작하고 싶은 콘텐츠 크리에이터
- 프라이버시 보호가 필요한 영상 편집이 필요한 사용자

## 2. 기술 스택

### **FrontEnd**

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=Android&logoColor=white) ![Kotlin 2.0.21](https://img.shields.io/badge/Kotlin%202.0.21-7F52FF?style=for-the-badge&logo=Kotlin&logoColor=white) ![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=Jetpack%20Compose&logoColor=white) ![Material3](https://img.shields.io/badge/Material3-1976D2?style=for-the-badge&logo=Material%20Design&logoColor=white)

### **Backend**

![Java 17](https://img.shields.io/badge/Java%2017-007396?style=for-the-badge&logo=java&logoColor=white) ![SpringBoot 3.4.4](https://img.shields.io/badge/SpringBoot%203.4.4-6DB33F?style=for-the-badge&logo=Spring%20Boot&logoColor=white) ![FastAPI 0.95.2](https://img.shields.io/badge/FastAPI%200.95.2-009688?style=for-the-badge&logo=FastAPI&logoColor=white) ![Python 3.8](https://img.shields.io/badge/Python%203.8-3776AB?style=for-the-badge&logo=Python&logoColor=white)

### **Database**

![MySQL 8.0.41](https://img.shields.io/badge/MySQL%208.0.41-4479A1?style=for-the-badge&logo=MySQL&logoColor=white) ![Redis 8.0.0](https://img.shields.io/badge/Redis%208.0.0-DC382D?style=for-the-badge&logo=Redis&logoColor=white) ![MongoDB 8.0.9](https://img.shields.io/badge/MongoDB%208.0.9-47A248?style=for-the-badge&logo=MongoDB&logoColor=white)

### **Infrastructure**

![Ubuntu 20.04](https://img.shields.io/badge/Ubuntu%2020.04-E95420?style=for-the-badge&logo=Ubuntu&logoColor=white) ![Docker 26.1.3](https://img.shields.io/badge/Docker%2026.1.3-2496ED?style=for-the-badge&logo=Docker&logoColor=white) ![Jenkins 2.504.1](https://img.shields.io/badge/Jenkins%202.504.1-D24939?style=for-the-badge&logo=Jenkins&logoColor=white) ![Nginx 1.27.5](https://img.shields.io/badge/Nginx%201.27.5-009639?style=for-the-badge&logo=NGINX&logoColor=white)

### **Monitoring**

![Prometheus 3.3.1](https://img.shields.io/badge/Prometheus%203.3.1-E6522C?style=for-the-badge&logo=Prometheus&logoColor=white) ![Loki 2.9.3](https://img.shields.io/badge/Loki%202.9.3-F7B93E?style=for-the-badge&logo=Loki&logoColor=white) ![Grafana 12.0.0](https://img.shields.io/badge/Grafana%2012.0.0-F46800?style=for-the-badge&logo=Grafana&logoColor=white) ![ELK 8.7.0](https://img.shields.io/badge/ELK%208.7.0-005571?style=for-the-badge&logo=Elastic&logoColor=white)

## 3. 주요 기능

| 기능 | 설명 | 관련 기술 |
|:---:|:---|:---|
| **프롬프트 기반 영상 컷 편집** | 사용자의 프롬프트를 분석하여 원하는 영상 구간을 자동으로 추출 | Gemini API, mmaction2 |
| **유튜브 파이프라인** | 완성된 영상을 유튜브에 자동 업로드 | YouTube API 연동 |
| **영상 공간 공유** | 완성된 영상을 지인과 공유할 수 있는 공간 제공 | 클라우드 스토리지 |
| **선택적 인물 모자이크** | 지정한 특정 인물을 제외한 나머지 인물 자동 모자이크 처리 | DeepSort,  InsightFace |
| **AI 기반 배경음악** | 프롬프트에 맞는 배경음악 자동 삽입 | MusicGen |
| **자동 자막 생성** | 편집된 영상에 자동으로 자막 삽입 | Whisper |


## 4. 시스템 아키텍처
### 이미지 넣기

## 5. 서비스 화면

### 로그인
| <img src="docs/readme/gif/login.webp" width="200"> |
|:---:|
| 로그인 |

- JWT 기반 인증 시스템 구현
- 구글 OAuth를 활용한 회원가입 지원

--- 

### 영상 편집 서비스

### 1. 프롬프트 기반 컷 편집집
| <img src="docs/readme/gif/prompt.webp" width="200"> |
|:---:|
| 프롬프트 기반 컷 편집 |

- 프롬프트에 걸맞게 컷 편집


### 1. 영상 모자이크 처리
| <img src="docs/readme/gif/moza.webp" width="200"> |<img src="docs/readme/gif/Before.webp" width="200"> |<img src="docs/readme/gif/after.webp" width="200"> |
|:-------------------------------------------------:|:---: |:---: |
|                   모자이크 처리/ 이미지                    | Before | After( 모자이크 / 자막) |

- AI 기반 자동 모자이크 처리
- 이미지 추가로 제외할 인물 선택

### 2. 영상 자막 생성
| <img src="docs/readme/gif/subtitle.webp" width="200"> |
|:---:|
| 한국어 자막 추가 |

- AI 기반 한국어 자막 생성

### 3. 영상 배경 음악 생성
| <img src="docs/readme/gif/bgm.webp" width="200"> | <img src="docs/readme/gif/prom_sub.webp" width="200"> |
|:---:|:---:|
| 자동 생성 | 프롬프트 입력 생성 |

- AI 기반 배경 음악 제작
- 영상에 어울리는 음악을 자동 선정해 삽입
- 프롬프트 입력으로 특정 음악 요구 가능

### 4. 영상 편집 알림
| <img src="docs/readme/gif/push.webp" width="200"> |
|:---:|
| 푸쉬 알림 |

- 영상 편집 비동기 처리
- 영상 편집 종료 시 fcm을 활용한 푸쉬 알림 전송

--- 

### 유튜브 쇼츠 업로드
| <img src="docs/readme/gif/auth.webp" width="200"> | <img src="docs/readme/gif/y_upload.webp" width="200"> |
|:---:|:---:|
| 업로드 권환 획득득 | 유튜브 쇼츠 업로드 |

- 제목, 설명, 태그 입력 후 업로드

--- 

### 공유 방 서비스

### 공유 방 관리
| <img src="docs/readme/gif/create_room.webp" width="200"> | <img src="docs/readme/gif/enter.webp" width="200"> |
|:---:|:---:|
| 생성 | 초대 코드로 입장 |


### 공유 방 관리
| <img src="docs/readme/gif/upload.webp" width="200"> |
|:---:|
| 영상 업로드 |


### 공유 방 관리
| <img src="docs/readme/gif/read.webp" width="200"> | <img src="docs/readme/gif/search.webp" width="200"> |
|:---:|:---:|
| 조회 | 검색 |



## 6. 개발 팀 소개

---

| <img src="https://avatars.githubusercontent.com/black4758" width="200"> | <img src="https://avatars.githubusercontent.com/HuiSeopKwak" width="200"> | <img src="https://avatars.githubusercontent.com/ssm-kim" width="200"> | <img src="https://avatars.githubusercontent.com/YDaewon" width="200">  | <img src="https://avatars.githubusercontent.com/chjw956" width="200">  | <img src="https://avatars.githubusercontent.com/pickup3415" width="200">
|---------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|
| 우성윤 ([@black4758](https://github.com/black4758)) | 곽희섭 ([@HuiSeopKwak](https://github.com/HuiSeopKwak)) | 김성민 ([@ssm-kim](https://github.com/ssm-kim)) | 양대원 ([@YDaewon](https://github.com/YDaewon)) | 최지원 ([@chjw956](https://github.com/chjw956)) | 박준현 ([@pickup3415](https://github.com/pickup3415)) | 
| Leader / Back End(AI) | Back End(AI) | Back End | Back End |  Infra | Front End |

<br />
<div id="산출물"></div>

## 📝 산출물

---

### 1. [기능 명세서](https://fluorescent-backpack-f2b.notion.site/1dc258eb697a807ea796e457461fd17e)

### 2. [와이어 프레임](https://www.figma.com/proto/Ex0tN8WhSVTxQyH5nxFyWy/D108_%EC%9E%90%EC%9C%A8?node-id=0-1&t=ioEh0D9SjDtngMMl-1)

### 3. [API 명세서](https://fluorescent-backpack-f2b.notion.site/API-1f8258eb697a80d2831bc1cd9b461d13)

### 4. [ERD](/docs/readme/erd.png)

### 5. [포팅매뉴얼](/exec/포팅메뉴얼/)

### 6. [최종발표](/exec/월간ZIP_발표.pdf)
