package tech.guus.rentacar.app.utils

import tech.guus.rentacar.app.AppContainerInterface
import tech.guus.rentacar.app.repositories.CarRepository
import tech.guus.rentacar.app.repositories.MyCarRepository
import tech.guus.rentacar.app.repositories.UserRepository
import tech.guus.rentacar.app.services.LocationService

class DummyAppContainer(
    override val userRepository: UserRepository,
    override val carRepository: CarRepository,
    override val locationService: LocationService,
    override val myCarRepository: MyCarRepository
) : AppContainerInterface