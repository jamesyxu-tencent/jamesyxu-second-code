package com.example.service.chat;

import com.example.module.ToolNode;
import com.example.module.travel.TravelPlanResult;
import com.example.tools.utils.ToolChainUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 基于工具调用链的旅行规划服务
 */
@Service
public class TravelPlannerService {

    // 模型类型常量
    public static final String MODEL_QWEN_TURBO = "qwen-turbo";
    public static final String MODEL_OLLAMA = "ollama";
    public static final String MODEL_AUTO = "auto";

    @Autowired
    private ToolChainUtil toolChainUtil;

    @Autowired
    @Qualifier("ollamaChatClient")
    private ChatClient ollamaChatClient;

    @Autowired
    @Qualifier("qwenTurboChatClient")
    private ChatClient qwenTurboChatClient;

    @Autowired
    @Qualifier("qwenPlusChatClient")
    private ChatClient qwenPlusChatClient;

    /**
     * 顺序执行旅行规划
     */
    public TravelPlanResult planTripSequential(String destination, int days, String preference) {
        long startTime = System.currentTimeMillis();

        System.out.println("\n========== 开始顺序旅行规划 ==========");
        System.out.println("目的地: " + destination + ", 天数: " + days + ", 偏好: " + preference);

        // 创建工具链
        List<ToolNode> chain = toolChainUtil.createTravelPlanChain(destination, days, preference);

        // 顺序执行
        Map<String, Object> results = toolChainUtil.executeSequential(chain);

        long duration = System.currentTimeMillis() - startTime;

        return new TravelPlanResult(results, duration, false);
    }

    /**
     * 并行执行旅行规划（更快）
     */
    public TravelPlanResult planTripParallel(String destination, int days, String preference) {
        long startTime = System.currentTimeMillis();

        System.out.println("\n========== 开始并行旅行规划 ==========");
        System.out.println("目的地: " + destination + ", 天数: " + days + ", 偏好: " + preference);

        // 创建工具链
        List<ToolNode> chain = toolChainUtil.createTravelPlanChain(destination, days, preference);

        // 并行执行
        Map<String, Object> results = toolChainUtil.executeParallel(chain);

        long duration = System.currentTimeMillis() - startTime;

        return new TravelPlanResult(results, duration, true);
    }

    /**
     * 生成格式化的旅行计划书
     */
    public String generateFormattedPlan(TravelPlanResult result, String modelType) {
        Map<String, Object> data = result.getData();

        String prompt = String.format("""
            请根据以下旅行规划数据，生成一份精美、详细的旅行计划书。
            
            【基本信息】
            - 当前日期：%s
            - 旅行结束日期：%s
            
            【天气信息】
            %s
            
            【目的地介绍】
            %s
            
            【景点推荐】
            %s
            
            【美食推荐】
            %s
            
            【行程安排】
            %s
            
            【酒店推荐】
            %s
            
            请生成一份结构清晰、内容丰富的旅行计划书，包括：
            1. 标题和概述
            2. 行前准备建议
            3. 每日详细行程（上午/下午/晚上）
            4. 美食推荐清单
            5. 预算估算
            6. 温馨提示
            
            如果某些信息缺失，请使用已有信息补充。
            """,
                data.getOrDefault("currentDate", "未知"),
                data.getOrDefault("endDate", "未知"),
                data.getOrDefault("weather", "暂无天气信息"),
                data.getOrDefault("destinationInfo", "暂无介绍"),
                data.getOrDefault("attractions", "暂无景点信息"),
                data.getOrDefault("foods", "暂无美食信息"),
                data.getOrDefault("itinerary", "暂无行程"),
                data.getOrDefault("hotels", "暂无酒店信息")
        );

        ChatClient chatClient = getChatClient(modelType);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    /**
     * 一键生成完整旅行计划
     */
    public String generateCompletePlan(String destination, int days, String preference, boolean parallel, String modelType) {
        TravelPlanResult result;

        if (parallel) {
            result = planTripParallel(destination, days, preference);
        } else {
            result = planTripSequential(destination, days, preference);
        }

        String formattedPlan = generateFormattedPlan(result, modelType);

        return String.format("""
            %s
            
            ---
            📊 执行统计
            - 执行模式：%s
            - 耗时：%d ms
            - 工具调用次数：%d
            """,
                formattedPlan,
                result.isParallel() ? "并行" : "顺序",
                result.getDuration(),
                result.getData().size()
        );
    }

    /**
     * 获取对应的ChatClient
     */
    private ChatClient getChatClient(String modelType) {
        if (modelType.equals(MODEL_OLLAMA)) {
            return ollamaChatClient;
        } else if (modelType.equals(MODEL_QWEN_TURBO)) {
            return qwenTurboChatClient;
        } else {
            return qwenPlusChatClient;
        }
    }
}