package searchengine.services;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.parsing.SiteMapParser;
import searchengine.config.SitesList;
import searchengine.repository.PageRepository;
import searchengine.model.SiteEntity;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;

@Service
@Getter
public class Storage {

    @Autowired
    private SitesList siteList;
    @Autowired
    SiteRepository siteRepository;
    @Autowired
    PageRepository pageRepository;

    List<Thread> threads = new ArrayList<>();
    List<ForkJoinPool> forkJoinPools = new ArrayList<>();
    private boolean indexing = false;

    public void startIndexing() {
        indexing = true;
        clearData();
        threads.clear();
        addSites();
        indexing = false;
    }

    public void addSites() {
        siteList.getSites()
                .stream()
                .forEach(site -> threads.add(new Thread(() -> {

                    String rootUrl = site.getUrl();
                    SiteEntity siteEntity = new SiteEntity();
                    siteEntity.setName(site.getName());
                    siteEntity.setStatus("INDEXING");
                    siteEntity.setStatusTime(LocalDateTime.now());
                    siteEntity.setUrl(rootUrl);
                    siteRepository.save(siteEntity);

                    try {
                        ForkJoinPool forkJoinPool = new ForkJoinPool();
                        forkJoinPool.invoke(new SiteMapParser(rootUrl, siteEntity, pageRepository));
                        forkJoinPools.add(forkJoinPool);

                        siteEntity.setStatus("INDEXED");
                        siteEntity.setStatusTime(LocalDateTime.now());
                        siteRepository.save(siteEntity);

                    } catch (CancellationException ex) {
                        siteEntity.setStatus("FAILED");
                        siteEntity.setLastError("Ошибка индексации: " + ex.getMessage());
                        siteEntity.setStatusTime(LocalDateTime.now());
                        siteRepository.save(siteEntity);
                    }
                })));
        threads.forEach(Thread::start);
        forkJoinPools.forEach(ForkJoinPool::shutdown);
    }

    public void stopIndexing() {
        forkJoinPools.forEach(ForkJoinPool::shutdownNow);
        threads.forEach(Thread::interrupt);

        siteRepository.findAll().forEach(siteEntity -> {
            siteEntity.setLastError("Индексация остановлена пользователем");
            siteEntity.setStatus("FAILED");
            siteEntity.setStatusTime(LocalDateTime.now());
            siteRepository.save(siteEntity);
        });

        threads.clear();
        indexing = false;
    }

    public void clearData() {
        siteRepository.deleteAll();
        pageRepository.deleteAll();
    }
}
