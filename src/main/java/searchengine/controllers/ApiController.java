package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.Storage;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private Storage storage;

    private final StatisticsService statisticsService;

    public ApiController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/startIndexing")
    public ResponseEntity startIndexing() {

        storage.startIndexing();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
}
