package searchengine.services;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;
import searchengine.lemmatizer.LemmaFinder;
import searchengine.repository.LemmaRepository;
import searchengine.repository.SiteRepository;

import java.util.ArrayList;

@Service
@Getter
public class SearchService {
    @Autowired
    SiteRepository siteRepository;
    @Autowired
    LemmaRepository lemmaRepository;

    private static SearchResponse searchResponse;
    //=================
    private String query;
    private String site;
    private int offset;
    private int limit;

    private ArrayList<String> lemmas = new ArrayList<>();

    public SearchResponse getSearchResults(String query, String site, int offset, int limit) {
        LemmaFinder lemmaFinder = new LemmaFinder(query);
        ArrayList<String> lemmas = lemmaFinder.getLemmasFromSearchQuery();
        System.out.println(lemmas);

        return null;
    }

}
