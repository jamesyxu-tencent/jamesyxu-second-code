package com.example.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // 更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // 创建人（可以从SecurityContext或ThreadLocal获取当前用户）
        this.strictInsertFill(metaObject, "createBy", String.class, getCurrentUser());
        // 更新人
        this.strictInsertFill(metaObject, "updateBy", String.class, getCurrentUser());
        // 逻辑删除默认值（未删除）
        this.strictInsertFill(metaObject, "deleted", Boolean.class, false);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // 更新人
        this.strictUpdateFill(metaObject, "updateBy", String.class, getCurrentUser());
    }

    /**
     * 获取当前用户（根据你的认证方式实现）
     * 示例：从Spring Security中获取
     */
    private String getCurrentUser() {
        // 这里需要根据你的实际认证方式来实现
        // 例如：return SecurityContextHolder.getContext().getAuthentication().getName();
        return "system"; // 默认值，实际使用时替换
    }
}