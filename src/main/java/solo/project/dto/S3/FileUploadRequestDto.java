package solo.project.dto.S3;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "파일 업로드 요청")
public class FileUploadRequestDto {

    @Schema(description = "업로드할 파일", type = "string", format = "binary", required = true)
    private MultipartFile file;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}