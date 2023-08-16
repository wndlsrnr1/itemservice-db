package hello.itemservice.domain;

import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import hello.itemservice.repository.memory.MemoryItemRepository;
import hello.itemservice.repository.mybatis.ItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
/*
일반 -> 로직 성공시에 커밋 아니면 롤백
테스트 상황 ->
테스트를 트랜잭션 안에서 실행하다? 트랜잭션 과정내에서 실행하다.
그리고 트랜잭션 후에 롤백한다.

'트랜잭션은 전파된다'? 그렇기때문에 JdbcTemplate도 같은 트랜잭션을 사용한다? -> 씨발련이 또 뒤에서 설명한다 그럼ㅋㅋ
아무튼 테스트가 실행하는 모든 코드는 같은 트랜잭션 범위에 들어간다.
== 같은 트랜잭션을 사용한다는 것은 같은 Connection을 이용하는 것이다. == 같은 세션

롤백은 Connection을 잃으면 DB에서 없애줌.
 */

//데이터를 저장해서 결과를 보고 싶을 때
//@Commit
//@Rollback(value = false)

/* ImbededMode
 테스트 데이터는 삭제되도됨. Java기반이라서 같은 자바 기반 JVM에 같이 올릴 수 있고 그래서 내장 DB처럼 사용 가능.
 해당 어플리케이션 종료시 DB어플리케이션도 종료, 메모리 DB이므로 데이터도 삭제됨.
 */

/*
Mybatis에서 H2 Database를 먼저 실행해야 한다.
 */
@Transactional
@Slf4j
class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepository;


    /*
    트랜잭션 관련 코드
    @Autowired
    PlatformTransactionManager transactionManager;
    TransactionStatus status;


    @BeforeEach
    void beforeEach() {
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
    }

    @AfterEach
    void afterEach() {
        if (itemRepository instanceof MemoryItemRepository) {
            ((MemoryItemRepository) itemRepository).clearStore();
        }

        transactionManager.rollback(status);
    }
     */

    @AfterEach
    void afterEach() {
        if (itemRepository instanceof MemoryItemRepository) {
            ((MemoryItemRepository) itemRepository).clearStore();
        }
    }

    @Test
    void save() {
        //given
        Item item = new Item("itemA", 10000, 10);

        //when
        Item savedItem = itemRepository.save(item);

        //then
        Item findItem = itemRepository.findById(item.getId()).get();
        assertThat(findItem).isEqualTo(savedItem);
    }

    @Test
    void updateItem() {
        //given
        Item item = new Item("item1", 10000, 10);
        Item savedItem = itemRepository.save(item);
        Long itemId = savedItem.getId();

        //when
        ItemUpdateDto updateParam = new ItemUpdateDto("item2", 20000, 30);
        itemRepository.update(itemId, updateParam);

        //then
        Item findItem = itemRepository.findById(itemId).get();
        assertThat(findItem.getItemName()).isEqualTo(updateParam.getItemName());
        assertThat(findItem.getPrice()).isEqualTo(updateParam.getPrice());
        assertThat(findItem.getQuantity()).isEqualTo(updateParam.getQuantity());
    }

    //개발환경과 테스트 환경을 분리 하지 않아서 오류가 생김

    @Test
    void findItems() {
        //given
        Item item1 = new Item("itemA-1", 10000, 10);
        Item item2 = new Item("itemA-2", 20000, 20);
        Item item3 = new Item("itemB-1", 30000, 30);

        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);

        //둘 다 없음 검증
        test(null, null, item1, item2, item3);
        test("", null, item1, item2, item3);

        //itemName 검증
        test("itemA", null, item1, item2);
        test("temA", null, item1, item2);
        test("itemB", null, item3);

        //maxPrice 검증
        test(null, 10000, item1);

        //둘 다 있음 검증
        test("itemA", 10000, item1);
    }

    @Test
    void testForeachMybatis() {
        Item item1 = new Item("item1", 10000, 10);
        Item item2 = new Item("item2", 10000, 10);
        Item item3 = new Item("item3", 10000, 10);

        Item save1 = itemRepository.save(item1);
        Item save2 = itemRepository.save(item2);
        Item save3 = itemRepository.save(item3);

        List<Long> idList = new ArrayList<>();
        idList.add(save1.getId());
        idList.add(save2.getId());
        idList.add(save3.getId());
    }

    void test(String itemName, Integer maxPrice, Item... items) {
        List<Item> result = itemRepository.findAll(new ItemSearchCond(itemName, maxPrice));
        assertThat(result).containsExactly(items);
    }


}
