package com.example.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailTools {

    @Autowired
    private ConfirmationTools confirmationTools;

    /**
     * 发送邮件（需要用户确认）
     */
    @Tool(description = "发送电子邮件。这是一个敏感操作，需要用户确认后才能执行")
    public String sendEmail(
            @ToolParam(description = "收件人邮箱") String to,
            @ToolParam(description = "邮件主题") String subject,
            @ToolParam(description = "邮件内容") String content) {

        System.out.println("====== 调用邮件工具: sendEmail, " + to + ", " + subject + ", " + content);

        // 先请求确认
        String confirmResult = confirmationTools.requestConfirmation(
                String.format("发送邮件到%s", to),
                "medium",
                String.format("主题: %s\n内容: %s", subject, content)
        );

        return confirmResult;
    }

    /**
     * 执行实际的邮件发送（确认后调用）
     */
    @Tool(description = "确认发送电子邮件")
    public String confirmSendEmail(@ToolParam(description = "收件人") String to,
                                   @ToolParam(description = "邮件主题") String subject,
                                   @ToolParam(description = "邮件内容") String content) {

        System.out.println("====== 执行邮件发送: " + to + ", " + subject + ", " + content);

        // 实际发送邮件的逻辑
        System.out.println("=== 发送邮件 ===");
        System.out.println("收件人: " + to);
        System.out.println("主题: " + subject);
        System.out.println("内容: " + content);

        return "✅ 邮件已发送成功！";
    }

    /**
     * 执行实际的邮件发送（确认后调用）
     */
    @Tool(description = "取消发送电子邮件")
    public String cancelSendEmail() {
        return "✅ 已取消发送邮件！";
    }
}