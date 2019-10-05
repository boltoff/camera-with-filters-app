package lv.slyfox.carguru.camera_with_filters_app.helper.extension

import android.util.Log

private const val TAG = "@logs"

fun Any.logAsDebug() {
    Log.d(TAG, toString())
}

fun Any.logAsError() {
    Log.e(TAG, toString())
}