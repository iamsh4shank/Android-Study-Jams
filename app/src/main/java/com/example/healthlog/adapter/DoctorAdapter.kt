package com.example.healthlog.adapter

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
import android.view.*
import com.example.healthlog.interfaces.OnItemClickListener
import org.junit.runner.RunWith
import java.util.ArrayList

class DoctorAdapter(
    private val mainDoctorList: MutableList<Doctor?>?,
    listener: OnItemClickListener<*>?
) : RecyclerView.Adapter<DoctorViewHolder?>() {
    private val allDoctorList: MutableList<Doctor?>?
    private val onItemClickListener: OnItemClickListener<*>?
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.doctor_list_item, parent, false)
        return DoctorViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = mainDoctorList.get(position)
        holder.doctorName.setText(doctor.getName())
        holder.doctorStatus.setText(doctor.getStatus())
        // holder.doctorLogDescription.setText(doctor.getLogDescription());
        holder.doctorRoom.setText(HealthLog.Companion.getDoctorLocation(doctor.getLocation()))
        holder.doctorType.setText(doctor.getDepartment())
        if (doctor.getStatus() == "Available") {
            holder.doctorColorStatus.setBackgroundResource(R.drawable.cured_status_circle)
        } else {
            holder.doctorColorStatus.setBackgroundResource(R.drawable.active_status_circle)
        }
        holder.bind(doctor, onItemClickListener)
    }

    override fun getItemCount(): Int {
        return mainDoctorList.size
    }

    fun add(d: Doctor?) {
        allDoctorList.add(d)
        mainDoctorList.add(d)
        notifyDataSetChanged()
    }

    // COMPLETED(DJ) implement filter
    fun filter(name: String?) {
        mainDoctorList.clear()
        if (name == "") {
            mainDoctorList.addAll(allDoctorList)
            return
        }
        for (d in allDoctorList) {
            if (d.getId().toLowerCase().contains(name.toLowerCase())
                || d.getName().toLowerCase().contains(name.toLowerCase())
            ) {
                mainDoctorList.add(d)
            }
        }
        notifyDataSetChanged()
    }

    inner class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var doctorName: TextView?
        var doctorStatus: TextView?
        var doctorLogDescription: TextView?
        var doctorRoom: TextView?
        var doctorType: TextView?
        var doctorColorStatus: View?
        var view: View?
        fun bind(d: Doctor?, listener: OnItemClickListener<*>?) {
            view.setOnClickListener(
                View.OnClickListener { view -> listener.onItemClicked(d, view) })
        }

        init {
            view = itemView
            doctorName = itemView.findViewById<TextView?>(R.id.doctor_list_item_name_textView)
            doctorStatus =
                itemView.findViewById<TextView?>(R.id.doctor_list_item_statusText_textView)
            doctorLogDescription =
                itemView.findViewById<TextView?>(R.id.doctor_list_item_logDescription_textView)
            doctorRoom = itemView.findViewById<TextView?>(R.id.doctor_list_item_dateAdded_textView)
            doctorColorStatus =
                itemView.findViewById<View?>(R.id.doctor_list_item_statusCircle_view)
            doctorType = itemView.findViewById<TextView?>(R.id.doctor_list_item_type_textView)
        }
    }

    init {
        allDoctorList = ArrayList()
        onItemClickListener = listener
    }
}