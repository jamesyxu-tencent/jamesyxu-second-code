package com.example.module;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 人员信息实体类
 * 用于从文本中提取个人基本信息
 */
@Data
public class PersonInfo {

    /**
     * 姓名
     */
    private String name;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 职业/职位
     */
    private String occupation;

    /**
     * 技能列表
     */
    private List<String> skills;

    /**
     * 联系方式
     */
    private Contact contactInfo;

    /**
     * 内部类：联系方式
     */
    @Data
    public static class Contact {
        /**
         * 电子邮箱
         */
        private String email;

        /**
         * 电话号码
         */
        private String phone;

        /**
         * 地址
         */
        private String address;

        /**
         * 微信
         */
        private String wechat;

        /**
         * QQ
         */
        private String qq;
    }

}