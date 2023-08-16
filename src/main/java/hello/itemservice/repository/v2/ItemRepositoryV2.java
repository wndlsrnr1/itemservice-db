package hello.itemservice.repository.v2;

import hello.itemservice.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

//단순한 CRUD와 조회 담당
public interface ItemRepositoryV2 extends JpaRepository<Item, Long> {

}
