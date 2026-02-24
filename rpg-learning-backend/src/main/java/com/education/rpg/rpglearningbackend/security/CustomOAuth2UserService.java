package com.education.rpg.rpglearningbackend.security;

import com.education.rpg.rpglearningbackend.model.Role;
import com.education.rpg.rpglearningbackend.model.User;
import com.education.rpg.rpglearningbackend.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    // Пам'ятаєш, ми видалили Lombok? Тому пишемо конструктор вручну
    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Отримуємо дані юзера від Google
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Шукаємо юзера в нашій БД
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            // Якщо це новий гравець - реєструємо його в нашій RPG-системі!
            User newUser = new User();
            newUser.setUsername(name);
            newUser.setEmail(email);
            newUser.setRole(Role.STUDENT);
            newUser.setPassword(""); // Пароль не потрібен, бо вхід через Google

            userRepository.save(newUser);
            System.out.println("New RPG Player created via Google: " + name);
        } else {
            System.out.println("Existing RPG Player logged in: " + name);
        }

        return oAuth2User;
    }
}