package solo.project.repository.mainpage;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import solo.project.dto.mainpage.response.MainPageReviewResponseDto;
import solo.project.dto.mainpage.response.MyInfoResponseDto;
import solo.project.dto.mainpage.response.PartnerInfoResponseDto;
import solo.project.entity.Couple;
import solo.project.entity.Review;
import solo.project.enums.FileType;

import static solo.project.entity.QUser.user;
import static solo.project.entity.QFile.file;
import static solo.project.entity.QCouple.couple;
import static solo.project.entity.QReview.review;
import static solo.project.entity.QReviewPlace.reviewPlace;
import static solo.project.entity.QPlace.place;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MainPageRepositoryImpl implements MainPageRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public MyInfoResponseDto getMyInfo(Long myUserId) {
        // User + File 조인 예시 (프로필 파일 1개만 있다고 가정)
        return queryFactory
                .select(Projections.fields(
                        MyInfoResponseDto.class,
                        user.id.as("userId"),
                        user.nickname.as("nickname"),
                        file.fileUrl.as("profileUrl")
                ))
                .from(user)
                .leftJoin(file)
                .on(file.user.id.eq(user.id)
                        .and(file.fileType.eq(FileType.PROFILE)))
                .where(user.id.eq(myUserId))
                .fetchOne();
    }

    @Override
    public PartnerInfoResponseDto getPartnerInfo(Long myUserId) {
        // 개선: DB 접근 횟수를 줄이기 위해 한 번의 쿼리로 파트너 정보를 조회
        return queryFactory
                .select(Projections.fields(
                        PartnerInfoResponseDto.class,
                        user.id.as("userId"),
                        user.nickname.as("nickname"),
                        file.fileUrl.as("profileUrl")
                ))
                .from(couple)
                .join(user)
                .on(new CaseBuilder()
                        .when(couple.user1.id.eq(myUserId))
                        .then(couple.user2.id)
                        .otherwise(couple.user1.id)
                        .eq(user.id))
                .leftJoin(file)
                .on(file.user.id.eq(user.id)
                        .and(file.fileType.eq(FileType.PROFILE)))
                .where(couple.user1.id.eq(myUserId)
                        .or(couple.user2.id.eq(myUserId)))
                .fetchOne();
    }

    @Override
    public Long getCoupleIdByUserId(Long myUserId) {
        return queryFactory
                .select(couple.id)
                .from(couple)
                .where(couple.user1.id.eq(myUserId)
                        .or(couple.user2.id.eq(myUserId)))
                .fetchOne();
    }

    @Override
    public Long getCoupleDays(Long coupleId) {
        LocalDateTime createdDate = queryFactory
                .select(couple.createdDate)
                .from(couple)
                .where(couple.id.eq(coupleId))
                .fetchOne();
        if (createdDate == null) return 0L;
        return ChronoUnit.DAYS.between(createdDate, LocalDateTime.now());
    }

    @Override
    public List<MainPageReviewResponseDto> getCoupleRecentReviews(Long myUserId, int limit) {
        Long coupleId = getCoupleIdByUserId(myUserId);
        if (coupleId == null) {
            return Collections.emptyList();
        }

        Couple c = queryFactory
                .selectFrom(couple)
                .where(couple.id.eq(coupleId))
                .fetchOne();
        if (c == null) {
            return Collections.emptyList();
        }
        List<Long> userIds = new ArrayList<>();
        userIds.add(c.getUser1().getId());
        userIds.add(c.getUser2().getId());

        // 3. 두 사용자 중 하나가 쓴 리뷰를 최신순으로 조회 (썸네일은 대표 이미지)
        List<Tuple> tuples = queryFactory
                .select(review, place.placeName, file.fileUrl)
                .from(review)
                .leftJoin(review.reviewPlaces, reviewPlace)
                .leftJoin(reviewPlace.place, place)
                .leftJoin(file)
                .on(file.review.id.eq(review.id)
                        .and(file.isThumbnail.eq(true)))
                .where(review.user.id.in(userIds))
                .orderBy(review.createdDate.desc())
                .limit(limit)
                .fetch();

        List<MainPageReviewResponseDto> result = new ArrayList<>();
        for (Tuple tuple : tuples) {
            Review r = tuple.get(review);
            String storeName = tuple.get(place.placeName);
            String thumbUrl = tuple.get(file.fileUrl);
            result.add(
                    MainPageReviewResponseDto.builder()
                            .reviewId(r.getId())
                            .storeName(storeName)
                            .title(r.getTitle())
                            .createdAt(r.getCreatedDate())
                            .thumbnailUrl(thumbUrl)
                            .build()
            );
        }
        return result;
    }
}
