package com.example.service.impl;

import com.example.module.PersonInfo;
import com.example.module.Resume;
import com.example.service.IChatService;
import com.example.service.IOutputParserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class OutputParserServiceImpl implements IOutputParserService {

    @Autowired
    private IChatService chatService;

    private final ObjectMapper objectMapper;

    public OutputParserServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 解析PersonInfo
     */
    @Override
    public PersonInfo parsePersonInfo(String text) throws IOException, InterruptedException {
        String instruction = "从以下文本中提取人员信息，包括姓名、年龄、职业、技能列表、联系方式（邮箱、电话、地址）";
        return parseToObject(instruction, text, PersonInfo.class);
    }

    /**
     * 解析简历
     */
    @Override
    public Resume parseResume(String text) throws IOException, InterruptedException {
        String instruction = """
                从以下简历文本中提取完整信息，包括：
                1. 个人信息(basicInfo)必须包含：姓名、年龄、电话、邮箱
                2. 工作经验(workExperiences)列表：每段工作包括公司、职位、起止时间、主要职责、成就
                3. 教育经历(educationList)列表：学校、学位、专业、起止时间、GPA(如果有)
                4. 技能(skills)列表：技术技能列表
                5. 证书(certifications)列表：专业认证列表
                6. 语言能力(languages)列表：语言能力列表
                7. 个人总结(summary)：简要概括候选人的特点
                8. 期望职位(expectedPosition)
                9. 期望薪资(expectedSalary)
                10. 到岗时间(availableDate)
                
                注意事项：
                - 日期格式使用ISO格式：YYYY-MM-DD
                - 如果没有某个字段，使用null
                - 只返回JSON，不要包含其他文字
                """;
        return parseToObject(instruction, text, Resume.class);
    }

    /**
     * 解析为指定Java对象
     */
    private <T> T parseToObject(String instruction, String input, Class<T> clazz)
            throws IOException, InterruptedException {

        // 构建提示词，要求AI返回JSON
        String systemPrompt = String.format("""
            %s
            
            请严格按照以下JSON格式返回数据，只返回JSON，不要包含其他说明文字：
            {
                // 根据%s类的字段定义返回JSON
            }
            
            要求：
            1. 字段名与Java类保持一致
            2. 日期格式使用：yyyy-MM-dd
            3. 如果没有值，用null
            """, instruction, clazz.getSimpleName());

        String prompt = systemPrompt + "\n\n输入文本：\n" + input;

        // 调用AI
        String response = chatService.chat(prompt, null);

        // 解析JSON为Java对象
        try {
            return objectMapper.readValue(response, clazz);
        } catch (JsonProcessingException e) {
            // 如果直接解析失败，尝试提取JSON部分
            String json = extractJsonFromResponse(response);
            return objectMapper.readValue(json, clazz);
        }
    }

    /**
     * 解析为Map
     */
    private Map<String, Object> parseToMap(String instruction, String input)
            throws IOException, InterruptedException {

        String systemPrompt = instruction + "\n\n请返回JSON格式的数据。";
        String response = chatService.chat(systemPrompt + "\n\n" + input, null);

        try {
            return objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            String json = extractJsonFromResponse(response);
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        }
    }

    /**
     * 解析为List
     */
    private List<String> parseToList(String instruction, String input)
            throws IOException, InterruptedException {

        String systemPrompt = instruction + "\n\n请返回JSON数组格式，如：[\"item1\", \"item2\"]";
        String response = chatService.chat(systemPrompt + "\n\n" + input, null);

        try {
            return objectMapper.readValue(response, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            String json = extractJsonFromResponse(response);
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        }
    }

    /**
     * 从AI响应中提取JSON部分
     */
    private String extractJsonFromResponse(String response) {
        // 查找JSON的开始和结束
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');

        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }

        // 尝试查找数组
        start = response.indexOf('[');
        end = response.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }

        return response;
    }

}
