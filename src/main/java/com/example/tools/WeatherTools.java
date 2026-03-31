package com.example.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 天气相关工具
 * 使用 @Tool 注解定义可供AI调用的函数
 */
@Component
public class WeatherTools {

    // 模拟天气数据（实际项目中可调用真实天气API）
    private static final Map<String, String[]> WEATHER_DATA = new HashMap<>();

    static {
        WEATHER_DATA.put("北京", new String[]{"晴", "22°C", "15°C", "东北风2级"});
        WEATHER_DATA.put("上海", new String[]{"多云", "25°C", "18°C", "东南风3级"});
        WEATHER_DATA.put("广州", new String[]{"阵雨", "28°C", "22°C", "南风2级"});
        WEATHER_DATA.put("深圳", new String[]{"阴", "27°C", "21°C", "微风"});
        WEATHER_DATA.put("杭州", new String[]{"晴", "23°C", "16°C", "东风2级"});
        WEATHER_DATA.put("成都", new String[]{"多云", "24°C", "17°C", "北风1级"});
        WEATHER_DATA.put("武汉", new String[]{"小雨", "26°C", "19°C", "东北风3级"});
        WEATHER_DATA.put("西安", new String[]{"晴", "21°C", "14°C", "西北风2级"});
        WEATHER_DATA.put("重庆", new String[]{"多云", "21°C", "14°C", "微风"});
    }

    /**
     * 获取当前天气
     * @Tool 注解的description帮助AI理解何时调用此工具[citation:1]
     */
    @Tool(description = "获取指定城市的当前天气信息，包括天气状况、温度、风力等")
    public String getCurrentWeather(
            @ToolParam(description = "城市名称，如：北京、上海、广州") String city) {

        System.out.println("====== 调用天气工具: " + city);

        String[] weather = WEATHER_DATA.get(city);
        if (weather == null) {
            return String.format("未找到城市「%s」的天气信息，请尝试查询北京、上海、广州等主要城市", city);
        }

        return String.format("【%s天气】%s，温度：%s，最低温：%s，风力：%s",
                city, weather[0], weather[1], weather[2], weather[3]);
    }

    /**
     * 获取未来天气预报（演示多参数工具）
     */
    @Tool(description = "获取指定城市未来几天的天气预报")
    public String getForecast(
            @ToolParam(description = "城市名称") String city,
            @ToolParam(description = "预报天数，可选值：1-7", required = false) Integer days) {

        if (days == null) {
            days = 3;
        }
        days = Math.min(days, 7);

        System.out.println("====== 调用天气预报工具: " + city + ", 天数: " + days);

        String[] weather = WEATHER_DATA.get(city);
        if (weather == null) {
            return String.format("未找到城市「%s」的天气信息", city);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("【%s未来%d天预报】\n", city, days));

        LocalDate today = LocalDate.now();
        for (int i = 1; i <= days; i++) {
            LocalDate date = today.plusDays(i);
            String dateStr = date.format(DateTimeFormatter.ofPattern("MM月dd日"));
            // 模拟预报：天气略有变化
            String forecastWeather = i % 2 == 0 ? "晴转多云" : weather[0];
            sb.append(String.format("  %s: %s，%s\n", dateStr, forecastWeather, weather[1]));
        }

        return sb.toString();
    }
}