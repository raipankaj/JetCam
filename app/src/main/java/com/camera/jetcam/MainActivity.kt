package com.camera.jetcam

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.camera.jetcam.ui.theme.JetCamTheme
import com.jet.cam.JetCam
import com.jet.cam.config.CameraLens
import com.jet.cam.config.rememberImageCapture
import com.jet.cam.utils.flashOff
import com.jet.cam.utils.flashOn
import java.nio.ByteBuffer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JetCamTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {

                    val imageCapture = rememberImageCapture()

                    var toggleCameraFlash by remember {
                        mutableStateOf(true)
                    }

                    var isBackCamShown by remember {
                        mutableStateOf(true)
                    }

                    var camera by remember {
                        mutableStateOf<Camera?>(null)
                    }

                    var cameraLensToggle by remember {
                        mutableStateOf<CameraLens?>(null)
                    }

                    Box(modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter) {

                        JetCam(
                            imageAnalysis = {
                                it?.let {
                                    val buffer = it.planes[0].buffer
                                    val data = buffer.toByteArray()
                                    val pixels = data.map { it.toInt() and 0xFF }
                                    val luma = pixels.average()

                                    Log.i("LUM", ":${luma}")
                                }
                            },
                            imageCapture = imageCapture,
                            cameraLensToggle = { cameraLens ->
                                cameraLensToggle = cameraLens
                            },
                            cameraProcess = { cam ->
                                camera = cam
                            }
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly) {
                            IconButton(onClick = {
                                toggleCameraFlash = toggleCameraFlash.not()
                                if (toggleCameraFlash) {
                                    camera?.flashOff()
                                } else {
                                    camera?.flashOn()
                                }
                            }, modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)) {
                                Icon(
                                    painter = painterResource(
                                        id = if (toggleCameraFlash) R.drawable.ic_flash_off else R.drawable.ic_flash_on
                                    ),
                                    contentDescription = "Flash On/Off"
                                )
                            }

                            IconButton(onClick = {
                                isBackCamShown = isBackCamShown.not()
                                if (isBackCamShown) {
                                    cameraLensToggle?.toggleToBack()
                                } else {
                                    toggleCameraFlash = true
                                    camera?.flashOff()
                                    cameraLensToggle?.toggleToFront()
                                }
                            }, modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_toggle),
                                    contentDescription = "Toggle Front/Back Cam"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun ByteBuffer.toByteArray(): ByteArray {
    rewind()    // Rewind the buffer to zero
    val data = ByteArray(remaining())
    get(data)   // Copy the buffer into a byte array
    return data // Return the byte array
}
