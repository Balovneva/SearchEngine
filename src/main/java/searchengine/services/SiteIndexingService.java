package searchengine.services;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.Page;
import searchengine.parsing.SiteParser;
import searchengine.config.SitesList;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.model.Site;
import searchengine.repository.SiteRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Getter
public class SiteIndexingService {
    @Autowired
    SiteRepository siteRepository;
    @Autowired
    PageRepository pageRepository;
    @Autowired
    LemmaRepository lemmaRepository;
    @Autowired
    IndexRepository indexRepository;
    @Autowired
    private SitesList sitesList;

    private List<Thread> threads = new ArrayList<>();
    private List<ForkJoinPool> forkJoinPools = new ArrayList<>();

    public boolean startIndexing() {
        AtomicBoolean indexing = new AtomicBoolean(false);

        siteRepository.findAll().forEach(site -> {
            if (site.getStatus().equals("INDEXING")) {
                indexing.set(true);
            }
        });

        if (indexing.get()) {
            return true;
        }

        addSites();
        return false;
    }

    public void addSites() {
        threads = new ArrayList<>();
        forkJoinPools = new ArrayList<>();
        clearData();

        sitesList.getSites()
                .stream()
                .forEach(site -> threads.add(new Thread(() -> {

                    Site siteEntity = addSiteInRepository(site);
                    SiteParser siteParser = new SiteParser(siteEntity.getUrl(),
                            siteEntity, sitesList, pageRepository,
                            lemmaRepository, indexRepository);
                    try {
                        ForkJoinPool forkJoinPool = new ForkJoinPool();
                        forkJoinPools.add(forkJoinPool);
                        forkJoinPool.invoke(siteParser);
                        Page mainPage = pageRepository.findByPath(site.getUrl());

                        if (mainPage.getPath().equals(site.getUrl())
                                && mainPage.getCode() >= 400) {
                            siteEntity.setLastError("Ошибка индексации: " + mainPage.getCode());
                            setFailedStatus(siteEntity);
                        } else {
                            setIndexedStatus(siteEntity);
                        }
                    } catch (CancellationException ex) {
                        siteEntity.setLastError("Ошибка индексации: " + ex.getMessage());
                        setFailedStatus(siteEntity);
                    }
                    siteParser.clearListOfLinks();
                })));
        threads.forEach(Thread::start);
        forkJoinPools.forEach(ForkJoinPool::shutdown);
    }

    public boolean stopIndexing() {
        AtomicBoolean indexing = new AtomicBoolean(false);

        siteRepository.findAll().forEach(site -> {
            if (site.getStatus().equals("INDEXING")) {
                indexing.set(true);
            }
        });

        if (!indexing.get()) {
            return true;
        }

        forkJoinPools.forEach(ForkJoinPool::shutdownNow);
        threads.forEach(Thread::interrupt);
        SiteParser.setStopIndexing(true);

        siteRepository.findAll().forEach(siteEntity -> {
            siteEntity.setLastError("Индексация остановлена пользователем");
            setFailedStatus(siteEntity);
        });

        threads.clear();
        forkJoinPools.clear();

        return false;
    }

    public boolean indexPage(String url) {

        AtomicBoolean addPage = new AtomicBoolean(false);
        SiteParser.setStopIndexing(false);

        sitesList.getSites().forEach(site ->
        {
            if (url.contains(site.getUrl()) && siteRepository.findByUrl(site.getUrl()) == null) {
                Site siteEntity = addSiteInRepository(site);

                SiteParser siteParser = new SiteParser(url, siteEntity,
                        sitesList, pageRepository,
                        lemmaRepository, indexRepository);
                siteParser.addAdditionalPage();
                setIndexedStatus(siteEntity);
                addPage.set(true);

            } else if (url.contains(site.getUrl())) {
                Site siteEntity = siteRepository.findByUrl(site.getUrl());
                SiteParser siteParser = new SiteParser(url, siteEntity,
                        sitesList, pageRepository,
                        lemmaRepository, indexRepository);
                siteEntity.setStatus("INDEXING");
                siteParser.addAdditionalPage();
                setIndexedStatus(siteEntity);
                addPage.set(true);
            }
        });

        if (addPage.get()) {
            return true;
        } else {
            return false;
        }
    }

    public Site addSiteInRepository(searchengine.config.Site site) {
        String rootUrl = site.getUrl();
        Site siteEntity = new Site();
        siteEntity.setName(site.getName());
        siteEntity.setStatus("INDEXING");
        siteEntity.setStatusTime(new Timestamp(System.currentTimeMillis()));
        siteEntity.setUrl(rootUrl);
        siteRepository.save(siteEntity);
        return siteEntity;
    }

    private void setIndexedStatus(Site siteEntity) {
        siteEntity.setStatus("INDEXED");
        siteEntity.setStatusTime(new Timestamp(System.currentTimeMillis()));
        siteRepository.save(siteEntity);
    }

    private void setFailedStatus(Site siteEntity) {
        siteEntity.setStatus("FAILED");
        siteEntity.setStatusTime(new Timestamp(System.currentTimeMillis()));
        siteRepository.save(siteEntity);
    }

    public void clearData() {
        indexRepository.deleteAllInBatch();
        lemmaRepository.deleteAllInBatch();
        pageRepository.deleteAllInBatch();
        siteRepository.deleteAllInBatch();
        SiteParser.setStopIndexing(false);
    }

}
