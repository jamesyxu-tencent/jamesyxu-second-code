package com.example.tools;

import com.example.module.travel.Hotel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class HotelTools {

    // 酒店数据库
    private static final Map<String, List<Hotel>> HOTELS = new HashMap<>();

    static {
        HOTELS.put("北京", Arrays.asList(
                new Hotel("北京王府井希尔顿", "东城区王府井大街", 1200, 4.8),
                new Hotel("北京香格里拉饭店", "海淀区紫竹院路", 1500, 4.7),
                new Hotel("北京如家精选", "西城区西单", 400, 4.3)
        ));

        HOTELS.put("上海", Arrays.asList(
                new Hotel("上海外滩华尔道夫", "黄浦区中山东一路", 2500, 4.9),
                new Hotel("上海和平饭店", "黄浦区南京东路", 1800, 4.8),
                new Hotel("上海全季酒店", "静安区南京西路", 500, 4.4)
        ));

        HOTELS.put("杭州", Arrays.asList(
                new Hotel("杭州西湖国宾馆", "西湖区杨公堤", 1600, 4.8),
                new Hotel("杭州香格里拉", "西湖区北山路", 1300, 4.7),
                new Hotel("杭州汉庭酒店", "上城区延安路", 350, 4.2)
        ));
    }

    /**
     * 搜索酒店
     */
    @Tool(description = "根据目的地和日期搜索可用酒店，返回酒店名称、位置、价格和评分")
    public String searchHotels(
            @ToolParam(description = "目的地城市名称") String city,
            @ToolParam(description = "入住日期，格式：yyyy-MM-dd") String checkIn,
            @ToolParam(description = "退房日期，格式：yyyy-MM-dd") String checkOut) {

        System.out.println("====== 调用酒店工具: searchHotels, " + city);

        List<Hotel> hotels = HOTELS.getOrDefault(city, new ArrayList<>());

        if (hotels.isEmpty()) {
            return String.format("未找到%s的酒店信息", city);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("【%s酒店推荐】\n入住：%s | 退房：%s\n\n", city, checkIn, checkOut));

        for (int i = 0; i < hotels.size(); i++) {
            Hotel h = hotels.get(i);
            sb.append(String.format("%d. 🏨 %s\n", i + 1, h.getName()));
            sb.append(String.format("   位置：%s\n", h.getLocation()));
            sb.append(String.format("   参考价：¥%d/晚 | 评分：%.1f\n", h.getPrice(), h.getRating()));
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 预订酒店（需要确认）
     */
    @Tool(description = "预订酒店，这是一个需要用户确认的敏感操作")
    public String bookHotel(
            @ToolParam(description = "酒店名称") String hotelName,
            @ToolParam(description = "入住日期") String checkIn,
            @ToolParam(description = "退房日期") String checkOut,
            @ToolParam(description = "入住人数") int guests) {

        System.out.println("====== 调用酒店工具: bookHotel, " + hotelName);

        return String.format("""
            【需要确认】预订酒店
            操作ID: BOOK_%d
            酒店: %s
            入住: %s
            退房: %s
            人数: %d人
            
            请确认是否预订？
            """, System.currentTimeMillis(), hotelName, checkIn, checkOut, guests);
    }
}