package com.education.rpg.rpglearningbackend.scheduler;

import com.education.rpg.rpglearningbackend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Resets all players' energy to 100 every day at 00:00 UTC. */
@Component
public class EnergyScheduler {

    private static final Logger log = LoggerFactory.getLogger(EnergyScheduler.class);

    private final UserRepository userRepository;

    public EnergyScheduler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Triggered daily at midnight UTC; performs a bulk energy reset via a single UPDATE. */
    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void resetEnergyAtMidnight() {
        log.info("[EnergyScheduler] Запуск відновлення енергії для всіх гравців...");
        userRepository.resetAllUsersEnergy();
        log.info("[EnergyScheduler] Енергію успішно відновлено до 100 для всіх гравців.");
    }
}
