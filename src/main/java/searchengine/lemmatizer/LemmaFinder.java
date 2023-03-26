package searchengine.lemmatizer;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;

import java.io.IOException;
import java.util.*;

public class LemmaFinder {

    private static LuceneMorphology luceneMorphology;
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "МС-П", " МС "};
    private static LemmaRepository lemmaRepository;
    private static IndexRepository indexRepository;
    private Page page;
    private Site site;
    private String query;
    private Set<Lemma> sortedLemmas;

    public LemmaFinder(){}

    public LemmaFinder(String query) {
        this.query = query;

        try {
            luceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LemmaFinder(Page page, Site site, LemmaRepository lemmaRepository, IndexRepository indexRepository) {

        this.page = page;
        this.site = site;
        LemmaFinder.lemmaRepository = lemmaRepository;
        LemmaFinder.indexRepository = indexRepository;

        try {
            luceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> collectNormalWords() {

        String[] words;
        ArrayList<String> lemmas = new ArrayList<>();

        if (page == null) {
            words = arrayContainsRussianWords(query);
        } else {
            words = arrayContainsRussianWords(page.getContent());
        }

        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }

            List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);

            if (anyWordBaseBeforeToParticle(wordBaseForms)) {
                continue;
            }

            List<String> normalForms = luceneMorphology.getNormalForms(word);

            if (normalForms.isEmpty()) {
                continue;
            }

            lemmas.add(normalForms.get(0));
        }
        return lemmas;
    }

    public void addLemmasInBase() {
        collectNormalWords().forEach(normalWord -> {
            Lemma lemma = lemmaRepository.findByLemmaAndSite(normalWord, page.getSite());
            Index index = indexRepository.findByPageAndLemma(page, lemma);

            if (lemma != null) {
                if (index != null) {
                    float rank = index.getRank();
                    index.setRank(rank + 1);
                    indexRepository.save(index);
                } else {
                    addNewIndex(lemma);
                    int frequency = lemma.getFrequency();
                    lemma.setFrequency(frequency + 1);
                    lemmaRepository.save(lemma);
                }
            } else {
                addNewLemma(normalWord);
            }
        });
    }

    public ArrayList<String> getLemmasFromSearchQuery() {
        return collectNormalWords();
    }

    private void addNewLemma(String normalWord) {
        Lemma lemma = new Lemma();
        lemma.setLemma(normalWord);
        lemma.setSite(page.getSite());
        lemma.setFrequency(1);
        lemmaRepository.save(lemma);

        addNewIndex(lemma);
    }

    private void addNewIndex(Lemma lemma) {
        Index index = new Index();
        index.setLemma(lemma);
        index.setPage(page);
        index.setRank(1);
        indexRepository.save(index);
    }

    boolean anyWordBaseBeforeToParticle(List<String> wordBaseForms) {
        return hasParticleProperty(wordBaseForms.get(0));
    }

    private boolean hasParticleProperty(String wordBase) {
        for (String property : particlesNames) {
            if (wordBase.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }

    private String[] arrayContainsRussianWords(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("[^а-я]+", " ")
                .trim()
                .split("\\s+");
    }
}
