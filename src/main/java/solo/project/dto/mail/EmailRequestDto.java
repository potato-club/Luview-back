package solo.project.dto.mail;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class EmailRequestDto {
    private String email;

    public void setEmail(String email) {
        this.email = email;
    }
}
