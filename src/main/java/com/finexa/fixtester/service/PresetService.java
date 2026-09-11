package com.finexa.fixtester.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finexa.fixtester.dto.Preset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Stores named form presets in a single JSON file so they survive restarts and can be shared
 * (committed, copied between machines) instead of living in one browser's local storage.
 *
 * <p>Shape on disk: {@code { "<category>": { "<preset name>": Preset, ... }, ... }}</p>
 */
@Service
@Slf4j
public class PresetService {

    private final ObjectMapper objectMapper;
    private final Path storeFile;

    /** category -> (preset name -> preset); case-insensitive names so "Buy To Close" and "buy to close" collide. */
    private final Map<String, Map<String, Preset>> presets = new LinkedHashMap<>();

    private final Object lock = new Object();

    public PresetService(ObjectMapper objectMapper,
                         @Value("${preset.data.file:./presets.json}") String presetFilePath) {
        this.objectMapper = objectMapper;
        this.storeFile    = Paths.get(presetFilePath).toAbsolutePath();
    }

    @PostConstruct
    void load() {
        synchronized (lock) {
            presets.clear();
            if (!Files.exists(storeFile)) {
                log.info("No preset file yet, will create on first save: {}", storeFile);
                return;
            }
            try {
                Map<String, Map<String, Preset>> loaded = objectMapper.readValue(
                        Files.readAllBytes(storeFile),
                        new TypeReference<Map<String, Map<String, Preset>>>() {});
                loaded.forEach((category, byName) -> presets.put(category, newNameMap(byName)));
                log.info("Loaded {} preset categories from {}", presets.size(), storeFile);
            } catch (Exception e) {
                log.error("Failed to read preset file {} — starting empty: {}", storeFile, e.getMessage(), e);
            }
        }
    }

    public List<Preset> list(String category) {
        synchronized (lock) {
            List<Preset> result = new ArrayList<>(presets.getOrDefault(category, Map.of()).values());
            result.sort(Comparator.comparing(p -> p.getName() == null ? "" : p.getName().toLowerCase()));
            return result;
        }
    }

    public Map<String, List<Preset>> listAll() {
        synchronized (lock) {
            Map<String, List<Preset>> all = new LinkedHashMap<>();
            presets.keySet().forEach(category -> all.put(category, list(category)));
            return all;
        }
    }

    /** Saves (or overwrites) a preset and returns the stored copy. */
    public Preset save(String category, Preset preset) {
        if (preset.getName() == null || preset.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Preset name is required");
        }
        preset.setName(preset.getName().trim());
        preset.setCategory(category);
        preset.setSavedAt(OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        synchronized (lock) {
            presets.computeIfAbsent(category, c -> newNameMap(Map.of())).put(preset.getName(), preset);
            persist();
        }
        log.info("Saved preset '{}' in category '{}'", preset.getName(), category);
        return preset;
    }

    /** @return true when a preset was actually removed. */
    public boolean delete(String category, String name) {
        synchronized (lock) {
            Map<String, Preset> byName = presets.get(category);
            if (byName == null || byName.remove(name) == null) {
                return false;
            }
            persist();
            log.info("Deleted preset '{}' from category '{}'", name, category);
            return true;
        }
    }

    /**
     * Bulk import. Accepts either the exported shape ({@code category -> [preset, ...]}) or a raw
     * {@code presets.json} ({@code category -> {name -> preset}}), so a copied store file imports as-is.
     * Presets with an existing name are overwritten.
     *
     * @return number imported
     */
    public int importAll(JsonNode root) {
        if (root == null || !root.isObject()) {
            throw new IllegalArgumentException("Expected a JSON object of category -> presets");
        }

        int count = 0;
        synchronized (lock) {
            Iterator<Map.Entry<String, JsonNode>> categories = root.fields();
            while (categories.hasNext()) {
                Map.Entry<String, JsonNode> entry = categories.next();
                String category = entry.getKey();
                JsonNode node   = entry.getValue();

                List<JsonNode> presetNodes = new ArrayList<>();
                if (node.isArray()) {
                    node.forEach(presetNodes::add);
                } else if (node.isObject()) {
                    node.fields().forEachRemaining(f -> presetNodes.add(f.getValue()));
                } else {
                    continue;
                }

                for (JsonNode presetNode : presetNodes) {
                    Preset preset;
                    try {
                        preset = objectMapper.treeToValue(presetNode, Preset.class);
                    } catch (JsonProcessingException e) {
                        log.warn("Skipping unreadable preset in category '{}': {}", category, e.getMessage());
                        continue;
                    }
                    if (preset == null || preset.getName() == null || preset.getName().trim().isEmpty()) continue;

                    preset.setName(preset.getName().trim());
                    preset.setCategory(category);
                    if (preset.getSavedAt() == null) {
                        preset.setSavedAt(OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                    }
                    presets.computeIfAbsent(category, c -> newNameMap(Map.of())).put(preset.getName(), preset);
                    count++;
                }
            }
            if (count > 0) persist();
        }
        log.info("Imported {} presets", count);
        return count;
    }

    /** Writes to a temp file first so a crash mid-write cannot truncate the existing presets. */
    private void persist() {
        try {
            Path parent = storeFile.getParent();
            if (parent != null) Files.createDirectories(parent);

            Path tmp = storeFile.resolveSibling(storeFile.getFileName() + ".tmp");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), presets);
            Files.move(tmp, storeFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to write preset file {}: {}", storeFile, e.getMessage(), e);
            throw new IllegalStateException("Could not save presets: " + e.getMessage(), e);
        }
    }

    private Map<String, Preset> newNameMap(Map<String, Preset> initial) {
        Map<String, Preset> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        map.putAll(initial);
        return map;
    }
}
