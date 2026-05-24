package com.pharmacy.controller;

import com.pharmacy.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(assignableTypes = UIController.class)
@RequiredArgsConstructor
public class GlobalUIControllerAdvice {

    private final HttpSession session;

    @ModelAttribute("currentUri")
    public String currentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("user")
    public User activeUser() {
        return (User) session.getAttribute("user");
    }
}
