# GX 프로그램 수강신청 시스템 (gxprogram)

GX(그룹 운동) 프로그램의 회원가입·강좌 검색·수강신청·결제를 처리하는 웹 애플리케이션입니다.
Spring Boot 기반 REST API 서버와 정적 HTML 프론트엔드(`index.html`)로 구성됩니다.

## 기술 스택

- **언어/프레임워크**: Java 25, Spring Boot 4.0.6 (Spring Web MVC)
- **빌드**: Gradle 9.4.1 (Wrapper 포함, 별도 설치 불필요)
- **데이터**: Spring Data JPA, QueryDSL 5.0.0, H2(인메모리, MySQL 모드)
- **기타**: Lombok, Bean Validation, Spring Security Crypto

## 설치 및 실행

```bash
git clone https://github.com/ewhayein/gxprogram.git
cd gxprogram
gradlew.bat bootRun
```

- 접속 주소: **http://localhost:8080** (기본 포트 8080)
- 종료: 터미널에서 `Ctrl + C`
- 최초 실행 시 의존성 다운로드 및 QueryDSL 코드 생성으로 시간이 다소 걸립니다.

## 빌드 및 JAR 실행

```bash
gradlew.bat build
java -jar build\libs\gxprogram-0.0.1-SNAPSHOT.jar
```

## 테스트

```bash
gradlew.bat test
```

별도 인메모리 H2(`testdb`)에서 실행되며, 결과 리포트는 `build\reports\tests\test\index.html`에 생성됩니다.

## 데이터베이스

기본값은 **H2 인메모리**라 별도 설치가 불필요하나, **서버 재시작 시 데이터가 초기화**됩니다.
서버 실행 중 H2 콘솔에서 데이터 확인이 가능합니다.

- 콘솔: **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:mem:fitnessdb` / User: `sa` / Password: (없음)

MySQL 전환 시, `src/main/resources/application.properties`에 주석 처리된 MySQL 설정을 활성화하고 계정 정보를 입력합니다. (MySQL 드라이버 의존성 추가 필요)

## 주요 API

| 기능 | 메서드 | 경로 |
| --- | --- | --- |
| 회원가입 | POST | `/api/members/signup` |
| 로그인 | POST | `/api/members/login` |
| 마이페이지 조회 | GET | `/api/members/{memberId}/mypage` |
| 강좌 검색 | GET | `/api/courses/search` |
| 수강 신청 (장바구니) | POST | `/api/reservations` |
| 결제 신청 (결제 대기 전환) | POST | `/api/reservations/request-payment` |
| 결제 내역 조회 | GET | `/api/payments/summary/{memberId}` |
| 결제 처리 | POST | `/api/payments/{memberId}` |

## 프로젝트 구조

```
src/main/java/_5/gxprogram/
├─ GxprogramApplication.java  # 메인 실행 클래스
├─ config/        # 비밀번호 인코더, QueryDSL 설정
├─ controller/    # REST 컨트롤러 (회원/강좌/예약/결제)
├─ domain/        # 엔티티
├─ dto/           # 요청/응답 DTO
├─ exception/     # 전역 예외 처리
├─ repository/    # JPA / QueryDSL 리포지토리
└─ service/       # 비즈니스 로직
src/main/resources/
├─ application.properties     # DB·JPA 설정
└─ static/index.html          # 프론트엔드 화면
```