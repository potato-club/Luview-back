package solo.project.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import solo.project.entity.File;

@Data
@Builder
@Schema(description = "파일 업로드 요청")
@AllArgsConstructor
@NoArgsConstructor
public class FileRequestDto {

    @Schema(description = "파일 식별")
    private Long fileId;

    @Schema(description = "파일 이름")
    private String fileName;

    @Schema(description = "파일 Url")
    private String fileUrl;

    @Schema(description = "메인 사진, 썸네일")
    private boolean isThumbnail;

    @Schema(description = "파일 삭제/교체 여부")
    private boolean deleted;

    public FileRequestDto(File file) {
        this.fileId = file.getFileId();              // ★파일 PK
        this.fileName = file.getFileName();
        this.fileUrl = file.getFileUrl();
        this.isThumbnail = file.isThumbnail();
        this.deleted = false;
    }

}