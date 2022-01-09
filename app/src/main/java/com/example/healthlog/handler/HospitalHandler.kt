package com.example.healthlog.handler

import android.app.*
import com.example.healthlog.adapter.DoctorAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.healthlog.ui.doctor.DoctorViewModel
import android.os.Bundle
import com.example.healthlog.R
import android.text.TextWatcher
import android.text.Editable
import android.view.View.OnTouchListener
import com.example.healthlog.model.Doctor
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
import com.google.android.gms.tasks.OnFailureListener
import com.example.healthlog.adapter.SuspectedPatientAdapter.SuspectedPatientViewHolder
import android.graphics.drawable.ColorDrawable
import androidx.core.app.NotificationManagerCompat
import android.os.Build
import androidx.annotation.RequiresApi
import android.app.DatePickerDialog.OnDateSetListener
import com.example.healthlog.MainActivity
import androidx.core.app.NotificationCompat
import kotlin.Throws
import com.example.healthlog.interfaces.DialogClickListener
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
import android.view.*
import android.widget.*
import org.junit.runner.RunWith
import java.util.ArrayList

class HospitalHandler(// Completed(SHANK) create dialog using ui
    var context: Context?, var suspectedPatient: SuspectedPatient?
) {
    var hospitalNameList: MutableList<String?>? = ArrayList()
    var hospitals: MutableList<Hospital?>? = ArrayList()
    var adapter: ArrayAdapter<String?>? = null
    var spinner: Spinner? = null
    var submit: Button? = null
    var dialog: Dialog? = null
    var mRef: FirebaseFirestore?
    var selectedHospital: Hospital? = null
    fun setUp() {
        dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_hospital)
        dialog.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog
            .getWindow()
            .setLayout(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT
            )
        spinner = dialog.findViewById<Spinner?>(R.id.dialog_hospitalList_spinner)
        submit = dialog.findViewById<Button?>(R.id.dialog_submit_button)
        adapter = ArrayAdapter(
            context, R.layout.spinner_item, hospitalNameList
        )
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter)
        spinner.setOnItemSelectedListener(
            object : OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    i: Int,
                    l: Long
                ) {
                    selectedHospital = hospitals.get(i)
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            })
        submit.setOnClickListener(
            View.OnClickListener { initializeServer() })
        fetchHospitalList()
    }

    fun initializeServer() {
        mRef.collection("Hospital")
            .document(selectedHospital.getId())
            .collection("suspect")
            .add(suspectedPatient)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mRef.collection("Hospital")
                        .document(HealthLog.Companion.ID)
                        .collection("suspect")
                        .whereEqualTo("address", suspectedPatient.getAddress())
                        .whereEqualTo("age", suspectedPatient.getAge())
                        .whereEqualTo("contact", suspectedPatient.getContact())
                        .whereEqualTo("email", suspectedPatient.getEmail())
                        .whereEqualTo("name", suspectedPatient.getName())
                        .whereEqualTo("type", suspectedPatient.getType())
                        .get()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val snapshots = task.result.getDocuments()
                                if (snapshots.size == 1) {
                                    snapshots[0]
                                        .reference
                                        .delete()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                dismiss()
                                            }
                                        }
                                }
                            }
                        }
                }
            }
    }

    fun fetchHospitalList() {
        mRef.collection("Hospital")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (snapshot in task.result) {
                        val hospital = snapshot.toObject(Hospital::class.java)
                        if (hospital.getId() != HealthLog.Companion.ID) {
                            hospitals.add(hospital)
                            hospitalNameList.add(snapshot.getString("name"))
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    fun init() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

    init {
        mRef = FirebaseFirestore.getInstance()
        setUp()
    }
}