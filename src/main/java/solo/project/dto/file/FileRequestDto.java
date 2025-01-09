package solo.project.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import solo.project.entity.File;

@Data
@Builder
@Schema(description = "파일 업로드 요청")
@AllArgsConstructor
public class FileRequestDto {

    @Schema(description = "파일 이름")
    private String fileName;

    @Schema(description = "파일 Url")
    private String fileUrl;

    @Schema(description = "파일 삭제/교체 여부")
    private boolean deleted;

    public FileRequestDto(File file) {
        this.fileName = file.getFileName();
        this.fileUrl = file.getFileUrl();
        this.deleted = true;
    }

    public FileRequestDto(FileResponseDto dto, boolean deleted) {
        this.fileName = dto.getFileName();
        this.fileUrl = dto.getFileUrl();
        this.deleted = deleted;
    }
}