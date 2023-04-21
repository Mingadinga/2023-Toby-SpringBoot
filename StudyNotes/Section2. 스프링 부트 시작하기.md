# SDKMAN

- SDK 버전 관리 도구
- 설치 : `curl -s "[https://get.sdkman.io](https://get.sdkman.io/)" | bash`
- 설치된 자바 jdk 버전 확인 : `sdk list`
- 자바 버전 설치 : `sdk install java 자바sdk아이디`
- 특정 프로젝트의 자바 jdk 확인 : `cd 프로젝트 && java -version`
- 특정 프로젝트의 자바 jdk 변경 :  `cd 프로젝트 && sdk use java 자바sdk아이디`

# Hello Controller와 테스트

스프링 부트 프로젝트를 만들고 Hello Controller을 작성했다.

```java
@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello(String name) {
        return "Hello " + name;
    }
}
```

intellij에서 http 테스트를 작성할 수 있다. 자세한 내용은 이 [블로그 링크](https://sihyung92.oopy.io/etc/intellij/2)를 참고했다.

```
### 1. hello name
GET {{localhost}}:{{port}}/hello?name=Spring
```

```
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 12
Date: Fri, 21 Apr 2023 01:08:16 GMT
Keep-Alive: timeout=60
Connection: keep-alive

Hello Spring

Response code: 200; 
Time: 28ms (28 ms); 
Content length: 12 bytes (12 B)
```

터미널에서 http 요청을 사용하면 더 자세한 정보를 확인할 수 있다.

```
> http -v ":8080/hello?name=Spring"

### 요청
GET /hello?name=Spring HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Host: localhost:8080
User-Agent: HTTPie/3.2.1

### 응답
HTTP/1.1 200 
Connection: keep-alive
Content-Length: 12
Content-Type: text/plain;charset=UTF-8
Date: Fri, 21 Apr 2023 01:03:24 GMT
Keep-Alive: timeout=60

Hello Spring
```

# HTTP 요청과 응답

웹을 통한 요청은 항상 응답과 쌍을 맺는다. 

요청과 응답을 어떻게 보내야하는지 정해놓은 표준이 HTTP이다. 

요청과 응답의 구조에서 이 내용은 확인하고 이해할 수 있어야 한다.

Request

- Request Line : Method, Path, HTTP Version
- Headers
- Message Body

Response

- Status Line : HTTP Version, Status Code, Status Text
- Headers
- Message Body

```
> http -v ":8080/hello?name=Spring"

### 요청
GET /hello?name=Spring HTTP/1.1   # Request Line
Accept: */*   # Header
Accept-Encoding: gzip, deflate   # Header - 원하는 미디어 타입 및 우선순위
Connection: keep-alive   # Header - 메시지 교환 후 TCP 연결 종료 여부
Host: localhost:8080   # Header - 요청하는 쪽의 호스트와 포트번호
User-Agent: HTTPie/3.2.1   # Header - 요청하는 쪽의 sw 정보

### 응답
HTTP/1.1 200   # Request Line
Connection: keep-alive   # Header
Content-Length: 12   # Header - 메시지 바디의 길이
Content-Type: text/plain;charset=UTF-8   # Header - 응답 내용의 타입과 문자 포맷
Date: Fri, 21 Apr 2023 01:03:24 GMT   # Header - 응답 메시지가 생성된 시간
Keep-Alive: timeout=60   # Header

Hello Spring   # Message Body
```