package com.example.backend.scan.service;

import com.example.backend.scan.entity.Vulnerabilite;
import com.example.backend.scan.repository.VulnerabiliteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VulnerabiliteServiceImpl implements VulnerabiliteService {

    private final VulnerabiliteRepository repository;

    public VulnerabiliteServiceImpl(VulnerabiliteRepository repository) {
        this.repository = repository;
    }

    @Override
    public Vulnerabilite saveVulnerabilite(Vulnerabilite vulnerabilite) {
        return repository.save(vulnerabilite);
    }

    @Override
    public List<Vulnerabilite> saveAllVulnerabilites(List<Vulnerabilite> vulnerabilites) {
        return repository.saveAll(vulnerabilites);
    }

    @Override
    public List<Vulnerabilite> getVulnerabilitesByScanId(Long scanId) {
        return repository.findByScanId(scanId);
    }

    @Override
    public void deleteVulnerabilite(Long id) {
        repository.deleteById(id);
    }
}
