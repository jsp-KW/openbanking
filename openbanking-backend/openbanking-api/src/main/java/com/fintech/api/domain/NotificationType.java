package com.fintech.api.domain;

public enum NotificationType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER,
    SCHEDULED_TRANSFER,
    SYSTEM_NOTICE,
    ACCOUNT_CREATED,
    INSUFFICIENT_BALANCE, //이체시 실패 알람 추가하기 위한 ENUM TYPE
    HIGH_VALUE_TRANSACTION // 고액 거래 알람을 위한
}