package ru.boltoff.camera_with_filters_app.helper.extension

import android.view.View

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.changeVisibility(visible: Boolean, makeInvisible: Boolean = false) {
    visibility = if (visible) {
        View.VISIBLE
    } else {
        if (makeInvisible) View.INVISIBLE else View.GONE
    }
}
