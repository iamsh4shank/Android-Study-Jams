package com.example.healthlog.ui.dashboard

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
import android.content.*
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
import org.junit.runner.RunWith
import java.util.ArrayList

class DashboardViewModel : ViewModel() {
    var mRef: FirebaseFirestore?
    private val patientArrayList: ArrayList<Patient?>? = ArrayList()
    var patient: MutableLiveData<ArrayList<Patient?>?>? = MutableLiveData()
    private var liveData: MutableLiveData<ArrayList<Patient?>?>? = null
    private var mContext: Context? = null
    private val mText: MutableLiveData<String?>?
    fun init(context: Context?) {
        mContext = context
        if (liveData != null) {
            return
        }
        liveData = getPatients()
    }

    fun getPatientsList(): LiveData<ArrayList<Patient?>?>? {
        return liveData
    }

    fun getPatients(): MutableLiveData<ArrayList<Patient?>?>? {
        fetchPatients()
        return patient
    }

    fun getText(): LiveData<String?>? {
        return mText
    }

    // COMPLETED(DJ) update data realtime
    fun fetchPatients() {
        val patientReference =
            mRef.collection("Hospital").document(HealthLog.Companion.ID).collection("Patient")
        patientReference
            .document("meta-data")
            .addSnapshotListener { documentSnapshot, e ->
                patientReference
                    .whereEqualTo("type", 0)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (document in task.result) {
                                val p = document.toObject(Patient::class.java)
                                p.refreshStatus()
                                patientArrayList.add(p)
                            }
                            patient.setValue(patientArrayList)
                        }
                    }
            }
    }

    init {
        mRef = FirebaseFirestore.getInstance()
        mText = MutableLiveData()
        mText.setValue("This is dashboard fragment")
    }
}