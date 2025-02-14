package solo.project.service.Impl;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import solo.project.dto.file.FileRequestDto;
import solo.project.entity.File;
import solo.project.entity.Review;
import solo.project.entity.User;
import solo.project.enums.FileType;
import solo.project.error.ErrorCode;
import solo.project.error.exception.S3Exception;
import solo.project.repository.file.FileRepository;
import solo.project.service.ImageService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final FileRepository fileRepository;
    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * [리뷰/프로필 이미지 업로드] 다중 파일 업로드 후 S3에 저장하고, DB에 파일 엔티티를 생성함
     * User 엔티티: 프로필 이미지는 1개만 허용
     * Review 엔티티: 여러 이미지 가능
     */
    @Transactional
    @Override
    public List<File> uploadImages(List<MultipartFile> files, Object entity) throws IOException {
        // 0) entity 타입에 따라 fileType 결정
        Class<?> entityType = entity.getClass();
        FileType fileType;

        if (entityType.equals(User.class)) {
            fileType = FileType.PROFILE;
            User user = (User) entity;
            // 프로필은 1개만 허용
            List<File> existingProfileFiles = fileRepository.findByUserAndFileType(user, FileType.PROFILE);
            if (!existingProfileFiles.isEmpty()) {
                throw new S3Exception("이미 프로필이 존재합니다.", ErrorCode.FORBIDDEN_EXCEPTION);
            }
        } else if (entityType.equals(Review.class)) {
            fileType = FileType.REVIEW;
        } else {
            throw new IllegalArgumentException("지원하지 않는 엔티티 타입: " + entityType.getName());
        }

        // 1) S3 업로드 및 임시 File 엔티티 생성 (DB 저장 전)
        List<File> newFileList = uploadToS3AndSave(files, fileType);

        // 2) 연관관계 매핑 및 추가 처리 (리뷰의 경우 대표 썸네일 지정)
        if (entityType.equals(User.class)) {
            User user = (User) entity;
            for (File file : newFileList) {
                file.setUser(user);
                fileRepository.save(file);
            }
        } else if (entityType.equals(Review.class)) {
            Review review = (Review) entity;
            for (int i = 0; i < newFileList.size(); i++) {
                File file = newFileList.get(i);
                file.setReview(review);
                // 첫 번째 파일을 대표 썸네일로 지정
                file.setIsThumbnail(i == 0);
                fileRepository.save(file);
            }
        }

        return newFileList;
    }

    /**
     * [리뷰/프로필 이미지 수정] 기존 파일 중 삭제 대상은 제거하고, 새 파일을 업로드 후 매핑.
     */
    @Transactional
    @Override
    public List<File> updateImages(Object entity, List<MultipartFile> files, List<FileRequestDto> requestDto)
            throws IOException {

        // 0) 엔티티 타입에 따른 fileType 및 기존 파일 조회
        Class<?> entityType = entity.getClass();
        FileType fileType;
        List<File> existingFileList = new ArrayList<>();

        if (entityType.equals(User.class)) {
            fileType = FileType.PROFILE;
            User user = (User) entity;
            existingFileList = fileRepository.findByUserAndFileType(user, FileType.PROFILE);
        } else if (entityType.equals(Review.class)) {
            fileType = FileType.REVIEW;
            Review review = (Review) entity;
            existingFileList = fileRepository.findByReviewAndFileType(review, FileType.REVIEW);
        } else {
            throw new IllegalArgumentException("지원하지 않는 엔티티 타입: " + entityType.getName());
        }

        // 1) 기존 파일 중 삭제 요청된 파일 제거
        for (int i = 0; i < existingFileList.size(); i++) {
            File oldFile = existingFileList.get(i);
            if (requestDto.size() > i
                    && requestDto.get(i).isDeleted()
                    && oldFile.getFileName().equals(requestDto.get(i).getFileName())) {
                // S3에서 삭제
                amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, oldFile.getFileName()));
                // DB에서 제거
                fileRepository.delete(oldFile);
            }
        }

        // 2) 새로 업로드할 파일 처리
        List<File> newFileList = CollectionUtils.isEmpty(files) ? new ArrayList<>() : uploadToS3AndSave(files, fileType);

        // 3) 연관관계 매핑 및 (리뷰의 경우 대표 썸네일 지정) 후 DB 저장
        List<File> resultList = new ArrayList<>();
        if (entityType.equals(User.class)) {
            User user = (User) entity;
            for (File file : newFileList) {
                file.setUser(user);
                fileRepository.save(file);
                resultList.add(file);
            }
        } else if (entityType.equals(Review.class)) {
            Review review = (Review) entity;
            for (int i = 0; i < newFileList.size(); i++) {
                File file = newFileList.get(i);
                file.setReview(review);
                file.setIsThumbnail(i == 0);
                fileRepository.save(file);
                resultList.add(file);
            }
        }
        return resultList;
    }

    @Transactional
    @Override
    public List<File> deleteImages(Object entity) throws IOException {
        // 필요에 따라 구현 (예: 리뷰 전체 이미지 삭제)
        return List.of();
    }

    @Transactional
    @Override
    public void uploadProfileImage(User user, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        List<File> existingProfile = fileRepository.findByUserAndFileType(user, FileType.PROFILE);
        if (!existingProfile.isEmpty()) {
            throw new IllegalStateException("이미 프로필 사진이 존재합니다. 수정 API를 이용하세요.");
        }
        String storeFileName = uploadToS3(file);
        File fileEntity = File.builder()
                .fileName(storeFileName)
                .fileUrl(amazonS3Client.getUrl(bucketName, storeFileName).toString())
                .fileType(FileType.PROFILE)
                .user(user)
                .build();
        fileRepository.save(fileEntity);
    }

    @Transactional
    @Override
    public void updateProfileImage(User user, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("수정할 파일이 없습니다.");
        }
        List<File> existingProfile = fileRepository.findByUserAndFileType(user, FileType.PROFILE);
        if (existingProfile.isEmpty()) {
            throw new IllegalStateException("프로필이 없습니다. 업로드를 먼저 해주세요.");
        }
        for (File oldFile : existingProfile) {
            deleteFromS3(oldFile.getFileName());
            fileRepository.delete(oldFile);
        }
        String storeFileName = uploadToS3(file);
        File fileEntity = File.builder()
                .fileName(storeFileName)
                .fileUrl(amazonS3Client.getUrl(bucketName, storeFileName).toString())
                .fileType(FileType.PROFILE)
                .user(user)
                .build();
        fileRepository.save(fileEntity);
    }

    @Transactional
    @Override
    public void deleteProfileImage(User user) throws IOException {
        List<File> existingProfile = fileRepository.findByUserAndFileType(user, FileType.PROFILE);
        if (existingProfile.isEmpty()) {
            throw new IllegalStateException("프로필이 없습니다.");
        }
        for (File oldFile : existingProfile) {
            deleteFromS3(oldFile.getFileName());
            fileRepository.delete(oldFile);
        }
    }

    @Override
    public byte[] downloadImage(String key) throws IOException {
        S3Object s3Object = amazonS3Client.getObject(bucketName, key);
        try (S3ObjectInputStream inputStream = s3Object.getObjectContent()) {
            return IOUtils.toByteArray(inputStream);
        } finally {
            s3Object.close();
        }
    }

    private String uploadToS3(MultipartFile multipartFile) throws IOException {
        String originalName = multipartFile.getOriginalFilename();
        String fileName = UUID.randomUUID() + "-" + originalName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        amazonS3Client.putObject(
                new PutObjectRequest(bucketName, fileName, multipartFile.getInputStream(), metadata)
        );
        return fileName;
    }

    private void deleteFromS3(String storeFileName) {
        amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, storeFileName));
    }

    /**
     * 다중 파일 업로드 시 S3에 업로드하고, File 엔티티 목록을 생성하여 반환합니다.
     */
    private List<File> uploadToS3AndSave(List<MultipartFile> files, FileType fileType) throws IOException {
        List<File> result = new ArrayList<>();
        for (MultipartFile multipartFile : files) {
            String fileName = uploadToS3(multipartFile);
            File fileEntity = File.builder()
                    .fileName(fileName)
                    .fileUrl(amazonS3Client.getUrl(bucketName, fileName).toString())
                    .fileType(fileType)
                    .build();
            result.add(fileEntity);
        }
        return result;
    }
}
