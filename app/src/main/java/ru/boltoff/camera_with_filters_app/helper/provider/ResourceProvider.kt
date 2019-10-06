package lv.slyfox.carguru.camera_with_filters_app.helper.provider

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import androidx.annotation.*
import androidx.core.content.ContextCompat

class ResourceProvider(private val context: Context) {

    fun getString(@StringRes id: Int): String = context.getString(id)

    fun getString(@StringRes id: Int, vararg args: Any): String = context.getString(id, *args)

    fun getStringArray(@ArrayRes id: Int): Array<String> = context.resources.getStringArray(id)

    fun getDrawable(@DrawableRes id: Int): Drawable? = context.getDrawable(id)

    fun getColor(@ColorRes id: Int): Int = ContextCompat.getColor(context, id)

    fun getBoolean(@BoolRes id: Int): Boolean = context.resources.getBoolean(id)

    fun getConfiguration(): Configuration = context.resources.configuration

}
