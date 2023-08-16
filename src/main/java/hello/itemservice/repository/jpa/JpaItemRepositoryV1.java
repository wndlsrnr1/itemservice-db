package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
/*
JPA 설정시 [EntityManagerFactory], [JpaTransactionManager] 설정해줌.
main 메서드부터 시작해서 JPA 설정하는 방법 지 강의 보라고함.
스프링 부트 기본 설정 [JpaBaseConfiguration]
 */

@Slf4j

/*
Repository Anotation
JPA예외를 [스프링 예외 추상화] DataAccessException으로 변환
컴포넌트 스캔의 대상이 된다.
AOP예외 변환 AOP의 적용 대상이 된다.
[서비스 계층]       -      [AOP Proxy]          -      [Repository Class] - [Entity Manager]
[DataAccessException]   PersistenceException         PersistenceException PersistenceException 발생
                        DataAccessException
 */
@Repository
//JPA의 모든 데이터 변경(등록, 수정, 삭제)는 트랜잭션 안에서 이루어져야 한다.
//조회는 트랜잭션이 없어도 가능하다. (OS 참조)
//일반적으로 서비스 계층에서 Transaction을 시작한다, 하지만 복잡한 비즈니즈 로직이 없어서 서비스계층에 걸지 않음.
//JPA에서는 데이터 변경시 트랜잭션 필수, 원래는 서비스 계층에 트랜잭션을 걸어 주는 것이 맞음.
@Transactional
public class JpaItemRepositoryV1 implements ItemRepository {

    //스프링을 통해서 EntityManager를 주입 받는다.
    //JPA의 모든 동작은 EntityManager를 통해서 이루어진다.
    //내부에 DataSource 가지고 있음.
    //순수한 JPA 기술
    private final EntityManager em;

    public JpaItemRepositoryV1(EntityManager em) {
        this.em = em;
    }

    /*
    insert into item (id, item_name, price, quantity) values (null, ?, ?, ?) 또는
    insert into item (id, item_name, price, quantity) values (default, ?, ?, ?) 또는
    insert into item (item_name, price, quantity) values (?, ?, ?)
    id 필드에 값이 빠져 있음.
     */
    @Override
    public Item save(Item item) {
        em.persist(item);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = em.find(Item.class, itemId);

        //여기 왜 set하는 부분이 있는 건지?
        /*
        트랜잭션이 커밋 되는 시점에 변경된 Entity 객체가 있는 지 확인 -> 변경 된 경우 -> execute update sql
        [영속성 컨텍스트]
        커밋 시점에 update sql을 시행하는데, 테스트에서는 commit하지 않으므로 update sql이 실행 되지 않음.
        @Commit Annotation을 이용해서 확인 가능.
         */
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = em.find(Item.class, id);
        return Optional.ofNullable(item);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        /*
        JPQL(Java Persistence Query Language) 객체지원 쿼리 언어
        - 여러 데이터를 복잡한 조건으로 조회할 때 사용
        - JPQL 앤티티 객체 대상으로 SQL 실행 (기존 SQL은 테이블을 대상으로 함)
        - from 다음에 Entity 객체 이름이 들어감. (앤티티 객체를 대상으로 하기 때문에)
         */
        String jpql = "select i from Item i";
        Integer maxPrice = cond.getMaxPrice();
        String itemName = cond.getItemName();

        if (StringUtils.hasText(itemName) || maxPrice != null) {
            jpql += " where";
        }

        boolean andFlag = false;
        if (StringUtils.hasText(itemName)) {
            jpql += " i.itemName like concat('%', :itemName, '%')";
            andFlag = true;
        }

        if (maxPrice != null) {
            if (andFlag) {
                jpql += " and";
            }
            jpql += " i.price <= :maxPrice";
        }

        log.info("jpql = {}", jpql);

        TypedQuery<Item> query = em.createQuery(jpql, Item.class);

        if (StringUtils.hasText(itemName)) {
            //JPQL에서의 파라미터 바인딩
            query.setParameter("itemName", itemName);
        }
        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }
        return query.getResultList();
    }

}
