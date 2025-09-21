package com.fintech.api.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 추천테이블 (상품)
// 하나의 유저-> 여러 상품 추천 가능
// 하나의 상품 여러 유저에게 추천받을 수 있으므로 N:M관계 -> 중간테이블 ProductLike

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) //jpa가 프록시 생성을 위해 기본 생성자를 필요로 하며, 외부접근 방지를 위해 PROTECTED
@Table( // Table 이름을 product_like로 지정하며, uniqueConstraints 로 동일 유저-상품 조합은 한번만 추천이 가능하도록 db레벨에서 강제 -> 데이터 무결성 보장
    name = "product_like",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "product_id"})}
)
@EqualsAndHashCode(of = {"user", "product"}) // 같은 user와 product 조합이면 같은 객체로 취급 -> set,map 과 같은 collection 자료구조에서 중복 방지, db의 유니크 제약 조건과 자바 객체의 동등성 규칙 일치
public class ProductLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본키

    @ManyToOne(fetch = FetchType.LAZY) // 하나의 유저가 여러개의 추천이 가능, 지연로딩 Like만 조회하는 경우 User/Product 가져오지 x
    @JoinColumn(name = "user_id", nullable = false) 
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) // 하나의 상품이 여러 추천을 받을 수 ㅇ있으므로
    @JoinColumn(name = "product_id", nullable = false)
    private DepositProduct product;

    // static factory method
    // of -> ProductLike like = ProductLike.of(user,product); 로 ProductLike 객체를 생성할때 직관적으로 표현
    public static ProductLike of(User user, DepositProduct product) {
        return new ProductLike(user, product);
    }
    // private 생성자 -> User 객체와 Product 객체를 함께 넘겨야 생성이 가능
    private ProductLike(User user, DepositProduct product) {
        this.user = user;
        this.product = product;
    }
}