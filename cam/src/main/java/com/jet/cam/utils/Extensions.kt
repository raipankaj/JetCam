/*
 * MIT License
 *
 * Copyright (c) 2022 Pankaj Rai
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.jet.cam.utils

import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner

fun ProcessCameraProvider.rebind(
    lifecycleOwner: LifecycleOwner,
    cameraLens: CameraSelector,
    preview: Preview,
    listOfUsecase: ArrayList<UseCase?>
): Camera {
    unbindAll()
    return bindToLifecycle(
        lifecycleOwner,
        cameraLens,
        preview,
        *listOfUsecase.toTypedArray()
    )
}

fun Camera?.flashOn() {
    if (this?.cameraInfo?.hasFlashUnit() == true) {
        this.cameraControl.enableTorch(true)
    }
}

fun Camera?.flashOff() {
    if (this?.cameraInfo?.hasFlashUnit() == true) {
        this.cameraControl.enableTorch(false)
    }
}