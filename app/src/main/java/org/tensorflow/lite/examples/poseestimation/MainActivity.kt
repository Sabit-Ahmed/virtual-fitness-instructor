package org.tensorflow.lite.examples.poseestimation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import org.tensorflow.lite.examples.poseestimation.api.IExerciseService
import org.tensorflow.lite.examples.poseestimation.api.request.ExerciseListRequestPayload
import org.tensorflow.lite.examples.poseestimation.api.response.ExerciseListResponse
import org.tensorflow.lite.examples.poseestimation.databinding.ActivityMainBinding
import org.tensorflow.lite.examples.poseestimation.domain.model.Exercise
import org.tensorflow.lite.examples.poseestimation.domain.model.LogInData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var menuToggle: ActionBarDrawerToggle
    private lateinit var exercises: Exercise
    private var assessmentListFragment: AssessmentListFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val loginData = loadLogInData()
        getAssignedExercises(loginData.patientId, loginData.tenant)

        menuToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            R.string.open,
            R.string.close
        )
        binding.drawerLayout.addDrawerListener(menuToggle)
        menuToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0f

        binding.menuButton.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.log_out_button -> {
                    saveLogInData(
                        LogInData(
                            firstName = "",
                            lastName = "",
                            patientId = "",
                            tenant = ""
                        )
                    )
                    Toast.makeText(this, "Successfully logged out", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, SignInActivity::class.java))
                    finish()
                }
            }
            true
        }

        assessmentListFragment?.let {
            changeScreen(it)
        }
    }

    override fun onBackPressed() {
        assessmentListFragment?.let {
            Log.d("FragmentVisibility", it.isVisible.toString())
            if (it.isVisible) {
                super.onBackPressed()
                finish()
            } else {
                changeScreen(it)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (menuToggle.onOptionsItemSelected(item)) return true
        return super.onOptionsItemSelected(item)
    }

    fun changeScreen(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            commit()
        }
    }

    private fun getAssignedExercises(patientId: String, tenant: String) {
        val service = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.injurycloud.com")
            .build()
            .create(IExerciseService::class.java)
        val requestPayload = ExerciseListRequestPayload(
            PatientId = patientId,
            start = 0,
            pageSize = 500,
            Tenant = tenant
        )
        val response = service.getExercises(requestPayload)
        response.enqueue(object : Callback<ExerciseListResponse> {
            override fun onResponse(
                call: Call<ExerciseListResponse>,
                response: Response<ExerciseListResponse>
            ) {
                val responseBody = response.body()
                if (responseBody != null) {
                    if (responseBody.Successful) {
                        val messageString = "{exercises: ${responseBody.Message}}"
                        val converter = Gson()
                        exercises = converter.fromJson(
                            messageString,
                            Exercise::class.java
                        )
                        binding.progressIndicator.visibility = View.GONE
                        assessmentListFragment = AssessmentListFragment(exercises.exercises)
                        if (exercises.exercises.isEmpty()) {
                            Toast.makeText(
                                this@MainActivity,
                                "No test ID found!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        changeScreen(assessmentListFragment!!)
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to get assigned exercise information",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Response body is null",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ExerciseListResponse>, t: Throwable) {
                Toast.makeText(
                    this@MainActivity,
                    "Failed to get data from API",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun saveLogInData(logInData: LogInData) {
        val preferences = getSharedPreferences(
            SignInActivity.LOGIN_PREFERENCE,
            SignInActivity.PREFERENCE_MODE
        )
        preferences.edit().apply {
            putString(SignInActivity.FIRST_NAME, logInData.firstName)
            putString(SignInActivity.LAST_NAME, logInData.lastName)
            putString(SignInActivity.PATIENT_ID, logInData.patientId)
            putString(SignInActivity.TENANT, logInData.tenant)
            apply()
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