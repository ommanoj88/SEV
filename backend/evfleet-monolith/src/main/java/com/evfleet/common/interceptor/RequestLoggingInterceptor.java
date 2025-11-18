package com.evfleet.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Enumeration;

/**
 * Request Logging Interceptor
 *
 * Logs all incoming HTTP requests and outgoing responses with timing information.
 * Helps with debugging, monitoring, and identifying performance bottlenecks.
 *
 * Logs:
 * - Request method, URI, headers (in DEBUG mode)
 * - Response status code
 * - Request processing time
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Component
@Slf4j
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTRIBUTE = "startTime";
    private static final String REQUEST_ID_ATTRIBUTE = "requestId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Record start time
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);

        // Generate request ID for tracking
        String requestId = String.format("%s-%d", request.getRemoteAddr(), startTime);
        request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);

        // Log request details
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUrl = queryString != null ? uri + "?" + queryString : uri;

        log.info("→ [{}] {} {} - User-Agent: {}",
                requestId,
                method,
                fullUrl,
                request.getHeader("User-Agent")
        );

        // Log headers in DEBUG mode
        if (log.isDebugEnabled()) {
            logHeaders(request, requestId);
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                          Object handler, ModelAndView modelAndView) {
        // This method is called after the handler method is executed but before the view is rendered
        // We'll do our logging in afterCompletion instead
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                               Object handler, Exception ex) {
        // Calculate request processing time
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        String requestId = (String) request.getAttribute(REQUEST_ID_ATTRIBUTE);

        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();
            String method = request.getMethod();
            String uri = request.getRequestURI();

            // Log response with timing
            String logLevel = getLogLevelForStatus(status);
            String emoji = getEmojiForStatus(status);

            if ("ERROR".equals(logLevel)) {
                log.error("← [{}] {} {} - Status: {} {} - Duration: {}ms",
                        requestId, method, uri, status, emoji, duration);
            } else if ("WARN".equals(logLevel)) {
                log.warn("← [{}] {} {} - Status: {} {} - Duration: {}ms",
                        requestId, method, uri, status, emoji, duration);
            } else {
                log.info("← [{}] {} {} - Status: {} {} - Duration: {}ms",
                        requestId, method, uri, status, emoji, duration);
            }

            // Log slow requests (> 1 second)
            if (duration > 1000) {
                log.warn("⚠️  SLOW REQUEST [{}] {} {} took {}ms",
                        requestId, method, uri, duration);
            }

            // Log exception if present
            if (ex != null) {
                log.error("❌ Exception in request [{}]: {}", requestId, ex.getMessage(), ex);
            }
        }
    }

    /**
     * Log request headers (DEBUG mode only)
     */
    private void logHeaders(HttpServletRequest request, String requestId) {
        StringBuilder headers = new StringBuilder();
        headers.append("\n[").append(requestId).append("] Request Headers:");

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);

            // Mask sensitive headers
            if (headerName.equalsIgnoreCase("Authorization")) {
                headerValue = maskAuthorizationHeader(headerValue);
            }

            headers.append("\n  ").append(headerName).append(": ").append(headerValue);
        }

        log.debug(headers.toString());
    }

    /**
     * Mask Authorization header for security
     */
    private String maskAuthorizationHeader(String value) {
        if (value == null || value.length() < 20) {
            return "***";
        }
        return value.substring(0, 10) + "..." + value.substring(value.length() - 10);
    }

    /**
     * Get log level based on HTTP status code
     */
    private String getLogLevelForStatus(int status) {
        if (status >= 500) {
            return "ERROR";
        } else if (status >= 400) {
            return "WARN";
        } else {
            return "INFO";
        }
    }

    /**
     * Get emoji for HTTP status code
     */
    private String getEmojiForStatus(int status) {
        if (status >= 200 && status < 300) {
            return "✅";
        } else if (status >= 300 && status < 400) {
            return "➡️";
        } else if (status >= 400 && status < 500) {
            return "⚠️";
        } else if (status >= 500) {
            return "❌";
        }
        return "";
    }
}
