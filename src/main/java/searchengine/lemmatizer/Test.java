package searchengine.lemmatizer;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class Test {

    private static LuceneMorphology luceneMorphology;

    private static String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "МС-П"};

    public static void main(String[] args) {

        try {
            luceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String text = "\n" +
                "   <div class=\"item center menC\">\n" +
                "     <h2>Операции сведения</h2><div class=\"date\">Последнее обновление: 29.04.2018</div>\n" +
                "\t<div class=\"socBlock\">\n" +
                "\t<div class=\"share soctop\">\n" +
                "\t<ul>\n" +
                "\t<li><a title=\"Поделиться в Вконтакте\" rel=\"nofollow\" class=\"fa fa-lg fa-vk\"></a></li>\n" +
                "\t<li><a title=\"Поделиться в Телеграм\" rel=\"nofollow\" class=\"fa fa-lg fa-telegram\"></a></li>\n" +
                "\t<li><a title=\"Поделиться в Одноклассниках\" rel=\"nofollow\" class=\"fa fa-lg fa-odnoklassniki\"></a></li>\n" +
                "\t<li><a title=\"Поделиться в Твиттере\" rel=\"nofollow\" class=\"fa fa-lg fa-twitter\"></a></li>\n" +
                "\t<li><a  rel=\"nofollow\" class=\"fa fa-lg fa-facebook\"></a></li>\n" +
                "\t</ul>\n" +
                "\t</div>\n" +
                "\t</div>\n" +
                "\n" +
                "\t<div style=\"margin-top:23px;margin-left:5px;\">\n" +
                "\t\t\n" +
                "\t\t<img src=\"https://metanit.com/1000х120_1.2.jpg\" id=\"jma\" style=\"cursor:pointer;\" />\n" +
                "\t\t\t</div>\n" +
                "\n" +
                "\t<p>Операции сведения представляют терминальные операции, которые возвращают некоторое значение - результат операции. \n" +
                "В Stream API есть ряд операций сведения.</p>\n" +
                "<h3>count</h3>\n" +
                "<p>Метод <code>count()</code> возвращает количество элементов в потоке данных:</p>\n" +
                "<pre class=\"brush:java;\">\n" +
                "import java.util.stream.Stream;\n" +
                "import java.util.Optional;\n" +
                "import java.u ой til.*;\n" +
                "public class Program {";

        collectLemmas(text).entrySet().forEach(lemma -> {
            System.out.println(lemma.getKey() + " - " + lemma.getValue());
        });

        //ToDo:  тестирование форм слов
        String test = "ой";

        List<String> testArray = luceneMorphology.getMorphInfo(test);

        System.out.println(testArray.get(0));
    }

    public static ConcurrentHashMap<String, Integer> collectLemmas(String text) {
        String[] words = arrayContainsRussianWords(text);
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

            if (lemmas.containsKey(normalWord)) {
                lemmas.put(normalWord, lemmas.get(normalWord) + 1);
            } else {
                lemmas.put(normalWord, 1);
            }

        }

        return lemmas;
    }

    public static boolean anyWordBaseBeforeToParticle(List<String> wordBaseForms) {

        return hasParticleProperty(wordBaseForms.get(0));

        //return wordBaseForms.stream().anyMatch(this::hasParticleProperty);
    }

    public static boolean hasParticleProperty(String wordBase) {
        for (String property : particlesNames) {
            if (wordBase.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }

    public static String[] arrayContainsRussianWords(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("[^а-я]+", " ")
                .trim()
                .split("\\s+");
    }
}
