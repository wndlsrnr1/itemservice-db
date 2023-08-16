package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import javax.swing.*;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JdbcTemplate
 * 문제점: 파라미터 바인딩이 순서대로 된다.
 * 추후에 바뀌었을 경우 DB에 잘못된 정보가 들어간다.
 */
@Slf4j
@Repository
public class JdbcTemplateItemRepositoryV1 implements ItemRepository {

    //JdbcTemplate이라는 클래스 사용
    private final JdbcTemplate template;

    //생성자로 DataSource 주입 -> JdbcTemplate은 생성시 dataSource를 사용함.
    public JdbcTemplateItemRepositoryV1(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }


    @Override
    public Item save(Item item) {
        String sql = "insert into item (item_name, price, quantity) values (?,?,?)";

        //키 홀더 생셩, id와 조합해서 insert 이후 auto increment된 key를 받기 위해서
        //나중에 SimpleJdbcInsert 기능 있으므로 그거 이용 함.
        KeyHolder keyHolder = new GeneratedKeyHolder();

        //키홀더 사용 및 업데이트
        //변경이 있는 경우에 update method 이용
        template.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
            preparedStatement.setString(1, item.getItemName());
            preparedStatement.setInt(2, item.getPrice());
            preparedStatement.setInt(3, item.getQuantity());
            return preparedStatement;
        }, keyHolder);

        //저장된키 꺼냄
        long key = keyHolder.getKey().longValue();
        //save key value in Item instance;
        item.setId(key);

        //return item instance
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item set item_name = ?, price = ?, quantity = ? where id = ?";
        //반환 값은 query에 영향 받은 row의 수.
        int update = template.update(sql, updateParam.getItemName(), updateParam.getPrice(), updateParam.getQuantity(), itemId);
    }

    @Override
    public Optional<Item> findById(Long id) {
        String sql = "select id, item_name, price, quantity, from item where id = ?";
        try {
            //데이터 하나를 조회할때 queryForObject를 사용
            Item item = template.queryForObject(sql, itemRowMapper(), id);
            return Optional.of(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /*
    RowMapper<Item> 을 반환, 여기에서 resultSet은 어디에서 오는 가?
    sql에서
    rowNum은 결과가 여러가지 이므로
    ResultSet(데이터베이스의 반환결과) 를 객체로 반환
    resultSet이 여러개이고 내부에서 이 함수를 이용해서 객체로 만든다.

    예시
    while(resultSet.hasNext){
        만들어 놓은 rowMapper
        rowMapper(resultSet, rowNum)
    }
     */
    private RowMapper<Item> itemRowMapper() {
        return (rs, rowNum) -> {
            Item item = new Item();
            item.setId(rs.getLong("id"));
            item.setItemName(rs.getString("item_name"));
            item.setPrice(rs.getInt("price"));
            item.setQuantity(rs.getInt("quantity"));
            return item;
        };
    }

    /*
    값이 있냐 없냐에 따라서 동적 쿼리를 만들어 내야 하는 상황
    1. select id, item_name, price, quantity from item
    2. select id, item_name, price, quantity from item where item_name like concat('%', ?, '%')
    3. select id, item_name, price, quantity from item where price <= ?
    4. select id, item_name, price, quantity from item where item_name like concat('%', ?, '%') and price <= ?
     */
    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        String sql = "select id, item_name, price, quantity from item";
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
        }

        boolean andFlag = false;
        List<Object> param = new ArrayList<>();
        //itemName이 String 형태이면, where 절 실행
        if (StringUtils.hasText(itemName)) {
            sql += " item_name, like concat('%', '?', '%')";
            param.add(itemName);
            andFlag = true;
        }

        if (maxPrice != null) {
            //앞에 조건이 하나 더 있으면 and를 붙여준다.
            if (andFlag) {
                sql += " and";
            }
            sql += " price <= ?";
            param.add(maxPrice);
        }

        log.info("sql={}", sql);
        //sql에서 rowMapper 이용 rowMapper, sql, Array를 이용해서  query 생성
        return template.query(sql, itemRowMapper(), param.toArray());
    }

}
