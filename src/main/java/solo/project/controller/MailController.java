package solo.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import solo.project.dto.mail.EmailDto;
import solo.project.service.Impl.MailServiceImpl;

import java.io.UnsupportedEncodingException;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/email")
public class MailController {
    private final MailServiceImpl mailService;

    @ResponseBody
    @Operation(summary = "메일인증")
    @PostMapping("/check")
    public String emailCheck(@RequestBody EmailDto mailDTO) throws MessagingException, UnsupportedEncodingException {
        String authCode = mailService.sendSimpleMessage(mailDTO.getEmail());
        return authCode; // Response body에 값을 반환
    }
}