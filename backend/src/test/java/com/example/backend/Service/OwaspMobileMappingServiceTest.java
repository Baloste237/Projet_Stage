package com.example.backend.Service;

import com.example.backend.scan.dto.OwaspMappingResult;
import com.example.backend.scan.service.OwaspMobileMappingService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OwaspMobileMappingServiceTest {

    private final OwaspMobileMappingService service = new OwaspMobileMappingService();

    @Test
    void testMapCategoryExplicit() {
        OwaspMappingResult result = service.mapCategory("M4 Insecure Authentication", "CWE-287", "Test auth");
        assertEquals("2024", result.getOwaspVersion());
        assertEquals("M3", result.getOwaspId());
        assertEquals("M4 Insecure Authentication", result.getLegacyCategory());
    }

    @Test
    void testMapCategoryFallbackKeyword() {
        OwaspMappingResult result = service.mapCategory("M1 Unknown", "CWE-312", "storage issue");
        assertEquals("M9", result.getOwaspId());
    }

    @Test
    void testMapCategoryUnknown() {
        OwaspMappingResult result = service.mapCategory("Random Issue", "CWE-0", "Something else");
        assertEquals("Unknown", result.getOwaspId());
        assertEquals("Random Issue", result.getLegacyCategory());
    }
}
