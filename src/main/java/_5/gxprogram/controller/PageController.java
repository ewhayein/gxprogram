package _5.gxprogram.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Thymeleaf HTML 페이지 라우팅 컨트롤러
@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    @GetMapping("/courses")
    public String courses() {
        return "courses";
    }
    @GetMapping("/mypage")
    public String mypage() {
        return "mypage";
    }
}
