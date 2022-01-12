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

package com.jet.cam

import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.jet.cam.config.CameraLens
import com.jet.cam.config.Lens
import com.jet.cam.utils.rebind
import java.util.concurrent.Executors

@Composable
fun JetCam(
    imageAnalysis: ((ImageProxy) -> Unit)? = null,
    imageAnalysisConfig: ImageAnalysis? = null,
    imageCapture: ImageCapture? = null,
    defaultCameraLens: Lens = Lens.Back,
    cameraLensToggle: ((CameraLens) -> Unit)? = null,
    cameraProcess: ((Camera) -> Unit)? = null
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val localContext = LocalContext.current

    val listOfUsecase = remember {
        ArrayList<UseCase?>()
    }

    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(localContext)
    }

    LaunchedEffect(key1 = Unit) {
        if (imageAnalysis != null && imageAnalysisConfig == null) {
            val imageAnalysisBuild = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor(), ImageAnalyzer {
                        imageAnalysis.invoke(it)
                    })
                }
            listOfUsecase.add(imageAnalysisBuild)
        }

        listOfUsecase.add(imageCapture)
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            PreviewView(context)
        },
        update = { previewView ->
            cameraProviderFuture.addListener({

                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                try {
                    val camera = cameraProvider.rebind(
                        lifecycleOwner,
                        defaultCameraLens.type,
                        preview,
                        listOfUsecase
                    )

                    cameraLensToggle?.invoke(object : CameraLens {

                        override fun toggleToFront() {
                            cameraProvider.rebind(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_FRONT_CAMERA,
                                preview,
                                listOfUsecase
                            )
                        }

                        override fun toggleToBack() {
                            cameraProvider.rebind(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                listOfUsecase
                            )
                        }
                    })

                    cameraProcess?.invoke(camera).run {
                        cameraProvider.rebind(
                            lifecycleOwner,
                            defaultCameraLens.type,
                            preview,
                            listOfUsecase
                        )
                    }
                } catch (exc: Exception) {
                    Log.e("Exc", "Unable to open camera")
                }

            }, ContextCompat.getMainExecutor(localContext))
        }
    )
}

private class ImageAnalyzer(val imageProxy: (ImageProxy) -> Unit) : ImageAnalysis.Analyzer {
    override fun analyze(imageProxy: ImageProxy) {
        val image = imageProxy
        imageProxy(image)
        image.close()
    }
}