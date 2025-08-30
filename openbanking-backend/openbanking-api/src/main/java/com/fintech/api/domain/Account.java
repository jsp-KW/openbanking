package com.fintech.api.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
// alt + shift  + o
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity // JPA 에서 관리할 테이블 mapping 용 객체 -> Hibernate가 이를 통해 sql table 자동 생성
        // 클래스 명을 소문자로 변환하여 테이블 이름으로 씀
@Getter @Setter // getter, setter method 자동 생성
@NoArgsConstructor // 기본 생성자 (no parameter)
@AllArgsConstructor //  전체 필드를 받는 생성자를 자동 생성

@Builder // 빌더 패턴 사용을 가능하게 해서 안정적인 객체 생성하도록


// 계좌 비밀번호 추가하기
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String bankName;
    
    @Column(unique = true)
    private String accountNumber;

    // 계좌 비밀번호
    @Column(name = "account_password", nullable = false)
    private String accountPassword;
    

    //accountCreated -> 계좌 생성시간도 추가적으로 고려해보기
    
    private String accountType; //입출금 적금 등 

    @Version //목적 : 동시에 두 건이 같은 계좌를 접근하는 경우 중복 차감 방지(충돌 방지를 위해)
    private Long version;
    // 사용자와 계좌 -> 1:N  일대다 관계 
    // 계좌 입장에서 다대일 관계이므로
    private Long balance;
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference("user-account")  
    private User user; // JPA 가 user_id fk 생성 + 객체 연관 매핑


    // 계좌와 은행  -> N:1관계
  
    @ManyToOne
    @JoinColumn(name = "bank_id")
    @JsonBackReference("bank-account")
    private Bank bank;

    // 거래내역과 계좌의 관계
    // 하나의 계좌에 여러 거래 내역 가능
   
    @OneToMany(mappedBy = "account", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Transaction> transactions = new ArrayList<>();


    //내 계좌로 가입된 예금상품 리스트를 보기위해
    @OneToMany(mappedBy = "account")
    private List<UserDeposit> deposits = new ArrayList<>();
}
