# KBuddy-Server

K-Buddy의 API 서버 입니다.

## 📌 프로젝트 소개
K-Buddy는 **한국에서 살아가는 외국인들의 정보 비대칭을 해결하기 위한 서비스**입니다.

## 🚀 주요 기능
- **멘토링 기능[진행 중]**: 외국인의 한국 적응을 도와줄 수 있는 멘토의 유료 상담 서비스
- **블로그 기능[완료]**: 한국 경험에 대해 공유하는 게시판
- **Q&A 커뮤니티[완료]**: 질의응답을 주고 받는 게시판
- **소셜 로그인[완료]**: Google, Kakao 계정을 활용한 간편 로그인 지원 -> refresh token은 Redis에 저장할 예정
- **북마크 기능[완료]**: 관심 있는 콘텐츠 저장 가능
- **결제 기능[예정]**: 멘토링 서비스 결제
- **정산 시스템[예정]**: 비즈니스 사용자인 멘토에게 상담 비용을 정산하는 시스템

## 🛠️ 기술 스택
- **Frontend**: React.js
- **Backend**: Spring Boot, Spring Security, Spring Data JPA, QueryDSL
- **Database**: PostgreSQL, Redis
- **Hosting**: AWS EC2, OCI
- **CI/CD**: GitHub Action, Jenkins
- **Monitoring**: Prometheus, Grafana

## 배포 환경
1. Spring Server : AWS EC2 프리티어
2. Monitoring Server(프로메테우스, 그라파나) : 오라클 프리티어
3. PostgreSQL : 오라클 프리티어

[서버 주소]
https://api.k-buddy.kr:8080

[서버 대시보드]
http://129.154.54.44:8081

## API 문서
https://api.k-buddy.kr/swagger-ui/index.html


## 개발 환경 설정

<details>
<summary><b>Code Convention XML 파일 IDE에 적용하는 방법</b></summary>
<div markdown="1">

![setting_1.png](.github%2Fsetting_1.png)

`Settings -> Editor -> Code Style -> Java` 를 선택합니다.

![setting_2.png](.github%2Fsetting_2.png)

탭 상단의 설정 버튼을 누르고 `Import Scheme -> IntelliJ IDEA code style XML` 을 선택한 후

![setting_3.png](.github%2Fsetting_3.png)

K-Buddy_Backend 프로젝트의 `rule-config` 폴더에 있는 `naver-intellij-formatter.xml` 파일을 선택합니다.


![setting_4.png](.github%2Fsetting_4.png)

XML 파일을 IDE가 성공적으로 읽었다면 위와 같은 알림이 나타나며, Apply 버튼을 눌러 설정을 적용합니다.

</div>
</details>

## Code Convention
- 개발 환경 세팅 시 JAVA 17 버전이 필수로 설치되어 있어야 합니다.
- 코드 컨벤션은 [NAVER Intellij 코딩 컨벤션](https://github.com/naver/hackday-conventions-java/blob/master/rule-config/naver-intellij-formatter.xml)을 따릅니다.
> Formatting 적용 방법
> ```bash
> # Windows
> $ CTRL + ALT + L
> 
> # MacOS
> $ Option + Command + L
> ```

## 📌 개발 방법
1. 이 저장소를 Fork 합니다.
2. 새로운 브랜치를 생성합니다. (`git checkout -b feature-branch`)
3. 변경 사항을 커밋합니다. (`git commit -m 'Add new feature'`)
4. 원격 저장소에 푸시합니다. (`git push origin feature-branch`)
5. Pull Request를 생성합니다.
