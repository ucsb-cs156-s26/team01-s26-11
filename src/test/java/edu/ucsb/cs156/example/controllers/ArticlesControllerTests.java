package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase {

  @MockitoBean ArticlesRepository articlesRepository;

  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/articles/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/articles/all")).andExpect(status().is(200)); // logged
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/articles/post")
                .param("title", "Science Communication Club aims to combat climate anxiety")
                .param(
                    "url",
                    "https://dailynexus.com/2026-04-23/science-communication-club-aims-to-combat-climate-anxiety/")
                .param("explanation", "climate anxiety")
                .param("email", "wilsonzlee@ucsb.edu")
                .param("dateAdded", "2022-01-03T00:00:00")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/articles/post")
                .param("title", "Science Communication Club aims to combat climate anxiety")
                .param(
                    "url",
                    "https://dailynexus.com/2026-04-23/science-communication-club-aims-to-combat-climate-anxiety/")
                .param("explanation", "climate anxiety")
                .param("email", "wilsonzlee@ucsb.edu")
                .param("dateAdded", "2022-01-03T00:00:00")
                .with(csrf()))
        .andExpect(status().is(403)); // only admins can post
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_articles() throws Exception {

    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    Articles articles1 =
        Articles.builder()
            .title("Science Communication Club aims to combat climate anxiety")
            .url(
                "https://dailynexus.com/2026-04-23/science-communication-club-aims-to-combat-climate-anxiety/")
            .explanation(
                "Members of the Science Communication Club at UC Santa Barbara gathered at the Career Center to discuss climate anxiety and courses of action on April 14. ")
            .email("wilsonzlee@ucsb.edu")
            .dateAdded(ldt1)
            .build();

    ArrayList<Articles> expectedArticles = new ArrayList<>();
    expectedArticles.add(articles1);

    when(articlesRepository.findAll()).thenReturn(expectedArticles);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/articles/all")).andExpect(status().isOk()).andReturn();

    // assert

    verify(articlesRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedArticles);

    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_article() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    Articles articles1 =
        Articles.builder()
            .title("Science Communication Club aims to combat climate anxiety")
            .url(
                "https://dailynexus.com/2026-04-23/science-communication-club-aims-to-combat-climate-anxiety/")
            .explanation("climate anxiety")
            .email("wilsonzlee@ucsb.edu")
            .dateAdded(ldt1)
            .build();

    when(articlesRepository.save(eq(articles1))).thenReturn(articles1);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/articles/post")
                    .param("title", "Science Communication Club aims to combat climate anxiety")
                    .param(
                        "url",
                        "https://dailynexus.com/2026-04-23/science-communication-club-aims-to-combat-climate-anxiety/")
                    .param("explanation", "climate anxiety")
                    .param("email", "wilsonzlee@ucsb.edu")
                    .param("dateAdded", "2022-01-03T00:00:00")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(articlesRepository, times(1)).save(eq(articles1));
    String expectedJson = mapper.writeValueAsString(articles1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
