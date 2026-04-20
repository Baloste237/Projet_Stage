package com.example.backend.scan.service;

import com.example.backend.scan.entity.Historique;
import com.example.backend.scan.repository.HistoriqueRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoriqueServiceImpl implements HistoriqueService {

    private final HistoriqueRepository repository;

    public HistoriqueServiceImpl(HistoriqueRepository repository) {
        this.repository = repository;
    }

    @Override
    public Historique saveHistorique(Historique historique) {
        return repository.save(historique);
    }

    @Override
    public List<Historique> getHistoriqueByScanId(Long scanId) {
        return repository.findByScanId(scanId);
    }
}
