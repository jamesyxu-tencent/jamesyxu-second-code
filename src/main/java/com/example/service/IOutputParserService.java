package com.example.service;

import com.example.module.PersonInfo;
import com.example.module.Resume;

import java.io.IOException;

public interface IOutputParserService {

    /**
     * 人员信息解析为对象
     *
     * @param text
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    PersonInfo parsePersonInfo(String text) throws IOException, InterruptedException;

    /**
     * 解析简历为对象
     *
     * @param text
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    Resume parseResume(String text) throws IOException, InterruptedException;

}
