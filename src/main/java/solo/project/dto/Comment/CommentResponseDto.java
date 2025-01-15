package solo.project.dto.Comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class CommentResponseDto {

  private Long parent_id;
  private Long id;
  private String content;
  private String nickname;
  private List<CommentResponseDto> children = new ArrayList<>();
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
  private LocalDateTime createdDate;
}
