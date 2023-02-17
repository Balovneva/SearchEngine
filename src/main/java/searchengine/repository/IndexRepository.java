package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;

public interface IndexRepository extends JpaRepository<Index, Integer> {
    Index findByPageAndLemma(Page page, Lemma lemma);
}
