package solo.project.repository.file;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import solo.project.dto.user.response.UserProfileResponseDto;
import solo.project.entity.QFile;
import solo.project.entity.QUser;
import solo.project.entity.User;

@Repository
@RequiredArgsConstructor
public class FileRepositoryImpl implements FileRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    //유저가 가진 첫번째 파일을 가져옴다
    @Override
    public UserProfileResponseDto getUserProfile(User user) {
        return jpaQueryFactory
                .selectDistinct(
                        Projections.constructor(
                                UserProfileResponseDto.class,
                                QUser.user.nickname,
                                QUser.user.loginType,
                                QUser.user.uniqueCode,
                                QUser.user.birthDate,
                                QFile.file.fileName,
                                QFile.file.fileUrl
                        )
                )
                .from(QUser.user)
                .leftJoin(QFile.file).on(QFile.file.user.eq(QUser.user))
                .where(QUser.user.eq(user))
                .fetchOne();
    }
}
