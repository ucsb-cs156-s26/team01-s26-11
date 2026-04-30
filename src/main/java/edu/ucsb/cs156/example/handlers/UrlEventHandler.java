package edu.ucsb.cs156.example.handlers;

import edu.ucsb.cs156.example.entities.Url;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler(Url.class)
public class UrlEventHandler {

  // This method runs *before* a new entity is saved (POST) or an existing one is updated
  // (PUT/PATCH)
  @HandleBeforeCreate
  public void handleBeforeCreate(Url url) {

    // Custom business logic validation
    if (!url.getUrl().startsWith("http://") && !url.getUrl().startsWith("https://")) {
      throw new IllegalArgumentException("URL must start with http:// or https://");
    }
    // Additional validation logic can be added here
    if (url.getUrl().length() > 2000) {
      throw new IllegalArgumentException("URL is too long.");
    }
    // Match against a regex pattern for legal URL formats
    String urlPattern = "^(http|https)://[a-zA-Z0-9.-]+(:[0-9]+)?(/.*)?$";
    if (!url.getUrl().matches(urlPattern)) {
      throw new IllegalArgumentException("URL format is invalid.");
    }
  }
}
