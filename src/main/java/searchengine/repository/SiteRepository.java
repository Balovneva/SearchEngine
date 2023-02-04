package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Site;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {

    Site findByUrl(String url);

//    @Modifying(clearAutomatically = true, flushAutomatically = true)
//    @Query(value = "ALTER TABLE site AUTO_INCREMENT = 0", nativeQuery = true)
//    void resetIdOnSite();
}
