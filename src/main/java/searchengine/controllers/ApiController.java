package searchengine.controllers;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.json.simple.JSONObject;
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
    public ResponseEntity<String> startIndexing() {

        JSONObject response = new JSONObject();

        if (storage.isIndexing()) {
            response.put("result", false);
            response.put("error", "Индексация уже запущена");
        } else {
            response.put("result", true);
            storage.startIndexing();
        }

        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<String> stopIndexing() {

        JSONObject response = new JSONObject();

        storage.stopIndexing();
        response.put("result", true);

//        if (storage.isIndexing()) {
//            storage.stopIndexing();
//            response.put("result", true);
//        } else {
//            response.put("result", false);
//            response.put("error", "Индексация не запущена");
//        }

        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
}
