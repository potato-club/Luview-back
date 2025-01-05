package solo.project.dto.kakao;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AdditionalInfoRequest {
    @NotBlank(message = "이름은 필수 입력값입니다.")
    private String name;

    @NotBlank(message = "생년월일은 필수 입력값입니다.")
    private LocalDate birthDate;
}
