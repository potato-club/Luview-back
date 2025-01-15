package solo.project.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import solo.project.entity.File;

import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
public class FileResponseDto {

    @Schema(description = "파일 Id")
    private Long fileId;

    @Schema(description = "이미지 파일 이름")
    private String fileName;

    @Schema(description = "이미지 Url")
    private String fileUrl;

    @Schema(description = "메인 사진, 썸네일")
    private boolean isThumbnail;

    public FileResponseDto(File file) {
        this.fileName=file.getFileName();
        this.fileUrl=file.getFileUrl();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof FileResponseDto)) return false;
        FileResponseDto dto = (FileResponseDto) o;
        return Objects.equals(this.fileName, dto.getFileName())&&
                Objects.equals(this.fileUrl, dto.getFileUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, fileUrl);
    }
}
