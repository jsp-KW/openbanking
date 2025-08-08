package com.fintech.api.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;
    private String bankName;
    private String website_url;

    @OneToMany(mappedBy = "bank") // 하나의 은행 -> 여러개의 계좌
    @JsonBackReference("bank-account")
    private List<Account> accounts = new ArrayList<>();

    @OneToMany(mappedBy = "bank")// 하나의 은행 -> 여러개의 예금상품
    private List<DepositProduct> depositProducts = new ArrayList<>();
}
