package com.example.underground.presenter.stations

import com.example.underground.domain.Station
import com.example.underground.presenter.BasePresenter
import com.example.underground.presenter.BaseView

interface StationsContract {

    interface View : BaseView<Presenter> {

        fun showLoadingIndicator()

        fun hideLoadingIndicator()

        fun showStations(stations: List<Station>)
    }

    interface Presenter : BasePresenter {
        fun filterStations(query: String)
    }
}