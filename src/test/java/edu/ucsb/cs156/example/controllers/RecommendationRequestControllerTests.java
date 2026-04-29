package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
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
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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
    RecommendationRequest request =
        RecommendationRequest.builder()
            .requesterEmail("student@ucsb.edu")
            .professorEmail("professor@ucsb.edu")
            .explanation("Need a letter")
            .dateRequested(LocalDateTime.parse("2022-01-03T00:00:00"))
            .dateNeeded(LocalDateTime.parse("2022-02-03T00:00:00"))
            .done(true)
            .build();

    ArrayList<RecommendationRequest> expected = new ArrayList<>();
    expected.add(request);

    when(recommendationRequestRepository.findAll()).thenReturn(expected);

    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequest/all"))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findAll();

    String expectedJson = mapper.writeValueAsString(expected);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc.perform(get("/api/recommendationrequest").param("id", "7")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void get_by_id_found() throws Exception {
    RecommendationRequest request =
        RecommendationRequest.builder()
            .requesterEmail("student@ucsb.edu")
            .professorEmail("professor@ucsb.edu")
            .explanation("Need a letter")
            .dateRequested(LocalDateTime.parse("2022-01-03T00:00:00"))
            .dateNeeded(LocalDateTime.parse("2022-02-03T00:00:00"))
            .done(true)
            .build();

    when(recommendationRequestRepository.findById(7L)).thenReturn(Optional.of(request));

    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequest").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(7L);

    String expectedJson = mapper.writeValueAsString(request);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void get_by_id_not_found() throws Exception {
    when(recommendationRequestRepository.findById(7L)).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequest").param("id", "7"))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(7L);

    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete() throws Exception {
    RecommendationRequest request =
        RecommendationRequest.builder()
            .requesterEmail("student@ucsb.edu")
            .professorEmail("professor@ucsb.edu")
            .explanation("Need a letter")
            .dateRequested(LocalDateTime.parse("2022-01-03T00:00:00"))
            .dateNeeded(LocalDateTime.parse("2022-02-03T00:00:00"))
            .done(true)
            .build();

    when(recommendationRequestRepository.findById(1L)).thenReturn(Optional.of(request));

    MvcResult response =
        mockMvc
            .perform(delete("/api/recommendationrequest").param("id", "1").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(1L);
    verify(recommendationRequestRepository, times(1)).delete(request);

    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 1 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_delete_not_found() throws Exception {
    when(recommendationRequestRepository.findById(1L)).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(delete("/api/recommendationrequest").param("id", "1").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(1L);

    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 1 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_update() throws Exception {
    RecommendationRequest original =
        RecommendationRequest.builder()
            .requesterEmail("oldstudent@ucsb.edu")
            .professorEmail("oldprofessor@ucsb.edu")
            .explanation("Old explanation")
            .dateRequested(LocalDateTime.parse("2022-01-03T00:00:00"))
            .dateNeeded(LocalDateTime.parse("2022-02-03T00:00:00"))
            .done(false)
            .build();

    RecommendationRequest edited =
        RecommendationRequest.builder()
            .requesterEmail("newstudent@ucsb.edu")
            .professorEmail("newprofessor@ucsb.edu")
            .explanation("New explanation")
            .dateRequested(LocalDateTime.parse("2023-01-03T00:00:00"))
            .dateNeeded(LocalDateTime.parse("2023-02-03T00:00:00"))
            .done(true)
            .build();

    String requestBody = mapper.writeValueAsString(edited);

    when(recommendationRequestRepository.findById(1L)).thenReturn(Optional.of(original));

    MvcResult response =
        mockMvc
            .perform(
                put("/api/recommendationrequest")
                    .param("id", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(1L);
    verify(recommendationRequestRepository, times(1)).save(original);

    assertEquals("newstudent@ucsb.edu", original.getRequesterEmail());
    assertEquals("newprofessor@ucsb.edu", original.getProfessorEmail());
    assertEquals("New explanation", original.getExplanation());
    assertEquals(LocalDateTime.parse("2023-01-03T00:00:00"), original.getDateRequested());
    assertEquals(LocalDateTime.parse("2023-02-03T00:00:00"), original.getDateNeeded());
    assertEquals(true, original.getDone());

    String expectedJson = mapper.writeValueAsString(original);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_update_not_found() throws Exception {
    RecommendationRequest edited =
        RecommendationRequest.builder()
            .requesterEmail("newstudent@ucsb.edu")
            .professorEmail("newprofessor@ucsb.edu")
            .explanation("New explanation")
            .dateRequested(LocalDateTime.parse("2023-01-03T00:00:00"))
            .dateNeeded(LocalDateTime.parse("2023-02-03T00:00:00"))
            .done(true)
            .build();

    String requestBody = mapper.writeValueAsString(edited);

    when(recommendationRequestRepository.findById(1L)).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(
                put("/api/recommendationrequest")
                    .param("id", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(1L);

    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 1 not found", json.get("message"));
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
    RecommendationRequest savedRequest =
        RecommendationRequest.builder()
            .requesterEmail("student@ucsb.edu")
            .professorEmail("professor@ucsb.edu")
            .explanation("Need a letter")
            .dateRequested(LocalDateTime.parse("2022-01-03T00:00:00"))
            .dateNeeded(LocalDateTime.parse("2022-02-03T00:00:00"))
            .done(true)
            .build();

    when(recommendationRequestRepository.save(any())).thenReturn(savedRequest);

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

    ArgumentCaptor<RecommendationRequest> captor =
        ArgumentCaptor.forClass(RecommendationRequest.class);

    verify(recommendationRequestRepository, times(1)).save(captor.capture());

    RecommendationRequest saved = captor.getValue();

    assertEquals("student@ucsb.edu", saved.getRequesterEmail());
    assertEquals("professor@ucsb.edu", saved.getProfessorEmail());
    assertEquals("Need a letter", saved.getExplanation());
    assertEquals(LocalDateTime.parse("2022-01-03T00:00:00"), saved.getDateRequested());
    assertEquals(LocalDateTime.parse("2022-02-03T00:00:00"), saved.getDateNeeded());
    assertEquals(true, saved.getDone());

    String expectedJson = mapper.writeValueAsString(savedRequest);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
