package solo.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import solo.project.dto.User.response.UserProfileResponseDto;
import solo.project.dto.file.FileRequestDto;
import solo.project.entity.File;
import solo.project.entity.User;
import solo.project.repository.File.FileRepository;
import solo.project.repository.UserRepository;
import solo.project.service.ImageService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserUploadController {

    private final UserRepository userRepository;
    private final ImageService imageService;
    private final FileRepository fileRepository; // QueryDSL 이용

    //프로필 업로드
    @Operation(summary = "프로필 사진 업로드")
    @PostMapping(
            value = "/{userId}/profile-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadProfileImage(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        imageService.uploadImages(Collections.singletonList(file), user
        );
        return ResponseEntity.ok("프로필 사진 업로드 성공");
    }

    /**
     * [PUT] 프로필 사진 수정/삭제
     *  - 새로 업로드할 파일(있다면) + 삭제 여부 정보(JSON) 를 multipart/form-data 로 함께 전송
     *  - @RequestPart 로 파일 파트와 JSON 파트를 동시에 매핑
     */
    @Operation(summary = "프로필 사진 수정, 삭제")
    @PutMapping(
            value = "/{userId}/profile-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> updateProfileImage(
            @PathVariable Long userId,
            @RequestPart(value = "file", required = false) MultipartFile newFile,
            @RequestPart(value = "requestDto", required = false) List<FileRequestDto> requestDto
    ) throws IOException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        List<MultipartFile> newFiles = (newFile != null)
                ? Collections.singletonList(newFile)
                : null;

        imageService.updateImages(user, newFiles, requestDto);
        return ResponseEntity.ok("프로필 파일 업데이트가 완료 되었습니다.");
    }

    /**
     *  - User + File(프로필 사진) DTO로 반환
     */
    @Operation(summary = "프로필 페이지")
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponseDto> getUserProfile(
            @PathVariable Long userId
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        // 커스텀 QueryDSL로 한 번에 조회
        UserProfileResponseDto userProfileDto = fileRepository.getUserProfile(user);
        return ResponseEntity.ok(userProfileDto);
    }

    /**
     * [GET] 내 정보 페이지
     *  - 프로필 사진, 소셜 로그인 여부, 유저 코드, 닉네임, 생년월일
     *  - UserProfileResponseDto 재활용
     */
    @Operation(summary = "내 정보 페이지")
    @GetMapping("/{userId}/my-info")
    public ResponseEntity<UserProfileResponseDto> getMyInfo(
            @PathVariable Long userId
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        UserProfileResponseDto dto = fileRepository.getUserProfile(user);
        return ResponseEntity.ok(dto);
    }
}
