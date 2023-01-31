package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.parsing.SiteMapParser;
import searchengine.config.SitesList;
import searchengine.repository.PageRepository;
import searchengine.model.SiteEntity;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.concurrent.ForkJoinPool;

@Service
public class Storage {

    @Autowired
    private SitesList siteList;

    @Autowired
    SiteRepository siteRepository;

    @Autowired
    PageRepository pageRepository;

    public void startIndexing() {
        clearData();
        addSites();
    }

    public void addSites() {
        siteList.getSites()
                .stream()
                .forEach(site -> {

                    String rootUrl = site.getUrl();

                    SiteEntity siteEntity = new SiteEntity();
                    siteEntity.setName(site.getName());
                    siteEntity.setStatus("INDEXING");
                    siteEntity.setStatusTime(LocalDateTime.now());
                    siteEntity.setUrl(rootUrl);
                    siteRepository.save(siteEntity);

                    new ForkJoinPool().invoke(new SiteMapParser(rootUrl, siteEntity, pageRepository));
                    siteEntity.setStatus("INDEXED");
                    siteEntity.setStatusTime(LocalDateTime.now());
                    siteRepository.save(siteEntity);
                });
    }

    public void clearData() {
        siteRepository.deleteAll();
        pageRepository.deleteAll();
    }
}
