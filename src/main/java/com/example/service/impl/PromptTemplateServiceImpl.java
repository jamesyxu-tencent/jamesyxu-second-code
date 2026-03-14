package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.PromptTemplate;
import com.example.mapper.PromptTemplateMapper;
import com.example.service.PromptTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 提示词模板 Service 实现类
 */
@Service
public class PromptTemplateServiceImpl extends ServiceImpl<PromptTemplateMapper, PromptTemplate> implements PromptTemplateService {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(.*?)\\}");

    @Override
    public List<PromptTemplate> getActiveByCategory(String category) {
        return baseMapper.selectActiveByCategory(category);
    }

    @Override
    public PromptTemplate getByName(String name) {
        return baseMapper.selectByName(name);
    }

    @Override
    public String getContentWithPlaceholders(String name, Map<String, String> placeholders) {
        PromptTemplate template = getByName(name);
        if (template == null || !"1".equals(template.getIsActive())) {
            throw new RuntimeException("模板不存在或已禁用: " + name);
        }

        String content = template.getContent();
        if (placeholders == null || placeholders.isEmpty()) {
            return content;
        }

        // 替换所有占位符 {key}
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = placeholders.getOrDefault(key, "{" + key + "}");
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableTemplate(Long id) {
        PromptTemplate template = new PromptTemplate();
        template.setId(id);
        template.setIsActive("1");
        return updateById(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableTemplate(Long id) {
        PromptTemplate template = new PromptTemplate();
        template.setId(id);
        template.setIsActive("0");
        return updateById(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateStatus(List<Long> ids, String isActive) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        return baseMapper.batchUpdateStatus(ids, isActive) > 0;
    }

    @Override
    public IPage<PromptTemplate> queryPage(IPage<PromptTemplate> page, String keyword, String category, String isActive) {
        LambdaQueryWrapper<PromptTemplate> wrapper = new LambdaQueryWrapper<>();

        // 关键词模糊搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(PromptTemplate::getName, keyword)
                    .or()
                    .like(PromptTemplate::getDescription, keyword)
                    .or()
                    .like(PromptTemplate::getContent, keyword)
            );
        }

        // 分类筛选
        if (StringUtils.hasText(category)) {
            wrapper.eq(PromptTemplate::getCategory, category);
        }

        // 状态筛选
        if (StringUtils.hasText(isActive)) {
            wrapper.eq(PromptTemplate::getIsActive, isActive);
        }

        // 按更新时间倒序
        wrapper.orderByDesc(PromptTemplate::getUpdateTime);

        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PromptTemplate copyTemplate(Long id, String newName) {
        // 1. 查询原模板
        PromptTemplate original = getById(id);
        if (original == null) {
            throw new RuntimeException("模板不存在，ID: " + id);
        }

        // 2. 检查新名称是否唯一
        if (!checkNameUnique(newName, null)) {
            throw new RuntimeException("模板名称已存在: " + newName);
        }

        // 3. 创建新模板
        PromptTemplate newTemplate = new PromptTemplate();
        newTemplate.setName(newName);
        newTemplate.setContent(original.getContent());
        newTemplate.setDescription(original.getDescription());
        newTemplate.setCategory(original.getCategory());
        newTemplate.setVersion(incrementVersion(original.getVersion()));
        newTemplate.setIsActive("0"); // 新模板默认禁用，需手动启用

        // 4. 保存
        save(newTemplate);
        return newTemplate;
    }

    @Override
    public boolean checkNameUnique(String name, Long excludeId) {
        LambdaQueryWrapper<PromptTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptTemplate::getName, name);

        if (excludeId != null) {
            wrapper.ne(PromptTemplate::getId, excludeId);
        }

        return count(wrapper) == 0;
    }

    /**
     * 版本号递增
     * @param version 原版本号
     * @return 新版本号
     */
    private String incrementVersion(String version) {
        if (version == null || version.isEmpty()) {
            return "1.0.0";
        }

        try {
            String[] parts = version.split("\\.");
            if (parts.length >= 3) {
                int minor = Integer.parseInt(parts[2]) + 1;
                return parts[0] + "." + parts[1] + "." + minor;
            }
        } catch (NumberFormatException e) {
            // 忽略解析错误
        }

        return version + ".1";
    }
}