package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sites")
@Getter
@Setter
public class SiteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')")
    private String status;

    @Column(name = "status_time", nullable = false)
    private LocalDateTime statusTime;

    @Column(columnDefinition = "TEXT", name = "last_error")
    private String lastError;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String name;
}
