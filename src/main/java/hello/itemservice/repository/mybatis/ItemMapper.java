package hello.itemservice.repository.mybatis;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.java.Log;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ItemMapper {

    void save(Item item);

    void update(@Param("id") Long id, @Param("updateParam") ItemUpdateDto updateParam);

    Optional<Item> findById(Long id);

    /*
    @Insert, @Update, @Delete, @Select 기능이 제공됨
    XML id="findById"와 충돌 하므로 삭제 해야함.
    danamic query 지원 안함
    @Select("select id, item_name, price, quantity, from item where id=#{id}")
    Optional<Item> findById(Long id);
    */

    List<Item> findAll(ItemSearchCond itemSearch);
}
