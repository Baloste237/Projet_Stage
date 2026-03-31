package com.example.backend.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class appscanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @Enumerated(EnumType.STRING)
    private ScanStatus status;

    @Column(length = 2000)
    private String resultSummary;

    @Lob
    private String vulnerabilities; // JSON ou texte

    private LocalDateTime createdAt;

}
