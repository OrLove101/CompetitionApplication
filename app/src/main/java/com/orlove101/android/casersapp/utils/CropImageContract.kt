package com.orlove101.android.casersapp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

class CropImageContract: ActivityResultContract<Any?, Uri?>() {
    override fun createIntent(context: Context, input: Any?): Intent {
        return CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .getIntent(context)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return CropImage.getActivityResult(intent)?.uri
    }
}