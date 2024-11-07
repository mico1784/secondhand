package com.SecondHand.member;

import com.SecondHand.member.User;
import com.SecondHand.member.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    @Autowired
    @Lazy
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HttpSession session;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Google OAuth2 user information
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name"); // Use Google name attribute directly
        String password = bCryptPasswordEncoder.encode("defaultPassword");
        String role = "ROLE_USER";

        // Use name directly as username for Google users
        String username = name != null ? name : (email != null && email.contains("@") ? email.split("@")[0] : email);

        // Check for duplicate usernames
        int suffix = 1;
        String originalUsername = username;
        while (userRepository.findByUsername(username).isPresent()) {
            username = originalUsername + suffix;
            suffix++;
        }

        User userEntity = userRepository.findByEmail(email).orElse(null);
        if (userEntity == null) {
            System.out.println("First-time Google login user.");
            userEntity = User.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .role(role)
                    .isGoogleUser(true)
                    .googleId(oAuth2User.getAttribute("sub"))
                    .build();
            userRepository.save(userEntity);
        } else {
            System.out.println("Returning Google login user.");
        }

        // Store user information in the session
        session.setAttribute("user", userEntity);
        session.setAttribute("isGoogleUser", true);
        session.setAttribute("name", name); // Add the name for display

        return new PrincipalDetails(userEntity, oAuth2User.getAttributes());
    }
}
