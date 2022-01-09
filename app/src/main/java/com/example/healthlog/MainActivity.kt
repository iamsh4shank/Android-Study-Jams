package com.example.healthlog

import com.example.healthlog.adapter.DoctorAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.healthlog.ui.doctor.DoctorViewModel
import android.widget.LinearLayout
import android.widget.EditText
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
import android.widget.TextView
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
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.healthlog.handler.PatientViewHandler
import com.example.healthlog.model.Patient
import android.widget.ArrayAdapter
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.AdapterView
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
import android.widget.DatePicker
import com.example.healthlog.MainActivity
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import kotlin.Throws
import com.example.healthlog.interfaces.DialogClickListener
import android.widget.RadioGroup
import android.widget.RadioButton
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
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import org.junit.runner.RunWith
import java.util.*

class MainActivity : AppCompatActivity() {
    // COMPLETED(SHANK) add logout button in appBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadLocale()
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)
        val navView = findViewById<BottomNavigationView?>(R.id.nav_view)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration.Builder(
            R.id.navigation_dashboard, R.id.navigation_doctor, R.id.navigation_hospitals
        )
            .build()
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(navView, navController)
    }

    fun dialog() {
        val builder = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        // Set the message show for the Alert time
        builder.setMessage(R.string.ask_logout)
        builder.setTitle(R.string.alert)
        builder.setCancelable(false)
        builder.setPositiveButton(
            android.R.string.yes
        ) { dialogInterface, i -> logOut() }
        builder.setNegativeButton(
            android.R.string.no
        ) { dialogInterface, i -> dialogInterface.cancel() }
        // Create the Alert dialog
        val alertDialog = builder.create()
        alertDialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> dialog()
            R.id.language -> showChangeLanguageDialogue()
            R.id.about -> showAboutAppPage()
            R.id.setting -> {
                showSettingsAppPage()
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun showChangeLanguageDialogue() {
        val listitems = arrayOf<String?>("French", "German", "Spanish", "English")
        val mbuilder = AlertDialog.Builder(this@MainActivity)
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

    // COMPLETED(SHANK) call this method when logout button is clicked
    // COMPLETED(DJ) implement the method
    fun logOut() {
        HealthLog.Companion.addHospitalIdToSharedPreference(null)
        /*SharedPreferences preferences = getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("ID", "");
        editor.commit();
        HealthLog.ID = null;*/startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        finish()
    }

    fun showAboutAppPage() {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
    }

    fun showSettingsAppPage() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}