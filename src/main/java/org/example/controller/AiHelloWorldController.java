package org.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class AiHelloWorldController {

    @GetMapping("/api/hello")
    public RedirectView showHelloPage() {
        return new RedirectView("/index.html");
    }


    @GetMapping("/api/sayHello")
    public String sayHello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format("Hello %s!欢迎进入 AI 开发世界！", name);
    }

}