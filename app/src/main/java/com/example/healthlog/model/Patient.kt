package com.example.healthlog.model

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
import android.content.Intent
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

class Patient {
    var id: String? = null
    var name: String? = null
    var address: String? = null
    var age: String? = null
    var status: String? = null
    var recentLog: String? = null
    var dob: String? = null
    var statusColor = 0
    var statusDrawable = 0
    var dateAdded: Timestamp? = null
    var location: MutableList<String?>? = null
    var type: Int? = 0

    constructor() {}
    constructor(name: String?, status: String?, recentLog: String?) {
        this.name = name
        this.status = status
        this.recentLog = recentLog
        setStatusColor()
        setStatusDrawable()
    }

    fun getId(): String? {
        return id
    }

    fun setId(id: String?) {
        this.id = id
    }

    fun getName(): String? {
        return name
    }

    fun setName(name: String?) {
        this.name = name
    }

    fun getAddress(): String? {
        return address
    }

    fun setAddress(address: String?) {
        this.address = address
    }

    fun getAge(): String? {
        return age
    }

    fun setAge(age: String?) {
        this.age = age
    }

    fun getDob(): String? {
        return dob
    }

    fun setDob(dob: String?) {
        this.dob = dob
    }

    fun getDateAdded(): Timestamp? {
        return dateAdded
    }

    fun setDateAdded(dateAdded: Timestamp?) {
        this.dateAdded = dateAdded
    }

    fun getLocation(): MutableList<String?>? {
        return location
    }

    fun setLocation(location: MutableList<String?>?) {
        this.location = location
    }

    fun getStatus(): String? {
        return status
    }

    fun setStatus(status: String?) {
        this.status = status
        setStatusColor()
        setStatusDrawable()
    }

    fun setRecentLog(recentLog: String?) {
        this.recentLog = recentLog
    }

    fun getRecentLog(): String? {
        return recentLog
    }

    fun getType(): Int? {
        return type
    }

    fun setType(type: Int?) {
        this.type = type
    }

    fun getStatusColor(): Int {
        return statusColor
    }

    private fun setStatusColor() {
        if (status == "Active") {
            statusColor = R.color.activeLightText
        } else if (status == "Cured") {
            statusColor = R.color.recoveredLightText
        } else if (status == "Deceased") {
            statusColor = R.color.deceasedLightText
        } else {
        }
    }

    fun getStatusDrawable(): Int {
        return statusDrawable
    }

    private fun setStatusDrawable() {
        if (status == "Active") {
            statusDrawable = R.drawable.active_status_circle
        } else if (status == "Cured") {
            statusDrawable = R.drawable.cured_status_circle
        } else if (status == "Deceased") {
            statusDrawable = R.drawable.deceased_status_circle
        } else {
        }
    }

    fun refreshStatus() {
        setStatusColor()
        setStatusDrawable()
    }
}