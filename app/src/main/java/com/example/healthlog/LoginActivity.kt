package com.example.healthlog

import com.example.healthlog.adapter.DoctorAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.healthlog.ui.doctor.DoctorViewModel
import android.os.Bundle
import com.example.healthlog.R
import android.text.TextWatcher
import android.text.Editable
import android.view.View.OnTouchListener
import com.example.healthlog.model.Doctor
import android.content.Intent
import com.example.healthlog.DoctorActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.example.healthlog.HealthLog
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.DocumentSnapshot
import com.example.healthlog.ui.hospital.HospitalViewModel
import com.example.healthlog.adapter.SuspectedPatientAdapter
import com.example.healthlog.model.SuspectedPatient
import com.example.healthlog.handler.HospitalProfileHandler
import com.example.healthlog.model.Hospital
import android.content.DialogInterface
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.CollectionReference
import com.example.healthlog.handler.NewPatientHandler
import com.example.healthlog.handler.HospitalHandler
import com.example.healthlog.ui.dashboard.DashboardViewModel
import com.example.healthlog.adapter.DashboardAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.healthlog.handler.PatientViewHandler
import com.example.healthlog.model.Patient
import android.widget.AdapterView.OnItemSelectedListener
import com.google.firebase.firestore.GeoPoint
import com.example.healthlog.adapter.DoctorAdapter.DoctorViewHolder
import com.example.healthlog.adapter.DashboardAdapter.DashboardViewHolder
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnSuccessListener
import android.content.ContentValues
import com.google.android.gms.tasks.OnFailureListener
import com.example.healthlog.adapter.SuspectedPatientAdapter.SuspectedPatientViewHolder
import android.graphics.drawable.ColorDrawable
import androidx.core.app.NotificationManagerCompat
import android.os.Build
import androidx.annotation.RequiresApi
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import com.example.healthlog.MainActivity
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import kotlin.Throws
import com.example.healthlog.interfaces.DialogClickListener
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.NavController
import androidx.navigation.ui.NavigationUI
import com.example.healthlog.LoginActivity
import com.example.healthlog.AboutActivity
import com.example.healthlog.SettingsActivity
import com.example.healthlog.handler.PatientLogHandler
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.healthlog.SettingsActivity.SettingsFragment
import androidx.preference.PreferenceFragmentCompat
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import org.junit.runner.RunWith
import java.util.*

class LoginActivity : AppCompatActivity() {
    var hospitalId: EditText? = null
    var login: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadLocale()
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_login)
        val actionBar = supportActionBar
        actionBar.setTitle(resources.getString(R.string.app_name))
        hospitalId = findViewById<View?>(R.id.loginActivity_login_editText) as EditText?
        login = findViewById<Button?>(R.id.loginActivity_login_btn)
        login.setOnClickListener(
            View.OnClickListener {
                if (hospitalId.getText().toString().isEmpty()) {
                    hospitalId.setError("Please Enter Hospital ID")
                    hospitalId.requestFocus()
                } else signIn()
            })
    }

    override fun onStart() {
        super.onStart()
        // COMPLETED(Danish) check if user is already login
        if (HealthLog.Companion.ID != null) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.language_change, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.language -> showChangeLanguageDialogue()
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun showChangeLanguageDialogue() {
        val listitems = arrayOf<String?>("French", "German", "Spanish", "English")
        val mbuilder = AlertDialog.Builder(this@LoginActivity)
        mbuilder.setTitle(R.string.choose_language)
        mbuilder.setSingleChoiceItems(listitems, -1) { dialogInterface, i ->
            if (i == 0) {   //French
                setLocale("fr")
                recreate()
            } else if (i == 1) {   //German
                setLocale("de")
                recreate()
            } else if (i == 2) {   //Spanish
                setLocale("es")
                recreate()
            } else if (i == 3) {    //English
                setLocale("en")
                recreate()
            }
            //dismiss alert dialog when language is selected
            dialogInterface.dismiss()
        }
        val mDialog = mbuilder.create()
        //show alert dialog
        mDialog.show()
    }

    private fun setLocale(language: String?) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration = Configuration()
        configuration.locale = locale
        var metrices: Any
        baseContext.resources.updateConfiguration(
            configuration,
            baseContext.resources.displayMetrics
        )
        //save data to shared preferences
        val editor = getSharedPreferences("Settings", MODE_PRIVATE).edit()
        editor.putString("My_Lang", language)
        editor.apply()
    }

    //load language saved in Shared Preferences
    fun loadLocale() {
        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        val language = prefs.getString("My_Lang", "")
        setLocale(language)
    }

    fun signIn() {
        val hId = hospitalId.getText().toString().trim { it <= ' ' }
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("Hospital").document(hId)
        docRef
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document.exists()) {
                        HealthLog.Companion.addHospitalIdToSharedPreference(hId)
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            applicationContext, "Wrong Login credentials", Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                } else {
                    Log.d("Loginok", "get failed with ", task.exception)
                }
            }
    }
}