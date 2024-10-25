package com.SecondHand.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute
    public void addAttributes(HttpServletRequest request, Principal principal) {
        boolean isLoggedIn = (principal != null);
        request.setAttribute("isLoggedIn", isLoggedIn);

        if (isLoggedIn) {
            request.setAttribute("username", principal.getName());
        }
    }
}