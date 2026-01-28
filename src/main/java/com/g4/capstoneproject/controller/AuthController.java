package com.g4.capstoneproject.controller;

import com.g4.capstoneproject.dto.AuthResponse;
import com.g4.capstoneproject.dto.LoginRequest;
import com.g4.capstoneproject.dto.RegisterRequest;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.UserRepository;
import com.g4.capstoneproject.security.CustomUserDetails;
import com.g4.capstoneproject.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;

/**
 * Controller xử lý authentication
 */
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    /**
     * POST /auth/register - Xử lý đăng ký
     */
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Kiểm tra validation errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Vui lòng kiểm tra lại thông tin đăng ký");
            model.addAttribute("registerRequest", request);
            return "auth/register";
        }

        // Xử lý đăng ký
        AuthResponse response = authService.register(request);

        if (response.getSuccess()) {
            redirectAttributes.addFlashAttribute("success", response.getMessage());
            return "redirect:/auth/login";
        } else {
            model.addAttribute("error", response.getMessage());
            model.addAttribute("registerRequest", request);
            return "auth/register";
        }
    }

    /**
     * POST /auth/login - Xử lý đăng nhập
     */
    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginRequest request,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Kiểm tra validation errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Vui lòng nhập đầy đủ thông tin đăng nhập");
            model.addAttribute("loginRequest", request);
            return "auth/login";
        }

        // Xử lý đăng nhập
        AuthResponse response = authService.login(request);

        if (!response.getSuccess()) {
            model.addAttribute("error", response.getMessage());
            model.addAttribute("loginRequest", request);
            return "auth/login";
        }

        // Lưu thông tin user vào session
        session.setAttribute("userId", response.getUserId());
        session.setAttribute("userFullName", response.getFullName());
        session.setAttribute("userEmail", response.getEmail());
        session.setAttribute("userPhone", response.getPhone());
        session.setAttribute("userRole", response.getRole());

        // Lấy User object để tạo CustomUserDetails
        String username = response.getEmail() != null ? response.getEmail() : response.getPhone();
        User user = userRepository.findByEmailOrPhoneNumber(username, username)
                .orElseThrow(() -> new RuntimeException("User not found after successful login"));

        // Tạo Spring Security Authentication với CustomUserDetails
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, // principal là CustomUserDetails object
                null,
                userDetails.getAuthorities());

        // Tạo SecurityContext mới và lưu vào session
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Lưu SecurityContext vào HttpSession (quan trọng cho Spring Security 6+)
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        log.info("User logged in and session created for: {}", username);

        // Điều hướng theo role
        if (response.getRole() == User.UserRole.PATIENT) {
            return "redirect:/patient";
        }

        return "redirect:/dashboard";
    }

    /**
     * GET /auth/logout - Đăng xuất
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Đăng xuất thành công!");
        return "redirect:/auth/login";
    }

    /**
     * GET /auth/oauth2/success - Xử lý thành công OAuth2 login
     */
    @GetMapping("/oauth2/success")
    public String oauthSuccess(@AuthenticationPrincipal OAuth2User principal,
            HttpSession session) {

        if (principal == null) {
            log.error("OAuth2 principal is null");
            return "redirect:/auth/login?error=oauth_failed";
        }

        try {
            // Lấy thông tin từ Google
            String email = principal.getAttribute("email");
            String name = principal.getAttribute("name");
            String googleId = principal.getAttribute("sub");

            log.info("OAuth2 login attempt for email: {}", email);

            // Xử lý đăng nhập/đăng ký
            AuthResponse response = authService.processOAuthPostLogin(email, name, googleId);

            if (response.getSuccess()) {
                // Lưu thông tin user vào session
                session.setAttribute("userId", response.getUserId());
                session.setAttribute("userFullName", response.getFullName());
                session.setAttribute("userEmail", response.getEmail());
                session.setAttribute("userPhone", response.getPhone());
                session.setAttribute("userRole", response.getRole());

                log.info("OAuth2 login successful for: {}", email);

                // Điều hướng theo role
                if (response.getRole() == User.UserRole.PATIENT) {
                    return "redirect:/patient";
                } else {
                    return "redirect:/dashboard";
                }
            } else {
                log.error("OAuth2 login failed: {}", response.getMessage());
                return "redirect:/auth/login?error=" + response.getMessage();
            }

        } catch (Exception e) {
            log.error("Error during OAuth2 success handler", e);
            return "redirect:/auth/login?error=oauth_processing_failed";
        }
    }
}
