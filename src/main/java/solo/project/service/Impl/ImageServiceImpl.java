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
import solo.project.repository.File.FileRepository;
import solo.project.service.ImageService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final FileRepository fileRepository;
    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    //엔티티 대상 받아서 그 엔티티에 저장하는 로직임,,
    @Transactional
    @Override
    public List<File> uploadImages(List<MultipartFile> files, Object entity) throws IOException {
        List<File> fileList = this.existsFiles(files);
        Class<?> entityType = entity.getClass();

        for (int i = 0; i < fileList.size(); i++) {
            File file = fileList.get(i);

            // 엔티티 타입에 따라 연관관계를 설정
            if (entityType.equals(Review.class)) {
                file.setReview((Review) entity);
            } else if (entityType.equals(User.class)) {
                file.setUser((User) entity);
            }

            File saved = fileRepository.save(file);
            fileList.set(i, saved);
        }
        return fileList;
    }

    //수정, 삭제 로직임
    @Transactional
    @Override
    public List<File> updateImages(Object entity, List<MultipartFile> files, List<FileRequestDto> requestDto)
            throws IOException {

        List<File> existingFileList = new ArrayList<>();
        Class<?> entityType = entity.getClass();

        // 1) 연결된 파일 목록 조회
        if (entityType.equals(Review.class)) {
            existingFileList = fileRepository.findByReview((Review) entity);
        } else if (entityType.equals(User.class)) {
            existingFileList = fileRepository.findByUser((User) entity);
        }

        // 2) 새로 업로드할 파일들 처리
        List<File> newFileList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(files)) {
            newFileList = this.existsFiles(files);
        }

        // 3) 결과 저장용
        List<File> resultList = new ArrayList<>();

        // 4) 삭제할 파일 삭제
        for (int i = 0; i < existingFileList.size(); i++) {
            File oldFile = existingFileList.get(i);

            // requestDto에서 해당 파일이 삭제 대상인지 확인
            if (requestDto.get(i).isDeleted() &&
                    oldFile.getFileName().equals(requestDto.get(i).getFileName())) {

                // S3에서 삭제
                amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, oldFile.getFileName()));

                // DB에서도 제거
                fileRepository.delete(oldFile);

            } else {
                // 유지하는 파일이면 결과 목록에 그대로 추가
                resultList.add(oldFile);
            }
        }

        // 5) 새로 업로드된 파일들 DB 저장 + 결과 목록에 추가
        for (File file : newFileList) {
            if (entityType.equals(Review.class)) {
                file.setReview((Review) entity);
            } else if (entityType.equals(User.class)) {
                file.setUser((User) entity);
            }
            File saved = fileRepository.save(file);
            resultList.add(saved);
        }

        return resultList;
    }

    //다운로드 반환 바이트로 변환함
    @Override
    public byte[] downloadImage(String key) throws IOException {
        S3Object s3Object = amazonS3Client.getObject(bucketName, key);
        try (S3ObjectInputStream inputStream = s3Object.getObjectContent()) {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new IOException("IO Exception = " + e.getMessage());
        } finally {
            s3Object.close();
        }
    }

    //s3에 이미지 있는지 확인
    private List<File> existsFiles(List<MultipartFile> files) throws IOException {
        List<File> imagelist = new ArrayList<>();
        for (MultipartFile file : files) {
            String originalKey = file.getOriginalFilename();
            if (amazonS3Client.doesObjectExist(bucketName, originalKey)) {
                // 이미 존재하면 스킵 (혹은 새로운 이름으로 업로드할 수도 있음)
                continue;
            }
            // S3에 저장할 새로운 파일명(키)
            String fileName = UUID.randomUUID() + "-" + originalKey;

            // S3 putObject
            InputStream inputStream = file.getInputStream();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            // contentType 지정 가능: metadata.setContentType(file.getContentType());
            amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata));

            // DB용 File 엔티티 생성
            File image = File.builder()
                    .fileName(fileName)
                    .fileUrl(amazonS3Client.getUrl(bucketName, fileName).toString())
                    .build();
            imagelist.add(image);
        }
        return imagelist;
    }
}
