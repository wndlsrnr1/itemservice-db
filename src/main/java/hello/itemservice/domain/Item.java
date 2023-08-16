package hello.itemservice.domain;

import lombok.Data;

import javax.persistence.*;

@Data
//JPA가 사용하는 객체라는 뜻, JPA가 인식할 수 있게 해준다.
@Entity
public class Item {

    //테이블의 PK와 이 필드를 매핑
    @Id
    //PK생성 값을 DB에서 생성하는 IDENTITY 방식을 사용한다. (MySQL auto increment)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //객체 필드를 테이블의 컬럼과 매핑
    //length JPA의 매핑 정보로 DDL(create table)도 생성할 수 있느넫, 컬럼의 길이 값으로 활용 된다. (varchar 10)
    @Column(name = "item_name", length=10)
    private String itemName;
    //@Column 어노테이션 생략시에 필드 이름을 테이블 컬럼을 기본으로 이용.
    //필드 이름을 테이블 컬럼 명으로 변경 할때 camelcase to sname_case해준다.
    //즉 @Column(name="item_name") 생략가능
    private Integer price;
    private Integer quantity;

    //기본 생성자 필수 Required만 사용하면 안됨.
    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
