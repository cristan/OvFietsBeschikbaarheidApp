package nl.ovfietsbeschikbaarheid.repository

import nl.ovfietsbeschikbaarheid.KtorApiClient
import nl.ovfietsbeschikbaarheid.mapper.VehiclesMapper
import nl.ovfietsbeschikbaarheid.model.VehicleModel

class VehiclesRepository {

    private val httpClient = KtorApiClient()

    suspend fun getAllVehicles(): List<VehicleModel> {
        return VehiclesMapper.map(httpClient.getVehicles().data.vehicles)
    }
}
