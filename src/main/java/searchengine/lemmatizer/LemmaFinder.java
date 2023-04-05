package searchengine.lemmatizer;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class LemmaFinder {

    private static LuceneMorphology luceneMorphology;
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "МС-П", " МС "};

    private static LemmaRepository lemmaRepository = new LemmaRepository() {
        @Override
        public Lemma findByLemmaAndSite(String lemma, Site site) {
            return null;
        }

        @Override
        public ArrayList<Lemma> findByLemma(String lemma) {
            return null;
        }

        @Override
        public int countBySite(Site site) {
            return 0;
        }

        @Override
        public List<Lemma> findAll() {
            return null;
        }

        @Override
        public List<Lemma> findAll(Sort sort) {
            return null;
        }

        @Override
        public List<Lemma> findAllById(Iterable<Integer> integers) {
            return null;
        }

        @Override
        public <S extends Lemma> List<S> saveAll(Iterable<S> entities) {
            return null;
        }

        @Override
        public void flush() {

        }

        @Override
        public <S extends Lemma> S saveAndFlush(S entity) {
            return null;
        }

        @Override
        public <S extends Lemma> List<S> saveAllAndFlush(Iterable<S> entities) {
            return null;
        }

        @Override
        public void deleteAllInBatch(Iterable<Lemma> entities) {

        }

        @Override
        public void deleteAllByIdInBatch(Iterable<Integer> integers) {

        }

        @Override
        public void deleteAllInBatch() {

        }

        @Override
        public Lemma getOne(Integer integer) {
            return null;
        }

        @Override
        public Lemma getById(Integer integer) {
            return null;
        }

        @Override
        public Lemma getReferenceById(Integer integer) {
            return null;
        }

        @Override
        public <S extends Lemma> List<S> findAll(Example<S> example) {
            return null;
        }

        @Override
        public <S extends Lemma> List<S> findAll(Example<S> example, Sort sort) {
            return null;
        }

        @Override
        public org.springframework.data.domain.Page<Lemma> findAll(Pageable pageable) {
            return null;
        }

        @Override
        public <S extends Lemma> S save(S entity) {
            return null;
        }

        @Override
        public Optional<Lemma> findById(Integer integer) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(Integer integer) {
            return false;
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public void deleteById(Integer integer) {

        }

        @Override
        public void delete(Lemma entity) {

        }

        @Override
        public void deleteAllById(Iterable<? extends Integer> integers) {

        }

        @Override
        public void deleteAll(Iterable<? extends Lemma> entities) {

        }

        @Override
        public void deleteAll() {

        }

        @Override
        public <S extends Lemma> Optional<S> findOne(Example<S> example) {
            return Optional.empty();
        }

        @Override
        public <S extends Lemma> org.springframework.data.domain.Page<S> findAll(Example<S> example, Pageable pageable) {
            return null;
        }

        @Override
        public <S extends Lemma> long count(Example<S> example) {
            return 0;
        }

        @Override
        public <S extends Lemma> boolean exists(Example<S> example) {
            return false;
        }

        @Override
        public <S extends Lemma, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
            return null;
        }
    };
    private static IndexRepository indexRepository = new IndexRepository() {
        @Override
        public Index findByPageAndLemma(Page page, Lemma lemma) {
            return null;
        }

        @Override
        public List<Index> findByLemma(Lemma lemma) {
            return null;
        }

        @Override
        public List<Index> findAll() {
            return null;
        }

        @Override
        public List<Index> findAll(Sort sort) {
            return null;
        }

        @Override
        public List<Index> findAllById(Iterable<Integer> integers) {
            return null;
        }

        @Override
        public <S extends Index> List<S> saveAll(Iterable<S> entities) {
            return null;
        }

        @Override
        public void flush() {

        }

        @Override
        public <S extends Index> S saveAndFlush(S entity) {
            return null;
        }

        @Override
        public <S extends Index> List<S> saveAllAndFlush(Iterable<S> entities) {
            return null;
        }

        @Override
        public void deleteAllInBatch(Iterable<Index> entities) {

        }

        @Override
        public void deleteAllByIdInBatch(Iterable<Integer> integers) {

        }

        @Override
        public void deleteAllInBatch() {

        }

        @Override
        public Index getOne(Integer integer) {
            return null;
        }

        @Override
        public Index getById(Integer integer) {
            return null;
        }

        @Override
        public Index getReferenceById(Integer integer) {
            return null;
        }

        @Override
        public <S extends Index> List<S> findAll(Example<S> example) {
            return null;
        }

        @Override
        public <S extends Index> List<S> findAll(Example<S> example, Sort sort) {
            return null;
        }

        @Override
        public org.springframework.data.domain.Page<Index> findAll(Pageable pageable) {
            return null;
        }

        @Override
        public <S extends Index> S save(S entity) {
            return null;
        }

        @Override
        public Optional<Index> findById(Integer integer) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(Integer integer) {
            return false;
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public void deleteById(Integer integer) {

        }

        @Override
        public void delete(Index entity) {

        }

        @Override
        public void deleteAllById(Iterable<? extends Integer> integers) {

        }

        @Override
        public void deleteAll(Iterable<? extends Index> entities) {

        }

        @Override
        public void deleteAll() {

        }

        @Override
        public <S extends Index> Optional<S> findOne(Example<S> example) {
            return Optional.empty();
        }

        @Override
        public <S extends Index> org.springframework.data.domain.Page<S> findAll(Example<S> example, Pageable pageable) {
            return null;
        }

        @Override
        public <S extends Index> long count(Example<S> example) {
            return 0;
        }

        @Override
        public <S extends Index> boolean exists(Example<S> example) {
            return false;
        }

        @Override
        public <S extends Index, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
            return null;
        }
    };
    private Page page;
    private Site site;
    private String query;

    static {
        try {
            luceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LemmaFinder(){}

    public LemmaFinder(String query) {
        this.query = query;
    }

    public LemmaFinder(Page page, Site site, LemmaRepository lemmaRepository, IndexRepository indexRepository) {
        this.page = page;
        this.site = site;
        LemmaFinder.lemmaRepository = lemmaRepository;
        LemmaFinder.indexRepository = indexRepository;
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
