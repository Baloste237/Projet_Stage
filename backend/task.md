# Refonte Architecture AppScan

- [x] Analyser l'implémentation existante ([AppScanServiceImpl](file:///e:/Projet_stage/Projet_Stage/backend/src/main/java/com/example/backend/scan/service/AppScanServiceImpl.java#26-187))
- [x] Rédiger le plan d'implémentation ([implementation_plan.md](file:///C:/Users/HP/.gemini/antigravity/brain/c8b41742-ec67-407f-9754-d97de15e72bf/implementation_plan.md))
- [x] Refactoriser les Entités
  - [x] Renommer [AppScan](file:///e:/Projet_stage/Projet_Stage/backend/src/main/java/com/example/backend/scan/entity/WebAppScan.java#13-23) en [AbstractScan](file:///e:/Projet_stage/Projet_Stage/backend/src/main/java/com/example/backend/scan/entity/AbstractScan.java#14-64) (Abstract, `@Inheritance`)
  - [x] Créer [WebAppScan](file:///e:/Projet_stage/Projet_Stage/backend/src/main/java/com/example/backend/scan/entity/WebAppScan.java#13-23)
  - [x] Créer [MobileAppScan](file:///e:/Projet_stage/Projet_Stage/backend/src/main/java/com/example/backend/scan/entity/MobileAppScan.java#13-23)
- [x] Refactoriser les Repositories
  - [x] Créer [AbstractScanBaseRepository](file:///e:/Projet_stage/Projet_Stage/backend/src/main/java/com/example/backend/scan/repository/AbstractScanBaseRepository.java#10-13)
  - [x] Renommer [AppScanRepository](file:///e:/Projet_stage/Projet_Stage/backend/src/main/java/com/example/backend/scan/repository/WebAppScanRepository.java#9-12) en [AbstractScanRepository](file:///e:/Projet_stage/Projet_Stage/backend/src/main/java/com/example/backend/scan/repository/AbstractScanRepository.java#9-12)
  - [x] Créer [WebAppScanRepository](file:///e:/Projet_stage/Projet_Stage/backend/src/main/java/com/example/backend/scan/repository/WebAppScanRepository.java#9-12) et [MobileAppScanRepository](file:///e:/Projet_stage/Projet_Stage/backend/src/main/java/com/example/backend/scan/repository/MobileAppScanRepository.java#9-12)
- [x] Refactoriser les Services (Pattern Strategy)
  - [x] Créer l'interface [ScanProcessor](file:///e:/Projet_stage/Projet_Stage/backend/src/main/java/com/example/backend/scan/service/ScanProcessor.java#8-23)
  - [x] Implémenter [WebScanProcessor](file:///e:/Projet_stage/Projet_Stage/backend/src/main/java/com/example/backend/scan/service/WebScanProcessor.java#10-25) et [MobileScanProcessor](file:///e:/Projet_stage/Projet_Stage/backend/src/main/java/com/example/backend/scan/service/MobileScanProcessor.java#10-25)
  - [x] Refactoriser [AppScanServiceImpl](file:///e:/Projet_stage/Projet_Stage/backend/src/main/java/com/example/backend/scan/service/AppScanServiceImpl.java#26-187) pour devenir l'orchestrateur (ou créer `AppScanOrchestrator`)
- [x] Refactoriser le Controller
  - [x] Mettre à jour [AppScanController](file:///e:/Projet_stage/Projet_Stage/backend/src/main/java/com/example/backend/scan/controller/AppScanController.java#15-39) avec la variable de chemin `{appType}`
