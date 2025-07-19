package com.fintech.api.controller;

import com.fintech.api.dto.CreateScheduledTransferRequestDto;
import com.fintech.api.dto.ScheduledTransferListResponseDto;
import com.fintech.api.dto.ScheduledTransferResponseDto;
import com.fintech.api.service.ScheduledTransferService;
import com.fintech.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduled-transfers")
@RequiredArgsConstructor
public class ScheduledTransferController {

    private final ScheduledTransferService scheduledTransferService;
    private final UserService userService;

    // front 필드 < dto 필드 -> 매핑 실패 (error 발생 가능)
    // front 필드 == dto 필드 -> 정상 매핑
    // front 필드 > dto 필드 -> dto 필드만 매핑, 나머지 필드는 무시

    /**
     *  예약이체 등록 API
     */
    @PostMapping("")
    public ResponseEntity<ScheduledTransferResponseDto> createScheduledTransfer(
            @RequestBody CreateScheduledTransferRequestDto dto,//front에서 보낸 필드 
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getUserIdByEmail(userDetails.getUsername());//현재로그인 된 유저 기반으로 userId 저장

        ScheduledTransferResponseDto result = scheduledTransferService.createScheduledTransfer(
                userService.getUserEntityById(userId), dto); // userId를 통해 유저 엔티티를 유저 서비스 계층에서 가져와 인자로 넣어준다

        return ResponseEntity.ok(result);// 예약이체 등록 응답 dto를 넣어서 보내줌.
    }

    /**
     * 나의 예약이체 목록 조회 API
     */
    @GetMapping("/my")
    public ResponseEntity<List<ScheduledTransferListResponseDto>> getMyScheduledTransfers(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getUserIdByEmail(userDetails.getUsername());

        // List의 자료형은 응답dto형
        // userId 기준으로 예약이체 엔티티들 조회-> dto 형태로 변환된 리스트로 받는다.
        List<ScheduledTransferListResponseDto> scheduledTransfers =
                scheduledTransferService.getMyScheduledTransfers(userId); //유저 id 기반으로 가져온 예약이체목록들 

        return ResponseEntity.ok(scheduledTransfers);
    }
}
