package com.example.backend.scan.service;

import com.example.backend.scan.entity.Historique;
import java.util.List;

public interface HistoriqueService {
    Historique saveHistorique(Historique historique);
    List<Historique> getHistoriqueByScanId(Long scanId);
}
