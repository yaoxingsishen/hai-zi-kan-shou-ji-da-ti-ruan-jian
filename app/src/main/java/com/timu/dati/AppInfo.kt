package com.timu.dati

import android.graphics.drawable.Drawable

data class AppInfo(
    val pkg: String,
    val name: String,
    val icon: Drawable,
    var monitored: Boolean = false
)
