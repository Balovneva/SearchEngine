package searchengine.services;

import lombok.Getter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.search.DetailedSearchItem;
import searchengine.dto.search.SearchResponse;
import searchengine.lemmatizer.LemmaFinder;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
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
    private int maxRelevance;

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

        collectSearchItems(pagesList, sortedLemmas);

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

        sortedLemmas.put(firstLemma, 0);
        return indexesFirstPage;
    }

    public void collectSearchItems(List<Integer> pagesList, Map<Lemma, Integer> sortedLemmas) {

        if (pagesList.isEmpty()) {}

        HashMap<Integer, Double> relevance = getRelevance(pagesList, sortedLemmas);

        for (int pageNumber : pagesList) {
            DetailedSearchItem searchItem = new DetailedSearchItem();

            Page page = pageRepository.findById(pageNumber);
            searchItem.setSite(page.getSite().getUrl());
            searchItem.setSiteName(page.getSite().getName());
            searchItem.setUri(page.getPath());
            searchItem.setTitle(getTitle(page.getPath()));
            searchItem.setRelevance(relevance.get(pageNumber));
            searchItem.setSnippet(getSnippet(page.getContent(), sortedLemmas));
        }
    }

    private HashMap<Integer, Double> getRelevance(List<Integer> pagesList, Map<Lemma, Integer> sortedLemmas) {

        HashMap<Integer, Double> relativeRelevance = new HashMap<>();
        HashMap<Integer, Double> absolutelyRelevance = new HashMap<>();
        Set<Lemma> lemmas = sortedLemmas.keySet();

        for (int pageNumber : pagesList) {
            Page page = pageRepository.findById(pageNumber);
            double counter = 0;

            for (Lemma lemma : lemmas) {
                counter += indexRepository.findByPageAndLemma(page, lemma).getRank();
            }

            absolutelyRelevance.put(pageNumber, counter);
        }

        Map.Entry<Integer, Double> maxEntry = absolutelyRelevance.entrySet().stream()
                .max(Comparator.comparing(Map.Entry::getValue))
                .orElse(null);

        double maxValue = maxEntry.getValue();

        absolutelyRelevance.entrySet().forEach(it -> {

            int value = (int) ((it.getValue() / maxValue) * 100);
            double result = value / 100.0;

            relativeRelevance.put(it.getKey(), result);
        });

        return relativeRelevance;
    }

    private String getSnippet(String content, Map<Lemma, Integer> sortedLemmas) {

        Set<Lemma> lemmas = sortedLemmas.keySet();
        StringBuilder stringBuilder = new StringBuilder();
        Document doc = Jsoup.parse(content);
        stringBuilder.append(doc.body().text());

        String text = String.valueOf(stringBuilder);

        LemmaFinder lemmaFinder = new LemmaFinder(text, lemmas);

        String[] array = lemmaFinder.collectWordsForSnippets();

        return "";
    }

    private String getTitle(String uri) {
        Document doc = null;
        try {
            doc = Jsoup.connect(uri).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String title = doc.title();
        return title;
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
