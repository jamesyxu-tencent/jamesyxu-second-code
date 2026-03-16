package com.example.module;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 简历实体类
 * 完整的简历信息模型
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Resume {

    /**
     * 基本信息
     */
    private BasicInfo basicInfo;

    /**
     * 工作经历列表
     */
    @JsonProperty("workExperiences")
    private List<WorkExperience> workExperiences;

    /**
     * 教育经历列表
     */
    @JsonProperty("educationList")
    private List<Education> educationList;

    /**
     * 技能列表
     */
    private List<String> skills;

    /**
     * 证书列表
     */
    private List<String> certifications;

    /**
     * 语言能力
     */
    private Map<String, String> languages;

    /**
     * 个人总结/自我评价
     */
    private String summary;

    /**
     * 期望职位
     */
    private String expectedPosition;

    /**
     * 期望薪资
     */
    private String expectedSalary;

    /**
     * 到岗时间
     */
    private String availableDate;

    /**
     * 基本信息内部类
     */
    @Data
    public static class BasicInfo {

        /**
         * 姓名
         */
        private String name;

        /**
         * 性别
         */
        private String gender;

        /**
         * 年龄
         */
        private Integer age;

        /**
         * 出生日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate birthDate;

        /**
         * 电话号码
         */
        private String phone;

        /**
         * 电子邮箱
         */
        private String email;

        /**
         * 微信
         */
        private String wechat;

        /**
         * 所在城市
         */
        private String city;

        /**
         * 工作年限
         */
        private Integer yearsOfExperience;

        /**
         * 学历
         */
        private String education;

        /**
         * 婚姻状况
         */
        private String maritalStatus;

        /**
         * 政治面貌
         */
        private String politicalStatus;

        /**
         * 个人网站/博客
         */
        private String personalWebsite;

        /**
         * GitHub
         */
        private String github;

    }

    /**
     * 工作经历内部类
     */
    @Data
    public static class WorkExperience {

        /**
         * 公司名称
         */
        private String company;

        /**
         * 职位
         */
        private String position;

        /**
         * 开始时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        /**
         * 结束时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;

        /**
         * 是否当前工作
         */
        private Boolean currentJob;

        /**
         * 工作职责列表
         */
        private String responsibilities;

        /**
         * 工作成就列表
         */
        private String achievements;

        /**
         * 使用的技术栈
         */
        private String technologies;

        /**
         * 所属部门
         */
        private String department;

        /**
         * 汇报对象
         */
        private String reportsTo;

        /**
         * 下属人数
         */
        private Integer teamSize;

        /**
         * 离职原因
         */
        private String leavingReason;

    }

    /**
     * 教育经历内部类
     */
    @Data
    public static class Education {

        /**
         * 学校名称
         */
        private String school;

        /**
         * 学位
         */
        private String degree;

        /**
         * 专业
         */
        private String major;

        /**
         * 开始时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        /**
         * 结束时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;

        /**
         * GPA/成绩
         */
        private String gpa;

        /**
         * 是否全日制
         */
        private Boolean fullTime;

        /**
         * 学历类型（统招/自考/成教）
         */
        private String educationType;

        /**
         * 主修课程
         */
        private List<String> majorCourses;

        /**
         * 荣誉奖项
         */
        private List<String> honors;

    }

    /**
     * 便捷方法：获取工作年限
     */
    public Integer getWorkYears() {
        if (basicInfo != null && basicInfo.getYearsOfExperience() != null) {
            return basicInfo.getYearsOfExperience();
        }

        if (workExperiences != null && !workExperiences.isEmpty()) {
            // 简单计算：从最早的工作到现在
            return workExperiences.size(); // 简化处理
        }

        return 0;
    }

    /**
     * 便捷方法：获取最新工作经历
     */
    public WorkExperience getLatestWorkExperience() {
        if (workExperiences == null || workExperiences.isEmpty()) {
            return null;
        }
        return workExperiences.get(0);
    }

    /**
     * 便捷方法：获取最高学历
     */
    public Education getHighestEducation() {
        if (educationList == null || educationList.isEmpty()) {
            return null;
        }

        // 按学位排序（博士 > 硕士 > 本科 > 大专）
        return educationList.get(0); // 简化处理
    }
}