package solo.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import solo.project.dto.jwt.UserDetailsImpl;
import solo.project.dto.mainpage.response.MainPageResponseDto;
import solo.project.service.Impl.MainPageServiceImpl;

@RestController
@RequestMapping("/main-page")
@Tag(name="MainPage Controller", description = "메인페이지 API")
@RequiredArgsConstructor
public class MainPageController {

    private final MainPageServiceImpl mainPageService;

    @Operation(summary = "메인페이지 조회")
    @GetMapping("/Check")
    public ResponseEntity<MainPageResponseDto> getMainPageData(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest request) {
        MainPageResponseDto mainPageData = mainPageService.getMainPageData(request, 2);
        return ResponseEntity.ok(mainPageData);
    }
}
