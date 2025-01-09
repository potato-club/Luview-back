package solo.project.service.Impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.InvalidFileNameException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import solo.project.error.ErrorCode;
import solo.project.error.exception.S3Exception;
import solo.project.service.FileUploadService;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final AmazonS3 amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public String uploadFile(MultipartFile file) throws InvalidFileNameException {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new S3Exception("파일 이름이 유효하지 않습니다.", ErrorCode.NOT_FOUND_EXCEPTION);
        }

        // 허용되지 않은 파일 형식 체크 (예: jpg, png만 허용)
        if (!isAllowedFileType(fileName)) {
            throw new S3Exception("허용되지 않은 파일 형식입니다.", ErrorCode.BAD_REQUEST_EXCEPTION);
        }

        // 파일 크기 제한 (예: 10MB 이상은 업로드 금지)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new S3Exception("파일 크기가 10MB를 초과할 수 없습니다.", ErrorCode.BAD_REQUEST_EXCEPTION);
        }

        String fileKey = "test/" + fileName;
        String fileUrl = "https://" + bucket + ".s3.amazonaws.com/" + fileKey;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        try {
            amazonS3Client.putObject(bucket, fileKey, file.getInputStream(), metadata);
        } catch (IOException e) {
            throw new S3Exception("파일 업로드 중 오류가 발생했습니다.", ErrorCode.FORBIDDEN_EXCEPTION);
        }

        return fileUrl;
    }

    // 허용된 파일 형식 체크 메서드
    private boolean isAllowedFileType(String fileName) {
        String lowerCaseFileName = fileName.toLowerCase();
        return lowerCaseFileName.endsWith(".jpg") || lowerCaseFileName.endsWith(".png");
    }
}
