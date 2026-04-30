package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.User;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

/** The UserRepository is a repository for User entities. */
@Repository
@RepositoryRestResource(exported = false)
public interface UserRepository extends CrudRepository<User, Long> {
  /**
   * This method returns a User entity with a given email.
   *
   * @param email email address of the user
   * @return Optional of User (empty if not found)
   */
  Optional<User> findByEmail(String email);
}
