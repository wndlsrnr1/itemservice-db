package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * NamedParameterJdbcTemplate 사용
 * 100개 정도 파라미터를 순서대로 넣어야 하는 문제 해결
 * SqlParameterSource
 * -BeanPropertySqlParameterSource
 * -MapSqlParameterSource
 * Map
 *
 * BeanPropertyRowMapper
 *
 * 단점 sql을 직접 작성 해야함 -> SimpleJdbcInsert
 */
@Slf4j
public class JdbcTemplateItemRepositoryV2 implements ItemRepository {

    //직접 스프링 빈으로 등록하고 주입해도 됨, 관례상 내부에서 생성해서 가지고 있는다.
    //NamedParameterJdbcTemplate은 DB가 생성해주는 키를 쉽게 조회하는 기능도 가지고 있다.
    private final NamedParameterJdbcTemplate template;

    public JdbcTemplateItemRepositoryV2(DataSource dataSource) {
        this.template = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public Item save(Item item) {

        //:key values는 해당 파라미터의 값 (property of item instance)
        String sql = "insert into item (item_name, price, quantity) " + "values (:itemName, :price, :quantity)";

        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(item);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        //update 하기 위해서 sqlParameterSource가 필요함. sqlParameterSource는 item 정보가 필요함.
        //sql, parameterSource, keyHolder로 update 함.
        int update = template.update(sql, parameterSource, keyHolder);

        Long key = keyHolder.getKey().longValue();
        item.setId(key);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item " + "set item_name=:itemName, price=:price, quantity=:quantity " + "where id=:id";

        //binding하기 위해서 있음 Map과 유사, Sql타입을 지정할 수 있는 Sql 특화 기능을 제공한다.
        //Subtype of SqlParameterSource
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("itemName", updateParam.getItemName())
                .addValue("price", updateParam.getPrice())
                .addValue("quantity", updateParam.getQuantity())
                .addValue("id", itemId);

        template.update(sql, param);
    }

    @Override
    public Optional<Item> findById(Long id) {
        String sql = "select id, item_name, price, quantity, from item where id = :id";
        try {
            //이름 지정하기 위한 파라미터 바인딩에서 사용 한다.
            Map<String, Object> param = Map.of("id", id);
            Item item = template.queryForObject(sql, param, itemRowMapper());
            return Optional.of(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    /*
    camel 변환 지원, ResultSet의 결과를 받아서 JavaBean 규약에 맞추어 데이터 변환
    reflexion해서 instance of item에 결과 저장
    item_name은 setItem_name이 존재하지 않음.
    그러므로 select item_anme as itemName 형태로 별칭 지정
    ex) member_name in db and username is property name
    select member_name as username

    snake_case camelCase 자주 변환 되다 보니 지원함.
    완전히 다른 경우에만 as 를 이용
     */
    private RowMapper<Item> itemRowMapper() {
        return BeanPropertyRowMapper.newInstance(Item.class);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        Integer maxPrice = cond.getMaxPrice();
        String itemName = cond.getItemName();

        //Java Bean property 규약을 통해 파라미터 객체 생성, 바인딩
        //getItemName(), getPrice()가 있을 경우
        //itemName, price의 key와 value를 생성
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(cond);

        String sql = "select id, item_name, price, quantity from item";

        //동적 쿼리
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
        }

        boolean andFlag = false;
        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%', ?, '%')";
            andFlag = true;
        }

        if (maxPrice != null) {
            if (andFlag) {
                sql += " and";
            }
            sql += " price <= :maxPrice";
        }

        log.info("sql={}", sql);
        return template.query(sql, parameterSource, itemRowMapper());
    }

}
