package hello.itemservice;

import hello.itemservice.config.*;
import hello.itemservice.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;


//설정파일로 사용하는 annotation
//@Import(MemoryConfig.class)
//JdbcTemplate를 사용하는 설정으로 변경
//이런 방식으로 설정을 선택적으로 사용함.
//@Import(JdbcTemplateV1Config.class)
//@Import(JdbcTemplateV2Config.class)
//@Import(JdbcTemplateV3Config.class)
//@Import(MybatisConfig.class)
//@Import(JpaConfig.class)
//@Import(SpringDataJpaConfig.class)
//@Import(QuerydslConfig.class)
@Import(V2Config.class)
@Slf4j
@SpringBootApplication(scanBasePackages = "hello.itemservice.web")
public class ItemServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItemServiceApplication.class, args);
	}

	@Bean
	//특정 경우의 프로필인 경우 스프링 빈을 등록.
	//spring.profiles.active in application.property
	@Profile("local")
	public TestDataInit testDataInit(ItemRepository itemRepository) {
		return new TestDataInit(itemRepository);
	}

	//테스트케이스 이어도 스프링을 띄우면 사용하게 되므로
	@Bean
	@Profile("test")
	public DataSource dataSource() {
		log.info("메모리 데이터베이스 초기화");
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.h2.Driver");
		//jdbc:h2:mem:db h2를 임베디드 모드로 사용 할 수 있게 함.
		//DB_CLOSE_DELAY=-1 DBConnection이 모두 끊어져도 DB가 종료되지 않게함. (Imbeded모드에서는 종료가 기본)
		dataSource.setUrl("jdbc:h2:mem:db;DB_CLOSE_DELAY=-1");
		dataSource.setUsername("sa");
		dataSource.setPassword("");
		return dataSource;
	}
}
