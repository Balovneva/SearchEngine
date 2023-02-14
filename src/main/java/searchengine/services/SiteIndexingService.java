package searchengine.services;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.parsing.SiteParser;
import searchengine.config.SitesList;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.model.Site;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Getter
public class SiteIndexingService {

    @Autowired
    private SitesList sitesList;
    @Autowired
    SiteRepository siteRepository;
    @Autowired
    PageRepository pageRepository;
    @Autowired
    LemmaRepository lemmaRepository; //ToDo: переброска репозиториев через класс-парсер, не оч. мб лучше?
    @Autowired
    IndexRepository indexRepository;

    List<Thread> threads = new ArrayList<>();
    List<ForkJoinPool> forkJoinPools = new ArrayList<>();

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

                        siteEntity.setStatus("INDEXED");
                        siteEntity.setStatusTime(LocalDateTime.now());
                        siteRepository.save(siteEntity);

                    } catch (CancellationException ex) {
                        siteEntity.setStatus("FAILED");
                        siteEntity.setLastError("Ошибка индексации: " + ex.getMessage());
                        siteEntity.setStatusTime(LocalDateTime.now());
                        siteRepository.save(siteEntity);
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

        siteRepository.findAll().forEach(siteEntity -> {
            siteEntity.setLastError("Индексация остановлена пользователем");
            siteEntity.setStatus("FAILED");
            siteEntity.setStatusTime(LocalDateTime.now());
            siteRepository.save(siteEntity);
        });

        threads.clear();
        forkJoinPools.clear();
        return false;
    }

    public boolean indexPage(String url) {

        AtomicBoolean addPage = new AtomicBoolean(false);

        sitesList.getSites().forEach(site ->
        {
            if (url.contains(site.getUrl()) && siteRepository.findByUrl(site.getUrl()) == null) {
                Site siteEntity = addSiteInRepository(site);

                SiteParser siteParser = new SiteParser(url, siteEntity,
                        sitesList, pageRepository,
                        lemmaRepository, indexRepository);
                siteParser.addAdditionalPage();
                siteEntity.setStatus("INDEXED");
                siteEntity.setStatusTime(LocalDateTime.now());
                siteRepository.save(siteEntity);
                addPage.set(true);

            }
            else if (url.contains(site.getUrl())) {
                Site siteEntity = siteRepository.findByUrl(site.getUrl());
                SiteParser siteParser = new SiteParser(url, siteEntity,
                        sitesList, pageRepository,
                        lemmaRepository, indexRepository);
                siteEntity.setStatus("INDEXING");
                siteParser.addAdditionalPage();
                siteEntity.setStatus("INDEXED");
                siteEntity.setStatusTime(LocalDateTime.now());
                siteRepository.save(siteEntity);
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
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntity.setUrl(rootUrl);
        siteRepository.save(siteEntity);
        return siteEntity;
    }

    public void clearData() {
        lemmaRepository.deleteAllInBatch();
        pageRepository.deleteAllInBatch();
        siteRepository.deleteAllInBatch();
    }
}
