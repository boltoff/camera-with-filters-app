package lv.slyfox.carguru.camera_with_filters_app.presentation.camera

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import lv.slyfox.carguru.camera_with_filters_app.R
import lv.slyfox.carguru.camera_with_filters_app.presentation._base.BaseActivity

class CameraActivity : BaseActivity<CameraViewModel>() {

    override val layoutId: Int = R.layout.activity_camera
    override val viewModel: CameraViewModel by lazy { CameraViewModel() }

    override fun initViews(savedInstanceState: Bundle?) {}

    override fun initViewModel(owner: LifecycleOwner) {
        super.initViewModel(owner)
    }
}