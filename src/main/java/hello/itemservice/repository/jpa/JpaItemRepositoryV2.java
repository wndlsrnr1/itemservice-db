package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/*
Repository의 메서드네임과 SpringDataJPA의 메서드 네임이 일치 하지 않아  사용 하기 힘들다.
ItemRepository의 구현체를 만들어서 Jpa를 이용하게 한다.
ItemRepository와 SpringDataJapItemRepository 사이를 맞추기 위한 어댑터처럼 사용된다.
                                    <<JpaRepository>>
<<ItemService>> <<ItemRepository>> <<SpringDataJpaItemRepository>>
                <<JpaItemRepositoryV2>> <<Proxy>>

 */
@Repository
@Transactional
@RequiredArgsConstructor
public class JpaItemRepositoryV2 implements ItemRepository {

    private final SpringDataJpaItemRepository springDataJpaItemRepository;

    @Override
    public Item save(Item item) {
        return springDataJpaItemRepository.save(item);
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item item = springDataJpaItemRepository.findById(itemId).orElseThrow();
        item.setItemName(updateParam.getItemName());
        item.setPrice(updateParam.getPrice());
        item.setQuantity(updateParam.getQuantity());
    }

    @Override
    public Optional<Item> findById(Long id) {
        return springDataJpaItemRepository.findById(id);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        if (StringUtils.hasText(itemName) && maxPrice != null) {
            return springDataJpaItemRepository.findItems("%" + itemName + "%", maxPrice);
        }
        if (StringUtils.hasText(itemName)) {
            return springDataJpaItemRepository.findByItemNameLike("%" + itemName + "%");
        }
        if (maxPrice != null) {
            return springDataJpaItemRepository.findByPriceLessThanEqual(maxPrice);
        }
        return springDataJpaItemRepository.findAll();
    }

}
