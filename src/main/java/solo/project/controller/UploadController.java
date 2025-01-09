package solo.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import solo.project.dto.file.FileRequestDto;
import solo.project.entity.File;
import solo.project.entity.Review;
import solo.project.entity.User;
import solo.project.repository.ReviewRepository;
import solo.project.repository.UserRepository;
import solo.project.service.ImageService;
import solo.project.service.Impl.ImageServiceImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/s3")
@Tag(name = "AWS S3 Download Controller", description = "S3 다운로드 API")
public class UploadController {

    private final ImageService imageService;

    @Operation(summary = "S3 Download API")
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> s3Download(@RequestParam String key) {
        try {
            byte[] data = imageService.downloadImage(key);
            InputStream inputStream = new ByteArrayInputStream(data);
            InputStreamResource resource = new InputStreamResource(inputStream);
            return ResponseEntity.ok()
                    .contentLength(data.length)
                    .header("Content-type", "application/octet-stream")
                    .header("Content-Disposition", "attachment; filename=" +
                            URLEncoder.encode(key, "UTF-8"))
                    .body(resource);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().contentLength(0).body(null);
        }
    }
}