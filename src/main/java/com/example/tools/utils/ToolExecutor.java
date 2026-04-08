package com.example.tools.utils;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 工具执行器
 */
public class ToolExecutor {

    private final Object target;
    private final Method method;

    ToolExecutor(Object target, Method method) {
        this.target = target;
        this.method = method;
    }

    Object execute(Map<String, Object> params) throws Exception {
        // 根据参数类型构建方法参数
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] args = new Object[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {
            String paramName = getParameterName(this.method, i);
            Object value = params.get(paramName);

            if (value == null) {
                // 尝试使用默认值
                value = getDefaultValue(paramTypes[i]);
            }

            args[i] = convertValue(value, paramTypes[i]);
        }

        return method.invoke(target, args);
    }

    private String getParameterName(Method method, int index) {
        // 关键：获取方法真实的参数名！
        java.lang.reflect.Parameter[] parameters = method.getParameters();
        return parameters[index].getName();
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;

        if (targetType == int.class || targetType == Integer.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(value.toString());
        }

        if (targetType == double.class || targetType == Double.class) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            return Double.parseDouble(value.toString());
        }

        if (targetType == boolean.class || targetType == Boolean.class) {
            if (value instanceof Boolean) {
                return value;
            }
            return Boolean.parseBoolean(value.toString());
        }

        return value.toString();
    }

    private Object getDefaultValue(Class<?> type) {
        if (type == int.class || type == Integer.class) return 0;
        if (type == double.class || type == Double.class) return 0.0;
        if (type == boolean.class || type == Boolean.class) return false;
        return null;
    }
}
