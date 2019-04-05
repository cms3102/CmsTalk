package com.csergio.cmstalk.model

class NotificationModel {

    var to = ""
    val notification = Notification()
    val data = Data()

    class Notification{
        var title = ""
        var text = ""
    }

    class Data{
        var title = ""
        var text = ""
    }

}