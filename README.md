# SPRING PLUS

## 목차

1. 프로젝트 소개
2. 주요 기능
3. 기술 스택
4. 리팩토링
5. 트러블슈팅

## 프로젝트 소개

해당 프로젝트는 이미 작성된 코드의 문제 사항을 인식하고 리팩토링을 진행하여 코드가 의도한대로 작동하게끔 만들며, 추가적인 기능을 구현하여 요구사항을 충족하는 것을 목표로 한다.

---

## 기술 스택

### 언어
JAVA 17

### 프레임워크
Spring Boot 3.3.3

### 빌드 도구
Gradle (Groovy)

### Version Control
Git / GitHub

### DBMS
MySQL 8.x

### 추가 라이브러리
- jjwt-api:0.11.5
- jjwt-impl:0.11.5
- jjwt-jackson:0.11.5
- favre.lib.bcrypt:0.10.2

---

## 리팩토링

### Level 1

    1. @Transactional의 이해
    
        - **문제**: TodoService.saveTodo 메서드에서 저장 실패
        - **원인**: TodoService 클래스에 @Transactional(readOnly = true)가 적용되어 있음 적용한 어노테이션이 하위 메서드들에 일괄 적용되어 저장이 불가
        - **해결**: 해당 메서드에 @Transactional 어노테이션 적용
    
    
    2. JWT의 이해
    
        - **요구 사항**: Jwt에 nickname 속성 추가
        - **반영**: User에 nickname 필드 추가, Jwt 속성에 nickname 추가, User 생성 시 nickname 필드 작성하도록 수정
    
    
    3. JPA의 이해
    
        - **요구 사항**: 기존의 getTodos에 검색 조건 추가
        - **반영**: JPQL로 동적 조건 검색을 만족하도록 수정
    
    
    4. 컨트롤러 테스트의 이해
    
        - **문제**: TodoController 테스트 중 하나 실패
        - **원인**: 해당 테스트의 의도대로면 404 에러를 반환하는데 검증을 200으로 하고 있었음
        - **해결**: 반환되는 상태값을 404로 변경
    
    
    5. AOP의 이해
    
        - **문제**: AdminAccessLoggingAspect가 의도대로 작동하지 않음(메서드 실행 전 이 아니라 후에 실행되고 있음)
        - **원인**: @After로 작성되어 메서드 실행 후에 작동하도록 설정되어 있음, 설정된 메서드도 다름
        - **해결**: @Before로 변경해 이전에 실행하도록 설정, 의도한 메서드에서 실행되도록 설정

### Level 2

    6. JPA CASCADE
    
        - **문제**: todo 저장 시 manager가 저장되지 않음
        - **원인**: Cascade 설정이 없었음
        - **해결**: CascadeType.PERSIST 및 REMOVE 적용
    
    
    7. N+1
    
       - **문제**: CommentService.getComments에서 N+1 문제 발생
       - **원인**: JPQL에서 JOIN이 있지만 FETCH JOIN이 아니라 lazy loading으로 N+1 발생
       - **해결**: FETCH JOIN으로 변경해서 user까지 로딩되도록 수정
    
    
    8. QueryDSL
    
        - **요구 사항**: 기존의 JPQL로 작성된 findByIdWithUser를 QueryDSL로 변경
        - **반영**: QueryDSL 적용
    
    
    9. Spring Security
    
        - **요구 사항**: Spring Security 적용
        - **반영**: 기존의 argumentResolver 및 WebFilter을 삭제하고 Spring Security 적용

### Level 3

    10. QueryDSL로 동적 검색 기능 구현

        - **요구 사항**: 검색 키워드로 제목 검색(부분 일치), 일정 생성일 범위 검색, 담당자 닉네임 검색(부분 일치), 페이징 적용
        - **구현**
            - Projections로 Dto로 바로 반환하도록 구현
            - 검색 조건 일부가 null이어도 되도록 구현

    11. Transaction 심화
        
        - **요구 사항**: 매니저 등록 시 로그를 기록, 로그는 DB에 저장, 매니저 등록 성공 여부와 관계없이 로그 저장
        - **구현**
            - AOP 및 어노테이션을 통해 로그를 기록하는 기능 구현
            - @Transactional(propagation = Propagation.REQUIRES_NEW)를 적용해 매니저 등록 중 롤백되어도 로그를 저장하도록 구현

    12. 실시간 채팅 구현

        - **요구 사항**: 익명으로 채팅이 가능한 서비스 구현
        - **설계 사항**
            - 익명 채팅으로 사용자 정보를 노출하지 않고 ANON_as12d처럼 사용자명을 명시
            - 채팅 저장 기능 및 채팅방을 나누는 기능은 포함하지 않음
        - **구현**
            - WebSocket 및 STOMP로 채팅 기능 구현
            - interceptor로 익명 이름 설정해 웹소켓 세션에 저장, 메시지에 익명 이름이 포함되도록 구현
            - DTO를 전송하도록 구현

## 트러블슈팅

### AOP 테스트 문제

- **문제**: Controller 슬라이스 테스트에서 AOP가 작동하지 않음
- **원인**: SpringBootTest가 아니기 때문에 모든 컨텍스트를 로딩하지 않음 따라서, AOP 관련 설정도 로딩되지 않음 
- **해결**: @EnableAspectJAutoProxy를 추가하여 aop 설정 활성화

### N+1 테스트에서 발생하지 않음

- **문제**: commentServiceIntegrationTest에서 의도적으로 N+1을 발생시키려고 했으나 발생하지 않음
- **원인**: 테스트에 필요한 user, todo, comment를 DB에 저장하고 영속성 컨텍스트를 초기화하지 않아 1차 캐시에 남아있었음
- **해결**: 영속성 컨텍스트를 초기화하여 1차 캐시에 저장된 정보를 초기화하여 해결

### 로그 저장 테스트에서 테스트 실패

- **문제**: 각각의 테스트를 실행하면 문제없이 작동하지만 전체 테스트를 작동하면 두 테스트 중 하나가 실패
- **원인**: findById(1)으로 저장된 로그를 불러오는데 작성된 순서대로 작동하는 것이 아니라 뒤의 테스트가 먼저 작동해서 잘못된 데이터를 보고 있었음
- **해결**: 각 저장된 로그 객체를 참조하도록 수정