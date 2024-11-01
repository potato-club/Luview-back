package solo.project.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseTimeEntity {
    //작성일, 등록일, 커플연동 시작 날짜
    @CreatedDate
    private LocalDate createdDate;

    //수정일
    @LastModifiedDate
    private LocalDate modifiedDate;
}
