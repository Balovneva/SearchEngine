package searchengine.services;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;
import searchengine.lemmatizer.LemmaFinder;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Site;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Getter
public class SearchService {
    @Autowired
    SiteRepository siteRepository;
    @Autowired
    PageRepository pageRepository;
    @Autowired
    LemmaRepository lemmaRepository;
    @Autowired
    IndexRepository indexRepository;

    private HashMap<String, Integer> lemmsFrequency = new HashMap<>();

    private static SearchResponse searchResponse;

    private String query;
    private String site;
    private int offset;
    private int limit;

    public SearchResponse getSearchResults(String query, String site, int offset, int limit) {

        HashMap<String, Integer> lemmas = new HashMap<>();
        LemmaFinder lemmaFinder = new LemmaFinder(query);
        ArrayList<String> words = lemmaFinder.getLemmasFromSearchQuery();

        if (words != null) {
            handleLemmasList(words, site);
        }

        return null;
    }

    private void handleLemmasList(ArrayList<String> words, String site) {

        HashMap<Lemma, Integer> lemmas = new HashMap<>();
        int pages = (int) pageRepository.count();

        if (site == null) {
            words.forEach(word -> {
                ArrayList<Lemma> arrayLemmasTemp = lemmaRepository.findByLemma(word);
                int wordFrequency = 0;

                for (Lemma lemma : arrayLemmasTemp) {
                    wordFrequency += lemma.getFrequency();
                }

                if (wordFrequency < (0.8 * pages) && wordFrequency != 0) {
                    lemmas.put(arrayLemmasTemp.get(0), wordFrequency);
                }
            });
        } else {
            words.forEach(word -> {
                Lemma lemma = lemmaRepository.findByLemmaAndSite(word, siteRepository.findByUrl(site));
                int wordFrequency = lemma.getFrequency();

                if (wordFrequency < (0.8 * pages) && wordFrequency != 0) {
                    lemmas.put(lemma, wordFrequency);
                }
            });
        }

        Map<Lemma, Integer> sortedLemmas = sortByValue(lemmas);

        findPages(sortedLemmas);

    }

    private void findPages(Map<Lemma, Integer> sortedLemmas) {

        List<Integer> indexesFirstPage = new ArrayList<>();

        Lemma firstLemma = sortedLemmas.keySet().iterator().next();

        indexRepository.findByLemma(firstLemma)
                .forEach(index -> indexesFirstPage.add(index.getPage().getId()));

        sortedLemmas.remove(firstLemma);

        sortedLemmas.forEach(lemma -> {
            indexRepository.findByLemma(lemma)
                    .forEach(index -> {
                        int pageNumber = index.getPage().getId();
                        if (!(indexesFirstPage.contains(pageNumber))) {
                            indexesFirstPage.remove(pageNumber);
                        }
                    });
        });

//        sortedLemmas.entrySet().stream().forEach(lemma -> {
//            indexesFirstPage.addAll(indexRepository.findByLemma(lemma.getKey()));
//        });
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

}
