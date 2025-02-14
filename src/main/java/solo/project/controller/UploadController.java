package solo.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import solo.project.dto.jwt.UserDetailsImpl;
import solo.project.entity.User;
import solo.project.service.ImageService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/uploads")
@Tag(name="Upload Controller", description = "프로필, 리뷰 사진 관련 API")
public class UploadController {

    private final ImageService imageService;

    //프로필 업로드 , 없으면 바로 올라가고 있으면 에러 ,,
    @Operation(summary = "프로필 사진 업로드")
    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadProfileImage(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam("file") MultipartFile file) throws IOException {
        User user = userDetails.getUser();
        // "프로필 업로드" -> 파일 1개만 넣어서 호출
        imageService.uploadProfileImage(user, file);
        return ResponseEntity.ok("프로필 사진 업로드 성공");
    }

    //프로필 수정, 기존 이미지 삭제 후 업데이트
    @Operation(summary = "프로필 사진 수정")
    @PutMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfileImage(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam("file") MultipartFile newFile) throws IOException {
        User user = userDetails.getUser();
        // "프로필 수정" -> 기존 것이 있어야 함
        imageService.updateProfileImage(user, newFile);
        return ResponseEntity.ok("프로필 파일 수정(교체) 완료");
    }

    //프로필 있으면 삭제
    @Operation(summary = "프로필 사진 삭제")
    @DeleteMapping("/profile-image")
    public ResponseEntity<?> deleteProfileImage(@AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        User user = userDetails.getUser();
        imageService.deleteProfileImage(user);
        return ResponseEntity.ok("프로필 사진 삭제 완료");
    }
}
