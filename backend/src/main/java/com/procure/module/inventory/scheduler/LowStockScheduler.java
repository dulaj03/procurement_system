package com.procure.module.inventory.scheduler;

import com.procure.module.company.entity.Company;
import com.procure.module.company.repository.CompanyRepository;
import com.procure.module.inventory.dto.InventoryDtos.LowStockAlert;
import com.procure.module.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LowStockScheduler {

    private final CompanyRepository companyRepository;
    private final InventoryService  inventoryService;

    /**
     * Daily scheduled cron job at 8:00 AM to scan all company warehouses for low stock items.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void scanLowStockAlerts() {
        log.info("Starting scheduled Low-Stock Inventory Scan...");
        List<Company> companies = companyRepository.findAll();

        for (Company company : companies) {
            List<LowStockAlert> alerts = inventoryService.getLowStockAlerts(company.getId());
            if (!alerts.isEmpty()) {
                log.warn("COMPANY ALERT [{}] found {} low-stock items requiring replenishment!",
                        company.getName(), alerts.size());
                for (LowStockAlert alert : alerts) {
                    log.warn("  -> Item: {} (SKU: {}) | Branch: {} | Current Stock: {} | Min Reorder Level: {}",
                            alert.productName(), alert.sku(), alert.branchName(), alert.quantityOnHand(), alert.reorderLevel());
                }
            }
        }
        log.info("Scheduled Low-Stock Inventory Scan completed.");
    }
}
