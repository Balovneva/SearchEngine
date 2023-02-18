package searchengine.dto.statistics;

import lombok.Data;

@Data
public class StatisticsResponse {
    private boolean result; //ToDo: что сюда нужно? и почему кнопки индексации нет
    private StatisticsData statistics;
}
