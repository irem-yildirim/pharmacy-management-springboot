package com.pharmacy.config;

import com.pharmacy.model.User;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

@Getter
public class PharmacyUserDetails extends org.springframework.security.core.userdetails.User {

    private final String fullName;
    private final Long userId;

    public PharmacyUserDetails(User user) {
        super(user.getUsername(), user.getPassword(), user.getIsActive(),
                true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        this.fullName = user.getName();
        this.userId = user.getId();
    }
}
