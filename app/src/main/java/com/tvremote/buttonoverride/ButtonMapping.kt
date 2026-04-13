package com.tvremote.buttonoverride

data class ButtonMapping(
    val keyCode: Int,
    val buttonName: String,
    val appPackage: String,
    val appName: String
)
