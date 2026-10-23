package nl.ovfietsbeschikbaarheid.testutils

import kotlinx.serialization.json.Json
import nl.ovfietsbeschikbaarheid.dto.LocationDTO

val locationsResponse: List<LocationDTO> = Json { ignoreUnknownKeys = true }
    .decodeFromString(ClassLoader.getSystemResource("locations.json").readText())
