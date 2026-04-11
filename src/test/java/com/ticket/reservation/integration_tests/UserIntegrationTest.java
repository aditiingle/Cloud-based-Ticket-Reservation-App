package com.ticket.reservation.integration_tests;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticket.reservation.dto.LoginRequestDTO;
import com.ticket.reservation.dto.UserRegistrationRequestDTO;
import com.ticket.reservation.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "springdoc.api-docs.enabled=false",
        "springdoc.swagger-ui.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    public void testCreateUser() throws Exception {
        UserRegistrationRequestDTO requestDTO = new UserRegistrationRequestDTO();

        requestDTO.setName("testuser");
        requestDTO.setPassword("password123");
        requestDTO.setEmail("test@email.com");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@email.com"));

        assert(userRepository.findByEmail("test@email.com").isPresent());
    }

    @Test
    void testLogin() throws Exception {
        UserRegistrationRequestDTO registerDTO = new UserRegistrationRequestDTO();
        registerDTO.setName("loginuser");
        registerDTO.setEmail("login@test.com");
        registerDTO.setPassword("password123");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated());

        LoginRequestDTO loginDTO = new LoginRequestDTO();
        loginDTO.setIdentifier("login@test.com");
        loginDTO.setPassword("password123");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("loginuser"))
                .andExpect(jsonPath("$.email").value("login@test.com"))
                .andExpect(jsonPath("$.token", notNullValue()));
    }


    @Test
    void testGetCurrentUser() throws Exception {
        UserRegistrationRequestDTO registerDTO = new UserRegistrationRequestDTO();
        registerDTO.setName("currentuser");
        registerDTO.setEmail("current@test.com");
        registerDTO.setPassword("password123");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated());

        LoginRequestDTO loginDTO = new LoginRequestDTO();
        loginDTO.setIdentifier("current@test.com");
        loginDTO.setPassword("password123");

        String loginResponse = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String token = loginJson.get("token").asText();

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("currentuser"))
                .andExpect(jsonPath("$.email").value("current@test.com"));
    }

    @Test
    void testCreateUser_duplicateEmail_returnsBadRequest() throws Exception {
        UserRegistrationRequestDTO firstUser = new UserRegistrationRequestDTO();
        firstUser.setName("firstuser");
        firstUser.setEmail("duplicate@test.com");
        firstUser.setPassword("password123");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstUser)))
                .andExpect(status().isCreated());

        UserRegistrationRequestDTO secondUser = new UserRegistrationRequestDTO();
        secondUser.setName("seconduser");
        secondUser.setEmail("duplicate@test.com");
        secondUser.setPassword("password456");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_wrongPassword_returnsUnauthorized() throws Exception {
        UserRegistrationRequestDTO registerDTO = new UserRegistrationRequestDTO();
        registerDTO.setName("wrongpassuser");
        registerDTO.setEmail("wrongpass@test.com");
        registerDTO.setPassword("password123");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated());

        LoginRequestDTO loginDTO = new LoginRequestDTO();
        loginDTO.setIdentifier("wrongpass@test.com");
        loginDTO.setPassword("wrongpassword");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }
}
