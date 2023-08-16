package hello.itemservice.config;

import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.jpa.JpaItemRepositoryV1;
import hello.itemservice.service.ItemService;
import hello.itemservice.service.ItemServiceV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;

//Import 하지 않으면 스프링 빈으로 등록 되지 않음.
@Configuration
public class JpaConfig {

    //JpaConfig가 생성되지 않으므로 주입 받지 못 함.
    private final EntityManager em;

    public JpaConfig(EntityManager em) {
        this.em = em;
    }

    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(ItemRepository());
    }

    @Bean
    public ItemRepository ItemRepository() {
        return new JpaItemRepositoryV1(em);
    }
}
