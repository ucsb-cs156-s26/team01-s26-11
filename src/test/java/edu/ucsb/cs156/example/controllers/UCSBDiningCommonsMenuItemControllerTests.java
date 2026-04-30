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
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase {

  @MockitoBean UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/all")).andExpect(status().is(200));
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBDiningCommonsMenuItem/post")
                .param("diningCommonsCode", "ortega")
                .param("name", "Chicken Tenders")
                .param("station", "Entree")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBDiningCommonsMenuItem/post")
                .param("diningCommonsCode", "ortega")
                .param("name", "Chicken Tenders")
                .param("station", "Entree")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_menu_items() throws Exception {
    UCSBDiningCommonsMenuItem item1 =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("ortega")
            .name("Chicken Tenders")
            .station("Entree")
            .build();

    UCSBDiningCommonsMenuItem item2 =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("dlg")
            .name("Pasta")
            .station("Main")
            .build();

    ArrayList<UCSBDiningCommonsMenuItem> expectedItems = new ArrayList<>();
    expectedItems.addAll(Arrays.asList(item1, item2));

    when(ucsbDiningCommonsMenuItemRepository.findAll()).thenReturn(expectedItems);

    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBDiningCommonsMenuItem/all"))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedItems);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_menu_item() throws Exception {
    UCSBDiningCommonsMenuItem item =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("ortega")
            .name("Chicken Tenders")
            .station("Entree")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.save(eq(item))).thenReturn(item);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/UCSBDiningCommonsMenuItem/post")
                    .param("diningCommonsCode", "ortega")
                    .param("name", "Chicken Tenders")
                    .param("station", "Entree")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(item);
    String expectedJson = mapper.writeValueAsString(item);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/UCSBDiningCommonsMenuItem").param("id", "7"))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    UCSBDiningCommonsMenuItem item =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("ortega")
            .name("Chicken Tenders")
            .station("Entree")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.findById(eq(7L))).thenReturn(Optional.of(item));

    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBDiningCommonsMenuItem").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(7L));

    String expectedJson = mapper.writeValueAsString(item);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    when(ucsbDiningCommonsMenuItemRepository.findById(eq(7L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBDiningCommonsMenuItem").param("id", "7"))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(7L));

    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBDiningCommonsMenuItem with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_menu_item() throws Exception {

    UCSBDiningCommonsMenuItem originalItem =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("ortega")
            .name("Chicken Tenders")
            .station("Entree")
            .build();

    UCSBDiningCommonsMenuItem editedItem =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("dlg")
            .name("Pasta")
            .station("Main")
            .build();

    String requestBody = mapper.writeValueAsString(editedItem);

    when(ucsbDiningCommonsMenuItemRepository.findById(eq(67L)))
        .thenReturn(Optional.of(originalItem));

    MvcResult response =
        mockMvc
            .perform(
                put("/api/UCSBDiningCommonsMenuItem")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(67L);
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(originalItem);

    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_menu_item_that_does_not_exist() throws Exception {

    UCSBDiningCommonsMenuItem editedItem =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("ortega")
            .name("Chicken Tenders")
            .station("Entree")
            .build();

    String requestBody = mapper.writeValueAsString(editedItem);

    when(ucsbDiningCommonsMenuItemRepository.findById(eq(67L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(
                put("/api/UCSBDiningCommonsMenuItem")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(67L);

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItem with id 67 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_menu_item() throws Exception {
    UCSBDiningCommonsMenuItem item =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("ortega")
            .name("Chicken Tenders")
            .station("Entree")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.findById(eq(15L))).thenReturn(Optional.of(item));

    MvcResult response =
        mockMvc
            .perform(delete("/api/UCSBDiningCommonsMenuItem").param("id", "15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(15L);
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).delete(item);

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItem with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_nonexistent_menu_item_and_gets_right_error_message()
      throws Exception {
    when(ucsbDiningCommonsMenuItemRepository.findById(eq(15L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(delete("/api/UCSBDiningCommonsMenuItem").param("id", "15").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(15L);

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItem with id 15 not found", json.get("message"));
  }
}
