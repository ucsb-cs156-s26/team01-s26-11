package edu.ucsb.cs156.example.controllers;

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
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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
    mockMvc.perform(get("/api/recommendationrequest/all")).andExpect(status().is(200));
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/recommendationrequest/post")
                .param("requesterEmail", "a@b.com")
                .param("professorEmail", "p@b.com")
                .param("explanation", "test")
                .param("dateRequested", "2022-01-03T00:00:00")
                .param("dateNeeded", "2022-01-03T00:00:00")
                .param("done", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void admin_can_post() throws Exception {

    LocalDateTime now = LocalDateTime.parse("2022-01-03T00:00:00");

    RecommendationRequest r =
        RecommendationRequest.builder()
            .requesterEmail("a@b.com")
            .professorEmail("p@b.com")
            .explanation("help")
            .dateRequested(now)
            .dateNeeded(now)
            .done(false)
            .build();

    when(recommendationRequestRepository.save(r)).thenReturn(r);

    mockMvc
        .perform(
            post("/api/recommendationrequest/post")
                .param("requesterEmail", "a@b.com")
                .param("professorEmail", "p@b.com")
                .param("explanation", "help")
                .param("dateRequested", "2022-01-03T00:00:00")
                .param("dateNeeded", "2022-01-03T00:00:00")
                .param("done", "false")
                .with(csrf()))
        .andExpect(status().isOk());
  }
}
