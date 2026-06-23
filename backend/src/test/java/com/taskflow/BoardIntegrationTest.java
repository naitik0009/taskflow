package com.taskflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BoardIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    private String register(String email, String name) throws Exception {
        String body = """
                {"email":"%s","displayName":"%s","password":"password123"}
                """.formatted(email, name);
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    @Test
    void fullBoardLifecycleWithRealtimeFriendlyFlow() throws Exception {
        String ownerToken = register("owner@test.dev", "Owner");

        // Create board
        MvcResult boardResult = mockMvc.perform(post("/api/boards")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Sprint Board","description":"Q3 work"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("OWNER"))
                .andReturn();
        String boardId = json(boardResult).get("id").asText();

        // Create a list
        MvcResult listResult = mockMvc.perform(post("/api/boards/" + boardId + "/lists")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"To Do"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String listId = json(listResult).get("id").asText();

        // Create two cards
        String cardAId = json(mockMvc.perform(post("/api/boards/" + boardId + "/lists/" + listId + "/cards")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"First card"}
                                """))
                .andExpect(status().isCreated())
                .andReturn()).get("id").asText();

        MvcResult cardBResult = mockMvc.perform(post("/api/boards/" + boardId + "/lists/" + listId + "/cards")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Second card"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        double cardBPos = json(cardBResult).get("position").asDouble();

        // Move card A above card B (reorder within list)
        MvcResult moveResult = mockMvc.perform(patch("/api/boards/" + boardId + "/cards/" + cardAId + "/move")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetListId":"%s","beforeCardId":null,"afterCardId":"%s"}
                                """.formatted(listId, json(cardBResult).get("id").asText())))
                .andExpect(status().isOk())
                .andReturn();
        double movedPos = json(moveResult).get("position").asDouble();
        assertThat(movedPos).isLessThan(cardBPos);

        // Board detail reflects the new ordering
        mockMvc.perform(get("/api/boards/" + boardId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lists[0].cards.length()").value(2))
                .andExpect(jsonPath("$.lists[0].cards[0].title").value("First card"));
    }

    @Test
    void viewerCannotMutateButCanRead() throws Exception {
        String ownerToken = register("owner2@test.dev", "Owner Two");
        register("viewer@test.dev", "Viewer"); // ensure user exists for invite

        String boardId = json(mockMvc.perform(post("/api/boards")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Read Only Board"}
                                """))
                .andExpect(status().isCreated())
                .andReturn()).get("id").asText();

        // Invite viewer
        mockMvc.perform(post("/api/boards/" + boardId + "/members")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"viewer@test.dev","role":"VIEWER"}
                                """))
                .andExpect(status().isCreated());

        String viewerToken = json(mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"viewer@test.dev","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andReturn()).get("token").asText();

        // Viewer CAN read
        mockMvc.perform(get("/api/boards/" + boardId)
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("VIEWER"));

        // Viewer CANNOT create a list (RBAC enforced server-side)
        mockMvc.perform(post("/api/boards/" + boardId + "/lists")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Sneaky list"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/boards"))
                .andExpect(status().isForbidden());
    }
}
