# Containerless

컨테이너리스를 지향한다는 것은 서블릿 컨테이너와 관련된 복잡한 설정과 지식을 몰라도 배포를 할 수 있다는 말이다. 개발자는 스프링 컨테이너에 올라가는 빈의 개발만 신경쓰면 된다. Standalone으로 main 메소드를 동작하는 방식으로 스프링 애플리케이션을 동작시킬 수 있다.

![](Images/image.png)

이 main 메소드를 실행하는 것만으로 컨테이너와 관련된 작업, 스프링이 구동되는데 필요한 모든 작업이 수행되었다. 우리가 작성한 컨트롤러가 컨테이너에 빈으로 등록되는 것까지 완료되었다. 개발자는 정말 컨테이너에 올라가는 애플리케이션 빈만 신경써주면 되는 것이다.

이렇게 컨테이너가 없는 것처럼 개발하는 방법을 Containerless라고 한다. (실제로는 컨테이너가 존재하는데 스프링 부트가 관련 작업을 다 해줘서 개발자가 신경쓸 필요가 없는 것이다) 이 섹션에서는 스프링 부트의 도움 없이 standalone으로 동작하도록 만들어본다.

그래서 서블릿 컨테이너 대신 standalone 프로그램에서 서블릿 컨테이너를 띄워주도록 만들고, 서블릿 컨테이너를 신경쓰지 않도록 만들어보는 작업을 한다.

# 서블릿 컨테이너 띄우기

빈 서블릿 컨테이너를 하나 띄워보자. 톰캣을 설치 없이 main 메소드로 한번 띄워보자.

톰캣도 자바로 만들어진 프로그램이다. 톰캣은 서버 머신에서 돌아가도록 만들어진 자바 웹 서버 프로그램인데, 그중 내장 톰캣은 웹 애플리케이션이 코드 내부에 톰캣을 포함시켜 사용하는 것을 말한다. 사용자가 톰캣 서버를 별도로 설치하거나 설정할 필요 없이 간단한 명령어를 통해 웹 애플리케이션을 실행할 수 있다. 스프링 부트도 내장 톰캣을 사용한다. 만약 스프링 이니셜라이저로 스프링부트 프로젝트를 만들었다면 내장 톰캣이 이미 포함된다.

```java
public class HellobootApplication {

	public static void main(String[] args) {
		ServletWebServerFactory serverFactory = new TomcatServletWebServerFactory();
		WebServer webServer = serverFactory.getWebServer();
		webServer.start();
	}

}
```

스프링 부트가 톰캣 서블릿 컨테이너를 내장해서 쉽게 구동할 수 있도록 도와주는 도우미 클래스를 사용했다. 원래 new Tomcat().start()로 스프링 부트의 도움 없이 내장 톰캣을 실행할 수 있는데, 설정해줘야하는 정보가 많아서 스프링 부트의 도움을 받아 내장 톰캣을 간단하게 띄웠다. 스프링 부트는 웹 서버 종류를 추상화하는 타입을 지원한다. 위에서는 톰캣을 사용했지만, 제티나 엔진엑스 등 다른 웹 서버를 사용하고 싶다면 의존성을 변경해주면 된다.

```java
public class HellobootApplication {

	public static void main(String[] args) {
		ServletWebServerFactory serverFactory = new JettyServletWebServerFactory();
		WebServer webServer = serverFactory.getWebServer();
		webServer.start();
	}

}
```

# 서블릿 등록

서블릿 컨테이너 안에 들어가는 웹 컴포넌트인 서블릿을 만들어 등록해보자. 서블릿 컨테이너는 클라이언트의 요청(http)을 받아 매핑 과정을 받아 요청을 처리할 서블릿을 결정한다. 서블릿이 웹 응답을 만들기 위해 필요한 작업을 수행하면 서블릿 컨테이너가 http 응답을 만들어 클라이언트에 반환한다.