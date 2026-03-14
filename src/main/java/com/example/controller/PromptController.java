package com.example.controller;

import jakarta.annotation.Resource;
import com.example.dto.CrispeRequestDTO;
import com.example.service.IChatService;
import com.example.vo.base.ApiResult;
import org.springframework.web.bind.annotation.*;

@RestController
public class PromptController {

    @Resource
    private IChatService chatService;

    @GetMapping("/prompt/chatWithRole")
    public ApiResult<String> chatWithRole(@RequestParam("question") String question,
                                          @RequestParam("role") String role) {
        return ApiResult.success(chatService.chatWithRole(question, role));
    }

    @PostMapping("/prompt/chatWithCrispe")
    public ApiResult<String> chatWithCrispe(@RequestBody CrispeRequestDTO dto) {
        return ApiResult.success(chatService.chatWithCrispe(dto));
    }
}
