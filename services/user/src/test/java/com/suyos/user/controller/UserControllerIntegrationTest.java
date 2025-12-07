package com.suyos.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suyos.user.dto.request.UserUpdateRequestDTO;
import com.suyos.user.model.User;
import com.suyos.user.repository.UserRepository;

/**
 * Integration tests for UserController.
 *
 * <p>Tests user profile endpoints with full Spring context and
 * database integration to verify end-to-end functionality.</p>
 */
@SpringBootTest
@AutoConfigureWebMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    /** MockMvc for simulating HTTP requests */
    @Autowired
    private MockMvc mockMvc;
    
    /** ObjectMapper for JSON serialization/deserialization */
    @Autowired
    private ObjectMapper objectMapper;
    
    /** User repository for test data setup */
    @Autowired
    private UserRepository userRepository;
    
    /** Test user entity */
    private User testUser;

    /**
     * Sets up test data before each test.
     */
    @BeforeEach
    void setUp() {
        // Build and save test user
        testUser = User.builder()
                .accountId(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .phone("1234567890")
                .termsAcceptedAt(Instant.now())
                .privacyPolicyAcceptedAt(Instant.now())
                .build();
        testUser = userRepository.save(testUser);
    }

    /**
     * Tests successful user retrieval by ID with database persistence.
     * 
     * <p>Verifies that user is retrieved from database and returned
     * with 200 OK status.</p>
     */
    @Test
    void getUserById_Success() throws Exception {
        // Perform get user request and verify response
        mockMvc.perform(get("/api/v1/users/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"));
    }

    /**
     * Tests user retrieval with non-existing ID.
     * 
     * <p>Verifies that 404 Not Found is returned when user does not
     * exist in database.</p>
     */
    @Test
    void getUserById_NotFound() throws Exception {
        // Generate non-existing ID
        UUID nonExistentId = UUID.randomUUID();
        
        // Perform get user request and expect not found
        mockMvc.perform(get("/api/v1/users/{userId}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    /**
     * Tests successful user update with database persistence.
     * 
     * <p>Verifies that user is updated in database and returned
     * with 200 OK status.</p>
     */
    @Test
    void updateUserById_Success() throws Exception {
        // Build update DTO
        UserUpdateRequestDTO updateDTO = UserUpdateRequestDTO.builder()
                .firstName("Updated")
                .lastName("Name")
                .phone("0987654321")
                .build();

        // Perform update user request and verify response
        mockMvc.perform(put("/api/v1/users/{userId}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"))
                .andExpect(jsonPath("$.phone").value("0987654321"));
    }

    /**
     * Tests successful user retrieval by account ID.
     */
    @Test
    void getUserByAccountId_Success() throws Exception {
        // Perform get user by account ID request and verify response
        mockMvc.perform(get("/api/v1/users/account/{accountId}", testUser.getAccountId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    /**
     * Tests successful paginated user retrieval.
     */
    @Test
    void getAllUsersPaginated_Success() throws Exception {
        // Perform get all users paginated request and verify response
        mockMvc.perform(get("/api/v1/users")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "createdAt")
                .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    /**
     * Tests successful user search by name.
     */
    @Test
    void searchUsersByName_Success() throws Exception {
        // Perform search users request and verify response
        mockMvc.perform(get("/api/v1/users/search")
                .param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    /**
     * Tests user update with non-existing ID.
     */
    @Test
    void updateUserById_NotFound() throws Exception {
        // Generate non-existing ID
        UUID nonExistentId = UUID.randomUUID();
        
        // Build update DTO
        UserUpdateRequestDTO updateDTO = UserUpdateRequestDTO.builder()
                .firstName("Updated")
                .build();

        // Perform update user request and expect not found
        mockMvc.perform(put("/api/v1/users/{userId}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    /**
     * Tests user update by account ID with non-existing account.
     */
    @Test
    void updateUserByAccountId_NotFound() throws Exception {
        // Generate non-existing account ID
        UUID nonExistentAccountId = UUID.randomUUID();
        
        // Build update DTO
        UserUpdateRequestDTO updateDTO = UserUpdateRequestDTO.builder()
                .firstName("Updated")
                .build();

        // Perform update user by account ID request and expect not found
        mockMvc.perform(put("/api/v1/users/account/{accountId}", nonExistentAccountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    /**
     * Tests user retrieval by account ID with non-existing account.
     */
    @Test
    void getUserByAccountId_NotFound() throws Exception {
        // Generate non-existing account ID
        UUID nonExistentAccountId = UUID.randomUUID();
        
        // Perform get user by account ID request and expect not found
        mockMvc.perform(get("/api/v1/users/account/{accountId}", nonExistentAccountId))
                .andExpect(status().isNotFound());
    }

    /**
     * Tests user search with no results.
     */
    @Test
    void searchUsersByName_NoResults() throws Exception {
        // Perform search users request with non-matching name
        mockMvc.perform(get("/api/v1/users/search")
                .param("name", "NonExistentName"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    /**
     * Tests paginated user retrieval with custom page size.
     */
    @Test
    void getAllUsersPaginated_CustomPageSize() throws Exception {
        // Perform get all users paginated request with custom size
        mockMvc.perform(get("/api/v1/users")
                .param("page", "0")
                .param("size", "5")
                .param("sortBy", "firstName")
                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(5));
    }

    /**
     * Tests user update with partial data.
     */
    @Test
    void updateUserById_PartialUpdate() throws Exception {
        // Build update DTO with only first name
        UserUpdateRequestDTO updateDTO = UserUpdateRequestDTO.builder()
                .firstName("OnlyFirstName")
                .build();

        // Perform update user request and verify only first name changed
        mockMvc.perform(put("/api/v1/users/{userId}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("OnlyFirstName"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    /**
     * Tests user update by account ID with successful persistence.
     */
    @Test
    void updateUserByAccountId_Success() throws Exception {
        // Build update DTO
        UserUpdateRequestDTO updateDTO = UserUpdateRequestDTO.builder()
                .firstName("AccountUpdated")
                .lastName("ByAccountId")
                .build();

        // Perform update user by account ID request and verify response
        mockMvc.perform(put("/api/v1/users/account/{accountId}", testUser.getAccountId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("AccountUpdated"))
                .andExpect(jsonPath("$.lastName").value("ByAccountId"));
    }

    /**
     * Tests user existence check returns true for existing user.
     */
    @Test
    void existsById_ReturnsTrue() throws Exception {
        // Perform exists by ID request and verify true response
        mockMvc.perform(get("/api/v1/users/{userId}/exists", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    /**
     * Tests user existence check returns false for non-existing user.
     */
    @Test
    void existsById_ReturnsFalse() throws Exception {
        // Generate non-existing ID
        UUID nonExistentId = UUID.randomUUID();
        
        // Perform exists by ID request and verify false response
        mockMvc.perform(get("/api/v1/users/{userId}/exists", nonExistentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }
}