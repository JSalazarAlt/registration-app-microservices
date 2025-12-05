package com.suyos.userservice.performance;

import com.suyos.common.event.UserCreationEvent;
import com.suyos.userservice.dto.request.UserUpdateRequestDTO;
import com.suyos.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UserServicePerformanceTest {

    /** Service for user operations under test */
    @Autowired
    private UserService userService;

    /** Identifier of test user created before each test */
    private UUID testUserId;

    /**
     * Initializes test data before each performance test execution.
     *
     * <p>Creates a single user record used as the target of read and write
     * operations during performance and load testing.</p>
     */
    @BeforeEach
    void setup() {
        // Build user creation request
        UserCreationEvent user = UserCreationEvent.builder()
                .accountId(UUID.randomUUID())
                .email("perftest@test.com")
                .username("perfuser")
                .build();

        // Persist user and store its identifier for test operations
        testUserId = userService.createUser(user).getId();
    }

    /**
     * Measures throughput and latency of concurrent user profile reads.
     *
     * <p>Simulates high-concurrency access patterns by issuing a large number
     * of parallel read requests. Collects response times to compute P50, P95,
     * P99 latency values and overall throughput.</p>
     *
     * @throws InterruptedException If test executor is interrupted
     * @throws ExecutionException If retrieving asynchronous results fails
     */
    @Test
    void testGetUserProfileThroughput() throws InterruptedException, ExecutionException {
        // Define total number of GetUser operations and worker threads
        int totalRequests = 5000;
        int concurrentThreads = 200;
        
        // Define executor for concurrent tasks
        ExecutorService executor = Executors.newFixedThreadPool(concurrentThreads);
        CountDownLatch latch = new CountDownLatch(totalRequests);

        // Count successful and failed operations
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        List<Future<Long>> futures = new ArrayList<>();
        Instant start = Instant.now();

        // Submit concurrent read tasks
        for (int i = 0; i < totalRequests; i++) {
            Future<Long> future = executor.submit(() -> {
                try {
                    // Record request start time
                    Instant requestStart = Instant.now();
                    
                    // Execute user fetch operation
                    userService.findUserByAccountId(testUserId);
                    successCount.incrementAndGet();
                    
                    // Return response time in milliseconds
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

        // Await completion of all tasks
        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        Instant end = Instant.now();
        long totalDuration = Duration.between(start, end).toMillis();

        // Collect valid response times
        List<Long> responseTimes = new ArrayList<>();
        for (Future<Long> future : futures) {
            if (future.isDone()) {
                Long time = future.get();
                if (time > 0) {
                    responseTimes.add(time);
                }
            }
        }

        // Sort response times for percentile calculations
        responseTimes.sort(Long::compareTo);
        long p50 = responseTimes.get(responseTimes.size() / 2);
        long p95 = responseTimes.get((int) (responseTimes.size() * 0.95));
        long p99 = responseTimes.get((int) (responseTimes.size() * 0.99));

        double avgResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        double throughput = (double) successCount.get() / (totalDuration / 1000.0);

        // Log test results
        System.out.println("=== Get User Profile Performance Test Results ===");
        System.out.println("Total Requests: " + totalRequests);
        System.out.println("Concurrent Threads: " + concurrentThreads);
        System.out.println("Success: " + successCount.get());
        System.out.println("Failures: " + failureCount.get());
        System.out.println("Total Duration: " + totalDuration + "ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " req/sec");
        System.out.println("Avg Response Time: " + String.format("%.2f", avgResponseTime) + "ms");
        System.out.println("P50 Response Time: " + p50 + "ms");
        System.out.println("P95 Response Time: " + p95 + "ms");
        System.out.println("P99 Response Time: " + p99 + "ms");

        // Assert acceptable success rate and latency thresholds
        assertThat(successCount.get()).isGreaterThan((int) (totalRequests * 0.95));
        assertThat(p95).isLessThan(200);
    }

    /**
     * Measures throughput and latency of concurrent user profile updates.
     *
     * <p>Simulates simultaneous update operations to observe write contention,
     * connection pooling limits, and persistence performance under load.</p>
     *
     * @throws InterruptedException If interrupted while awaiting tasks
     * @throws ExecutionException If retrieving asynchronous results fails
     */
    @Test
    void testUpdateUserProfileThroughput() throws InterruptedException, ExecutionException {
        int totalRequests = 1000;           // Number of update requests
        int concurrentThreads = 50;         // Worker thread count
        
        ExecutorService executor = Executors.newFixedThreadPool(concurrentThreads);
        CountDownLatch latch = new CountDownLatch(totalRequests);

        AtomicInteger successCount = new AtomicInteger(0); // Successful updates
        AtomicInteger failureCount = new AtomicInteger(0); // Failed updates
        
        List<Future<Long>> futures = new ArrayList<>();
        Instant start = Instant.now();

        // Submit concurrent update tasks
        for (int i = 0; i < totalRequests; i++) {
            final int index = i;

            Future<Long> future = executor.submit(() -> {
                try {
                    // Record request start time
                    Instant requestStart = Instant.now();

                    // Build update request
                    UserUpdateRequestDTO updateDTO = UserUpdateRequestDTO.builder()
                            .firstName("Updated" + index)
                            .lastName("User" + index)
                            .build();

                    // Execute update operation
                    userService.updateUserByAccountId(testUserId, updateDTO);
                    successCount.incrementAndGet();

                    // Return latency
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

        // Await task completion
        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        Instant end = Instant.now();
        long totalDuration = Duration.between(start, end).toMillis();

        // Collect valid response times
        List<Long> responseTimes = new ArrayList<>();
        for (Future<Long> future : futures) {
            if (future.isDone()) {
                Long time = future.get();
                if (time > 0) {
                    responseTimes.add(time);
                }
            }
        }

        // Sort for percentile calculations
        responseTimes.sort(Long::compareTo);
        long p50 = responseTimes.get(responseTimes.size() / 2);
        long p95 = responseTimes.get((int) (responseTimes.size() * 0.95));
        long p99 = responseTimes.get((int) (responseTimes.size() * 0.99));

        double avgResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        double throughput = (double) successCount.get() / (totalDuration / 1000.0);

        // Log performance metrics
        System.out.println("=== Update User Profile Performance Test Results ===");
        System.out.println("Total Requests: " + totalRequests);
        System.out.println("Concurrent Threads: " + concurrentThreads);
        System.out.println("Success: " + successCount.get());
        System.out.println("Failures: " + failureCount.get());
        System.out.println("Total Duration: " + totalDuration + "ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " req/sec");
        System.out.println("Avg Response Time: " + String.format("%.2f", avgResponseTime) + "ms");
        System.out.println("P50 Response Time: " + p50 + "ms");
        System.out.println("P95 Response Time: " + p95 + "ms");
        System.out.println("P99 Response Time: " + p99 + "ms");

        // Assert minimum acceptable performance
        assertThat(successCount.get()).isGreaterThan((int) (totalRequests * 0.95));
        assertThat(p95).isLessThan(500);
    }

    /**
     * Evaluates database connection pool saturation across increasing load levels.
     *
     * <p>Executes a series of read operations across a range of thread counts
     * to observe throughput degradation and identify connection pool bottlenecks.</p>
     *
     * @throws InterruptedException If thread synchronization is interrupted
     */
    @Test
    void testDatabaseConnectionPoolBottleneck() throws InterruptedException {
        int[] threadCounts = {10, 50, 100, 200, 500};  // Load tiers
        int requestsPerThread = 10;                    // Operations per thread
        
        System.out.println("=== Database Connection Pool Bottleneck Analysis ===");

        // Iterate through increasing thread load scenarios
        for (int threads : threadCounts) {
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            CountDownLatch latch = new CountDownLatch(threads * requestsPerThread);

            AtomicInteger successCount = new AtomicInteger(0); // Count of successful reads
            
            Instant start = Instant.now();

            // Submit read operations for this load level
            for (int i = 0; i < threads * requestsPerThread; i++) {
                executor.submit(() -> {
                    try {
                        userService.findUserByAccountId(testUserId);
                        successCount.incrementAndGet();
                    } catch (Exception ignored) {
                        // Failures ignored for bottleneck analysis
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Await completion of all tasks
            latch.await(60, TimeUnit.SECONDS);
            executor.shutdown();

            Instant end = Instant.now();
            long duration = Duration.between(start, end).toMillis();

            // Compute throughput in requests per second
            double throughput = (double) successCount.get() / (duration / 1000.0);

            System.out.println("Threads: " + threads +
                    " | Duration: " + duration + "ms" +
                    " | Throughput: " + String.format("%.2f", throughput) + " req/sec");
        }
    }

}