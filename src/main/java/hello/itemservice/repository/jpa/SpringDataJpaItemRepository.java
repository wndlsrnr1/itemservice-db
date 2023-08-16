package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SpringDataJpaItemRepository extends JpaRepository<Item, Long> {
    //기본 검색 기능을 사용 할 수 있다.
    @Override
    List<Item> findAll();

    //하지만 이름 검색, 가격 검색 기능은 공통으로 제공할 수 있는 기능이 아니다.
    //이름 조건만 검색 했을때 사용
    //select i from Item i where i.name like ? 생성 됨.
    List<Item> findByItemNameLike(String itemName);

    //가격 조건만 검색했을 때 사용
    //select i from Item i where i.price <= ?
    List<Item> findByPriceLessThanEqual(Integer price);

    //쿼리 메서드 (아래 메서드와 같은 기능 수행)
    //이름과 가격 조건을 검색 했을 때 사용하는 쿼리 메서드
    //select i from ITem i where i.itemName like ? and i.price <= ? 실행됨.
    List<Item> findByItemNameLikeAndPriceLessThanEqual(String itemName, Integer price);

    //쿼리를 실행할때 순서대로 입력하면 되지만, 쿼리를 직접 실행시 파라미터 바인딩이 필수
    @Query("select i from Item i where i.itemName like :itemName and i.price <= :price")
    List<Item> findItems(@Param("itemName") String itemName, @Param("price") Integer price);

    /*
    메서드 이름으로 쿼리 실행시 단점
    1. 조건이 너무 많아지면 메서드 이름이 너무 길어진다.
    2. 조인 같은 복잡한 조건을 사용할 수 없다.
    복잡한 쿼리는 JPQL쿼리를 이용
     */
    //모든 데이터조회, 이름 조회, 가격 조회, 이름 + 가격 조회 해보기
}
