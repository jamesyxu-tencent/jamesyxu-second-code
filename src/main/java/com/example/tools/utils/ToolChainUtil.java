package com.example.tools.utils;

import com.example.module.ToolNode;
import com.example.tools.DateTimeTools;
import com.example.tools.HotelTools;
import com.example.tools.TravelTools;
import com.example.tools.WeatherTools;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具调用链服务
 * 支持依赖解析、并行执行、结果传递
 */
@Component
public class ToolChainUtil {

    @Autowired
    private DateTimeTools dateTimeTools;

    @Autowired
    private WeatherTools weatherTools;

    @Autowired
    private TravelTools travelTools;

    @Autowired
    private HotelTools hotelTools;

    // 工具注册表
    private final Map<String, ToolExecutor> toolRegistry = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        registerTools();
    }

    /**
     * 注册所有可用工具
     */
    private void registerTools() {
        // 日期时间工具
        register("getCurrentDate", dateTimeTools, "getCurrentDate");
        register("getCurrentDateTime", dateTimeTools, "getCurrentDateTime");
        register("addDays", dateTimeTools, "addDays", int.class);
        register("calculateDateDifference", dateTimeTools, "calculateDateDifference", String.class, String.class);

        // 天气工具
        register("getCurrentWeather", weatherTools, "getCurrentWeather", String.class);
        register("getForecast", weatherTools, "getForecast", String.class, Integer.class);

        // 旅行工具
        register("getDestinationInfo", travelTools, "getDestinationInfo", String.class);
        register("getAttractions", travelTools, "getAttractions", String.class, Integer.class);
        register("getFoods", travelTools, "getFoods", String.class);
        register("generateItinerary", travelTools, "generateItinerary", String.class, int.class, String.class);

        // 酒店工具
        register("searchHotels", hotelTools, "searchHotels", String.class, String.class, String.class);
        register("bookHotel", hotelTools, "bookHotel", String.class, String.class, String.class, int.class);
    }

    /**
     * 注册工具
     */
    private void register(String toolName, Object target, String methodName, Class<?>... parameterTypes) {
        try {
            Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
            toolRegistry.put(toolName, new ToolExecutor(target, method));
        } catch (NoSuchMethodException e) {
            System.err.println("注册工具失败: " + toolName + " - " + e.getMessage());
        }
    }

    // ==================== 核心执行方法 ====================

    /**
     * 顺序执行工具调用链
     */
    public Map<String, Object> executeSequential(List<ToolNode> chain) {
        Map<String, Object> results = new LinkedHashMap<>();

        System.out.println("========== 开始顺序执行工具链 ==========");

        for (ToolNode node : chain) {
            System.out.println("执行: " + node.getToolName());

            try {
                // 解析参数中的占位符
                Map<String, Object> resolvedParams = resolvePlaceholders(node.getParameters(), results);

                // 执行工具
                ToolExecutor executor = toolRegistry.get(node.getToolName());
                if (executor == null) {
                    throw new RuntimeException("工具不存在: " + node.getToolName());
                }

                Object result = executor.execute(resolvedParams);
                results.put(node.getResultKey(), result);

                System.out.println("  结果: " + (result != null ? result.toString().substring(0, Math.min(50, result.toString().length())) : "null"));

            } catch (Exception e) {
                System.err.println("  执行失败: " + e.getMessage());
                results.put(node.getResultKey(), "错误: " + e.getMessage());
            }
        }

        System.out.println("========== 顺序执行完成 ==========");
        return results;
    }

    /**
     * 并行执行工具调用链（自动处理依赖）
     */
    public Map<String, Object> executeParallel(List<ToolNode> chain) {
        System.out.println("========== 开始并行执行工具链 ==========");

        // 拓扑排序
        List<ToolNode> sorted = topologicalSort(chain);

        // 存储结果
        Map<String, Object> results = new ConcurrentHashMap<>();

        // 按层级执行
        List<List<ToolNode>> levels = groupByLevel(sorted);

        for (List<ToolNode> level : levels) {
            System.out.println("并行执行层级，共 " + level.size() + " 个任务");

            // 并行执行当前层级的所有任务
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (ToolNode node : level) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        Map<String, Object> resolvedParams = resolvePlaceholders(node.getParameters(), results);

                        ToolExecutor executor = toolRegistry.get(node.getToolName());
                        if (executor == null) {
                            throw new RuntimeException("工具不存在: " + node.getToolName());
                        }

                        Object result = executor.execute(resolvedParams);
                        results.put(node.getResultKey(), result);

                        System.out.println("  ✓ " + node.getToolName() + " 执行完成");

                    } catch (Exception e) {
                        System.err.println("  ✗ " + node.getToolName() + " 执行失败: " + e.getMessage());
                        results.put(node.getResultKey(), "错误: " + e.getMessage());
                    }
                });
                futures.add(future);
            }

            // 等待当前层级所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        System.out.println("========== 并行执行完成 ==========");
        return results;
    }

    /**
     * 解析参数中的占位符
     * 支持格式：${resultKey.field} 或 ${resultKey}
     */
    private Map<String, Object> resolvePlaceholders(Map<String, Object> params, Map<String, Object> results) {
        Map<String, Object> resolved = new HashMap<>();

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object value = entry.getValue();

            if (value instanceof String && ((String) value).startsWith("${") && ((String) value).endsWith("}")) {
                String placeholder = (String) value;
                String path = placeholder.substring(2, placeholder.length() - 1);

                Object resolvedValue = resolvePath(path, results);
                resolved.put(entry.getKey(), resolvedValue != null ? resolvedValue : value);
            } else {
                resolved.put(entry.getKey(), value);
            }
        }

        return resolved;
    }

    /**
     * 解析路径，如 "getCurrentDate" 或 "getCurrentDate.year"
     */
    private Object resolvePath(String path, Map<String, Object> results) {
        String[] parts = path.split("\\.");
        Object current = results.get(parts[0]);

        for (int i = 1; i < parts.length && current != null; i++) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(parts[i]);
            } else if (current instanceof String) {
                // 字符串无法进一步解析
                break;
            }
        }

        return current;
    }

    /**
     * 拓扑排序
     */
    private List<ToolNode> topologicalSort(List<ToolNode> nodes) {
        List<ToolNode> sorted = new ArrayList<>();
        Set<ToolNode> visited = new HashSet<>();
        Set<ToolNode> tempMark = new HashSet<>();

        for (ToolNode node : nodes) {
            if (!visited.contains(node)) {
                dfs(node, visited, tempMark, sorted);
            }
        }

        return sorted;
    }

    private void dfs(ToolNode node, Set<ToolNode> visited, Set<ToolNode> tempMark, List<ToolNode> sorted) {
        if (tempMark.contains(node)) {
            throw new RuntimeException("检测到循环依赖: " + node.getToolName());
        }
        if (visited.contains(node)) return;

        tempMark.add(node);
        for (ToolNode dep : node.getDependencies()) {
            dfs(dep, visited, tempMark, sorted);
        }
        tempMark.remove(node);
        visited.add(node);
        sorted.add(node);
    }

    /**
     * 按依赖层级分组
     */
    private List<List<ToolNode>> groupByLevel(List<ToolNode> sorted) {
        Map<ToolNode, Integer> levels = new HashMap<>();

        for (ToolNode node : sorted) {
            int maxDepLevel = -1;
            for (ToolNode dep : node.getDependencies()) {
                maxDepLevel = Math.max(maxDepLevel, levels.getOrDefault(dep, -1));
            }
            levels.put(node, maxDepLevel + 1);
        }

        // 按层级分组
        Map<Integer, List<ToolNode>> levelGroups = new HashMap<>();
        for (Map.Entry<ToolNode, Integer> entry : levels.entrySet()) {
            levelGroups.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }

        // 按层级顺序返回
        List<List<ToolNode>> result = new ArrayList<>();
        for (int i = 0; i <= levelGroups.size(); i++) {
            if (levelGroups.containsKey(i)) {
                result.add(levelGroups.get(i));
            }
        }

        return result;
    }

    // ==================== 预定义工具链 ====================

    /**
     * 创建旅行规划工具链
     */
    public List<ToolNode> createTravelPlanChain(String destination, int days, String preference) {
        List<ToolNode> chain = new ArrayList<>();

        // 节点1：获取当前日期
        Map<String, Object> dateParams = new HashMap<>();
        ToolNode getDateNode = new ToolNode("getCurrentDate", dateParams, "currentDate");
        chain.add(getDateNode);

        // 节点2：计算结束日期（依赖当前日期）
        Map<String, Object> addDaysParams = new HashMap<>();
        addDaysParams.put("days", days);
        ToolNode addDaysNode = new ToolNode("addDays", addDaysParams, "endDate");
        addDaysNode.addDependency(getDateNode);
        chain.add(addDaysNode);

        // 节点3：查询天气
        Map<String, Object> weatherParams = new HashMap<>();
        weatherParams.put("city", destination);
        ToolNode weatherNode = new ToolNode("getCurrentWeather", weatherParams, "weather");
        chain.add(weatherNode);

        // 节点4：获取目的地信息
        Map<String, Object> infoParams = new HashMap<>();
        infoParams.put("city", destination);
        ToolNode infoNode = new ToolNode("getDestinationInfo", infoParams, "destinationInfo");
        chain.add(infoNode);

        // 节点5：获取景点推荐
        Map<String, Object> attractionsParams = new HashMap<>();
        attractionsParams.put("city", destination);
        attractionsParams.put("limit", 5);
        ToolNode attractionsNode = new ToolNode("getAttractions", attractionsParams, "attractions");
        chain.add(attractionsNode);

        // 节点6：获取美食推荐
        Map<String, Object> foodsParams = new HashMap<>();
        foodsParams.put("city", destination);
        ToolNode foodsNode = new ToolNode("getFoods", foodsParams, "foods");
        chain.add(foodsNode);

        // 节点7：生成行程（依赖景点推荐）
        Map<String, Object> itineraryParams = new HashMap<>();
        itineraryParams.put("city", destination);
        itineraryParams.put("days", days);
        itineraryParams.put("preference", preference);
        ToolNode itineraryNode = new ToolNode("generateItinerary", itineraryParams, "itinerary");
        itineraryNode.addDependency(attractionsNode);
        chain.add(itineraryNode);

        // 节点8：搜索酒店（依赖当前日期和结束日期）
        Map<String, Object> hotelsParams = new HashMap<>();
        hotelsParams.put("city", destination);
        hotelsParams.put("checkIn", "${currentDate}");
        hotelsParams.put("checkOut", "${endDate}");
        ToolNode hotelsNode = new ToolNode("searchHotels", hotelsParams, "hotels");
        hotelsNode.addDependencies(getDateNode, addDaysNode);
        chain.add(hotelsNode);

        return chain;
    }

    /**
     * 创建简单的天气查询链
     */
    public List<ToolNode> createWeatherChain(String city, int forecastDays) {
        List<ToolNode> chain = new ArrayList<>();

        // 当前天气
        Map<String, Object> weatherParams = new HashMap<>();
        weatherParams.put("city", city);
        chain.add(new ToolNode("getCurrentWeather", weatherParams, "currentWeather"));

        // 天气预报
        if (forecastDays > 0) {
            Map<String, Object> forecastParams = new HashMap<>();
            forecastParams.put("city", city);
            forecastParams.put("days", forecastDays);
            chain.add(new ToolNode("getForecast", forecastParams, "forecast"));
        }

        return chain;
    }
}