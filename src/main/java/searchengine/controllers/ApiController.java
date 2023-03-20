package searchengine.controllers;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.repository.SiteRepository;
import searchengine.services.SearchService;
import searchengine.services.SiteIndexingService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private SiteIndexingService siteIndexingService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private SiteRepository siteRepository;

    private final StatisticsService statisticsService;

    public ApiController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<String> startIndexing() {

        boolean indexing = siteIndexingService.startIndexing();

        JSONObject response = new JSONObject();

        if (indexing) {
            response.put("result", false);
            response.put("error", "Индексация уже запущена");
        } else {
            response.put("result", true);
        }

        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<String> stopIndexing() {

        boolean stopIndexing = siteIndexingService.stopIndexing();

        JSONObject response = new JSONObject();

            if (stopIndexing) {
                response.put("result", false);
                response.put("error", "Индексация не запущена");
            } else {
                response.put("result", true);
            }

        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }

    @PostMapping("/indexPage")
    public ResponseEntity<String> indexPage(@RequestParam String url) {
        boolean addPage = siteIndexingService.indexPage(url);
        JSONObject response = new JSONObject();

        if (addPage) {
            response.put("result", true);
        } else {
            response.put("result", false);
            response.put("error", "Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }

        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(required = false) String query,
                                                 @RequestParam(required = false) String site,
                                                 @RequestParam(defaultValue = "0") int offset,
                                                 @RequestParam(defaultValue = "3") int limit) {
        JSONObject response = new JSONObject();

        if (query == null || query.isBlank()) {
            response.put("result", false);
            response.put("error", "Задан пустой поисковой запрос");

            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        }

        //return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        return new ResponseEntity(searchService.getSearchResults(query, site, offset, limit), HttpStatus.OK);
    }
}
