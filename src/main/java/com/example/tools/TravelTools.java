package com.example.tools;

import com.example.module.travel.Attraction;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 旅行规划工具集
 */
@Component
public class TravelTools {

    // 景点数据库
    private static final Map<String, List<Attraction>> ATTRACTIONS = new HashMap<>();

    // 美食数据库
    private static final Map<String, List<String>> FOODS = new HashMap<>();

    static {
        // 北京景点
        ATTRACTIONS.put("北京", Arrays.asList(
                new Attraction("故宫博物院", "明清皇家宫殿", 60, 4.8),
                new Attraction("长城", "世界七大奇迹之一", 40, 4.9),
                new Attraction("颐和园", "皇家园林", 30, 4.7),
                new Attraction("天坛", "明清皇帝祭天场所", 15, 4.6),
                new Attraction("鸟巢", "2008奥运会主体育场", 50, 4.5)
        ));

        // 上海景点
        ATTRACTIONS.put("上海", Arrays.asList(
                new Attraction("外滩", "万国建筑博览群", 0, 4.8),
                new Attraction("东方明珠", "上海地标", 199, 4.7),
                new Attraction("迪士尼乐园", "童话世界", 399, 4.9),
                new Attraction("豫园", "江南古典园林", 40, 4.6),
                new Attraction("南京路步行街", "中华第一商业街", 0, 4.5)
        ));

        // 杭州景点
        ATTRACTIONS.put("杭州", Arrays.asList(
                new Attraction("西湖", "人间天堂", 0, 4.9),
                new Attraction("灵隐寺", "千年古刹", 75, 4.7),
                new Attraction("宋城", "宋代文化主题公园", 300, 4.6),
                new Attraction("千岛湖", "天下第一秀水", 130, 4.8)
        ));

        // 美食
        FOODS.put("北京", Arrays.asList("北京烤鸭", "炸酱面", "豆汁焦圈", "涮羊肉", "驴打滚"));
        FOODS.put("上海", Arrays.asList("小笼包", "生煎包", "蟹粉豆腐", "红烧肉", "糖醋排骨"));
        FOODS.put("杭州", Arrays.asList("西湖醋鱼", "东坡肉", "龙井虾仁", "叫花鸡", "片儿川"));
        FOODS.put("成都", Arrays.asList("火锅", "串串香", "担担面", "麻婆豆腐", "夫妻肺片"));
        FOODS.put("西安", Arrays.asList("肉夹馍", "羊肉泡馍", "凉皮", "biangbiang面", "甑糕"));
    }

    /**
     * 获取目的地基本信息
     */
    @Tool(description = "获取旅行目的地的基本信息，包括简介、最佳旅行时间等")
    public String getDestinationInfo(
            @ToolParam(description = "目的地城市名称，如：北京、上海、杭州") String city) {

        System.out.println("====== 调用旅行工具: getDestinationInfo, " + city);

        Map<String, String> cityInfo = new HashMap<>();
        cityInfo.put("北京", "中国的首都，历史文化名城，拥有故宫、长城等世界文化遗产。最佳旅行时间：4-10月");
        cityInfo.put("上海", "国际大都市，现代与传统的完美结合。最佳旅行时间：3-5月、9-11月");
        cityInfo.put("杭州", "人间天堂，以西湖闻名天下。最佳旅行时间：3-5月、9-11月");
        cityInfo.put("成都", "天府之国，美食之都。最佳旅行时间：3-6月、9-11月");
        cityInfo.put("西安", "十三朝古都，丝绸之路起点。最佳旅行时间：3-5月、9-10月");

        String info = cityInfo.getOrDefault(city, city + "是一座美丽的城市，值得一游。");

        return String.format("【%s旅行信息】\n%s", city, info);
    }

    /**
     * 获取景点推荐
     */
    @Tool(description = "获取旅行目的地的热门景点推荐，返回景点名称、简介、门票价格和评分")
    public String getAttractions(
            @ToolParam(description = "目的地城市名称") String city,
            @ToolParam(description = "推荐数量，默认5个", required = false) Integer limit) {

        System.out.println("====== 调用旅行工具: getAttractions, " + city);

        int count = limit != null ? Math.min(limit, 5) : 5;
        List<Attraction> attractions = ATTRACTIONS.getOrDefault(city, new ArrayList<>());

        if (attractions.isEmpty()) {
            return String.format("未找到%s的景点信息", city);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("【%s热门景点推荐】\n\n", city));

        for (int i = 0; i < Math.min(count, attractions.size()); i++) {
            Attraction a = attractions.get(i);
            sb.append(String.format("%d. 🏛️ %s\n", i + 1, a.getName()));
            sb.append(String.format("   简介：%s\n", a.getDescription()));
            sb.append(String.format("   门票：¥%d | 评分：%.1f\n", a.getPrice(), a.getRating()));
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 获取美食推荐
     */
    @Tool(description = "获取旅行目的地的特色美食推荐")
    public String getFoods(
            @ToolParam(description = "目的地城市名称") String city) {

        System.out.println("====== 调用旅行工具: getFoods, " + city);

        List<String> foods = FOODS.getOrDefault(city, Arrays.asList("当地特色美食"));

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("【%s特色美食】\n", city));
        for (int i = 0; i < foods.size(); i++) {
            sb.append(String.format("  %d. 🍜 %s\n", i + 1, foods.get(i)));
        }

        return sb.toString();
    }

    /**
     * 生成每日行程
     */
    @Tool(description = "根据目的地和天数生成详细的每日行程安排")
    public String generateItinerary(
            @ToolParam(description = "目的地城市名称") String city,
            @ToolParam(description = "旅行天数") int days,
            @ToolParam(description = "旅行偏好：culture/food/nature/relax", required = false) String preference) {

        System.out.println("====== 调用旅行工具: generateItinerary, " + city + ", " + days + "天");

        String pref = preference != null ? preference : "culture";
        List<Attraction> attractions = ATTRACTIONS.getOrDefault(city, new ArrayList<>());

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("【%s%d日行程安排】\n", city, days));
        sb.append("=".repeat(40)).append("\n\n");

        // 根据偏好排序景点
        List<Attraction> sorted = new ArrayList<>(attractions);
        if ("food".equals(pref)) {
            // 美食偏好：随机排序
            Collections.shuffle(sorted);
        } else if ("nature".equals(pref)) {
            // 自然偏好：优先推荐低门票景点
            sorted.sort(Comparator.comparingInt(Attraction::getPrice));
        } else {
            // 文化偏好：按评分排序
            sorted.sort((a, b) -> Double.compare(b.getRating(), a.getRating()));
        }

        for (int day = 1; day <= days && day <= sorted.size(); day++) {
            Attraction a = sorted.get(day - 1);
            sb.append(String.format("第%d天：\n", day));
            sb.append(String.format("  上午：参观 %s（%.1f分）\n", a.getName(), a.getRating()));
            sb.append(String.format("  下午：游览%s周边区域\n", a.getName()));

            if (day % 2 == 0 && !FOODS.getOrDefault(city, new ArrayList<>()).isEmpty()) {
                sb.append(String.format("  晚上：品尝%s特色美食\n", city));
            }
            sb.append("\n");
        }

        // 添加预算估算
        double estimatedCost = attractions.stream()
                .limit(days)
                .mapToDouble(a -> a.getPrice())
                .sum() + days * 200;  // 餐饮住宿估算

        sb.append(String.format("💰 预算估算：约¥%.0f（含门票、餐饮、交通）\n", estimatedCost));

        return sb.toString();
    }

}