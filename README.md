# JetCam
[![](https://jitpack.io/v/raipankaj/JetCam.svg)](https://jitpack.io/#raipankaj/JetCam)


Add camera capability to your app with just a single method - JetCam

To get started with JetCam just add the maven url and the Chip dependency

<b>build.gradle (Project level)</b>
```groovy
allprojects {
    repositories {
    ...
    //Add this url
    maven { url 'https://jitpack.io' }
    }
}
```
If you are using Android Studio Arctic Fox and above where you don't have allProjects in build.gradle then add following maven url in <b>settings.gradle</b> like below
```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        //Add this url
        maven { url 'https://jitpack.io' }
        jcenter() // Warning: this repository is going to shut down soon
    }
}
```

Once you have added the maven url now add the Chip dependency in the <b>build.gradle (module level)</b>
```groovy
implementation 'com.github.raipankaj:JetCam:0.1.0'

def camerax_version = "1.0.2"
// CameraX core library using camera2 implementation
implementation "androidx.camera:camera-camera2:$camerax_version"
// CameraX Lifecycle Library
implementation "androidx.camera:camera-lifecycle:$camerax_version"
// CameraX View class
implementation "androidx.camera:camera-view:1.0.0-alpha32"
```

Congratulations, you have successfully added the dependency. 
Now to get started with JetCam add the following code snippet
```kotlin
JetCam(
      imageAnalysis = { it?.let {
                            val buffer = it.planes[0].buffer
                            val data = buffer.toByteArray()
                            val pixels = data.map { it.toInt() and 0xFF }
                            val luma = pixels.average()

                            Log.i("LUM", ":${luma}")
                        }
                    }
)
```

<br>
JetCam composable provides various option to perform like 
1. Toggle on/off flash light 
2. Switch between front and back camera 
3. Set back or front camera as default
4. Get image analysis lambda without creating class
5. Set image analysis object explicitly

```kotlin
@Composable
fun JetCam(
    imageAnalysis: ((ImageProxy?) -> Unit)? = null,
    imageAnalysisConfig: ImageAnalysis? = null,
    imageCapture: ImageCapture? = null,
    defaultCameraLens: Lens = Lens.Back,
    cameraLensToggle: ((CameraLens) -> Unit)? = null,
    cameraProcess: ((Camera) -> Unit)? = null
)
```

Following is a full fledged working sample code
```kotlin
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
```
<br>
Also do not forget to request for camera permission.
Note: If you like this library, then please hit the star button! :smiley:
