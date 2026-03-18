package com.example.controller;

import com.example.service.IChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/compare/local-vs-cloud")
public class LocalCloudCompareController {

    @Autowired
    private ChatClient chatClient;  // 本地模型

    @Autowired
    private IChatService chatService;  // 云端模型（Day 5）

    @GetMapping
    public Map<String, Object> compare(
            @RequestParam String question,
            @RequestParam(defaultValue = "false") boolean stream) {

        Map<String, Object> result = new HashMap<>();
        result.put("question", question);

        Map<String, Object> localResult = new HashMap<>();
        Map<String, Object> cloudResult = new HashMap<>();

        try {
            // 测试本地模型
            long localStart = System.currentTimeMillis();
            String localAnswer = chatClient.prompt()
                    .user(question)
                    .call()
                    .content();
            long localTime = System.currentTimeMillis() - localStart;

            localResult.put("success", true);
            localResult.put("answer", localAnswer);
            localResult.put("time_ms", localTime);
            localResult.put("model", "qwen2.5:7b (本地)");

        } catch (Exception e) {
            localResult.put("success", false);
            localResult.put("error", e.getMessage());
        }

        try {
            // 测试云端模型
            long cloudStart = System.currentTimeMillis();
            String cloudAnswer = chatService.chat(question, null);
            long cloudTime = System.currentTimeMillis() - cloudStart;

            cloudResult.put("success", true);
            cloudResult.put("answer", cloudAnswer);
            cloudResult.put("time_ms", cloudTime);
            cloudResult.put("model", "qwen-turbo (云端)");

        } catch (Exception e) {
            cloudResult.put("success", false);
            cloudResult.put("error", e.getMessage());
        }

        result.put("local", localResult);
        result.put("cloud", cloudResult);

        // 简单对比分析
        if (localResult.containsKey("time_ms") && cloudResult.containsKey("time_ms")) {
            long localTime = (long) localResult.get("time_ms");
            long cloudTime = (long) cloudResult.get("time_ms");

            String faster = localTime < cloudTime ? "本地模型更快" : "云端模型更快";
            result.put("analysis", String.format(
                    "本地: %dms, 云端: %dms, %s",
                    localTime, cloudTime, faster
            ));
        }

        return result;
    }
}