package com.example.healthlog.ui.hospital

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
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.healthlog.interfaces.OnItemClickListener
import org.junit.runner.RunWith
import java.util.ArrayList

// run this for spotless check  "./gradlew spotlessApply"
class HospitalFragment : Fragment() {
    // COMPLETED(SHANK) create recyclerView
    // COMPLETED(SHANK) display list of requested patients
    /* COMPLETED(INSTRUCTION)
    * 1. When user tap on listItem an alert dialog will popUp
    * 2. Alert dialog will have 2 options
    *   2.1. Positive -> add to ur hospital
    *   2.2. Negative -> send request to diff hospital
    * 3. And call form viewModel @addPatientToHospital for +ve and @sendRequestToHospital for -ve.
    * */
    // COMPLETED(SHANK) implement ui
    private var notificationsViewModel: HospitalViewModel? = null
    private var suspectedAdapter: SuspectedPatientAdapter? = null
    private var suspectedRecyclerView: RecyclerView? = null
    var root: View? = null
    var totalPatientsTv: TextView? = null
    var activePatientsTv: TextView? = null
    var curedPatientsTv: TextView? = null
    var deceasedPatientsTv: TextView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel = ViewModelProviders.of(this).get(
            HospitalViewModel::class.java
        )
        root = inflater.inflate(R.layout.fragment_hospital, container, false)
        setup()
        showSuspectList()
        showHospitalProfile()
        return root
    }

    // COMPLETED(SHANK) find all views, create object of viewModel and call attachModel
    fun setup() {
        totalPatientsTv = root.findViewById<TextView?>(R.id.hospital_confirmed_textView)
        activePatientsTv = root.findViewById<TextView?>(R.id.hospital_active_textView)
        curedPatientsTv = root.findViewById<TextView?>(R.id.hospital_cured_textView)
        deceasedPatientsTv = root.findViewById<TextView?>(R.id.hospital_deceased_textView)
        notificationsViewModel = ViewModelProviders.of(this).get(
            HospitalViewModel::class.java
        )
        notificationsViewModel.init(context)
        notificationsViewModel
            .getNoOfActivePatient()
            .observe(
                viewLifecycleOwner,
                Observer { integer -> activePatientsTv.setText(integer.toString()) })
        notificationsViewModel
            .getTotalNoOfPatient()
            .observe(
                viewLifecycleOwner,
                Observer { integer -> totalPatientsTv.setText(integer.toString()) })
        notificationsViewModel
            .getNoOfCuredPatient()
            .observe(
                viewLifecycleOwner,
                Observer { integer -> curedPatientsTv.setText(integer.toString()) })
        notificationsViewModel
            .getNoOfDeceasedPatient()
            .observe(
                viewLifecycleOwner,
                Observer { integer -> deceasedPatientsTv.setText(integer.toString()) })
    }

    fun showSuspectList() {
        suspectedAdapter = SuspectedPatientAdapter(ArrayList())
        suspectedAdapter.setListener(
            OnItemClickListener<SuspectedPatient?> { suspectedPatient, v ->
                showDialog(
                    suspectedPatient
                )
            })
        suspectedRecyclerView = root.findViewById(R.id.hospital_suspect_list_recyclerView)
        suspectedRecyclerView.setHasFixedSize(false)
        suspectedRecyclerView.setLayoutManager(LinearLayoutManager(context))
        suspectedRecyclerView.setAdapter(suspectedAdapter)
        notificationsViewModel = ViewModelProviders.of(requireActivity()).get(
            HospitalViewModel::class.java
        )
        notificationsViewModel.initSuspectList(activity)
        notificationsViewModel
            .getSuspectPatientsList()
            .observe(
                viewLifecycleOwner,
                Observer { suspectedPatients ->
                    for (s in suspectedPatients) {
                        suspectedAdapter.add(s)
                    }
                })
    }

    fun showHospitalProfile() {
        val hospitalProfileHandler = HospitalProfileHandler(context, activity)
        val hospitalProfile = root.findViewById<Button?>(R.id.hospital_profile)
        val hospital = Hospital()

        // Set Hospital info HERE
        hospital.id = "H1"
        hospital.name = "Sir Sunderlal Hospital"
        hospital.address = "31/3,Andheri,Mumbai"
        hospital.bedCount = 120
        hospital.doctorCount = 30
        hospitalProfile.setOnClickListener(View.OnClickListener {
            hospitalProfileHandler.update(hospital)
            hospitalProfileHandler.init()
        })
    }

    fun showDialog(currentSuspectedPatient: SuspectedPatient?) {
        val builder = AlertDialog.Builder(
            context, R.style.CustomDialogTheme
        )
        // Set the message show for the Alert time
        builder.setMessage(R.string.add_patient)
        builder.setTitle(R.string.alert)
        builder.setCancelable(true)
        builder.setPositiveButton(
            android.R.string.yes
        ) { dialogInterface, i ->
            notificationsViewModel.addPatientToHospital(
                currentSuspectedPatient
            )
        }
        builder.setNegativeButton(
            R.string.send
        ) { dialogInterface, i ->
            notificationsViewModel.sendRequestToHospital(
                currentSuspectedPatient
            )
        }
        // Create the Alert dialog
        val alertDialog = builder.create()
        alertDialog.show()
    }
}