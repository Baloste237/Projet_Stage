package com.example.backend.scan.service;

import com.example.backend.scan.entity.MobileAppScan;
import com.example.backend.scan.entity.Vulnerabilite;
import com.example.backend.scan.repository.VulnerabiliteRepository;
import com.example.backend.scan.dto.OwaspMappingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DataMigrationService {

    private static final Logger log = LoggerFactory.getLogger(DataMigrationService.class);
    private final VulnerabiliteRepository vulnerabiliteRepository;
    private final OwaspMobileMappingService owaspMappingService;

    public DataMigrationService(VulnerabiliteRepository vulnerabiliteRepository, OwaspMobileMappingService owaspMappingService) {
        this.vulnerabiliteRepository = vulnerabiliteRepository;
        this.owaspMappingService = owaspMappingService;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migrateOldMobileVulnerabilities() {
        log.info("[Migration] Démarrage de la migration automatique des anciennes vulnérabilités mobiles vers OWASP 2024...");
        
        List<Vulnerabilite> allVulns = vulnerabiliteRepository.findAll();
        int migratedCount = 0;

        for (Vulnerabilite v : allVulns) {
            // Only migrate if it's a mobile scan and hasn't been migrated yet (owaspVersion is null)
            if (v.getScan() instanceof MobileAppScan && v.getOwaspVersion() == null) {
                
                String legacyCat = v.getOwaspcat() != null ? v.getOwaspcat() : "";
                if (legacyCat.contains(" - ")) {
                    // if it already has the format "M3 - Insecure Authentication", keep the first part as legacy if possible
                    String[] parts = legacyCat.split(" - ", 2);
                    // Just pass it as is to the mapper
                }

                OwaspMappingResult result = owaspMappingService.mapCategory(legacyCat, v.getCweId(), v.getDescription());
                
                v.setLegacyCategory(legacyCat);
                v.setOwaspVersion(result.getOwaspVersion());
                v.setOwaspId(result.getOwaspId());
                v.setOwaspName(result.getOwaspName());
                
                // Update the owaspcat field to the new format to ensure compatibility with old methods if they are still called
                v.setOwaspcat(result.getOwaspId() + " - " + result.getOwaspName());

                vulnerabiliteRepository.save(v);
                migratedCount++;
            }
        }
        
        if (migratedCount > 0) {
            log.info("[Migration] Migration terminée : {} vulnérabilités mobiles mises à jour vers OWASP 2024.", migratedCount);
        } else {
            log.info("[Migration] Aucune vulnérabilité mobile nécessitant une migration n'a été trouvée.");
        }
    }
}
