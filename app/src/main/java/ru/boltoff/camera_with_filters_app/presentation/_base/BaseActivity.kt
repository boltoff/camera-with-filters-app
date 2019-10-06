package ru.boltoff.camera_with_filters_app.presentation._base

import android.os.Bundle
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import lv.slyfox.carguru.camera_with_filters_app.R

abstract class BaseActivity<VM> : AppCompatActivity(), BaseView<VM> {

    abstract fun initViews(savedInstanceState: Bundle?)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)
        initViews(savedInstanceState)
        initViewModel(this)
    }

    @CallSuper
    protected open fun initViewModel(owner: LifecycleOwner) {
        viewModel?.takeIf { it is BaseViewModel }?.let { viewModel ->
            with(viewModel as BaseViewModel) {
                showError.observe(owner, Observer { onErrorMessage(it) })
            }
        }
    }

    protected open fun onErrorMessage(message: String) {
        val humanMessage: String = when (message) {
            else -> getString(R.string.message_something_went_wrong)
        }
        Toast.makeText(this, humanMessage, Toast.LENGTH_SHORT).show()
    }
}