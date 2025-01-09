package solo.project.repository.File;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import solo.project.dto.User.response.UserProfileResponseDto;
import solo.project.entity.QFile;
import solo.project.entity.QUser;
import solo.project.entity.User;

@Repository
@RequiredArgsConstructor
public class FileRepositoryImpl implements FileRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public UserProfileResponseDto getUserProfile(User user) {
        return jpaQueryFactory
                .selectDistinct(
                        Projections.constructor(
                                UserProfileResponseDto.class,
                                QUser.user.nickname,
                                QUser.user.loginType,
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
