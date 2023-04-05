package searchengine.parsing;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import searchengine.config.SitesList;
import searchengine.lemmatizer.LemmaFinder;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;

import static java.lang.Thread.sleep;

public class SiteParser extends RecursiveTask<Integer> {

    private String page;
    private String siteName;
    private Site siteEntity;
    private static boolean stopIndexing;
    private int pageCount;

    public static CopyOnWriteArraySet <String> allLinks = new CopyOnWriteArraySet<>();
    private static PageRepository pageRepository = new PageRepository() {
        @Override
        public Page findByPath(String path) {
            return null;
        }

        @Override
        public Page findById(int id) {
            return null;
        }

        @Override
        public int countBySite(Site site) {
            return 0;
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public List<Page> findAll() {
            return null;
        }

        @Override
        public List<Page> findAll(Sort sort) {
            return null;
        }

        @Override
        public List<Page> findAllById(Iterable<Integer> integers) {
            return null;
        }

        @Override
        public <S extends Page> List<S> saveAll(Iterable<S> entities) {
            return null;
        }

        @Override
        public void flush() {

        }

        @Override
        public <S extends Page> S saveAndFlush(S entity) {
            return null;
        }

        @Override
        public <S extends Page> List<S> saveAllAndFlush(Iterable<S> entities) {
            return null;
        }

        @Override
        public void deleteAllInBatch(Iterable<Page> entities) {

        }

        @Override
        public void deleteAllByIdInBatch(Iterable<Integer> integers) {

        }

        @Override
        public void deleteAllInBatch() {

        }

        @Override
        public Page getOne(Integer integer) {
            return null;
        }

        @Override
        public Page getById(Integer integer) {
            return null;
        }

        @Override
        public Page getReferenceById(Integer integer) {
            return null;
        }

        @Override
        public <S extends Page> List<S> findAll(Example<S> example) {
            return null;
        }

        @Override
        public <S extends Page> List<S> findAll(Example<S> example, Sort sort) {
            return null;
        }

        @Override
        public org.springframework.data.domain.Page<Page> findAll(Pageable pageable) {
            return null;
        }

        @Override
        public <S extends Page> S save(S entity) {
            return null;
        }

        @Override
        public Optional<Page> findById(Integer integer) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(Integer integer) {
            return false;
        }

        @Override
        public void deleteById(Integer integer) {

        }

        @Override
        public void delete(Page entity) {

        }

        @Override
        public void deleteAllById(Iterable<? extends Integer> integers) {

        }

        @Override
        public void deleteAll(Iterable<? extends Page> entities) {

        }

        @Override
        public void deleteAll() {

        }

        @Override
        public <S extends Page> Optional<S> findOne(Example<S> example) {
            return Optional.empty();
        }

        @Override
        public <S extends Page> org.springframework.data.domain.Page<S> findAll(Example<S> example, Pageable pageable) {
            return null;
        }

        @Override
        public <S extends Page> long count(Example<S> example) {
            return 0;
        }

        @Override
        public <S extends Page> boolean exists(Example<S> example) {
            return false;
        }

        @Override
        public <S extends Page, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
            return null;
        }
    };
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
    private static SitesList sitesList = new SitesList();

    private List<SiteParser> children;

    public SiteParser(String page, String siteName, Site siteEntity) {
        children = new ArrayList<>();
        this.page = page;
        this.siteName = siteName;
        this.siteEntity = siteEntity;
        allLinks.add(page);
    }

    public SiteParser(String siteName, Site siteEntity, SitesList sitesList,
                      PageRepository pageRepository, LemmaRepository lemmaRepository,
                      IndexRepository indexRepository) {
        this(siteName, siteEntity.getUrl(), siteEntity);
        allLinks.add(siteEntity.getUrl());
        allLinks.add(siteEntity.getUrl() + "/");
        SiteParser.sitesList = sitesList;
        SiteParser.pageRepository = pageRepository;
        SiteParser.lemmaRepository = lemmaRepository;
        SiteParser.indexRepository = indexRepository;
    }

    @Override
    protected Integer compute() {
        if (stopIndexing) {
            children.clear();
            return 0;
        }

        try {
            sleep(random());
            Connection.Response response = Jsoup.connect(page)
                    .ignoreHttpErrors(true)
                    .userAgent(sitesList.getUserAgent())
                    .referrer(sitesList.getReferrer())
                    .execute();
            Document doc = response.parse();

            addPage(response, doc);

            Elements links = doc.select("a");

            for (Element link : links) {
                String url = link.attr("href");

                if (!url.contains("http")) {
                    if (!url.startsWith("/") && url.length() > 1) {
                        url = "/" + url;
                    }
                    url = siteName + url;
                }

                if (isCorrected(url)) {
                    addChild(url);
                }
            }
        } catch (InterruptedException | IOException | NullPointerException ex) {
            ex.printStackTrace();
        }

        children.forEach(it -> {
                pageCount += it.join();
        });

        return pageCount;
    }

    public void addAdditionalPage() {
        try {
            Connection.Response response = Jsoup.connect(page)
                    .ignoreHttpErrors(true)
                    .userAgent(sitesList.getUserAgent())
                    .referrer(sitesList.getReferrer())
                    .execute();
            Document doc = response.parse();
            addPage(response, doc);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addPage(Connection.Response response, Document doc) {

        Page pageEntity = pageRepository.findByPath(page);
        if (pageEntity == null) {
            pageEntity = new Page();
        }

        pageEntity.setContent(doc.html());
        pageEntity.setPath(page);
        pageEntity.setCode(response.statusCode());
        pageEntity.setSite(siteEntity);
        pageRepository.save(pageEntity);

        if (response.statusCode() < 400) {
            LemmaFinder lemmaFinder = new LemmaFinder(pageEntity, siteEntity, lemmaRepository, indexRepository);
            lemmaFinder.addLemmasInBase();
        }
    }

    private void addChild(String url) {
        SiteParser child = new SiteParser(url, siteName, siteEntity);
        children.add(child);
        child.fork();
    }

    private boolean isCorrected(String url) {
        return (url.contains(siteName) &&
                !allLinks.contains(url) &&
                !url.contains("#") &&
                !url.matches("n+(.jpg|.jpeg|.png|.pdf|gif|.zip|.tar|.jar|.gz|.svg|ppt|.pptx)"));
    }

    private int random() {
        return (int) Math.round(Math.random() * 51 + 100);
    }

    public void clearListOfLinks() {
        allLinks.clear();
    }

    public static void setStopIndexing(boolean stopIndexing) {
        SiteParser.stopIndexing = stopIndexing;
    }
}

