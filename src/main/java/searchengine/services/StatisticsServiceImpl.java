package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SitesList sites;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;

    @Override
    public StatisticsResponse getStatistics() {

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        for(int i = 0; i < sitesList.size(); i++) {
            Site siteConf = sitesList.get(i);
            searchengine.model.Site siteEntity = siteRepository.findByUrl(siteConf.getUrl());

            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(siteConf.getName());
            item.setUrl(siteConf.getUrl());
            int pages = pageRepository.countBySite(siteEntity);
            int lemmas = lemmaRepository.countBySite(siteEntity);

            item.setPages(pages);
            item.setLemmas(lemmas);
            if (siteEntity == null) {
                item.setStatus(" ");
                item.setError(" ");
                item.setStatusTime(System.currentTimeMillis());
            } else {
                item.setStatus(siteEntity.getStatus());
                item.setError(siteEntity.getLastError());
                item.setStatusTime(siteEntity.getStatusTime().getTime());

                if (siteEntity.getStatus().equals("INDEXING")) {
                    total.setIndexing(true);
                }
            }
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);

            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(total.isIndexing());
        return response;
    }
}
