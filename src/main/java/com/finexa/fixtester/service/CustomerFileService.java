package com.finexa.fixtester.service;

import com.finexa.fixtester.dto.CustomerDataEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CustomerFileService {

    private final ResourceLoader resourceLoader;
    private final String customerFilePath;

    public CustomerFileService(ResourceLoader resourceLoader,
                               @Value("${customer.data.file:classpath:customerDetails.txt}") String customerFilePath) {
        this.resourceLoader   = resourceLoader;
        this.customerFilePath = customerFilePath;
    }

    /**
     * Load the first {@code limit} entries from the customer data file.
     * Each line is pipe-delimited: tradeAccId|customerNo  (3rd column symbol is ignored — set via UI)
     */
    public List<CustomerDataEntry> load(int limit) {
        List<CustomerDataEntry> entries = new ArrayList<>();
        Resource resource = resourceLoader.getResource(customerFilePath);

        if (!resource.exists()) {
            log.warn("Customer data file not found at: {}", customerFilePath);
            return entries;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null && entries.size() < limit) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length < 2) {
                    log.warn("Skipping malformed line: {}", line);
                    continue;
                }
                try {
                    int    tradeAccId = Integer.parseInt(parts[0].trim());
                    String customerNo = parts[1].trim();
                    entries.add(new CustomerDataEntry(tradeAccId, customerNo));
                } catch (NumberFormatException e) {
                    log.warn("Skipping line with non-numeric tradeAccId: {}", line);
                }
            }
        } catch (Exception e) {
            log.error("Failed to read customer data file: {}", e.getMessage(), e);
        }

        log.info("Loaded {} customer entries (limit={})", entries.size(), limit);
        return entries;
    }
}
