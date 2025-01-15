package solo.project.service.Impl;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
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
import solo.project.repository.File.FileRepository;
import solo.project.service.ImageService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ImageServiceImpl
 *
 * - 프로필 로직:
 *   1) uploadProfileImage(User, MultipartFile)
 *   2) updateProfileImage(User, MultipartFile)
 *   3) deleteProfileImage(User)
 *   => User 1명당 PROFILE 파일 1개만 관리
 *
 * - 리뷰 로직:
 *   1) uploadImages(List<MultipartFile>, Object entity)
 *   2) updateImages(Object entity, List<MultipartFile>, List<FileRequestDto>)
 *   3) deleteImages(Object entity)
 *   => Review 1개당 여러 파일(FileType.REVIEW) 관리
 */
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final FileRepository fileRepository;
    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;


    /** ==============================
     *  [리뷰 로직] 다중 이미지 처리
     * ============================== */
    @Transactional
    @Override
    public List<File> uploadImages(List<MultipartFile> files, Object entity) throws IOException {
        // 0) entity 타입에 따라 fileType 결정
        Class<?> entityType = entity.getClass();
        FileType fileType = null;

        if (entityType.equals(User.class)) {
            // PROFILE 용도
            fileType = FileType.PROFILE;
            User user = (User) entity;

            // 이미 프로필이 존재하면 에러 처리 (1장만 허용)
            List<File> existingProfileFiles = fileRepository.findByUserAndFileType(user, FileType.PROFILE);
            if (!existingProfileFiles.isEmpty()) {
                throw new S3Exception("이미 프로필이 존재합니다.", ErrorCode.FORBIDDEN_EXCEPTION);
            }

        } else if (entityType.equals(Review.class)) {
            // REVIEW 용도 (다중 이미지 가능)
            fileType = FileType.REVIEW;
        }

        // 1) S3 업로드 & DB 저장
        List<File> newFileList = this.uploadToS3AndSave(files, fileType);

        // 2) 연관관계 매핑
        for (File file : newFileList) {
            if (entityType.equals(User.class)) {
                file.setUser((User) entity);
            } else if (entityType.equals(Review.class)) {
                file.setReview((Review) entity);
            }
            fileRepository.save(file);
        }

        return newFileList;
    }

    /** 수정 + 삭제 로직 (주로 리뷰, 썸네일) */
    @Override
    @Transactional
    public List<File> updateImages(Object entity, List<MultipartFile> files, List<FileRequestDto> requestDto)
            throws IOException {

        // 0) entity에 따른 fileType, 기존 리스트 찾기
        Class<?> entityType = entity.getClass();
        List<File> existingFileList = new ArrayList<>();
        FileType fileType = null;

        if (entityType.equals(User.class)) {
            fileType = FileType.PROFILE;
            User user = (User) entity;
            existingFileList = fileRepository.findByUserAndFileType(user, FileType.PROFILE);

        } else if (entityType.equals(Review.class)) {
            fileType = FileType.REVIEW;
            Review review = (Review) entity;
            existingFileList = fileRepository.findByReviewAndFileType(review, FileType.REVIEW);
        }

        // 1) 삭제 로직 (requestDto에서 isDeleted() == true 인 파일 제거)
        for (int i = 0; i < existingFileList.size(); i++) {
            File oldFile = existingFileList.get(i);

            if (requestDto.size() > i  // 인덱스 범위 체크
                    && requestDto.get(i).isDeleted()
                    && oldFile.getFileName().equals(requestDto.get(i).getFileName())) {

                // S3에서 삭제
                amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, oldFile.getFileName()));
                // DB에서 제거
                fileRepository.delete(oldFile);
            } else {
                // 유지
            }
        }

        // 2) 새로 업로드할 파일 처리
        List<File> newFileList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(files)) {
            newFileList = this.uploadToS3AndSave(files, fileType);
        }

        // 3) 매핑 & 저장
        List<File> resultList = new ArrayList<>();
        // (유지되는 파일들은 이미 DB에 남아있으므로, 여기선 새 파일만 처리)
        for (File file : newFileList) {
            if (entityType.equals(User.class)) {
                file.setUser((User) entity);
            } else if (entityType.equals(Review.class)) {
                file.setReview((Review) entity);
            }
            fileRepository.save(file);
            resultList.add(file);
        }
        return resultList;
    }

    /** 삭제 로직 (현재 미구현, 필요시 작성) */
    @Override
    @Transactional
    public List<File> deleteImages(Object entity) throws IOException {
        // 리뷰 전체 이미지 삭제 등, 필요시 구현
        return List.of();
    }


    /** =====================================
     *  [프로필 전용 로직] User 1명당 1장만
     * ===================================== */
    @Override
    @Transactional
    public void uploadProfileImage(User user, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        // 1) 기존 프로필 존재 여부 확인
        List<File> existingProfile = fileRepository.findByUserAndFileType(user, FileType.PROFILE);
        if (!existingProfile.isEmpty()) {
            throw new IllegalStateException("이미 프로필 사진이 존재합니다. 수정 API를 이용하세요.");
        }

        // 2) S3 업로드
        String storeFileName = uploadToS3(file);

        // 3) DB에 File 저장
        File fileEntity = File.builder()
                .fileName(storeFileName)
                .fileUrl(amazonS3Client.getUrl(bucketName, storeFileName).toString())
                .fileType(FileType.PROFILE)
                .user(user)
                .build();
        fileRepository.save(fileEntity);
    }

    @Override
    @Transactional
    public void updateProfileImage(User user, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("수정할 파일이 없습니다.");
        }
        // 1) 기존 프로필 조회
        List<File> existingProfile = fileRepository.findByUserAndFileType(user, FileType.PROFILE);
        if (existingProfile.isEmpty()) {
            throw new IllegalStateException("프로필이 없습니다. 업로드를 먼저 해주세요.");
        }

        // 2) 기존 파일(S3 + DB) 삭제
        for (File oldFile : existingProfile) {
            deleteFromS3(oldFile.getFileName());
            fileRepository.delete(oldFile);
        }

        // 3) 새 파일 업로드
        String storeFileName = uploadToS3(file);

        // 4) 새 파일 DB 저장
        File fileEntity = File.builder()
                .fileName(storeFileName)
                .fileUrl(amazonS3Client.getUrl(bucketName, storeFileName).toString())
                .fileType(FileType.PROFILE)
                .user(user)
                .build();
        fileRepository.save(fileEntity);
    }

    @Override
    @Transactional
    public void deleteProfileImage(User user) throws IOException {
        // 1) 기존 프로필 조회
        List<File> existingProfile = fileRepository.findByUserAndFileType(user, FileType.PROFILE);
        if (existingProfile.isEmpty()) {
            throw new IllegalStateException("프로필이 없습니다.");
        }

        // 2) S3 + DB에서 삭제
        for (File oldFile : existingProfile) {
            deleteFromS3(oldFile.getFileName());
            fileRepository.delete(oldFile);
        }
    }

    /** =====================================
     *  [공통] 파일 다운로드 / S3 업로드/삭제
     * ===================================== */
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
     * 다중 파일 업로드할 때 사용되는 유틸 메서드
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
