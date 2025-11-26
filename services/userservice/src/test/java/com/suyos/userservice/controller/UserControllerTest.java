package com.suyos.userservice.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suyos.common.dto.response.PagedResponseDTO;
import com.suyos.userservice.dto.request.UserUpdateRequestDTO;
import com.suyos.userservice.dto.response.UserProfileDTO;
import com.suyos.userservice.service.UserService;

/**
 * Unit tests for UserController.
 *
 * <p>Tests user profile endpoints using mocked services to verify
 * controller behavior and request/response handling.</p>
 *
 * @author Joel Salazar
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    /** MockMvc for simulating HTTP requests */
    @Autowired
    private MockMvc mockMvc;
    
    /** ObjectMapper for JSON serialization/deserialization */
    @Autowired
    private ObjectMapper objectMapper;
    
    /** Mocked user service */
    @MockitoBean
    private UserService userService;
    
    /** Test user profile DTO */
    private UserProfileDTO userProfileDTO;
    
    /** Test user update DTO */
    private UserUpdateRequestDTO userUpdateDTO;
    
    /** Test user ID */
    private UUID userId;
    
    /** Test account ID */
    private UUID accountId;

    /**
     * Sets up test data before each test.
     */
    @BeforeEach
    void setUp() {
        // Generate test IDs
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        
        // Build test user profile DTO
        userProfileDTO = UserProfileDTO.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .phone("1234567890")
                .createdAt(Instant.now())
                .build();
        
        // Build test update DTO
        userUpdateDTO = UserUpdateRequestDTO.builder()
                .firstName("Updated")
                .lastName("Name")
                .phone("0987654321")
                .build();
    }

    /**
     * Tests successful user retrieval by ID.
     * 
     * <p>Verifies that endpoint returns 200 OK with user profile
     * when valid user ID is provided.</p>
     */
    @Test
    void getUserById_Success() throws Exception {
        // Mock service to return user profile
        when(userService.findUserById(userId)).thenReturn(userProfileDTO);

        // Perform get user request
        mockMvc.perform(get("/api/v1/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"));
        
        // Verify service was called
        verify(userService).findUserById(userId);
    }

    /**
     * Tests successful user update by ID.
     * 
     * <p>Verifies that endpoint returns 200 OK with updated profile
     * when valid update data is provided.</p>
     */
    @Test
    void updateUserById_Success() throws Exception {
        // Build updated profile
        UserProfileDTO updatedProfile = UserProfileDTO.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .firstName("Updated")
                .lastName("Name")
                .phone("0987654321")
                .build();
        
        // Mock service to return updated profile
        when(userService.updateUserById(eq(userId), any())).thenReturn(updatedProfile);

        // Perform update user request
        mockMvc.perform(put("/api/v1/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"));
        
        // Verify service was called
        verify(userService).updateUserById(eq(userId), any());
    }

    /**
     * Tests successful user retrieval by account ID.
     */
    @Test
    void getProfileByAccountId_Success() throws Exception {
        // Mock service to return user profile
        when(userService.findUserByAccountId(accountId)).thenReturn(userProfileDTO);

        // Perform get profile by account ID request
        mockMvc.perform(get("/api/v1/users/account/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
        
        // Verify service was called
        verify(userService).findUserByAccountId(accountId);
    }

    /**
     * Tests successful user update by account ID.
     */
    @Test
    void updateProfileByAccountId_Success() throws Exception {
        // Build updated profile
        UserProfileDTO updatedProfile = UserProfileDTO.builder()
                .id(userId)
                .firstName("Updated")
                .lastName("Name")
                .build();
        
        // Mock service to return updated profile
        when(userService.updateUserByAccountId(eq(accountId), any())).thenReturn(updatedProfile);

        // Perform update profile by account ID request
        mockMvc.perform(put("/api/v1/users/account/{accountId}", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));
        
        // Verify service was called
        verify(userService).updateUserByAccountId(eq(accountId), any());
    }

    /**
     * Tests successful paginated user retrieval.
     */
    @Test
    void getAllUsersPaginated_Success() throws Exception {
        // Build paginated response
        List<UserProfileDTO> users = Arrays.asList(userProfileDTO);
        PagedResponseDTO<UserProfileDTO> pagedResponse = PagedResponseDTO.<UserProfileDTO>builder()
                .content(users)
                .currentPage(0)
                .totalPages(1)
                .totalElements(1L)
                .size(10)
                .first(true)
                .last(true)
                .build();
        
        // Mock service to return paginated response
        when(userService.findAllUsers(0, 10, "createdAt", "desc")).thenReturn(pagedResponse);

        // Perform get all users paginated request
        mockMvc.perform(get("/api/v1/users")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "createdAt")
                .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));
        
        // Verify service was called
        verify(userService).findAllUsers(0, 10, "createdAt", "desc");
    }

    /**
     * Tests successful user search by name.
     */
    @Test
    void searchUsersByName_Success() throws Exception {
        // Build search results
        List<UserProfileDTO> users = Arrays.asList(userProfileDTO);
        
        // Mock service to return search results
        when(userService.searchUsersByName("Test")).thenReturn(users);

        // Perform search users request
        mockMvc.perform(get("/api/v1/users/search")
                .param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Test"));
        
        // Verify service was called
        verify(userService).searchUsersByName("Test");
    }

    /**
     * Tests user retrieval with non-existing ID.
     */
    @Test
    void getUserById_NotFound() throws Exception {
        // Mock service to throw exception
        when(userService.findUserById(userId)).thenThrow(new RuntimeException("User not found"));

        // Perform get user request and expect error
        mockMvc.perform(get("/api/v1/users/{userId}", userId))
                .andExpect(status().is5xxServerError());
        
        // Verify service was called
        verify(userService).findUserById(userId);
    }

    /**
     * Tests user update with non-existing ID.
     */
    @Test
    void updateUserById_NotFound() throws Exception {
        // Mock service to throw exception
        when(userService.updateUserById(eq(userId), any())).thenThrow(new RuntimeException("User not found"));

        // Perform update user request and expect error
        mockMvc.perform(put("/api/v1/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().is5xxServerError());
        
        // Verify service was called
        verify(userService).updateUserById(eq(userId), any());
    }

    /**
     * Tests user retrieval by account ID with non-existing account.
     */
    @Test
    void getProfileByAccountId_NotFound() throws Exception {
        // Mock service to throw exception
        when(userService.findUserByAccountId(accountId)).thenThrow(new RuntimeException("User not found"));

        // Perform get profile by account ID request and expect error
        mockMvc.perform(get("/api/v1/users/account/{accountId}", accountId))
                .andExpect(status().is5xxServerError());
        
        // Verify service was called
        verify(userService).findUserByAccountId(accountId);
    }

    /**
     * Tests user update by account ID with non-existing account.
     */
    @Test
    void updateProfileByAccountId_NotFound() throws Exception {
        // Mock service to throw exception
        when(userService.updateUserByAccountId(eq(accountId), any())).thenThrow(new RuntimeException("User not found"));

        // Perform update profile by account ID request and expect error
        mockMvc.perform(put("/api/v1/users/account/{accountId}", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().is5xxServerError());
        
        // Verify service was called
        verify(userService).updateUserByAccountId(eq(accountId), any());
    }

    /**
     * Tests user search with no results.
     */
    @Test
    void searchUsersByName_NoResults() throws Exception {
        // Mock service to return empty list
        when(userService.searchUsersByName("NonExistent")).thenReturn(List.of());

        // Perform search users request
        mockMvc.perform(get("/api/v1/users/search")
                .param("name", "NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        
        // Verify service was called
        verify(userService).searchUsersByName("NonExistent");
    }

    /**
     * Tests paginated user retrieval with empty results.
     */
    @Test
    void getAllUsersPaginated_EmptyResults() throws Exception {
        // Build empty paginated response
        PagedResponseDTO<UserProfileDTO> pagedResponse = PagedResponseDTO.<UserProfileDTO>builder()
                .content(List.of())
                .currentPage(0)
                .totalPages(0)
                .totalElements(0L)
                .size(10)
                .first(true)
                .last(true)
                .build();
        
        // Mock service to return empty paginated response
        when(userService.findAllUsers(0, 10, "createdAt", "desc")).thenReturn(pagedResponse);

        // Perform get all users paginated request
        mockMvc.perform(get("/api/v1/users")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "createdAt")
                .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
        
        // Verify service was called
        verify(userService).findAllUsers(0, 10, "createdAt", "desc");
    }

    /**
     * Tests user update with invalid data.
     */
    @Test
    void updateUserById_InvalidData() throws Exception {
        UserUpdateRequestDTO invalidDTO = UserUpdateRequestDTO.builder()
                .phone("invalid")  // Invalid phone format
                .build();

        mockMvc.perform(put("/api/v1/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests pagination with custom parameters.
     */
    @Test
    void getAllUsersPaginated_CustomParams() throws Exception {
        List<UserProfileDTO> users = Arrays.asList(userProfileDTO);
        PagedResponseDTO<UserProfileDTO> pagedResponse = PagedResponseDTO.<UserProfileDTO>builder()
                .content(users)
                .currentPage(2)
                .totalPages(5)
                .totalElements(50L)
                .size(10)
                .first(false)
                .last(false)
                .build();

        when(userService.findAllUsers(2, 10, "username", "asc")).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/users")
                .param("page", "2")
                .param("size", "10")
                .param("sortBy", "username")
                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(2))
                .andExpect(jsonPath("$.totalPages").value(5));

        verify(userService).findAllUsers(2, 10, "username", "asc");
    }



    /**
     * Tests getting all users with default pagination.
     */
    @Test
    void getAllUsersPaginated_DefaultParams() throws Exception {
        List<UserProfileDTO> users = Arrays.asList(userProfileDTO);
        PagedResponseDTO<UserProfileDTO> pagedResponse = PagedResponseDTO.<UserProfileDTO>builder()
                .content(users)
                .currentPage(0)
                .totalPages(1)
                .totalElements(1L)
                .size(10)
                .first(true)
                .last(true)
                .build();

        when(userService.findAllUsers(0, 10, "createdAt", "desc")).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(userService).findAllUsers(0, 10, "createdAt", "desc");
    }

    /**
     * Tests search with empty query.
     */
    @Test
    void searchUsersByName_EmptyQuery() throws Exception {
        when(userService.searchUsersByName("")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/users/search")
                .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
    
}