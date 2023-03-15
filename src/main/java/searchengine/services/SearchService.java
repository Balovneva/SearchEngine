package searchengine.services;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.search.DetailedSearchItem;
import searchengine.dto.search.SearchResponse;
import searchengine.lemmatizer.LemmaFinder;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.*;

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

    private HashMap<String, Integer> lemmasFrequency = new HashMap<>();

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

        List<Integer> pagesList = findPages(sortedLemmas);

    }

    private List<Integer> findPages(Map<Lemma, Integer> sortedLemmas) {

        List<Integer> indexesFirstPage = new ArrayList<>();

        Lemma firstLemma = sortedLemmas.keySet().iterator().next();

        List<Index> indexes = indexRepository.findByLemma(firstLemma);

        for (Index index : indexes) {
            indexesFirstPage.add(index.getPage().getId());
        }

        sortedLemmas.remove(firstLemma);

        sortedLemmas.entrySet().forEach(lemma -> {

            ArrayList<Integer> indexesAnotherPage = new ArrayList<>();

            indexRepository.findByLemma(lemma.getKey())
                    .forEach(index -> {
                        int pageNumber = index.getPage().getId();

                        indexesAnotherPage.add(pageNumber);
                    });

            for (int i = 0; i < indexesFirstPage.size(); i++) {
                boolean flag = false;
                int item = indexesFirstPage.get(i);
                for (int j = 0; j < indexesAnotherPage.size(); j++) {
                    if (item == indexesAnotherPage.get(j)) {
                        flag = true;
                    }
                }
                if (!flag) {
                    indexesFirstPage.set(i, 0);
                }
            }
        });

        for (int i = indexesFirstPage.size() - 1; i >= 0; i--) {
            if (indexesFirstPage.get(i) == 0) {
                indexesFirstPage.remove(i);
            }
        }

        System.out.println(indexesFirstPage.size());

        indexesFirstPage.forEach(item -> {
            System.out.print(item + " ");
        });

        return indexesFirstPage;
    }

    public static void collectSearchItems(List<Integer> pagesList, Map<Lemma, Integer> sortedLemmas) {
        if (pagesList.isEmpty()) {

        }
        // 1 2

//        pagesList.forEach(page -> {
//            DetailedSearchItem searchItem = new DetailedSearchItem();
//            //searchItem.setSite();
//            //pageRepository
//
//        });

        for (int pageNumber : pagesList) {
            DetailedSearchItem searchItem = new DetailedSearchItem();

            searchItem.setSite();

        }
    }

    //getTitle
    //getSnippet
    //calculateRelevance


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
