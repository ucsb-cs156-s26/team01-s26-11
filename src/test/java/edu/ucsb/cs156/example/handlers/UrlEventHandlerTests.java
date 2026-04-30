package edu.ucsb.cs156.example.handlers;

import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.ucsb.cs156.example.entities.Url;
import org.junit.jupiter.api.Test;

public class UrlEventHandlerTests {
  @Test
  public void checksUrlPrefix() {
    Url url = Url.builder().id(0).url("nothttps").build();
    assertThrows(
        IllegalArgumentException.class, () -> (new UrlEventHandler()).handleBeforeCreate(url));
  }

  @Test
  public void checksLength() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            (new UrlEventHandler())
                .handleBeforeCreate(Url.builder().url("http://" + "a".repeat(5000)).build()));
  }

  @Test
  public void checksUrlPattern() {
    assertThrows(
        IllegalArgumentException.class,
        () -> (new UrlEventHandler()).handleBeforeCreate(Url.builder().url("https://").build()));
  }

  @Test
  void validUrl() {
    UrlEventHandler handler = new UrlEventHandler();
    handler.handleBeforeCreate(Url.builder().url("https://github.com/ucsb-cs156").build());
    handler.handleBeforeCreate(Url.builder().url("http://github.com/ucsb-cs156").build());
    handler.handleBeforeCreate(Url.builder().url("https://" + "a".repeat(1992)).build());
  }
}
