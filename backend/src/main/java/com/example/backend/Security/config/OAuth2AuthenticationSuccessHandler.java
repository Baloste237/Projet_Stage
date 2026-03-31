package com.example.backend.Security.config;

import com.example.backend.Security.service.UserInfoService;
import com.example.backend.Security.dto.UserInfoDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserInfoService userInfoService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {

        try {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String provider = extractProvider(authentication);
            String providerId = extractProviderId(oauth2User, provider);

            // Validate required attributes
            if (email == null || email.isEmpty()) {
                throw new RuntimeException("Email is required for OAuth2 authentication");
            }
            if (name == null || name.isEmpty()) {
                name = email.split("@")[0]; // Fallback to email prefix
            }

            // Create or get user
            UserInfoDto userDto = userInfoService.createOAuth2User(email, name, provider, providerId);

            // Generate JWT token
            String token = userInfoService.generateTokenForOAuth2User(email);

            // Redirect to frontend with token
            String redirectUrl = "http://localhost:3000/oauth2/redirect?token=" + token + "&provider=" + provider.toLowerCase();
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);

        } catch (Exception e) {
            System.err.println("Error during OAuth2 authentication success: " + e.getMessage());
            e.printStackTrace();

            // Redirect to error page
            String errorUrl = "http://localhost:3000/oauth2/error?error=" +
                            java.net.URLEncoder.encode("Authentication processing failed: " + e.getMessage(), "UTF-8");
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }

    private String extractProvider(Authentication authentication) {
        String name = authentication.getName();
        if (name.contains("@")) {
            String domain = name.split("@")[1].toLowerCase();
            if (domain.contains("google")) {
                return "GOOGLE";
            } else if (domain.contains("github")) {
                return "GITHUB";
            } else if (domain.contains("gitlab")) {
                return "GITLAB";
            }
        }
        // Fallback: check the registration ID from the request
        return "UNKNOWN";
    }

    private String extractProviderId(OAuth2User oauth2User, String provider) {
        Object id = oauth2User.getAttribute("id");
        if (id != null) {
            return id.toString();
        }

        // Fallback based on provider
        switch (provider) {
            case "GITHUB":
                return oauth2User.getAttribute("login").toString();
            case "GITLAB":
                return oauth2User.getAttribute("id").toString();
            default:
                return oauth2User.getAttribute("sub") != null ?
                       oauth2User.getAttribute("sub").toString() :
                       "unknown";
        }
    }
}