package solo.project.repository.review;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import solo.project.dto.Comment.CommentResponseDto;
import solo.project.dto.Review.response.MainReviewResponseDto;
import solo.project.dto.Review.response.ReviewResponseDto;
import solo.project.dto.ReviewPlace.response.ReviewPlaceResponseDto;
import solo.project.dto.file.FileResponseDto;
import solo.project.entity.*;
import solo.project.error.ErrorCode;
import solo.project.error.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    private final QReview qReview = QReview.review;
    private final QFile qFile = QFile.file;
    private final QReviewPlace qReviewPlace = QReviewPlace.reviewPlace;
    private final QPlace qPlace = QPlace.place;
    private final QComment qComment = QComment.comment;
    private final QUser qUser = QUser.user;

    @Override
    public ReviewResponseDto getReviewDetail(Long reviewId) {
        Review review = jpaQueryFactory
                .select(qReview).distinct()
                .from(qReview)
                .leftJoin(qReview.user, qUser).fetchJoin()
                .leftJoin(qReview.files, qFile).fetchJoin()
                .leftJoin(qReview.reviewPlaces, qReviewPlace).fetchJoin()
                .leftJoin(qReviewPlace.place, qPlace).fetchJoin()
                .leftJoin(qReview.comments, qComment).fetchJoin()
                .where(qReview.id.eq(reviewId))
                .fetchOne();

        if (review == null) {
            throw new NotFoundException("존재하지 않는 리뷰입니다.", ErrorCode.NOT_FOUND_EXCEPTION);
        }
        return mapToReviewResponseDto(review);
    }

    @Override
    public List<Review> findPopularReview() {
        return jpaQueryFactory
                .selectFrom(qReview)
                .orderBy(qReview.viewCount.desc())
                .limit(10)
                .fetch();
    }

    @Override
    public List<Review> findPopularByLikes() {
        return jpaQueryFactory
                .selectFrom(qReview)
                .orderBy(qReview.likeCount.desc())
                .limit(10)
                .fetch();
    }

    @Override
    public List<MainReviewResponseDto> searchReview(String keyword) {
        // 검색 조건: 제목, 내용, 작성자 닉네임에 keyword가 포함되는 경우
        BooleanExpression predicate = qReview.title.containsIgnoreCase(keyword)
                .or(qReview.content.containsIgnoreCase(keyword))
                .or(qReview.user.nickname.containsIgnoreCase(keyword));

        // QueryDSL 프로젝션을 사용하여 MainReviewResponseDto로 직접 매핑
        return jpaQueryFactory
                .select(Projections.bean(MainReviewResponseDto.class,
                        qReview.id.as("reviewId"),
                        qReview.title,
                        qReview.likeCount,
                        qReview.commentCount,
                        qReview.viewCount,
                        qReview.user.nickname.as("nickName"),
                        qReviewPlace.place.category.as("category"),
                        qReviewPlace.place.placeName.as("placeName"),
                        qFile.fileUrl.as("thumbnailUrl")
                ))
                .from(qReview)
                .join(qReview.reviewPlaces, qReviewPlace)
                .join(qReviewPlace.place, qPlace)
                .leftJoin(qReview.files, qFile)
                .where(predicate)
                .orderBy(qReview.createdDate.desc())
                .limit(10)
                .fetch();
    }

    // 엔티티를 ReviewResponseDto로 변환하는 메서드
    private ReviewResponseDto mapToReviewResponseDto(Review review) {
        // 파일 처리
        Set<File> fileSet = review.getFiles();
        List<File> fileList = new ArrayList<>(fileSet);
        String thumbnailUrl = fileList.isEmpty() ? null : fileList.get(0).getFileUrl();
        List<FileResponseDto> fileDtos = fileList.stream()
                .map(f -> FileResponseDto.builder()
                        .fileId(f.getFileId())
                        .fileName(f.getFileName())
                        .fileUrl(f.getFileUrl())
                        .isThumbnail(false)
                        .build())
                .collect(Collectors.toList());
        if (!fileList.isEmpty()) {
            fileDtos.get(0).setThumbnail(true);
        }

        // 리뷰 장소 처리
        List<ReviewPlaceResponseDto> reviewPlaceDtos = review.getReviewPlaces().stream()
                .map(rp -> ReviewPlaceResponseDto.builder()
                        .placeId(rp.getPlace().getId())
                        .placeName(rp.getPlace().getPlaceName())
                        .addressName(rp.getPlace().getAddressName())
                        .category(rp.getPlace().getCategory())
                        .rating(rp.getRating())
                        .build())
                .collect(Collectors.toList());

        // 댓글 처리
        List<CommentResponseDto> commentDtos = review.getComments().stream()
                .map(c -> CommentResponseDto.builder()
                        .parent_id(c.getId())
                        .content(c.getContent())
                        .nickname(c.getUser().getNickname())
                        .createdDate(c.getCreatedDate())
                        .build())
                .collect(Collectors.toList());

        return ReviewResponseDto.builder()
                .reviewId(review.getId())
                .title(review.getTitle())
                .content(review.getContent())
                .viewCount(review.getViewCount())
                .likeCount(review.getLikeCount())
                .commentCount(review.getCommentCount())
                .createdAt(review.getCreatedDate())
                .nickname(review.getUser().getNickname())
                .reviewPlaces(reviewPlaceDtos)
                .files(fileDtos)
                .comments(commentDtos)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }
}


