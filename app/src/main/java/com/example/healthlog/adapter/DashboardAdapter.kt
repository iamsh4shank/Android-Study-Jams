package com.example.healthlog.adapter

import android.annotation.SuppressLint
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
import android.util.Log
import android.view.*
import android.widget.*
import com.example.healthlog.interfaces.OnItemClickListener
import org.junit.runner.RunWith
import java.util.ArrayList

class DashboardAdapter(
    var context: Context?,
    private val allPatientList: MutableList<Patient?>?,
    listener: OnItemClickListener<*>?
) : RecyclerView.Adapter<DashboardViewHolder?>() {
    var onItemClickListener: OnItemClickListener<*>?
    private val currentPatientList: MutableList<Patient?>?
    private var currentFilter: String? = "Active"
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DashboardViewHolder {
        val itemView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.patient_list_item, parent, false)
        return DashboardViewHolder(itemView)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: DashboardViewHolder, position: Int) {
        // COMPLETED(Shashank) add colors to res directory rather than hardcoding
        val patient = currentPatientList.get(position)
        holder.patientName.setText(patient.getId())
        holder.patientStatus.setText(patient.getStatus())
        holder.patientStatus.setTextColor(ContextCompat.getColor(context, patient.getStatusColor()))
        holder.patientColorStatus.setBackgroundResource(patient.getStatusDrawable())
        holder.patientLogDescription.setText(patient.getRecentLog())
        holder.patientDateAdded.setText(
            "Added: " + HealthLog.Companion.getDate(patient.getDateAdded()) + ", " + HealthLog.Companion.getTime(
                patient.getDateAdded()
            )
        )
        holder.bind(patient, onItemClickListener)
    }

    fun listenForStatusAndLogChanges(p: Patient?) {
        val mRef = FirebaseFirestore.getInstance()
        mRef.collection("Hospital")
            .document(HealthLog.Companion.ID)
            .collection("Patient")
            .document(p.getId())
            .addSnapshotListener { documentSnapshot, e ->
                if (documentSnapshot.exists()) {
                    val status = documentSnapshot.getString("status")
                    val log = documentSnapshot.getString("recentLog")
                    p.setStatus(status)
                    p.setRecentLog(log)
                }
                notifyDataSetChanged()
            }
    }

    override fun getItemCount(): Int {
        return currentPatientList.size
    }

    fun add(p: Patient?) {
        for (patient in allPatientList) {
            if (p.getId() == patient.getId()) {
                return
            }
        }
        listenForStatusAndLogChanges(p)
        allPatientList.add(p)
        if (currentFilter == "All") {
            currentPatientList.clear()
            currentPatientList.addAll(allPatientList)
            notifyDataSetChanged()
            return
        }
        if (p.getStatus() == currentFilter) {
            currentPatientList.add(p)
            notifyDataSetChanged()
        }
    }

    // COMPLETED(Danish) handle filter  here
    fun applyFilter(filter: String?) {
        currentFilter = filter
        if (filter == "All") {
            for (p in allPatientList) {
                if (!currentPatientList.contains(p)) {
                    currentPatientList.add(p)
                }
            }
            notifyDataSetChanged()
            return
        }
        currentPatientList.clear()
        for (p in allPatientList) {
            if (p.getStatus() == filter) {
                currentPatientList.add(p)
            }
        }
        notifyDataSetChanged()
    }

    fun filter(name: String?) {
        currentPatientList.clear()
        if (name == "") {
            applyFilter(currentFilter)
            return
        }
        for (p in allPatientList) {
            if (p.getId().toLowerCase().contains(name.toLowerCase())
                || p.getName().toLowerCase().contains(name.toLowerCase())
            ) {
                currentPatientList.add(p)
            }
        }
        notifyDataSetChanged()
    }

    fun setCurrentFilter(currentFilter: String?) {
        this.currentFilter = currentFilter
    }

    // COMPLETED(Shashank) create Add and AddAll method to add new patients in array
    inner class DashboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var patientName: TextView?
        var patientStatus: TextView?
        var patientLogDescription: TextView?
        var patientDateAdded: TextView?
        var patientColorStatus: View?
        var view: View?
        var patient_delete: Button?
        fun bind(currentPatient: Patient?, listener: OnItemClickListener<*>?) {
            view.setOnClickListener(
                View.OnClickListener { listener.onItemClicked(currentPatient, itemView) })
            patient_delete.setOnClickListener(View.OnClickListener {
                val alert = AlertDialog.Builder(
                    context, R.style.CustomDialogTheme
                )
                alert.setTitle("Delete Patient")
                alert.setMessage("Are you sure you want to delete?")
                alert.setPositiveButton(android.R.string.yes) { dialog, which ->
                    allPatientList.remove(currentPatient)
                    if (currentPatientList.contains(currentPatient)) currentPatientList.remove(
                        currentPatient
                    )
                    FirebaseFirestore.getInstance().collection("Hospital")
                        .document(HealthLog.Companion.ID).collection("Patient")
                        .document(currentPatient.getId())
                        .delete()
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "Patient deleted")
                            Toast.makeText(
                                context,
                                "Patient " + currentPatient.getId() + " deleted",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e -> Log.e(ContentValues.TAG, "Error", e) }
                }
                alert.setNegativeButton(android.R.string.no) { dialog, which -> dialog.cancel() }
                alert.show()
            })
        }

        init {
            view = itemView
            patientName = itemView.findViewById<TextView?>(R.id.patient_list_item_name_textView)
            patientStatus =
                itemView.findViewById<TextView?>(R.id.patient_list_item_statusText_textView)
            patientLogDescription =
                itemView.findViewById<TextView?>(R.id.patient_list_item_logDescription_textView)
            patientColorStatus =
                itemView.findViewById<View?>(R.id.patient_list_item_statusCircle_view)
            patientDateAdded =
                itemView.findViewById<TextView?>(R.id.patient_list_item_dateAdded_textView)
            patient_delete = itemView.findViewById<Button?>(R.id.patient_delete_button)
        }
    }

    init {
        currentPatientList = ArrayList()
        onItemClickListener = listener
    }
}