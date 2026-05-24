package com.pharmacy.interceptor;

import com.pharmacy.entity.Role;
import com.pharmacy.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final List<String> CASHIER_ALLOWED_PATHS = Arrays.asList(
            "/pos",
            "/inventory",
            "/account",
            "/logout",
            "/api/sales",
            "/api/drugs",
            "/api/customers",
            "/api/users/performance"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // Allow static resources and auth endpoints
        if (uri.startsWith("/css") || uri.startsWith("/js") || uri.startsWith("/images") || uri.equals("/favicon.ico")) {
            return true;
        }
        if (uri.equals("/login") || uri.equals("/api/auth/login")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect("/login");
            return false;
        }

        // Apply RBAC for CASHIER
        if (user.getRole() == Role.CASHIER) {
            boolean isAllowed = CASHIER_ALLOWED_PATHS.stream()
                    .anyMatch(path -> uri.equals(path) || uri.startsWith(path + "/"));
            if (!isAllowed) {
                response.sendRedirect("/pos");
                return false;
            }
        }

        // Apply RBAC for PHARMACIST: allow /settings, GET /api/brands, GET /api/categories, GET /api/drugs
        // Block only modifications (POST/PUT/DELETE) on /api/users
        if (user.getRole() == Role.PHARMACIST) {
            if (uri.startsWith("/api/users") && !request.getMethod().equalsIgnoreCase("GET")) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Pharmacists are not allowed to manage staff accounts");
                return false;
            }
        }

        return true;
    }
}
