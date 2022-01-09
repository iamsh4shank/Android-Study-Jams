package com.example.healthlog.handler

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
import android.widget.DatePicker
import com.example.healthlog.MainActivity
import androidx.core.app.NotificationCompat
import kotlin.Throws
import com.example.healthlog.interfaces.DialogClickListener
import android.widget.RadioGroup
import android.widget.RadioButton
import android.content.*
import android.graphics.Color
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

class PatientViewHandler(// activity var
    var context: Context?, var activity: Activity?
) {
    // patient var
    var patient: Patient? = null

    // views
    var id: TextView? = null
    var name: TextView? = null
    var address: TextView? = null
    var dob: TextView? = null
    var age: TextView? = null
    var dateAdded: TextView? = null
    var location: TextView? = null
    var log: TextView? = null
    var dialog: Dialog? = null
    fun setUp() {
        // initialising dialog
        dialog = Dialog(context)
        dialog.setContentView(R.layout.patient_layout)
        dialog.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog
            .getWindow()
            .setLayout(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT
            )
        id = dialog.findViewById<TextView?>(R.id.patient_layout_id_tV)
        name = dialog.findViewById<TextView?>(R.id.patient_layout_name_tV)
        address = dialog.findViewById<TextView?>(R.id.patient_layout_address_tV)
        dob = dialog.findViewById<TextView?>(R.id.patient_layout_dob_tV)
        age = dialog.findViewById<TextView?>(R.id.patient_layout_age_tV)
        dateAdded = dialog.findViewById<TextView?>(R.id.patient_layout_dateAdded_tV)
        location = dialog.findViewById<TextView?>(R.id.patient_layout_location_tV)
        log = dialog.findViewById<TextView?>(R.id.patient_layout_log_tV)
    }

    fun initViews() {
        id.setText("Patient: " + patient.getId())
        name.setText("Name: " + patient.getName())
        address.setText("Address: " + patient.getAddress())
        dob.setText("DOB: " + patient.getDob())
        age.setText("Age: " + patient.getAge())
        dateAdded.setText("Added On: " + patient.getDateAdded().seconds)
        location.setText(
            "Location: "
                    + patient.getLocation()[0]
                    + patient.getLocation()[1]
                    + patient.getLocation()[2]
        )
        log.setText("Log: " + patient.getRecentLog())
    }

    fun init() {
        dialog.show()
    }

    fun destroy() {
        dialog.dismiss()
    }

    fun update(newPatient: Patient?) {
        setPatient(newPatient)
        initViews()
    }

    fun getPatient(): Patient? {
        return patient
    }

    fun setPatient(patient: Patient?) {
        this.patient = patient
    }

    init {
        if (dialog == null) {
            setUp()
        }
    }
}