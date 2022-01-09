package com.example.healthlog.ui.hospital

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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.example.healthlog.HealthLog
import com.google.android.gms.tasks.OnCompleteListener
import com.example.healthlog.ui.hospital.HospitalViewModel
import com.example.healthlog.adapter.SuspectedPatientAdapter
import android.widget.TextView
import com.example.healthlog.model.SuspectedPatient
import com.example.healthlog.handler.HospitalProfileHandler
import com.example.healthlog.model.Hospital
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
import com.google.firebase.firestore.*
import org.junit.runner.RunWith
import java.util.ArrayList

class HospitalViewModel : ViewModel() {
    private val mText: MutableLiveData<String?>?
    private var mContext: Context? = null
    private var patientMetaDataRef: DocumentReference? = null
    private val suspectedPatientArrayList: ArrayList<SuspectedPatient?>? = ArrayList()
    var suspect: MutableLiveData<ArrayList<SuspectedPatient?>?>? = MutableLiveData()
    private var liveData: MutableLiveData<ArrayList<SuspectedPatient?>?>? = null
    private val totalNoOfPatient: MutableLiveData<Int?>? = MutableLiveData()
    private val totalNoOfActivePatient: MutableLiveData<Int?>? = MutableLiveData()
    private val totalNoOfCuredPatient: MutableLiveData<Int?>? = MutableLiveData()
    private val totalNoOfDeceasedPatient: MutableLiveData<Int?>? = MutableLiveData()
    fun getText(): LiveData<String?>? {
        return mText
    }

    fun init(context: Context?) {
        mContext = context
        fetchPatientMetaData()
    }

    fun initSuspectList(context: Context?) {
        mContext = context
        if (liveData != null) {
            return
        }
        liveData = getSuspectPatients()
    }

    fun getSuspectPatientsList(): LiveData<ArrayList<SuspectedPatient?>?>? {
        return liveData
    }

    fun getSuspectPatients(): MutableLiveData<ArrayList<SuspectedPatient?>?>? {
        fetchSuspectPatients()
        return suspect
    }

    // COMPLETED(SHANK) implement_1
    fun getTotalNoOfPatient(): LiveData<Int?>? {
        return totalNoOfPatient
    }

    // COMPLETED(SHANK) implement_2
    fun getNoOfActivePatient(): LiveData<Int?>? {
        return totalNoOfActivePatient
    }

    // COMPLETED(SHANK) implement_3
    fun getNoOfCuredPatient(): LiveData<Int?>? {
        return totalNoOfCuredPatient
    }

    // COMPLETED (SHANK) implement_4
    fun getNoOfDeceasedPatient(): LiveData<Int?>? {
        return totalNoOfDeceasedPatient
    }

    // COMPLETED(DJ) implement_1
    private fun fetchPatientMetaData() {
        patientMetaDataRef = FirebaseFirestore.getInstance()
            .collection("Hospital")
            .document(HealthLog.Companion.ID)
            .collection("Patient")
            .document("meta-data")
        patientMetaDataRef.addSnapshotListener(
            EventListener { doc, e ->
                totalNoOfPatient.setValue(doc.getLong("size").toInt())
                totalNoOfActivePatient.setValue(doc.getLong("active").toInt())
                totalNoOfCuredPatient.setValue(doc.getLong("cured").toInt())
                totalNoOfDeceasedPatient.setValue(doc.getLong("deceased").toInt())
            })
    }

    private fun fetchSuspectPatients() {
        val patientRef = FirebaseFirestore.getInstance()
            .collection("Hospital")
            .document(HealthLog.Companion.ID)
            .collection("suspect")
        patientRef
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (snapshot in task.result) {
                        val suspectedPatient = snapshot.toObject(
                            SuspectedPatient::class.java
                        )
                        suspectedPatientArrayList.add(suspectedPatient)
                    }
                    suspect.setValue(suspectedPatientArrayList)
                }
            }
    }

    // COMPLETED(DJ) implement_1
    fun addPatientToHospital(patient: SuspectedPatient?) {
        val patientHandler: NewPatientHandler<*> = NewPatientHandler<Any?>(mContext)
        patientHandler.setName(patient.getName())
        patientHandler.setAddress(patient.getAddress())
        patientHandler.init()
    }

    // COMPLETED(DJ) implement_2
    fun sendRequestToHospital(patient: SuspectedPatient?) {
        val hospitalHandler = HospitalHandler(mContext, patient)
        hospitalHandler.init()
    }

    init {
        mText = MutableLiveData()
        mText.setValue("This is Hospital fragment")
    }
}