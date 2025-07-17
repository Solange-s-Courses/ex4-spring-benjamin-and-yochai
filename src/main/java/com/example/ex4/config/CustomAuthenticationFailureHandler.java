package com.example.ex4.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {
        String errorMessage;

        if (exception instanceof InternalAuthenticationServiceException) {
            Throwable cause = exception.getCause();
            if (cause instanceof LockedException || cause instanceof DisabledException) {
                errorMessage = cause.getMessage();
            } else {
                errorMessage = "תקלה בהתחברות, אנא נסו שנית במועד מאוחר יותר.";
            }
        } else if (exception instanceof LockedException || exception instanceof DisabledException) {
            errorMessage = exception.getMessage();
        } else if (exception instanceof BadCredentialsException) {
            errorMessage = "שם משתמש או סיסמה שגויים";
        } else {
            errorMessage = "תקלה בהתחברות, אנא נסו שנית במועד מאוחר יותר.";
        }

        FlashMap flashMap = new FlashMap();
        flashMap.put("loginError", errorMessage);

        SessionFlashMapManager flashMapManager = new SessionFlashMapManager();
        flashMapManager.saveOutputFlashMap(flashMap, request, response);

        getRedirectStrategy().sendRedirect(request, response, "/login?error");
    }
}