package searchengine.lemmatizer;

import lombok.Setter;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.LemmaRepository;
import searchengine.services.SiteIndexingService;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class LemmaFinder {

    private static LuceneMorphology luceneMorphology;
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "МС-П", " МС "};
    private static LemmaRepository lemmaRepository;
    private final Page page;
    private final Site site;

    public LemmaFinder(Page page, Site site, LemmaRepository lemmaRepository) {

        this.page = page;
        this.site = site;
        LemmaFinder.lemmaRepository = lemmaRepository;


        try {
            luceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            e.printStackTrace();
        }


//        String text = " ";
//
//        collectLemmas(text).entrySet().forEach(lemma -> {
//            System.out.println(lemma.getKey() + " - " + lemma.getValue());
//        });

//        //ToDo:  тестирование форм слов
//        String test = "ой";
//
//        List<String> testArray = luceneMorphology.getMorphInfo(test);
//
//        System.out.println(testArray.get(0));
    }

    public void collectLemmas() {

        String[] words = arrayContainsRussianWords(page.getContent());
        ConcurrentHashMap<String, Integer> lemmas = new ConcurrentHashMap<>();

        for (String word : words) {

            if (word.isBlank()) {
                continue;
            }

            List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
            if(anyWordBaseBeforeToParticle(wordBaseForms)) {
                continue;
            }

            List<String> normalForms = luceneMorphology.getNormalForms(word);

            if (normalForms.isEmpty()) {
                continue;
            }

            String normalWord = normalForms.get(0);

            //String normalForm = wordBaseForms.get(0);

            Lemma test = lemmaRepository.findByLemma(normalWord);
            if (test != null) {
                int frequency = test.getFrequency();
                test.setFrequency(frequency + 1);
                continue;
            }
            Lemma lemma = new Lemma();
            lemma.setLemma(normalWord);
            lemma.setSite(page.getSite());
            lemma.setFrequency(1);
            lemmaRepository.save(lemma);
        }
    }

    private boolean anyWordBaseBeforeToParticle(List<String> wordBaseForms) {

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
