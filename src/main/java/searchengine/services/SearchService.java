package searchengine.services;

import lombok.Getter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.search.DetailedSearchItem;
import searchengine.dto.search.SearchResponse;
import searchengine.lemmatizer.KeywordFinder;
import searchengine.lemmatizer.LemmaFinder;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
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

    private SearchResponse searchResponse;
    private List<DetailedSearchItem> detailed;

    private int offset;
    private int limit;
    private int count;

    public SearchResponse getSearchResults(String query, String site, int offset, int limit) {

        this.limit = limit;
        this.offset = offset;
        this.count = 0;

        detailed = new ArrayList<>();
        LemmaFinder lemmaFinder = new LemmaFinder(query);
        ArrayList<String> words = lemmaFinder.getLemmasFromSearchQuery();

        searchResponse = new SearchResponse();
        searchResponse.setResult(true);
        searchResponse.setCount(0);
        searchResponse.setData(new ArrayList<>());

        if (words.isEmpty()) {
            return searchResponse;
        }

        return handleLemmasList(words, site);
    }

    private SearchResponse handleLemmasList(ArrayList<String> words, String site) {

        List<Site> sites = new ArrayList<>();

        if (site == null) {
            sites.addAll(siteRepository.findAll());
        } else {
            sites.add(siteRepository.findByUrl(site));
        }

        sites.forEach(item -> {

            HashMap<Lemma, Integer> lemmas = new HashMap<>();
            int pages = pageRepository.countBySite(item);

            words.forEach(word -> {
                Lemma lemma = lemmaRepository.findByLemmaAndSite(word, item);
                if (lemma == null) {
                    return;
                }
                int wordFrequency = lemma.getFrequency();

                if (wordFrequency < (0.8 * pages) && wordFrequency != 0) {
                    lemmas.put(lemma, wordFrequency);
                }
            });

            if (lemmas.isEmpty()) {
                return;
            }

            Map<Lemma, Integer> sortedLemmas = sortByValue(lemmas);
            List<Integer> pagesList = findPages(sortedLemmas);
            collectSearchItems(pagesList, sortedLemmas);
        });

        sortByRelevance(detailed);
        getOffset();
        searchResponse.setCount(count);
        searchResponse.setData(detailed);
        searchResponse.setResult(true);

        return searchResponse;
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
        if (pagesList.isEmpty()) { return; }

        HashMap<Integer, Double> relevance = getRelevance(pagesList, sortedLemmas);

        for (int i = 0; i < pagesList.size(); i++) {
            if (i >= limit) {
                break;
            }
            int pageNumber = pagesList.get(i);
            DetailedSearchItem searchItem = new DetailedSearchItem();

            Page page = pageRepository.findById(pageNumber);
            searchItem.setSite(page.getSite().getUrl());
            searchItem.setSiteName(page.getSite().getName());
            searchItem.setUri(page.getPath());
            searchItem.setTitle(getTitle(page.getPath()));
            searchItem.setRelevance(relevance.get(pageNumber));
            searchItem.setSnippet(getSnippet(page.getContent(), sortedLemmas));

            detailed.add(searchItem);
            count++;
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
        KeywordFinder keywordFinder = new KeywordFinder(text, lemmas);
        String[] array = keywordFinder.collectWordsForSnippets();
        int keyword = keywordFinder.getKeyword();

        return String.valueOf(handlePhrase(array, keyword));
    }

    private StringBuilder handlePhrase(String[] array, int keyword) {

        StringBuilder stringBuilder = new StringBuilder();
        int start = 0;
        int digit = 0;

        for (int i = keyword - 1;  i > 0; i--) {
            if (array[i].matches("[А-Я]{1}[а-я]+")) {
                start = i + 28;
                digit = i;
                break;
            }
        }

        if (start > array.length) {
            start = array.length;
        }

        for (int i = digit; i < start; i++) {
            stringBuilder.append(array[i] + " ");
        }

        stringBuilder.append("...");
        return stringBuilder;
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

    private void getOffset() {
        if (offset != 0) {
            for (int i = 0; i < offset; i++) {
                detailed.remove(i);
            }
        }
    }

    public void sortByRelevance(List<DetailedSearchItem> list) {
        list.sort(new Comparator<DetailedSearchItem>() {
            @Override
            public int compare(DetailedSearchItem o1, DetailedSearchItem o2) {
                if (o1.getRelevance() == o2.getRelevance()) return 0;
                else if (o1.getRelevance() < o2.getRelevance()) return 1;
                else return -1;
            }
        });
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

