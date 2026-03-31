package com.example.backend.Security.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception) throws IOException, ServletException {

        String errorMessage = "OAuth2 authentication failed";
        String provider = extractProviderFromRequest(request);

        // Log the error for debugging
        System.err.println("OAuth2 authentication failed for provider: " + provider);
        System.err.println("Error: " + exception.getMessage());

        // Redirect to frontend with error
        String redirectUrl = String.format("http://localhost:3000/oauth2/error?error=%s&provider=%s",
                                         java.net.URLEncoder.encode(errorMessage, "UTF-8"),
                                         provider != null ? provider : "unknown");

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String extractProviderFromRequest(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        if (requestUri.contains("google")) {
            return "google";
        } else if (requestUri.contains("github")) {
            return "github";
        } else if (requestUri.contains("gitlab")) {
            return "gitlab";
        }
        return null;
    }
}