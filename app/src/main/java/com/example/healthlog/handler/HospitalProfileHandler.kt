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
import java.util.*

class HospitalProfileHandler(// activity var
    var context: Context?, var activity: Activity?
) {
    // Hospital var
    var hospital: Hospital? = null

    // views
    var id: TextView? = null
    var name: TextView? = null
    var address: TextView? = null
    var bedCount: TextView? = null
    var doctorCount: TextView? = null
    var dialog: Dialog? = null
    fun setUp() {
        // initialising dialog
        dialog = Dialog(context)
        dialog.setContentView(R.layout.profile_layout)
        Objects.requireNonNull(dialog.getWindow())
            .setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.getWindow()
            .setLayout(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT
            )
        id = dialog.findViewById<TextView?>(R.id.hospital_id_text)
        name = dialog.findViewById<TextView?>(R.id.hospital_name_text)
        address = dialog.findViewById<TextView?>(R.id.hospital_address)
        bedCount = dialog.findViewById<TextView?>(R.id.hospital_bed_count)
        doctorCount = dialog.findViewById<TextView?>(R.id.hospital_doctor_count)
    }

    fun initViews() {
        id.setText(context.getString(R.string.hospital_id_text, hospital.getId()))
        name.setText(context.getString(R.string.hospital_name, hospital.getName()))
        address.setText(context.getString(R.string.hospital_address, hospital.getAddress()))
        bedCount.setText(context.getString(R.string.bed_count, hospital.getBedCount()))
        doctorCount.setText(context.getString(R.string.doctor_count, hospital.getDoctorCount()))
    }

    fun init() {
        dialog.show()
    }

    fun destroy() {
        dialog.dismiss()
    }

    fun update(newHospital: Hospital?) {
        setHospital(newHospital)
        initViews()
    }

    fun getHospital(): Hospital? {
        return hospital
    }

    fun setHospital(newHospital: Hospital?) {
        hospital = newHospital
    }

    init {
        if (dialog == null) {
            setUp()
        }
    }
}