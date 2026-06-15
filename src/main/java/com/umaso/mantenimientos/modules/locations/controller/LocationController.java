package com.umaso.mantenimientos.modules.locations.controller;

import com.umaso.mantenimientos.modules.locations.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/maintenances/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;
}
