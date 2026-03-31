package com.example.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 日期时间相关工具
 * AI模型无法获取实时时间，通过工具提供此能力[citation:5]
 */
@Component
public class DateTimeTools {

    /**
     * 获取当前日期时间
     */
    @Tool(description = "获取当前的日期和时间，包括年月日时分秒")
    public String getCurrentDateTime() {
        System.out.println("====== 调用时间工具: getCurrentDateTime");
        LocalDateTime now = LocalDateTime.now();
        return now.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"));
    }

    /**
     * 获取当前日期
     */
    @Tool(description = "获取今天的日期")
    public String getCurrentDate() {
        System.out.println("====== 调用时间工具: getCurrentDate");
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
    }

    /**
     * 计算日期差
     */
    @Tool(description = "计算两个日期之间的天数差")
    public String calculateDateDifference(
            @ToolParam(description = "开始日期，格式：yyyy-MM-dd") String startDate,
            @ToolParam(description = "结束日期，格式：yyyy-MM-dd") String endDate) {

        System.out.println("====== 调用时间工具: calculateDateDifference, " + startDate + " -> " + endDate);

        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            long daysBetween = ChronoUnit.DAYS.between(start, end);
            return String.format("从 %s 到 %s 相差 %d 天", startDate, endDate, Math.abs(daysBetween));
        } catch (Exception e) {
            return "日期格式错误，请使用 yyyy-MM-dd 格式";
        }
    }

    /**
     * 计算N天后的日期
     */
    @Tool(description = "计算从今天起N天后的日期")
    public String addDays(
            @ToolParam(description = "要增加的天数") int days) {

        System.out.println("====== 调用时间工具: addDays, " + days);

        LocalDate futureDate = LocalDate.now().plusDays(days);
        return String.format("%d天后的日期是：%s", days,
                futureDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
    }
}