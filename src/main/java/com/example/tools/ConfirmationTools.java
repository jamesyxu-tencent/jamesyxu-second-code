package com.example.tools;

import com.example.module.PendingOperation;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 确认工具 - 实现敏感操作的确认机制
 */
@Component
public class ConfirmationTools {

    // 存储待确认的操作
    private static final Map<String, PendingOperation> pendingOperations = new HashMap<>();

    /**
     * 请求用户确认（敏感操作前调用）
     */
    @Tool(description = "在执行敏感操作前，请求用户确认。用户确认后系统会自动执行")
    public String requestConfirmation(
            @ToolParam(description = "操作描述，说明要做什么") String operation,
            @ToolParam(description = "操作的风险等级：low/medium/high") String riskLevel,
            @ToolParam(description = "操作的详细信息") String details) {

        System.out.println("=== 请求用户确认 ===");

        String confirmId = UUID.randomUUID().toString().substring(0, 8);

        PendingOperation pending = new PendingOperation();
        pending.setOperation(operation);
        pending.setDetails(details);
        pending.setRiskLevel(riskLevel);
        pending.setStatus("pending");

        pendingOperations.put(confirmId, pending);

        // 返回给AI，让AI展示给用户
        if ("high".equals(riskLevel) || "medium".equals(riskLevel)) {
            return String.format("""
                    【需要确认】操作ID: %s
                    操作: %s。
                    """, confirmId, operation);
        }

        return null;
    }

    /**
     * 执行已确认的操作
     */
    public String executeConfirmed(String confirmId) {
        PendingOperation pending = pendingOperations.get(confirmId);
        if (pending == null) {
            return "操作不存在或已过期";
        }

        if (!"pending".equals(pending.getStatus())) {
            return "操作已被处理过";
        }

        pending.setStatus("confirmed");

        // 这里执行实际操作
        return String.format("✅ 已确认并执行: %s\n详情: %s",
                pending.getOperation(), pending.getDetails());
    }

    /**
     * 拒绝操作
     */
    public String rejectOperation(String confirmId) {
        PendingOperation pending = pendingOperations.get(confirmId);
        if (pending == null) {
            return "操作不存在";
        }

        pending.setStatus("rejected");
        pendingOperations.remove(confirmId);

        return String.format("❌ 已拒绝操作: %s", pending.getOperation());
    }

}