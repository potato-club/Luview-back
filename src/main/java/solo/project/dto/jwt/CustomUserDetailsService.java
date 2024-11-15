package solo.project.dto.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import solo.project.repository.UserRepository;

@Service
@Component("userDetailsService")
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        return new UserDetailsImpl(userRepository.findByEmail(name)
                .orElseThrow(() -> new UsernameNotFoundException("회원이 존재하지 않습니다.")));
    }
}