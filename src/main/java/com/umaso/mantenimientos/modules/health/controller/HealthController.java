package com.umaso.mantenimientos.modules.health.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    // Liveness only: intentionally does not access databases or external services.
    @GetMapping(value = "/api/health", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> health() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body("OK");
    }

    @RequestMapping(value = "/api/health", method = RequestMethod.HEAD)
    public ResponseEntity<Void> healthHead() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .contentType(MediaType.TEXT_PLAIN).contentLength(2).build();
    }
}
