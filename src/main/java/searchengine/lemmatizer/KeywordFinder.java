package searchengine.lemmatizer;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class KeywordFinder extends LemmaFinder{

    private static LuceneMorphology luceneMorphology;
    private String content;
    private String mostRareLemma;
    private ArrayList<String> queryWords;
    private ArrayList<Integer> priorityKeywords;
    private ArrayList<Integer> keywords;

    public KeywordFinder(String content, ArrayList<String> queryWords, String mostRareLemma) {
        this.content = content;
        this.queryWords = queryWords;
        this.mostRareLemma = mostRareLemma;
        this.priorityKeywords = new ArrayList<>();
        this.keywords = new ArrayList<>();

        try {
            luceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] collectWordsForSnippets() {

        String[] words;
        words = content.split(" ");

        return findKeyWords(words);
    }

    private String[] findKeyWords(String[] words) {
        for (int i = 0; i < words.length; i++) {

            String parentWord = words[i];
            String word = checkWord(parentWord);

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

            String element = normalForms.get(0);

            for (int j = 0; j < queryWords.size(); j++) {
                if (element.equals(queryWords.get(j))) {
                    words[i] = "<b>" + parentWord + "</b>";
                    if (element.equals(mostRareLemma)) {
                        priorityKeywords.add(i);
                    } else {
                        keywords.add(i);
                    }
                    break;
                }
            }
        }

        return words;
    }

    private String checkWord(String word) {
        return word.toLowerCase(Locale.ROOT)
                .replaceAll("[^а-я]+", "")
                .trim();
    }

    public Integer getKeyword() {
        int result = 0;
        int value = 0;
        if (!priorityKeywords.isEmpty()) {
            value = (int) (Math.random() * priorityKeywords.size());
            result = priorityKeywords.get(value);
        } else {
            value = (int) (Math.random() * keywords.size());
            result = keywords.get(value);
        }
        return result;
    }
}
