package com.suyos.authservice.util;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class ClientIpResolver {

    /**
     * Extracts the client IP address from the request.
     *
     * @param request HTTP servlet request
     * @return Client IP address
     */
    public String extractClientIp(HttpServletRequest request) {
        // Common headers used by proxies/CDNs to forward client IP
        String[] headers = {
            "X-Forwarded-For",
            "X-Real-IP",
            "CF-Connecting-IP",
            "True-Client-IP"
        };

        // Check headers in order of priority
        for (String header : headers) {
            String value = request.getHeader(header);
            if (value != null && !value.isBlank()) {
                // Take first IP from X-Forwarded-For
                return value.split(",")[0].trim();
            }
        }

        // Fallback to direct remote address
        return request.getRemoteAddr();
    }

}