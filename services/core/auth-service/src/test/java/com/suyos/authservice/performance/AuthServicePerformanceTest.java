package com.suyos.authservice.performance;

import com.suyos.authservice.dto.request.AuthenticationRequestDTO;
import com.suyos.authservice.dto.request.RegistrationRequestDTO;
import com.suyos.authservice.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AuthServicePerformanceTest {

    @Autowired
    private AuthService authService;

    // ============ Helper Methods ============

    /**
     * Calculates percentile from sorted list of response times.
     *
     * @param responseTimes Sorted list of response times in milliseconds
     * @param percentile Percentile value (0.0 to 1.0)
     * @return Response time at the given percentile
     */
    private long calculatePercentile(List<Long> responseTimes, double percentile) {
        if (responseTimes.isEmpty()) {
            return 0;
        }
        int index = Math.max(0, (int) (responseTimes.size() * percentile) - 1);
        return responseTimes.get(index);
    }

    /**
     * Prints performance metrics to console.
     *
     * @param title Test title
     * @param totalRequests Total number of requests
     * @param concurrentThreads Number of concurrent threads
     * @param successCount Number of successful requests
     * @param failureCount Number of failed requests
     * @param totalDuration Total execution time in milliseconds
     * @param throughput Requests per second
     * @param avgResponseTime Average response time in milliseconds
     * @param p50 50th percentile response time
     * @param p95 95th percentile response time
     * @param p99 99th percentile response time
     */
    private void printPerformanceMetrics(String title, int totalRequests, int concurrentThreads,
                                        int successCount, int failureCount, long totalDuration,
                                        double throughput, double avgResponseTime,
                                        long p50, long p95, long p99) {
        System.out.println("=== " + title + " ===");
        System.out.println("Total Requests: " + totalRequests);
        System.out.println("Concurrent Threads: " + concurrentThreads);
        System.out.println("Success: " + successCount);
        System.out.println("Failures: " + failureCount);
        System.out.println("Total Duration: " + totalDuration + "ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " req/sec");
        System.out.println("Avg Response Time: " + String.format("%.2f", avgResponseTime) + "ms");
        System.out.println("P50 Response Time: " + p50 + "ms");
        System.out.println("P95 Response Time: " + p95 + "ms");
        System.out.println("P99 Response Time: " + p99 + "ms");
    }

    /**
     * Prints bottleneck analysis results for a thread count.
     *
     * @param threads Number of threads
     * @param duration Execution duration in milliseconds
     * @param throughput Requests per second
     */
    private void printBottleneckResult(int threads, long duration, double throughput) {
        System.out.println("Threads: " + threads + 
                         " | Duration: " + duration + "ms" +
                         " | Throughput: " + String.format("%.2f", throughput) + " req/sec");
    }

    // ============ Tests: Registration Performance ============

    @Test
    @DisplayName("should measure registration throughput under concurrent load")
    void testRegistrationThroughput() throws InterruptedException, ExecutionException {
        int totalRequests = 1000;
        int concurrentThreads = 50;
        
        ExecutorService executor = Executors.newFixedThreadPool(concurrentThreads);
        CountDownLatch latch = new CountDownLatch(totalRequests);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        List<Future<Long>> futures = new ArrayList<>();
        Instant start = Instant.now();
        
        for (int i = 0; i < totalRequests; i++) {
            final int index = i;
            Future<Long> future = executor.submit(() -> {
                try {
                    Instant requestStart = Instant.now();

                    MockHttpServletRequest httpRequest = new MockHttpServletRequest();
                    httpRequest.setRemoteAddr("127.0.0.1");
                    httpRequest.addHeader("User-Agent", "Perf-Test-Agent");
                    
                    AuthenticationRequestDTO request = AuthenticationRequestDTO.builder()
                            .identifier("perf" + index + "@test.com")
                            .password("Password123!")
                            .build();
                    
                    authService.authenticateAccount(request, httpRequest);
                    successCount.incrementAndGet();
                    
                    return Duration.between(requestStart, Instant.now()).toMillis();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    return -1L;
                } finally {
                    latch.countDown();
                }
            });
            futures.add(future);
        }
        
        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();
        
        Instant end = Instant.now();
        long totalDuration = Duration.between(start, end).toMillis();
        
        List<Long> responseTimes = new ArrayList<>();
        for (Future<Long> future : futures) {
            if (future.isDone()) {
                Long time = future.get();
                if (time > 0) {
                    responseTimes.add(time);
                }
            }
        }
        
        responseTimes.sort(Long::compareTo);
        long p50 = calculatePercentile(responseTimes, 0.50);
        long p95 = calculatePercentile(responseTimes, 0.95);
        long p99 = calculatePercentile(responseTimes, 0.99);
        double avgResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double throughput = (double) successCount.get() / (totalDuration / 1000.0);
        
        printPerformanceMetrics("Registration Performance Test Results", totalRequests, concurrentThreads,
                              successCount.get(), failureCount.get(), totalDuration, throughput,
                              avgResponseTime, p50, p95, p99);
        
        assertThat(successCount.get()).isGreaterThan((int) (totalRequests * 0.95));
        assertThat(p95).isLessThan(1000);
    }

    @Test
    @DisplayName("should identify performance bottlenecks across varying thread counts")
    void testConcurrentRegistrationBottleneck() throws InterruptedException {
        int[] threadCounts = {10, 50, 100, 200};
        int requestsPerThread = 10;
        
        System.out.println("=== Bottleneck Analysis ===");
        
        for (int threads : threadCounts) {
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            CountDownLatch latch = new CountDownLatch(threads * requestsPerThread);
            AtomicInteger successCount = new AtomicInteger(0);
            
            Instant start = Instant.now();
            
            for (int i = 0; i < threads * requestsPerThread; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        RegistrationRequestDTO request = RegistrationRequestDTO.builder()
                                .email("bottleneck" + index + "@test.com")
                                .username("bottleneckuser" + index)
                                .password("Password123!")
                                .build();
                        
                        authService.createAccount(request, "idempotency-key");
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        // Ignore
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            latch.await(60, TimeUnit.SECONDS);
            executor.shutdown();
            
            Instant end = Instant.now();
            long duration = Duration.between(start, end).toMillis();
            double throughput = (double) successCount.get() / (duration / 1000.0);
            
            printBottleneckResult(threads, duration, throughput);
        }
    }
}