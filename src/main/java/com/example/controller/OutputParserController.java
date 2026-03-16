package com.example.controller;

import com.example.module.PersonInfo;
import com.example.module.Resume;
import com.example.service.IOutputParserService;
import com.example.vo.base.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class OutputParserController {

    @Autowired
    private IOutputParserService outputParserService;

    @PostMapping("/api/parserPerson")
    public ApiResult<PersonInfo> parserPerson(@RequestParam("input") String input) throws IOException, InterruptedException {
        PersonInfo personInfo = outputParserService.parsePersonInfo(input);
        return ApiResult.success(personInfo);
    }

    @PostMapping("/api/parserResume")
    public ApiResult<Resume> parserResume(@RequestParam("input") String input) throws IOException, InterruptedException {
        Resume resume = outputParserService.parseResume(input);
        return ApiResult.success(resume);
    }
}
