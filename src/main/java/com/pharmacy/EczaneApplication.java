package com.pharmacy;

import com.pharmacy.entity.Role;
import com.pharmacy.entity.User;
import com.pharmacy.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCrypt;

@SpringBootApplication
public class EczaneApplication {

    public static void main(String[] args) {
        SpringApplication.run(EczaneApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {
            // Seed / Force Reset Admin
            userRepository.findByUsername("admin").ifPresentOrElse(
                admin -> {
                    admin.setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()));
                    userRepository.save(admin);
                },
                () -> {
                    User admin = User.builder()
                            .name("Admin User")
                            .username("admin")
                            .password(BCrypt.hashpw("password123", BCrypt.gensalt()))
                            .role(Role.ADMIN)
                            .isActive(true)
                            .build();
                    userRepository.save(admin);
                }
            );

            // Seed / Force Reset Pharmacist
            userRepository.findByUsername("eczaci_ayse").ifPresentOrElse(
                pharmacist -> {
                    pharmacist.setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()));
                    userRepository.save(pharmacist);
                },
                () -> {
                    User pharmacist = User.builder()
                            .name("Pharmacist Ayse")
                            .username("eczaci_ayse")
                            .password(BCrypt.hashpw("password123", BCrypt.gensalt()))
                            .role(Role.PHARMACIST)
                            .isActive(true)
                            .build();
                    userRepository.save(pharmacist);
                }
            );

            // Seed / Force Reset Cashier
            userRepository.findByUsername("kasiyer_veli").ifPresentOrElse(
                cashier -> {
                    cashier.setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()));
                    userRepository.save(cashier);
                },
                () -> {
                    User cashier = User.builder()
                            .name("Cashier Veli")
                            .username("kasiyer_veli")
                            .password(BCrypt.hashpw("password123", BCrypt.gensalt()))
                            .role(Role.CASHIER)
                            .isActive(true)
                            .build();
                    userRepository.save(cashier);
                }
            );
        };
    }
}

