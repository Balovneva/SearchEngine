package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.model.SiteEntity;
import searchengine.model.SiteRepository;

import java.time.LocalDateTime;

@Service
public class Storage {

    @Autowired
    private SitesList siteList;

    @Autowired
    SiteRepository siteRepository;

    public void startIndexing() {
        addSites();
    }

    public void addSites() {
        siteList.getSites()
                .stream()
                .forEach(site -> {
                    SiteEntity siteEntity = new SiteEntity();
                    siteEntity.setName(site.getName());
                    siteEntity.setStatus("INDEXING");
                    siteEntity.setLastError("-");
                    siteEntity.setStatusTime(LocalDateTime.now());
                    siteEntity.setUrl(site.getUrl());
                    siteRepository.save(siteEntity);
                });
    }
}
