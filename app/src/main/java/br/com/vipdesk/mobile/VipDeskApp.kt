package br.com.vipdesk.mobile

import android.app.Application
import br.com.vipdesk.mobile.data.notifications.MessageNotifier
import br.com.vipdesk.mobile.di.AppContainer

class VipDeskApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
        MessageNotifier.start(this, AppContainer.socketService)
    }
}
