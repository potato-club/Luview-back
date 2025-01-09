package solo.project.service;

import org.springframework.web.multipart.MultipartFile;
import solo.project.dto.file.FileRequestDto;
import solo.project.entity.File;

import java.io.IOException;
import java.util.List;

public interface ImageService {
    List<File> uploadImages(List<MultipartFile> files, Object entity) throws IOException;

    List<File> updateImages(Object entity, List<MultipartFile> files, List<FileRequestDto> requestDto) throws IOException;

    byte[] downloadImage(String key) throws IOException;
}
