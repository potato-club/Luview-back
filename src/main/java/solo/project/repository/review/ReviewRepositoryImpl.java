package solo.project.repository.review;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import solo.project.dto.Comment.CommentResponseDto;
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

    @Override
    @Transactional
    public ReviewResponseDto getReviewDetail(Long reviewId) {
        QReview qReview = QReview.review;
        QFile qFile = QFile.file;
        QReviewPlace qReviewPlace = QReviewPlace.reviewPlace;
        QPlace qPlace = QPlace.place;
        QComment qComment = QComment.comment;
        QUser qUser = QUser.user;

        // fetchJoin
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
        QReview qReview = QReview.review;
        return jpaQueryFactory
                .selectFrom(qReview)
                .orderBy(qReview.viewCount.desc())
                .limit(10)
                .fetch();
    }

    @Override
    public List<Review> findPopularByLikes() {
        QReview qReview = QReview.review;
        return jpaQueryFactory
                .selectFrom(qReview)
                .orderBy(qReview.likeCount.desc())
                .limit(10)
                .fetch();
    }

    private ReviewResponseDto mapToReviewResponseDto(Review review) {
        Set<File> fileSet = review.getFiles();
        List<File> fileList = new ArrayList<>(fileSet);

        String thumbnailUrl = fileList.isEmpty() ? null : fileList.get(0).getFileUrl(); // 첫 번째 파일 썸네일

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


        List<ReviewPlaceResponseDto> reviewPlaceDtos = review.getReviewPlaces().stream()
                .map(rp -> ReviewPlaceResponseDto.builder()
                        .placeId(rp.getPlace().getId())
                        .placeName(rp.getPlace().getPlaceName())
                        .addressName(rp.getPlace().getAddressName())
                        .category(rp.getPlace().getCategory())
                        .rating(rp.getRating())  // 별점
                        .build())
                .collect(Collectors.toList());

        List<CommentResponseDto> commentDtos = review.getComments().stream()
                .map(c -> CommentResponseDto.builder()
                        .parent_id(c.getId())
                        .content(c.getContent())
                        .nickname(c.getUser().getNickname())
                        .createdDate(c.getCreatedDate())
                        .build())
                .collect(Collectors.toList());

        // 4) 리턴
        return ReviewResponseDto.builder()
                .reviewId(review.getId())
                .title(review.getTitle())
                .content(review.getContent())
                .viewCount(review.getViewCount())
                .likeCount(review.getLikeCount())
                .commentCount(review.getCommentCount())
                .createdAt(review.getCreatedDate())
                .nickname(review.getUser().getNickname())
                .reviewPlaces(reviewPlaceDtos)        // 장소
                .files(fileDtos)                // 이미지
                .comments(commentDtos)          // 댓글
                .thumbnailUrl(thumbnailUrl)     // 첫 번째 이미지의 URL
                .build();
    }
}


