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
import android.content.DialogInterface
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
import android.util.Pair
import android.view.*
import com.google.firebase.firestore.*
import org.junit.runner.RunWith
import java.util.ArrayList
import java.util.HashMap

class DoctorActivity : AppCompatActivity() {
    // COMPLETED(SHANK) implement the ui
    // COMPLETED(SHANK) display list of patient allotted
    // COMPLETED(SHANK) create layout for adding log in patient data
    // COMPLETED(SHANK) add feature for doctor to enter log for patient
    // COMPLETED(DJ) verify code before allowing doctor to add log
    var patientAdapter: DashboardAdapter? = null
    private var patientRecyclerView: RecyclerView? = null
    var mRef: FirebaseFirestore? = null
    var patientLogHandler: PatientLogHandler? = null
    var doctor: Doctor? = null
    var changeStatus: Pair<String?, String?>? = Pair("", "") // status change Pair<from, to>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_doctor)
        mRef = FirebaseFirestore.getInstance()
        val intent = intent
        doctor = intent.getSerializableExtra("doctor") as Doctor
        val actionBar = supportActionBar
        actionBar.setDisplayHomeAsUpEnabled(true)
        setUpRecyclerView()
    }

    fun setUpRecyclerView() {
        patientLogHandler = PatientLogHandler(this@DoctorActivity)
        patientLogHandler.setVerifyCode(doctor.getVerifyKey())
        patientAdapter = DashboardAdapter(
            this@DoctorActivity,
            ArrayList()
        ) { patient, v ->
            patientLogHandler.init()
            patientLogHandler.setDialogClickListener(
                DialogClickListener { log, status -> updatePatient(patient, log, status) })
        }
        patientAdapter.setCurrentFilter("All")
        patientRecyclerView =
            findViewById<View?>(R.id.doctor_patient_list_recyclerView) as RecyclerView?
        patientRecyclerView.setHasFixedSize(false)
        patientRecyclerView.setLayoutManager(LinearLayoutManager(applicationContext))
        patientRecyclerView.addItemDecoration(
            DividerItemDecoration(this@DoctorActivity, LinearLayoutManager.VERTICAL)
        )
        patientRecyclerView.setAdapter(patientAdapter)
        fetchPatient()
    }

    // fetch patient details
    fun fetchPatient() {
        mRef.collection("Hospital")
            .document(HealthLog.Companion.ID)
            .collection("Doctor")
            .document(doctor.getId())
            .collection("Routine")
            .document("Routine")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    val patientsRef =
                        document.get("patientList") as MutableList<DocumentReference?>?
                    for (ref in patientsRef) {
                        ref.get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val p = task.result.toObject(Patient::class.java)
                                    patientAdapter.add(p)
                                }
                            }
                    }
                }
            }
    }

    // update status and log of patient
    fun updatePatient(p: Patient?, log: String?, status: String?) {
        val update: MutableMap<String?, Any?>
        update = if (status == null) {
            object : HashMap<String?, Any?>() {
                init {
                    put("recentLog", log)
                }
            }
        } else {
            object : HashMap<String?, Any?>() {
                init {
                    put("recentLog", log)
                    put("status", status)
                }
            }
        }
        setChangeStatus(Pair(p.getStatus(), status))
        mRef.collection("Hospital")
            .document(HealthLog.Companion.ID)
            .collection("Patient")
            .document(p.getId())
            .update(update)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // COMPLETED(DJ) call @updatePatientMetaData() function & destroy dialog after
                    // that task is completed
                    updatePatientMetaData()
                }
            }
    }

    // COMPLETED(DJ) update meta-data
    fun updatePatientMetaData() {
        if (changeStatus.first == changeStatus.second) {
            return
        }
        val metaDataRef = mRef.collection("Hospital")
            .document(HealthLog.Companion.ID)
            .collection("Patient")
            .document("meta-data")
        mRef.runTransaction(
            Transaction.Function<Void?> {
                metaDataRef.update(changeStatus.first.toLowerCase(), FieldValue.increment(-1))
                metaDataRef.update(changeStatus.second.toLowerCase(), FieldValue.increment(1))
                null
            })
            .addOnCompleteListener { patientLogHandler.destroyDialog() }
    }

    fun setChangeStatus(changeStatus: Pair<String?, String?>?) {
        this.changeStatus = changeStatus
    }
}