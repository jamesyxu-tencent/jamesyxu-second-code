package com.example.controller.travel;

import com.example.service.chat.TravelPlannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/travel")
@CrossOrigin(origins = "*")
public class TravelController {

    @Autowired
    private TravelPlannerService travelPlannerService;

    /**
     * 顺序执行旅行规划
     */
    @GetMapping("/plan/sequential")
    public Map<String, Object> planSequential(
            @RequestParam String destination,
            @RequestParam(defaultValue = "3") int days,
            @RequestParam(defaultValue = "culture") String preference,
            @RequestParam(defaultValue = "auto") String modelType) {

        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            String plan = travelPlannerService.generateCompletePlan(destination, days, preference, false, modelType);

            result.put("success", true);
            result.put("destination", destination);
            result.put("days", days);
            result.put("preference", preference);
            result.put("plan", plan);
            result.put("mode", "sequential");
            result.put("total_time_ms", System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 并行执行旅行规划（更快）
     */
    @GetMapping("/plan/parallel")
    public Map<String, Object> planParallel(
            @RequestParam String destination,
            @RequestParam(defaultValue = "3") int days,
            @RequestParam(defaultValue = "culture") String preference,
            @RequestParam(defaultValue = "auto") String modelType) {

        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            String plan = travelPlannerService.generateCompletePlan(destination, days, preference, true, modelType);

            result.put("success", true);
            result.put("destination", destination);
            result.put("days", days);
            result.put("preference", preference);
            result.put("plan", plan);
            result.put("mode", "parallel");
            result.put("total_time_ms", System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 对比顺序和并行执行性能
     */
    @GetMapping("/benchmark")
    public Map<String, Object> benchmark(
            @RequestParam String destination,
            @RequestParam(defaultValue = "3") int days,
            @RequestParam("auto") String modelType) {

        Map<String, Object> result = new HashMap<>();

        // 顺序执行
        long seqStart = System.currentTimeMillis();
        travelPlannerService.generateCompletePlan(destination, days, "culture", false, modelType);
        long seqDuration = System.currentTimeMillis() - seqStart;

        // 并行执行
        long parStart = System.currentTimeMillis();
        travelPlannerService.generateCompletePlan(destination, days, "culture", true, modelType);
        long parDuration = System.currentTimeMillis() - parStart;

        result.put("success", true);
        result.put("destination", destination);
        result.put("days", days);
        result.put("sequential_ms", seqDuration);
        result.put("parallel_ms", parDuration);
        result.put("speedup", String.format("%.2fx", (double) seqDuration / parDuration));

        return result;
    }

    /**
     * 查看工具调用链结构
     */
    @GetMapping("/chain-info")
    public Map<String, Object> getChainInfo(
            @RequestParam String destination,
            @RequestParam(defaultValue = "3") int days) {

        Map<String, Object> result = new HashMap<>();

        // 这里可以返回工具链的DAG结构
        result.put("message", "工具调用链已配置，包含以下工具");
        result.put("tools", new String[]{
                "getCurrentDate - 获取当前日期",
                "addDays - 计算结束日期（依赖getCurrentDate）",
                "getCurrentWeather - 查询天气",
                "getDestinationInfo - 获取目的地信息",
                "getAttractions - 获取景点推荐",
                "getFoods - 获取美食推荐",
                "generateItinerary - 生成行程（依赖getAttractions）",
                "searchHotels - 搜索酒店（依赖getCurrentDate和addDays）"
        });

        return result;
    }
}