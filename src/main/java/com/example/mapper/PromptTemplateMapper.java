package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.PromptTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 提示词模板 Mapper 接口
 */
@Mapper
public interface PromptTemplateMapper extends BaseMapper<PromptTemplate> {

    /**
     * 根据分类查询启用的模板
     * @param category 分类
     * @return 模板列表
     */
    List<PromptTemplate> selectActiveByCategory(@Param("category") String category);

    /**
     * 根据名称查询模板
     * @param name 模板名称
     * @return 模板
     */
    PromptTemplate selectByName(@Param("name") String name);

    /**
     * 批量更新模板状态
     * @param ids ID列表
     * @param isActive 状态
     * @return 更新数量
     */
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("isActive") String isActive);
}