package com.fintech.api.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

public class User {

    // 사용자 테이블 정의
    
    @Id  //이 엔티티의 PK 기본키를 나타내기 위해-> DB에서 PRIMARY KEY 로 인식
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT와, IDENTITY 전략은  1:1 대응
    
    private Long id; //고유 id
    private String name; // 이름

    @Column(unique = true)
    private String email; // 이메일

    private String password; // 비밀번호
    
    @Column(unique = true)
    private String phone; // 전화번호

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List <Account> accounts = new ArrayList<>();


    
}





// Spring boot 실행
// JPA-> @Entity class 스캔
// ddl-auto= update 설정을 보고 db 테이블 자동 생성 or 업데이트
// @Id, @GeneratedValue , 타입 등을 기반으로 DDL(테이블 생성 SQL) 생성 후 실행

