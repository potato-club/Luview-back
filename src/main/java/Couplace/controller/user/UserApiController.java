package Couplace.controller.user;

import Couplace.dto.AddUserRequest;
import Couplace.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class UserApiController {

    private final UserService userService;

    @PostMapping("/user")
    public String signup(AddUserRequest request, Model model) {
        try {
            userService.save(request);
            return "redirect:/loginpage";
        } catch (Exception e) {
            // 에러 메시지를 모델에 추가하여 에러 페이지로 전달
            model.addAttribute("error", "회원가입 중 오류가 발생했습니다: " + e.getMessage());
            return "signup";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/loginpage";
    }

    // 모든 예외를 처리하는 핸들러
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception e, Model model) {
        model.addAttribute("error", "서버 오류: " + e.getMessage());
        return "error"; // 에러 페이지 템플릿
    }
}
