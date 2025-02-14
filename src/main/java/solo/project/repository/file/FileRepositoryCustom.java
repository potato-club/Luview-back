package solo.project.repository.file;

import solo.project.dto.user.response.UserProfileResponseDto;
import solo.project.entity.User;

public interface FileRepositoryCustom {
    UserProfileResponseDto getUserProfile(User user);
}
