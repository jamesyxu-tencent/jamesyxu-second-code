package com.example.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 计算器工具
 * 弥补LLM在数学计算方面的不足[citation:9]
 */
@Component
public class CalculatorTools {

    /**
     * 基础数学计算
     */
    @Tool(description = "执行基础数学计算，支持加减乘除运算，表达式格式如：1+2、10-5、3*4、8/2")
    public String calculate(
            @ToolParam(description = "数学表达式，如：1+2、10-5、3*4、8/2") String expression) {

        System.out.println("====== 调用计算器工具: " + expression);

        try {
            // 移除空格
            expression = expression.replaceAll("\\s", "");

            // 简单计算器实现
            double result = evaluateExpression(expression);
            return String.format("%s = %.2f", expression, result);

        } catch (Exception e) {
            return String.format("计算失败：%s，请检查表达式格式", e.getMessage());
        }
    }

    /**
     * 简单表达式求值
     * 支持 + - * /
     */
    private double evaluateExpression(String expr) {
        // 先处理乘除
        if (expr.contains("*") || expr.contains("/")) {
            // 简化实现：查找运算符位置
            int mulPos = expr.indexOf('*');
            int divPos = expr.indexOf('/');
            int opPos = -1;
            char op = ' ';

            if (mulPos > 0 && (divPos == -1 || mulPos < divPos)) {
                opPos = mulPos;
                op = '*';
            } else if (divPos > 0) {
                opPos = divPos;
                op = '/';
            }

            if (opPos > 0) {
                double left = Double.parseDouble(expr.substring(0, opPos));
                double right = Double.parseDouble(expr.substring(opPos + 1));
                double result = op == '*' ? left * right : left / right;
                return result;
            }
        }

        // 处理加减
        if (expr.contains("+")) {
            String[] parts = expr.split("\\+");
            double result = 0;
            for (String part : parts) {
                result += evaluateExpression(part);
            }
            return result;
        }

        if (expr.contains("-")) {
            // 处理负数情况
            int lastMinus = expr.lastIndexOf('-');
            if (lastMinus > 0) {
                double left = Double.parseDouble(expr.substring(0, lastMinus));
                double right = Double.parseDouble(expr.substring(lastMinus + 1));
                return left - right;
            }
        }

        // 纯数字
        return Double.parseDouble(expr);
    }

    /**
     * 单位转换
     */
    @Tool(description = "进行单位转换，支持：温度(摄氏度/华氏度)、长度(米/公里/英里)、重量(千克/磅)")
    public String convertUnit(
            @ToolParam(description = "转换类型：temperature/length/weight") String type,
            @ToolParam(description = "源单位") String fromUnit,
            @ToolParam(description = "目标单位") String toUnit,
            @ToolParam(description = "数值") double value) {

        System.out.println("====== 调用单位转换工具: " + type + ", " + value + fromUnit + "->" + toUnit);

        switch (type.toLowerCase()) {
            case "temperature":
                return convertTemperature(fromUnit, toUnit, value);
            case "length":
                return convertLength(fromUnit, toUnit, value);
            case "weight":
                return convertWeight(fromUnit, toUnit, value);
            default:
                return "不支持的转换类型：" + type;
        }
    }

    private String convertTemperature(String from, String to, double value) {
        if (from.equals("celsius") && to.equals("fahrenheit")) {
            double result = value * 9 / 5 + 32;
            return String.format("%.1f°C = %.1f°F", value, result);
        } else if (from.equals("fahrenheit") && to.equals("celsius")) {
            double result = (value - 32) * 5 / 9;
            return String.format("%.1f°F = %.1f°C", value, result);
        }
        return "不支持的温度单位转换";
    }

    private String convertLength(String from, String to, double value) {
        if (from.equals("km") && to.equals("mile")) {
            double result = value * 0.621371;
            return String.format("%.2f km = %.2f 英里", value, result);
        } else if (from.equals("mile") && to.equals("km")) {
            double result = value * 1.60934;
            return String.format("%.2f 英里 = %.2f km", value, result);
        }
        return "不支持的长度单位转换";
    }

    private String convertWeight(String from, String to, double value) {
        if (from.equals("kg") && to.equals("lb")) {
            double result = value * 2.20462;
            return String.format("%.2f kg = %.2f 磅", value, result);
        } else if (from.equals("lb") && to.equals("kg")) {
            double result = value * 0.453592;
            return String.format("%.2f 磅 = %.2f kg", value, result);
        }
        return "不支持的重量单位转换";
    }
}