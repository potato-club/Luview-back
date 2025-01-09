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

    @Transactional
    public List<File> uploadImages(List<MultipartFile> files, Object entity) throws IOException {
        List<File> list = this.existsFiles(files);
        Class<?> entityType = entity.getClass();

        for (int i = 0; i < list.size(); i++) {
            File file = list.get(i);

            if (entityType.equals(Review.class)) {
                file.setReview((Review) entity);
            } else if (entityType.equals(User.class)) {
                file.setUser((User) entity);
            }
            File saveFile = fileRepository.save(file);
            list.set(i, saveFile);
        }
        return list;
    }

    //수정,삭제등 메서드
    @Transactional
    public List<File> updateImages(Object entity, List<MultipartFile> files, List<FileRequestDto> requestDto)
            throws IOException {

        List<File> fileList = new ArrayList<>();
        Class<?> entityType = entity.getClass();

        //연관관계 확인
        if (entityType.equals(Review.class)) {
            fileList = fileRepository.findByReview((Review) entity);
        } else if (entityType.equals(User.class)) {
            fileList = fileRepository.findByUser((User) entity);
        }

        //파일만 변경 기존것 삭제
        List<File> list = new ArrayList<>();

        if (!CollectionUtils.isEmpty(files)) {
            list = this.existsFiles(files);
        }

        //결과저장 리스트
        List<File> resultList = new ArrayList<>();

        for (int i = 0; i < fileList.size(); i++) {
            if (requestDto.get(i).isDeleted() && fileList.get(i).getFileName().equals(requestDto.get(i).getFileName())) {
                amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, fileList.get(i).getFileName()));
                fileRepository.delete(fileList.get(i)); // DB에서도 해당 파일 엔티티 삭제
            } else { // 삭제 조건 만족 안한다면 resultList 에 추가
                resultList.add(fileList.get(i));
            }
        }

        for (File file : list) {
            if (entityType.equals(Review.class)) {
                file.setReview((Review) entity);
            } else if (entityType.equals(User.class)) {
                file.setUser((User) entity);
            }

            File saveFile = fileRepository.save(file);
            resultList.add(saveFile);
        }

        return resultList;
    }

    @Override
    public byte[] downloadImage(String key) throws IOException {
        byte[] content;
        final S3Object s3Object = amazonS3Client.getObject(bucketName, key);
        final S3ObjectInputStream inputStream = s3Object.getObjectContent();
        try {
            content = IOUtils.toByteArray(inputStream);
            s3Object.close();
        } catch (final IOException e) {
            throw new IOException("IO Exception = " + e.getMessage());
        }
        return content;
    }

    //이미지 여부 확인 및 중복 이미지 확인
    private List<File> existsFiles(List<MultipartFile> files) throws IOException {
        List<File> imagelist = new ArrayList<>();
        for (MultipartFile file : files) {
            String key = file.getOriginalFilename();
            if (amazonS3Client.doesObjectExist(bucketName, key)) {
                continue;
            }
            String fileName = UUID.randomUUID() + "-" + key;
            InputStream inputStream = file.getInputStream();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata));
            File image = File.builder()
                    .fileName(fileName)
                    .fileUrl(amazonS3Client.getUrl(bucketName, fileName).toString())
                    .build();
            imagelist.add(image);
        }
        return imagelist;
    }
}
