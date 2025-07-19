package com.fintech.api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
// 예약 이체기능을 위한 엔티티 

public class ScheduledTransfer {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY) 
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) 
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account toAccount;

    private Long amount;

    private LocalDateTime scheduledAt;

    private String status;
}
