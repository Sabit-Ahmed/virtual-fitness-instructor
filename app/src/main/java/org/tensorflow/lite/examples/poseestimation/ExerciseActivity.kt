package org.tensorflow.lite.examples.poseestimation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.media.ImageReader
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.tensorflow.lite.examples.poseestimation.api.IExerciseService
import org.tensorflow.lite.examples.poseestimation.api.request.ExerciseData
import org.tensorflow.lite.examples.poseestimation.api.request.ExerciseRequestPayload
import org.tensorflow.lite.examples.poseestimation.api.request.ExerciseTrackingPayload
import org.tensorflow.lite.examples.poseestimation.api.response.ExerciseTrackingResponse
import org.tensorflow.lite.examples.poseestimation.api.response.KeyPointRestrictions
import org.tensorflow.lite.examples.poseestimation.core.ImageUtils
import org.tensorflow.lite.examples.poseestimation.core.Utilities
import org.tensorflow.lite.examples.poseestimation.core.VisualizationUtils
import org.tensorflow.lite.examples.poseestimation.domain.model.*
import org.tensorflow.lite.examples.poseestimation.exercise.IExercise
import org.tensorflow.lite.examples.poseestimation.ml.MoveNet
import org.tensorflow.lite.examples.poseestimation.ml.PoseDetector
import org.tensorflow.lite.examples.poseestimation.shared.Exercises
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ExerciseActivity : AppCompatActivity() {
    companion object {
        const val ExerciseId = "ExerciseId"
        const val TestId = "TestId"
        const val Name = "Name"
        const val ProtocolId = "ProtocolId"
        const val TAG = "ExerciseActivityTag"
        private const val PREVIEW_WIDTH = 640
        private const val PREVIEW_HEIGHT = 480
    }

    private lateinit var surfaceView: SurfaceView
    private lateinit var surfaceHolder: SurfaceHolder
    private var backgroundHandler: Handler? = null
    private var previewSize: Size? = null
    private var backgroundThread: HandlerThread? = null
    private var cameraId: String = ""
    private var previewWidth = 0
    private var previewHeight = 0
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var poseDetector: PoseDetector? = null
    private var device = Device.CPU
    private var modelPos = 2
    private var imageReader: ImageReader? = null
    private val minConfidence = .2f
    private var previewRequestBuilder: CaptureRequest.Builder? = null
    private var previewRequest: CaptureRequest? = null

    private lateinit var tvScore: TextView
    private lateinit var tvTime: TextView
    private lateinit var spnDevice: Spinner
    private lateinit var spnModel: Spinner

    private lateinit var exercise: IExercise
    private var exerciseConstraints: List<Phase> = listOf()

    private var isFrontCamera = true
    private var url: String = "https://vaapi.injurycloud.com"

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            this@ExerciseActivity.cameraDevice = camera
            createCameraPreviewSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            cameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            onDisconnected(camera)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                openCamera()
            } else {
                Toast.makeText(
                    this,
                    "Camera permission is required to use this feature!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private var imageAvailableListener = object : ImageReader.OnImageAvailableListener {
        override fun onImageAvailable(imageReader: ImageReader) {
            // We need wait until we have some size from onPreviewSizeChosen
            if (previewWidth == 0 || previewHeight == 0) {
                return
            }

            val image = imageReader.acquireLatestImage() ?: return
            val nv21Buffer =
                ImageUtils.yuv420ThreePlanesToNV21(image.planes, previewWidth, previewHeight)
            val imageBitmap = ImageUtils.getBitmap(nv21Buffer!!, previewWidth, previewHeight)

            // Create rotated version for portrait display
            val rotateMatrix = Matrix()
            if (isFrontCamera) {
                rotateMatrix.postRotate(-90.0f)
            } else {
                rotateMatrix.postRotate(90.0f)
            }

            val rotatedBitmap = Bitmap.createBitmap(
                imageBitmap!!, 0, 0, previewWidth, previewHeight,
                rotateMatrix, true
            )
            image.close()

            processImage(rotatedBitmap)
        }
    }

    private var changeModelListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            // do nothing
        }

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            changeModel(position)
        }
    }

    private var changeDeviceListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            changeDevice(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            // do nothing
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val testId = intent.getStringExtra(TestId)
        val exerciseId = intent.getIntExtra(ExerciseId, 122)
        val exerciseName = intent.getStringExtra(Name)
        val protocolId = intent.getIntExtra(ProtocolId, 1)
        val logInData = loadLogInData()

        getExerciseConstraints(logInData.tenant, exerciseId)

        exercise = Exercises.get(this, exerciseId)

        findViewById<TextView>(R.id.textView).text = exerciseName

        findViewById<Button>(R.id.done_button).setOnClickListener {
            saveExerciseData(
                ExerciseId = exerciseId,
                TestId = testId!!,
                ProtocolId = protocolId,
                PatientId = logInData.patientId,
                ExerciseDate = Utilities.currentDate(),
                NoOfReps = exercise.getRepetitionCount(),
                NoOfSets = exercise.getSetCount(),
                NoOfWrongCount = exercise.getWrongCount(),
                Tenant = logInData.tenant
            )
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setMessage("Do you feel any pain while performing this exercise?")
            alertDialog.setPositiveButton("Yes") { _, _ ->
                val alertDialog2 = AlertDialog.Builder(this)
                alertDialog2.setMessage("Do you want to track your pain with EMMA?")
                alertDialog2.setPositiveButton("Yes") { _, _ ->
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://emma.injurycloud.com/account/painemmalogin?patientId=${logInData.patientId}&redirecturl=journal")
                    )
                    startActivity(intent)
                    finish()
                }
                alertDialog2.setNegativeButton("No") { _, _ ->
                    finish()
                }
                alertDialog2.show()
            }
            alertDialog.setNegativeButton("No") { _, _ ->
                finish()
            }
            alertDialog.show()
        }

        findViewById<ImageButton>(R.id.camera_switch_button).setOnClickListener {
            isFrontCamera = !isFrontCamera
            closeCamera()
            openCamera()
        }

        createPoseEstimator()

        tvScore = findViewById(R.id.tvScore)
        tvTime = findViewById(R.id.tvTime)
        spnModel = findViewById(R.id.spnModel)
        spnDevice = findViewById(R.id.spnDevice)
        surfaceView = findViewById(R.id.surfaceView)
        surfaceHolder = surfaceView.holder

        initSpinner()
        requestPermission()
    }

    override fun onStart() {
        super.onStart()
        openCamera()
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        poseDetector?.close()
    }

    private fun changeModel(position: Int) {
        modelPos = position
        createPoseEstimator()
    }

    private fun changeDevice(position: Int) {
        device = when (position) {
            0 -> Device.CPU
            1 -> Device.GPU
            else -> Device.NNAPI
        }
        createPoseEstimator()
    }

    private fun createPoseEstimator() {
        closeCamera()
        stopBackgroundThread()
        poseDetector?.close()
        poseDetector = null
        poseDetector = MoveNet.create(this, device)
        openCamera()
        startBackgroundThread()
    }

    private fun initSpinner() {
        ArrayAdapter.createFromResource(
            this,
            R.array.tfe_pe_models_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spnModel.adapter = adapter
            spnModel.onItemSelectedListener = changeModelListener
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.tfe_pe_device_name, android.R.layout.simple_spinner_item
        ).also { adaper ->
            adaper.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spnDevice.adapter = adaper
            spnDevice.onItemSelectedListener = changeDeviceListener
        }
    }

    private fun requestPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) -> {
                // You can use the API that requires the permission.
                openCamera()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    }

    private fun openCamera() {
        // check if permission is granted or not.
        if (checkPermission(
                Manifest.permission.CAMERA,
                Process.myPid(),
                Process.myUid()
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setUpCameraOutputs()
            val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            manager.openCamera(cameraId, stateCallback, backgroundHandler)
        }
    }

    private fun closeCamera() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
        imageReader?.close()
        imageReader = null
    }

    private fun setUpCameraOutputs() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                // We don't use a front facing camera in this sample.
                val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)

//                Log.d("CameraIdNumber", "Camera ID: $cameraId")
                if (isFrontCamera) {
                    if (cameraDirection != null && cameraDirection != CameraCharacteristics.LENS_FACING_FRONT) {
                        continue
                    }
                } else {
                    if (cameraDirection != null && cameraDirection == CameraCharacteristics.LENS_FACING_FRONT) {
                        continue
                    }
                }

                previewSize = Size(PREVIEW_WIDTH, PREVIEW_HEIGHT)

                imageReader = ImageReader.newInstance(
                    PREVIEW_WIDTH, PREVIEW_HEIGHT,
                    ImageFormat.YUV_420_888, /*maxImages*/ 2
                )

                previewHeight = previewSize!!.height
                previewWidth = previewSize!!.width

                this.cameraId = cameraId

                // We've found a viable camera and finished setting up member variables,
                // so we don't need to iterate through other available cameras.
                return
            }
        } catch (e: CameraAccessException) {
        } catch (e: NullPointerException) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
        }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("imageAvailableListener").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            // do nothing
        }
    }

    private fun createCameraPreviewSession() {
        try {
            // We capture images from preview in YUV format.
            imageReader = ImageReader.newInstance(
                previewSize!!.width, previewSize!!.height, ImageFormat.YUV_420_888, 2
            )
            imageReader!!.setOnImageAvailableListener(imageAvailableListener, backgroundHandler)

            // This is the surface we need to record images for processing.
            val recordingSurface = imageReader!!.surface

            // We set up a CaptureRequest.Builder with the output Surface.
            previewRequestBuilder = cameraDevice!!.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW
            )
            previewRequestBuilder!!.addTarget(recordingSurface)

            // Here, we create a CameraCaptureSession for camera preview.
            cameraDevice!!.createCaptureSession(
                listOf(recordingSurface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        // The camera is already closed
                        if (cameraDevice == null) return

                        // When the session is ready, we start displaying the preview.
                        captureSession = cameraCaptureSession
                        try {
                            // Auto focus should be continuous for camera preview.
                            previewRequestBuilder!!.set(
                                CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                            )
                            // Finally, we start displaying the camera preview.
                            previewRequest = previewRequestBuilder!!.build()
                            captureSession!!.setRepeatingRequest(
                                previewRequest!!,
                                null, null
                            )
                        } catch (e: CameraAccessException) {
                            Log.e(TAG, e.toString())
                        }
                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                        Toast.makeText(this@ExerciseActivity, "Failed", Toast.LENGTH_SHORT).show()
                    }
                },
                null
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Error creating camera preview session.", e)
        }
    }

    private fun processImage(bitmap: Bitmap) {
        var score = 0f
        var outputBitmap = bitmap

        // run detect pose
        // draw points and lines on original image
        poseDetector?.estimateSinglePose(bitmap)?.let { person ->
            score = person.score
            if (score > minConfidence) {
                val height = bitmap.height
                val width = bitmap.width
                exercise.exerciseCount(person, height, width, phases = exerciseConstraints)
                exercise.wrongExerciseCount(person, height, width)

                outputBitmap = VisualizationUtils.drawBodyKeyPoints(
                    bitmap,
                    exercise.drawingRules(person, phases = exerciseConstraints),
                    exercise.getRepetitionCount(),
                    exercise.getSetCount(),
                    exercise.getWrongCount(),
                    exercise.getBorderColor(person, height, width),
                    isFrontCamera
                )
            }
        }

        val canvas: Canvas = surfaceHolder.lockCanvas()
        if (isFrontCamera) {
            canvas.scale(-1f, 1f, canvas.width.toFloat() / 2, canvas.height.toFloat() / 2)
        }

        val screenWidth: Int
        val screenHeight: Int
        val left: Int
        val top: Int

        if (canvas.height > canvas.width) {
            val ratio = outputBitmap.height.toFloat() / outputBitmap.width
            screenWidth = canvas.width
            left = 0
            screenHeight = (canvas.width * ratio).toInt()
            top = (canvas.height - screenHeight) / 2
        } else {
            val ratio = outputBitmap.width.toFloat() / outputBitmap.height
            screenHeight = canvas.height
            top = 0
            screenWidth = (canvas.height * ratio).toInt()
            left = (canvas.width - screenWidth) / 2
        }
        val right: Int = left + screenWidth
        val bottom: Int = top + screenHeight

        Log.d("BitMapValue", "$outputBitmap")

        canvas.drawBitmap(
            outputBitmap, Rect(0, 0, outputBitmap.width, outputBitmap.height),
            Rect(left, top, right, bottom), Paint()
        )
        surfaceHolder.unlockCanvasAndPost(canvas)
        tvScore.text = getString(R.string.tfe_pe_tv_score).format(score)
        poseDetector?.lastInferenceTimeNanos()?.let {
            tvTime.text =
                getString(R.string.tfe_pe_tv_time).format(it * 1.0f / 1_000_000)
        }
    }

    private fun saveExerciseData(
        ExerciseId: Int,
        TestId: String,
        ProtocolId: Int,
        PatientId: String,
        ExerciseDate: String,
        NoOfReps: Int,
        NoOfSets: Int,
        NoOfWrongCount: Int,
        Tenant: String
    ) {
        val service = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.injurycloud.com")
            .build()
            .create(IExerciseService::class.java)
        val requestPayload = ExerciseTrackingPayload(
            ExerciseId = ExerciseId,
            TestId = TestId,
            ProtocolId = ProtocolId,
            PatientId = PatientId,
            ExerciseDate = ExerciseDate,
            NoOfReps = NoOfReps,
            NoOfSets = NoOfSets,
            NoOfWrongCount = NoOfWrongCount,
            Tenant = Tenant
        )
        val response = service.saveExerciseData(requestPayload)
        response.enqueue(object : Callback<ExerciseTrackingResponse> {
            override fun onResponse(
                call: Call<ExerciseTrackingResponse>,
                response: Response<ExerciseTrackingResponse>
            ) {
                val responseBody = response.body()
                if (responseBody != null) {
                    if (responseBody.Successful) {
                        Toast.makeText(
                            this@ExerciseActivity,
                            responseBody.Message,
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@ExerciseActivity,
                            "Could not save exercise data",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@ExerciseActivity,
                        "Failed to save! Got empty response",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ExerciseTrackingResponse>, t: Throwable) {
                Toast.makeText(
                    this@ExerciseActivity,
                    "Failed to save exercise data!",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun getExerciseConstraints(tenant: String, exerciseId: Int) {
        val phases = mutableListOf<Phase>()
        val service = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(url)
            .build()
            .create(IExerciseService::class.java)
        val requestPayload = ExerciseRequestPayload(
            Tenant = tenant,
            KeyPointsRestrictions = listOf(
                ExerciseData(exerciseId)
            )
        )
        val response = service.getConstraint(requestPayload)
        response.enqueue(object : Callback<KeyPointRestrictions> {
            override fun onResponse(
                call: Call<KeyPointRestrictions>,
                response: Response<KeyPointRestrictions>
            ) {
                val responseBody = response.body()!!
                Log.d("dataForExercise", "data ::::  $responseBody")
                responseBody[0].KeyPointsRestrictionGroup.forEach { group ->
                    val constraints = mutableListOf<Constraint>()
                    group.KeyPointsRestriction.forEach { restriction ->
                        constraints.add(
                            Constraint(
                                minValue = restriction.MinValidationValue,
                                maxValue = restriction.MaxValidationValue,
                                type = if (restriction.Scale == "degree") {
                                    ConstraintType.ANGLE
                                } else {
                                    ConstraintType.LINE
                                },
                                startPointIndex = getIndex(restriction.StartKeyPosition),
                                middlePointIndex = getIndex(restriction.MiddleKeyPosition),
                                endPointIndex = getIndex(restriction.EndKeyPosition),
                                clockWise = restriction.AngleArea == "inner"
                            )
                        )
                    }
                    phases.add(
                        Phase(
                            phase = group.Phase,
                            constraints = constraints
                        )
                    )
                }
                exerciseConstraints = phases.sortedBy { it.phase }
            }

            override fun onFailure(call: Call<KeyPointRestrictions>, t: Throwable) {
                Log.d("retrofit", "on failure ::: " + t.message)
            }
        })
    }

    private fun getIndex(name: String): Int {
        return when (name) {
            "NOSE".lowercase() -> 0
            "LEFT_EYE".lowercase() -> 1
            "RIGHT_EYE".lowercase() -> 2
            "LEFT_EAR".lowercase() -> 3
            "RIGHT_EAR".lowercase() -> 4
            "LEFT_SHOULDER".lowercase() -> 5
            "RIGHT_SHOULDER".lowercase() -> 6
            "LEFT_ELBOW".lowercase() -> 7
            "RIGHT_ELBOW".lowercase() -> 8
            "LEFT_WRIST".lowercase() -> 9
            "RIGHT_WRIST".lowercase() -> 10
            "LEFT_HIP".lowercase() -> 11
            "RIGHT_HIP".lowercase() -> 12
            "LEFT_KNEE".lowercase() -> 13
            "RIGHT_KNEE".lowercase() -> 14
            "LEFT_ANKLE".lowercase() -> 15
            "RIGHT_ANKLE".lowercase() -> 16
            else -> 0
        }
    }

    private fun loadLogInData(): LogInData {
        val preferences = getSharedPreferences(
            SignInActivity.LOGIN_PREFERENCE,
            SignInActivity.PREFERENCE_MODE
        )
        return LogInData(
            firstName = preferences.getString(SignInActivity.FIRST_NAME, "") ?: "",
            lastName = preferences.getString(SignInActivity.LAST_NAME, "") ?: "",
            patientId = preferences.getString(SignInActivity.PATIENT_ID, "") ?: "",
            tenant = preferences.getString(SignInActivity.TENANT, "") ?: ""
        )
    }
}