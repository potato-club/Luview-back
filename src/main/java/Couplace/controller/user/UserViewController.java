package Couplace.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserViewController {

    @GetMapping("/loginpage")
    public String login() {
        return "loginpage";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }
}