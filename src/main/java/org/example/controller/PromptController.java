package org.example.controller;

import jakarta.annotation.Resource;
import org.example.dto.CrispeRequestDTO;
import org.example.service.IChatService;
import org.example.vo.base.ApiResult;
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
