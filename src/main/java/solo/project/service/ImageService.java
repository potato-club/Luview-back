package solo.project.service;

import org.springframework.web.multipart.MultipartFile;
import solo.project.dto.file.FileRequestDto;
import solo.project.entity.File;
import solo.project.entity.User;

import java.io.IOException;
import java.util.List;

public interface ImageService {
    //다중 이미지 (리뷰용)
    List<File> uploadImages(List<MultipartFile> files, Object entity) throws IOException;
    List<File> updateImages(Object entity, List<MultipartFile> files, List<FileRequestDto> requestDto) throws IOException;
    List<File> deleteImages(Object entity)throws IOException;
    byte[] downloadImage(String key) throws IOException;

    //프로필 전용 나중에 ProfileService를 만들어도 될듯함
    void uploadProfileImage(User user, MultipartFile file) throws IOException;
    void updateProfileImage(User user, MultipartFile file) throws IOException;
    void deleteProfileImage(User user) throws IOException;
}
