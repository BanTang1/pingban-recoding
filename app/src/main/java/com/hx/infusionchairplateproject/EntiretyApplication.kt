package com.hx.infusionchairplateproject

import android.app.Application


class EntiretyApplication : Application() {

    // TODO WebSocket 的初始化应在Application中  ，以保证在应用程序的生命周期中  连接存活  private val webSocketClient = getApplication<MyApplication>().webSocketClient


}