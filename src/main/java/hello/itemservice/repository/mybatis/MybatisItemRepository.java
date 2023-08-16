package hello.itemservice.repository.mybatis;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//이 클래스는 ItemMapper에 기능을 단순히 위임함.

/*
프록시로 생성된 매퍼 구현체는 MyBatis에서 발생한 예외를 스프링 예외 추상화인 DataAccessException에 맞게 변환해서 반환해준다.
JdbcTemplate이 제공하는 기능을 여기에서도 제공함.
DBConnection, Trasaction 관련 기능도 연동, 동기화 해준다. -> 예가 어떤 것이 있음.
마이바티스 프링 연동 모듈이 자동으로 등록해주는 부분 -> MybatisAutoConfiguration 클래스 참고
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MybatisItemRepository implements ItemRepository {

    private final ItemMapper itemMapper;

    @Override
    public Item save(Item item) {
        //동적 프록시 기술이 적용 되어 있는지 확인 하기
        //itemMapper class=class com.sun.proxy.$Proxy69
        log.info("itemMapper class={}", itemMapper.getClass());
        itemMapper.save(item);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        itemMapper.update(itemId, updateParam);
    }

    @Override
    public Optional<Item> findById(Long id) {
        return itemMapper.findById(id);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        return itemMapper.findAll(cond);
    }

}
