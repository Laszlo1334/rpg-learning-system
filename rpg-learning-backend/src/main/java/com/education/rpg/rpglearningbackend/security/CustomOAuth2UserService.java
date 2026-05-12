package com.education.rpg.rpglearningbackend.security;

import com.education.rpg.rpglearningbackend.model.Role;
import com.education.rpg.rpglearningbackend.model.User;
import com.education.rpg.rpglearningbackend.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            User newUser = new User();
            newUser.setUsername(name);
            newUser.setEmail(email);
            newUser.setRole(Role.STUDENT);
            newUser.setPassword("");
            newUser.setAvatarUrl("/assets/default_avatar.png");

            // --- Starter pack defaults so the DB does not fail on null fields ---
            newUser.setLevel(1);
            newUser.setCurrentXp(0);
            newUser.setGold(0);
            newUser.setCrystals(0);
            newUser.setCampfireLevel(1);
            newUser.setEnergy(100);
            newUser.setIsPublicProfile(true);
            newUser.setLifetimeGold(0);
            newUser.setLifetimeCrystals(0);
            newUser.setTotalTasksCompleted(0);
            newUser.setTotalFailures(0);
            newUser.setLastLoginDate(LocalDateTime.now());

            userRepository.save(newUser);
            System.out.println("✨ New RPG Player created via Google: " + name);
        } else {
            System.out.println("🔥 Existing RPG Player logged in: " + name);
        }

        return oAuth2User;
    }
}