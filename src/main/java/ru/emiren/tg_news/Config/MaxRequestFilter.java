package ru.emiren.tg_news.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class MaxRequestFilter extends OncePerRequestFilter {
    private final Map<String, RequestCount> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 20; // requests it can proceed from one IP (1 ip can produce till 15 requests)
    private static final int TIME_WINDOW = 60; // time in sec to clean

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String ipAddress = request.getRemoteAddr();
        requestCounts.putIfAbsent(ipAddress, new RequestCount(0, System.currentTimeMillis()));
        RequestCount requestCount = requestCounts.get(ipAddress);

        long currentTime = System.currentTimeMillis();
        log.info("difference between them is {} and flag is {}", currentTime - requestCount.timestamp, currentTime - requestCount.timestamp > TIME_WINDOW );
        if (currentTime - requestCount.timestamp > TIME_WINDOW ) {
            requestCount.setCount(0);
            requestCount.setTimestamp(currentTime);
        }

        if (requestCount.getCount() >= MAX_REQUESTS_PER_MINUTE ) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().println("Too many requests - please try again later");
            return;
        }
        requestCount.setCount(requestCount.getCount() + 1);
        requestCounts.put(ipAddress, requestCount);
        filterChain.doFilter(request, response);
    }

    @AllArgsConstructor
    @Getter
    @Setter
    private static class RequestCount {
        private int count;
        private long timestamp;
    }
}
