package solo.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solo.project.dto.S3.FileUploadRequestDto;
import solo.project.service.FileUploadService;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final FileUploadService fileUploadService;

    @Operation(
            summary = "S3 파일 업로드",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "업로드할 파일",
                    required = true,
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = FileUploadRequestDto.class)
                    )
            ) //스웨거 테스트를 위해 임시 코드
    )
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<String> uploadFile(@ModelAttribute FileUploadRequestDto request) {
        String fileUrl = fileUploadService.uploadFile(request.getFile());
        return ResponseEntity.ok("파일 업로드를 완료 했습니다.");
    }
}
