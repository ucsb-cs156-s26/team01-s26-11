package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Url;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;

/**
 * The UrlRepository is a repository for Url entities, that is, it is the abstraction for the
 * database table for Urls
 */
@RepositoryRestResource(path = "urls")
@Repository
public interface UrlRepository extends CrudRepository<Url, Long> {

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  void deleteById(Long id);

  // Anyone authenticated can view urls
  @Override
  @PreAuthorize("hasRole('USER')")
  List<Url> findAll();

  // Only Admins can create/update urls
  @Override
  @PreAuthorize("hasRole('ADMIN')")
  <S extends Url> S save(S entity);
}
