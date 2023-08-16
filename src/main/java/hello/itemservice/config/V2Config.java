package hello.itemservice.config;

import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.jpa.JpaItemRepositoryV3;
import hello.itemservice.repository.v2.ItemQueryRepositoryV2;
import hello.itemservice.repository.v2.ItemRepositoryV2;
import hello.itemservice.service.ItemService;
import hello.itemservice.service.ItemServiceV2;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;

@Configuration
@RequiredArgsConstructor
public class V2Config {

    private final EntityManager entityManager;
    private final ItemRepositoryV2 itemRepository;

    @Bean
    public ItemService itemService() {
        return new ItemServiceV2(itemRepository, itemQueryRepository());
    }

    //ItemService를 만들기 위해서 2개의 ItemRepository가 필요함.
    @Bean
    public ItemQueryRepositoryV2 itemQueryRepository() {
        return new ItemQueryRepositoryV2(entityManager);
    }

    @Bean
    public ItemRepository itemRepository() {
        return new JpaItemRepositoryV3(entityManager);
    }

}
