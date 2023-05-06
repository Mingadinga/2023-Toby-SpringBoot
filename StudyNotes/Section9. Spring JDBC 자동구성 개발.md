## 자동 구성 클래스와 빈 설계

![스크린샷 2023-05-05 오전 11.26.06.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/6e59216c-f368-42ff-8ad9-e453ccac9c8e/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2023-05-05_%E1%84%8B%E1%85%A9%E1%84%8C%E1%85%A5%E1%86%AB_11.26.06.png)

인프라 스트럭처 빈

- 빈 설계 시 생각해야할 점 : 어떤 조건에서 등록되는가. 라이브러리에 포함되는가, dependency로 잡혀있는가.
- DataSourceConfig : jdbc를 사용할 때 필요한 인프라 스트럭처 빈의 정보를 갖고 있는 설정 파일. 모든 jdbc 기술은 DataSource라는 DB와의 커넥션을 연결해주는 자바의 표준 기술. JdbcOperations 타입 빈이 존재하면 생성.
- DataSource 타입 빈 : DB 커넥션 연결. 런타입에 주입하는 구현체는 SimpleDriverDataSource or HikariDataSource(Hikari 존재 여부에 따라 다르게 등록), 설정값은 DataSourceProperties로 등록한다. 인 메모리 db인 h2를 사용한다.
- JdbcTemplate : Jdbc의 일관적인 API를 제공하는 유틸. DataSource에 의존함. 자동구성 등록 대상
- JdbcTransactionManager : 트랜잭션 기술을 추상화하는 API. DataSource에 의존함. 자동구성 등록 대상
- 하나의 자바 프로그램 안에 여러개의 DataSource 빈이 등록될 수 있다. 어떤 빈을 사용해 JdbcTemplate나 JdbcTransactionManager를 만들어야할지 선택해야한다. 자동구성에서는 DataSource 빈이 하나만 등록되어있을 때 두 빈을 만든다.

## DataSource 자동 구성 클래스

DataSource에 해당하는 빈으로 `SimpleDriverDataSource`, `HikariDataSource`를 등록한다. 어떤 작업을 해야하는지 순서를 정리해보자.

- DataSourceConfig 컨피그 클래스 작성하고, imports 파일에 풀 패키지 경로 추가해 빈 등록 후보로 추가
- DataSourceConfig 조건 등록 : JdbcOperations 타입이 패키지에 존재함
- `SimpleDriverDataSource` 빈 등록, 조건 추가 : JdbcOperations 타입이 패키지에 존재함
- DataSource가 사용하는 외부 설정정보 `MyDataSourceProperties` 추가하고 빈 등록, `SimpleDriverDataSource` 빈 등록 부분에 매개변수 주입
- [application.properties](http://application.properties) 파일에 설정값 작성
- `HikariDataSource` 빈 등록, 조건 추가 : DataSource 타입 빈이 등록되지 않음

```java
@MyAutoConfiguration
@ConditionalMyOnClass("org.springframework.jdbc.core.JdbcOperations")
@EnableMyConfigurationProperties(MyDataSourceProperties.class)
public class DataSourceConfig {
    @Bean
    @ConditionalMyOnClass("org.springframework.jdbc.core.JdbcOperations") // 클래스 존재 유무 확인
    DataSource dataSource(MyDataSourceProperties properties) throws ClassNotFoundException {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass((Class<? extends Driver>) Class.forName(properties.getDriverClassName()));
        dataSource.setUrl(properties.getUrl());
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());
        return dataSource;
    }

    @Bean
    @ConditionalOnMissingBean // 위에서 만들어지지 않은 경우 등록
    DataSource hikariDataSource(MyDataSourceProperties properties) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(properties.getDriverClassName());
        dataSource.setJdbcUrl(properties.getUrl());
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());
        return dataSource;
    }
}

@MyConfigurationProperties(prefix = "data")
public class MyDataSourceProperties {

    private String driverClassName;
    private String url;
    private String username;
    private String password;

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}

// application.properties
data.driver-class-name=org.h2.Driver
data.url=jdbc:h2:mem:test
data.username=sa
data.password=
```

자동구성을 마쳤으니 스프링 컨테이너를 사용하는 테스트를 작성하여 커넥션을 잘 얻어오는지 확인한다. 통과하면 성공.

```java
@ExtendWith(SpringExtension.class) // 스프링 컨테이너 사용
@ContextConfiguration(classes = HellobootApplication.class) // 빈 로딩 정보
@TestPropertySource("classpath:/application.properties") // 테스트에서의 바인딩 작업 (보통 빈 생성이 끝난 후에 일어남)
public class DataSourceTest {
    @Autowired
    DataSource dataSource;

    @Test
    void connect() throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.close();
    }

}
```

## JdbcTemplate과 트랜잭션 매니저 구성

JdbcTemplate과 JdbcTransactionManager를 빈으로 등록해보자. 이 두 빈은 DataSource에 의존하며, DataSource 타입 빈이 하나만 등록되었을 때 동작하도록 한다.

```java
@MyAutoConfiguration
@ConditionalMyOnClass("org.springframework.jdbc.core.JdbcOperations")
@EnableMyConfigurationProperties(MyDataSourceProperties.class)
@EnableTransactionManagement // aop 관련 구성정보 import
public class DataSourceConfig {
		// ..

    @Bean
    @ConditionalOnSingleCandidate(DataSource.class)
    @ConditionalOnMissingBean
    JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @ConditionalOnSingleCandidate(DataSource.class)
    @ConditionalOnMissingBean
    JdbcTransactionManager jdbcTransactionManager(DataSource dataSource) {
        return new JdbcTransactionManager(dataSource);
    }
}
```

JdbcTemplate api를 사용하는 테스트를 돌려본다. @HelloBootTest에 스프링 환경에서 테스트를 돌리기 위한 애노테이션을 붙였다.

```java
@HelloBootTest
public class jdbcTemplateTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void init() {
        jdbcTemplate.execute("create table if not exists hello(name varchar(50) primary key, count int)");
    }

    @Test
    void insertAndQuery() {
        Long count = jdbcTemplate.queryForObject("select count(*) from hello", Long.class);
        assertThat(count).isEqualTo(0);

        jdbcTemplate.update("insert into hello values(?, ?)", "Toby", 3);
        jdbcTemplate.update("insert into hello values(?, ?)", "Spring", 1);

        count = jdbcTemplate.queryForObject("select count(*) from hello", Long.class);
        assertThat(count).isEqualTo(2);
    }

}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(SpringExtension.class) // 스프링 컨테이너 사용
@ContextConfiguration(classes = HellobootApplication.class) // 빈 로딩 정보
@TestPropertySource("classpath:/application.properties") // 테스트에서의 바인딩 작업 (보통 빈 생성이 끝난 후에 일어남)
@Transactional
public @interface HelloBootTest {
}
```

## Hello 레포지토리

```java
public interface HelloRepository {
    Hello findHello(String name);

    void increaseCount(String name);

    default int countOf(String name){
        Hello hello = findHello(name);
        return hello == null ? 0 : hello.getCount();
    }
}

@Repository // 컴포넌트 등록
public class HelloRepositoryJdbc implements HelloRepository {

    private final JdbcTemplate jdbcTemplate;

    public HelloRepositoryJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Hello findHello(String name) {
        try {
            return jdbcTemplate.queryForObject("select * from hello where name = '" + name + "'",
                    (rs, rowNum) -> new Hello(
                            rs.getString("name"), rs.getInt("count")
                    ));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void increaseCount(String name) {
        Hello hello = findHello(name);
        if(hello == null) jdbcTemplate.update("insert into hello values(?,?)", name, 1);
        else jdbcTemplate.update("update hello set count = ? where name = ?", hello.getCount() + 1, name);
    }
}
```

## 레포지토리를 사용하는 HelloService

```java
public interface HelloService {
    String sayHello(String name);

    default int countOf(String name) {
        return 0;
    }
}

@Service
public class SimpleHelloService implements HelloService {

    private final HelloRepository helloRepository;

    public SimpleHelloService(HelloRepository helloRepository) {
        this.helloRepository = helloRepository;
    }

    @Override
    public String sayHello(String name) {
        this.helloRepository.increaseCount(name);
        return "Hello " + name;
    }

    @Override
    public int countOf(String name) {
        return helloRepository.countOf(name);
    }
}

@RestController
public class HelloController {
		// ..

    @GetMapping("/count")
    public String count(String name) {
        return name + helloService.countOf(name);
    }
}

```

HelloApplication이 실행될 때 hello 테이블이 만들어지도록 PostConstruct 메소드를 작성해 라이프사이클의 빈 생성 후 단계에서 실행한다.

```java
@MySpringBootApplication
public class HellobootApplication {

	private final JdbcTemplate jdbcTemplate;

	public HellobootApplication(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@PostConstruct
	void init() {
		System.out.println(jdbcTemplate);
		jdbcTemplate.execute("create table if not exists hello(name varchar(50) primary key, count int)");
	}

	public static void main(String[] args) {
//		MySpringApplication.run(HellobootApplication.class, args);
		SpringApplication.run(HellobootApplication.class, args);
	}

}
```