package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

  @MockitoBean RecommendationRequestRepository recommendationRequestRepository;

  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/recommendationrequest/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    LocalDateTime date1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime date2 = LocalDateTime.parse("2022-02-03T00:00:00");

    RecommendationRequest request1 =
        RecommendationRequest.builder()
            .requesterEmail("student1@ucsb.edu")
            .professorEmail("prof1@ucsb.edu")
            .explanation("Grad school letter")
            .dateRequested(date1)
            .dateNeeded(date2)
            .done(false)
            .build();

    RecommendationRequest request2 =
        RecommendationRequest.builder()
            .requesterEmail("student2@ucsb.edu")
            .professorEmail("prof2@ucsb.edu")
            .explanation("Scholarship letter")
            .dateRequested(date1)
            .dateNeeded(date2)
            .done(true)
            .build();

    ArrayList<RecommendationRequest> expectedRequests = new ArrayList<>();
    expectedRequests.addAll(Arrays.asList(request1, request2));

    when(recommendationRequestRepository.findAll()).thenReturn(expectedRequests);

    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequest/all"))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findAll();

    String expectedJson = mapper.writeValueAsString(expectedRequests);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/recommendationrequest/post")
                .param("requesterEmail", "student@ucsb.edu")
                .param("professorEmail", "professor@ucsb.edu")
                .param("explanation", "Need a letter")
                .param("dateRequested", "2022-01-03T00:00:00")
                .param("dateNeeded", "2022-02-03T00:00:00")
                .param("done", "true")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/recommendationrequest/post")
                .param("requesterEmail", "student@ucsb.edu")
                .param("professorEmail", "professor@ucsb.edu")
                .param("explanation", "Need a letter")
                .param("dateRequested", "2022-01-03T00:00:00")
                .param("dateNeeded", "2022-02-03T00:00:00")
                .param("done", "true")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_post() throws Exception {
    LocalDateTime dateRequested = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime dateNeeded = LocalDateTime.parse("2022-02-03T00:00:00");

    RecommendationRequest request =
        RecommendationRequest.builder()
            .requesterEmail("student@ucsb.edu")
            .professorEmail("professor@ucsb.edu")
            .explanation("Need a letter")
            .dateRequested(dateRequested)
            .dateNeeded(dateNeeded)
            .done(true)
            .build();

    when(recommendationRequestRepository.save(request)).thenReturn(request);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/recommendationrequest/post")
                    .param("requesterEmail", "student@ucsb.edu")
                    .param("professorEmail", "professor@ucsb.edu")
                    .param("explanation", "Need a letter")
                    .param("dateRequested", "2022-01-03T00:00:00")
                    .param("dateNeeded", "2022-02-03T00:00:00")
                    .param("done", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).save(request);

    String expectedJson = mapper.writeValueAsString(request);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
