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
    private ArrayList<String> queryWords;
    private ArrayList<Integer> keywords = new ArrayList<>();

    public KeywordFinder(String content, ArrayList<String> queryWords) {
        this.content = content;
        this.queryWords = queryWords;

        try {
            luceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] collectWordsForSnippets() {

        String[] words;
        words = content.split(" ");

        return findKeyWords(words, queryWords);
    }

    private String[] findKeyWords(String[] words, ArrayList<String> queryWords) {
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
                    keywords.add(i);
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
        return keywords.get((int)Math.random() * keywords.size());
    }
}
