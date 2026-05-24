package com.pharmacy;

import com.pharmacy.model.Role;
import com.pharmacy.model.User;
import com.pharmacy.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class EczaneApplication {

    public static void main(String[] args) {
        SpringApplication.run(EczaneApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            userRepository.findByUsername("admin").ifPresentOrElse(
                    admin -> {
                        admin.setPassword(encoder.encode("password123"));
                        userRepository.save(admin);
                    },
                    () -> {
                        User admin = User.builder()
                                .name("Admin User")
                                .username("admin")
                                .password(encoder.encode("password123"))
                                .role(Role.ADMIN)
                                .isActive(true)
                                .build();
                        userRepository.save(admin);
                    });

            userRepository.findByUsername("eczaci_ayse").ifPresentOrElse(
                    pharmacist -> {
                        pharmacist.setPassword(encoder.encode("password123"));
                        userRepository.save(pharmacist);
                    },
                    () -> {
                        User pharmacist = User.builder()
                                .name("Pharmacist Ayse")
                                .username("eczaci_ayse")
                                .password(encoder.encode("password123"))
                                .role(Role.PHARMACIST)
                                .isActive(true)
                                .build();
                        userRepository.save(pharmacist);
                    });

            userRepository.findByUsername("kasiyer_veli").ifPresentOrElse(
                    cashier -> {
                        cashier.setPassword(encoder.encode("password123"));
                        userRepository.save(cashier);
                    },
                    () -> {
                        User cashier = User.builder()
                                .name("Cashier Veli")
                                .username("kasiyer_veli")
                                .password(encoder.encode("password123"))
                                .role(Role.CASHIER)
                                .isActive(true)
                                .build();
                        userRepository.save(cashier);
                    });
        };
    }
}
