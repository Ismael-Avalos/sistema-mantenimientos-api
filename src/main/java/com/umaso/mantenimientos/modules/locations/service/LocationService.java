package com.umaso.mantenimientos.modules.locations.service;

import com.umaso.mantenimientos.modules.locations.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
}
