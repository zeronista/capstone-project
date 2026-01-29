package com.g4.capstoneproject.controller;

import com.g4.capstoneproject.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xử lý email verification
 */
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationController {

    private final AuthService authService;

    /**
     * Xử lý email verification link
     */
    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token, 
                             Model model, 
                             RedirectAttributes redirectAttributes) {
        
        log.info("Email verification attempt with token: {}", token);
        
        boolean success = authService.verifyEmail(token);
        
        if (success) {
            redirectAttributes.addFlashAttribute("success", true);
            redirectAttributes.addFlashAttribute("message", 
                "Email đã được xác thực thành công! Bạn có thể đăng nhập ngay bây giờ.");
            return "redirect:/auth/login";
        } else {
            model.addAttribute("error", true);
            model.addAttribute("message", 
                "Link xác thực không hợp lệ hoặc đã hết hạn. Vui lòng thử lại hoặc yêu cầu gửi lại email xác thực.");
            return "auth/verification-error";
        }
    }

    /**
     * Hiển thị trang yêu cầu gửi lại email xác thực
     */
    @GetMapping("/resend-verification")
    public String showResendVerificationPage() {
        return "auth/resend-verification";
    }

    /**
     * Xử lý yêu cầu gửi lại email xác thực
     */
    @GetMapping("/resend-verification-email")
    public String resendVerificationEmail(@RequestParam("email") String email,
                                         RedirectAttributes redirectAttributes) {
        
        log.info("Resend verification email request for: {}", email);
        
        boolean success = authService.resendVerificationEmail(email);
        
        if (success) {
            redirectAttributes.addFlashAttribute("success", true);
            redirectAttributes.addFlashAttribute("message", 
                "Email xác thực đã được gửi lại. Vui lòng kiểm tra hộp thư của bạn.");
        } else {
            redirectAttributes.addFlashAttribute("error", true);
            redirectAttributes.addFlashAttribute("message", 
                "Không thể gửi lại email xác thực. Vui lòng kiểm tra địa chỉ email hoặc liên hệ hỗ trợ.");
        }
        
        return "redirect:/auth/resend-verification";
    }
}
