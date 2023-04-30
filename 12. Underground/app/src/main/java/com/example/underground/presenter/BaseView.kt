package com.example.underground.presenter

interface BaseView<PresenterT : BasePresenter> {
    val presenter: PresenterT
}