package hello.itemservice.repository.jpa;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hello.itemservice.domain.Item;
import hello.itemservice.domain.QItem;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import static hello.itemservice.domain.QItem.*;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class JpaItemRepositoryV3 implements ItemRepository {

    private final EntityManager entityManager;
    private final JPAQueryFactory jpaQueryFactory;

    public JpaItemRepositoryV3(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Item save(Item item) {
        entityManager.persist(item);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = findById(itemId).orElseThrow();
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = entityManager.find(Item.class, id);
        return Optional.ofNullable(item);
    }

    public List<Item> findAllOld(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        QItem item = QItem.item;
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (StringUtils.hasText(itemName)) {
            booleanBuilder.and(item.itemName.like("%" + itemName + ""));
        }
        if (maxPrice != null) {
            booleanBuilder.and(item.price.loe(maxPrice));
        }

        List<Item> result = jpaQueryFactory
                .select(item)
                .from(item)
                .where(booleanBuilder)
                .fetch();
        return result;
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();
        List<Item> result = jpaQueryFactory
                .select(item)
                .from(item)
                //and 조건으로 인식 됨
                .where(likeItemName(itemName), maxPrice(maxPrice))
                .fetch();
        return result;
    }

    //모듈화
    private BooleanExpression likeItemName(String itemName) {
        if (StringUtils.hasText(itemName)) {
            //Item을 만들어 준다.
            return item.itemName.like("%" + itemName + "%");
        }
        //해당 조건은 무시
        return null;
    }
    private BooleanExpression maxPrice(Integer maxPrice) {
        if (maxPrice != null) {
            return item.price.loe(maxPrice);
        }
        return null;
    }
}
