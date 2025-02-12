package solo.project.repository.File;

import solo.project.dto.User.response.UserProfileResponseDto;
import solo.project.entity.User;

public interface FileRepositoryCustom {
    UserProfileResponseDto getUserProfile(User user);
}
