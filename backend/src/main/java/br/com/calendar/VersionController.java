package br.com.calendar;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VersionController {

    @GetMapping("/version")
    public Map<String, String> version() {
        return Map.of(
                "version", "0.0.1-SNAPSHOT",
                "application", "calendar"
        );
    }
}