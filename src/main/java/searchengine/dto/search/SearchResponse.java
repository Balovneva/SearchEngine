package searchengine.dto.search;

import lombok.Data;

import java.util.List;

@Data
public class SearchResponse {
    private int count;
    private List<DetailedSearchItem> detailed;
}
