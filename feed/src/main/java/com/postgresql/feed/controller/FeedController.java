package com.postgresql.feed.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.postgresql.feed.dto.FeedRequest;
import com.postgresql.feed.dto.FeedResponse;
import com.postgresql.feed.service.FeedService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/feeds")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    /**
     * 사용자 피드 목록 조회
     * 
     * @param request 피드 조회 요청 DTO
     * @return FeedResponse 피드 응답 DTO
     */
    @GetMapping("")
    public ResponseEntity<FeedResponse> getUserFeeds(@Valid FeedRequest request) {
        FeedResponse response = feedService.getUserFeeds(request.userId(), request.size(), request.cursor());
        return ResponseEntity.ok().body(response);
    }
}
