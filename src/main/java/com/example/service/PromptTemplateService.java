package com.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.PromptTemplate;

import java.util.List;
import java.util.Map;

/**
 * 提示词模板 Service 接口
 */
public interface PromptTemplateService extends IService<PromptTemplate> {

    /**
     * 根据分类查询启用的模板
     * @param category 分类
     * @return 模板列表
     */
    List<PromptTemplate> getActiveByCategory(String category);

    /**
     * 根据名称查询模板
     * @param name 模板名称
     * @return 模板
     */
    PromptTemplate getByName(String name);

    /**
     * 根据名称获取模板内容（带占位符替换）
     * @param name 模板名称
     * @param placeholders 占位符键值对
     * @return 替换后的内容
     */
    String getContentWithPlaceholders(String name, Map<String, String> placeholders);

    /**
     * 启用模板
     * @param id 模板ID
     * @return 是否成功
     */
    boolean enableTemplate(Long id);

    /**
     * 禁用模板
     * @param id 模板ID
     * @return 是否成功
     */
    boolean disableTemplate(Long id);

    /**
     * 批量启用/禁用
     * @param ids ID列表
     * @param isActive 状态
     * @return 是否成功
     */
    boolean batchUpdateStatus(List<Long> ids, String isActive);

    /**
     * 分页条件查询
     * @param page 分页参数
     * @param keyword 关键词
     * @param category 分类
     * @param isActive 状态
     * @return 分页结果
     */
    IPage<PromptTemplate> queryPage(IPage<PromptTemplate> page, String keyword, String category, String isActive);

    /**
     * 复制模板（创建新版本）
     * @param id 原模板ID
     * @param newName 新模板名称
     * @return 新模板
     */
    PromptTemplate copyTemplate(Long id, String newName);

    /**
     * 校验模板名称是否唯一
     * @param name 模板名称
     * @param excludeId 排除的ID（修改时用）
     * @return 是否唯一
     */
    boolean checkNameUnique(String name, Long excludeId);

    /**
     * 使用模板生成回答
     *
     * @param templateName 模板名称
     * @param params       参数映射，用于替换模板中的占位符
     */
    String generateWithTemplate(String templateName,
                                Map<String, Object> params);

    /**
     * 初始化一些常用模板
     */
    void initDefaultTemplates();
}