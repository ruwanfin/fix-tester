package com.finexa.fixtester.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.finexa.fixtester.dto.Preset;
import com.finexa.fixtester.service.PresetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/presets")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PresetController {

    private final PresetService presetService;

    @GetMapping
    public ResponseEntity<Map<String, List<Preset>>> listAll() {
        return ResponseEntity.ok(presetService.listAll());
    }

    @GetMapping("/{category}")
    public ResponseEntity<List<Preset>> list(@PathVariable String category) {
        return ResponseEntity.ok(presetService.list(category));
    }

    @PostMapping("/{category}")
    public ResponseEntity<?> save(@PathVariable String category, @RequestBody Preset preset) {
        try {
            return ResponseEntity.ok(presetService.save(category, preset));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{category}/{name}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String category, @PathVariable String name) {
        boolean removed = presetService.delete(category, name);
        return removed
                ? ResponseEntity.ok(Map.of("deleted", true, "name", name))
                : ResponseEntity.status(404).body(Map.of("deleted", false, "name", name));
    }

    /** Bulk import of an exported preset file; same-named presets are overwritten. */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importAll(@RequestBody JsonNode incoming) {
        try {
            return ResponseEntity.ok(Map.of("imported", presetService.importAll(incoming)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
