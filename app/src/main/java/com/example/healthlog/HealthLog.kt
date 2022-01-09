package com.example.healthlog

import android.app.*
import com.example.healthlog.adapter.DoctorAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.healthlog.ui.doctor.DoctorViewModel
import android.widget.LinearLayout
import android.widget.EditText
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.example.healthlog.R
import android.text.TextWatcher
import android.text.Editable
import android.view.View.OnTouchListener
import android.view.MotionEvent
import com.example.healthlog.model.Doctor
import com.example.healthlog.DoctorActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.ViewModelProviders
import android.view.MenuInflater
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
import com.google.android.gms.tasks.OnFailureListener
import com.example.healthlog.adapter.SuspectedPatientAdapter.SuspectedPatientViewHolder
import android.graphics.drawable.ColorDrawable
import androidx.core.app.NotificationManagerCompat
import android.os.Build
import androidx.annotation.RequiresApi
import android.app.DatePickerDialog.OnDateSetListener
import android.content.*
import android.widget.DatePicker
import com.example.healthlog.MainActivity
import androidx.core.app.NotificationCompat
import kotlin.Throws
import com.example.healthlog.interfaces.DialogClickListener
import android.widget.RadioGroup
import android.widget.RadioButton
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
import com.google.firebase.Timestamp
import org.junit.runner.RunWith
import java.text.SimpleDateFormat

class HealthLog : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        getHospitalId()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

//          For high priority Notifications, eg new patient admitted etc.
            val channel1 = NotificationChannel(
                CHANNEL_1_ID,
                "@string/notification_channel_info",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel1.description = "@string/notification_channel_name"

//          For relatively less important notifications
            val channel2 = NotificationChannel(
                CHANNEL_2_ID,
                "@string/notification_channel2_info",
                NotificationManager.IMPORTANCE_LOW
            )
            channel1.description = "@string/notification_channel2_name"
            val manager: NotificationManager = getSystemService<NotificationManager?>(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(channel1)
            manager.createNotificationChannel(channel2)
        }
    }

    companion object {
        val CHANNEL_1_ID: String? = "channel1"
        val CHANNEL_2_ID: String? = "channel2"
        var ID: String? = null
        var context: Context? = null
        fun addHospitalIdToSharedPreference(s: String?) {
            val preferences = context.getSharedPreferences("data", MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putString("ID", s)
            editor.commit()
            getHospitalId()
        }

        private fun getHospitalId() {
            val preferences = context.getSharedPreferences("data", MODE_PRIVATE)
            ID = preferences.getString("ID", null)
        }

        fun getDate(timestamp: Timestamp?): String? {
            val simpleDateFormat =
                SimpleDateFormat("dd MMM")
            return simpleDateFormat.format(timestamp.toDate())
        }

        fun getTime(timestamp: Timestamp?): String? {
            val simpleTimeFormat = SimpleDateFormat("H:m")
            return simpleTimeFormat.format(timestamp.toDate())
        }

        fun getDoctorLocation(location: MutableList<String?>?): String? {
            return "Cabin: " + location.get(1) + " Floor: " + location.get(0)
        }
    }
}