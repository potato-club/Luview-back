package solo.project.dto.mail;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Getter
@ConfigurationProperties(prefix = "mail")
public class MailProperties {
    private final String host;
    private final int port;
    private final String username;
    private final String password;

    @ConstructorBinding
    public MailProperties(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

}
