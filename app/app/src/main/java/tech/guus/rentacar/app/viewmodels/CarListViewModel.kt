package tech.guus.rentacar.app.viewmodels

class CarListViewModel : BaseViewModel() {
    override val screenTitle: String
        get() = "Alle auto's"

    private val _cars: List<Any> = mutableListOf()
}
