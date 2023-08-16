package hello.itemservice;

import hello.itemservice.config.JdbcTemplateV3Config;
import hello.itemservice.config.V2Config;
import hello.itemservice.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

//@Import(JdbcTemplateV3Config.class)
//@Import(V2Config.class)
@SpringBootTest
@Slf4j
class ItemServiceApplicationTests {

	/*
	@Profile("test")
	@Bean
	public TestDataInit testDataInit(ItemRepository itemRepository){
		return new TestDataInit(itemRepository);
	}
	 */

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

	@Test
	void contextLoads() {
	}

}
