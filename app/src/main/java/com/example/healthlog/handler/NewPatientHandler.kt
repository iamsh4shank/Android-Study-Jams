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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.example.healthlog.HealthLog
import com.google.android.gms.tasks.OnCompleteListener
import com.example.healthlog.ui.hospital.HospitalViewModel
import com.example.healthlog.adapter.SuspectedPatientAdapter
import com.example.healthlog.model.SuspectedPatient
import com.example.healthlog.handler.HospitalProfileHandler
import com.example.healthlog.model.Hospital
import com.example.healthlog.handler.NewPatientHandler
import com.example.healthlog.handler.HospitalHandler
import com.example.healthlog.ui.dashboard.DashboardViewModel
import com.example.healthlog.adapter.DashboardAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.healthlog.handler.PatientViewHandler
import com.example.healthlog.model.Patient
import android.widget.AdapterView.OnItemSelectedListener
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
import android.util.Log
import android.view.*
import android.widget.*
import com.google.firebase.firestore.*
import org.junit.runner.RunWith
import java.util.*

class NewPatientHandler<notificationCount>(  // activity
    var context: Context?
) {
    // COMPLETED(DJ) allot patient to doctor using desired logic
    //Notification Manager
    private val notificationManager: NotificationManagerCompat?

    // Date object for dob
    var date: Date? = null

    // dialog
    var dialog: Dialog? = null

    // editText
    var name: EditText? = null
    var address: EditText? = null
    var dob: EditText? = null
    var floor: EditText? = null
    var roomNo: EditText? = null
    var bedNo: EditText? = null

    // button
    var submit: Button? = null
    var cancel: Button? = null

    // textView
    var id: TextView? = null

    // String
    var patientId: String? = null

    // firebase reference
    var mRef: FirebaseFirestore? = null
    var size = 0
    fun setUp() {
        mRef = FirebaseFirestore.getInstance()
        fetchPatientMetaData()
        dialog = Dialog(context)
        dialog.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.new_patient_layout)
        dialog
            .getWindow()
            .setLayout(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT
            )
        id = dialog.findViewById<TextView?>(R.id.new_patient_layout_id_tV)
        name = dialog.findViewById<EditText?>(R.id.new_patient_layout_name_eT)
        address = dialog.findViewById<EditText?>(R.id.new_patient_layout_address_eT)
        dob = dialog.findViewById<EditText?>(R.id.new_patient_layout_dob_eT)
        floor = dialog.findViewById<EditText?>(R.id.new_patient_layout_floor_eT)
        roomNo = dialog.findViewById<EditText?>(R.id.new_patient_layout_roomNo_eT)
        bedNo = dialog.findViewById<EditText?>(R.id.new_patient_layout_bedNo_eT)
        cancel = dialog.findViewById<Button?>(R.id.new_patient_layout_cancel_btn)
        submit = dialog.findViewById<Button?>(R.id.new_patient_layout_submit_btn)
        dob
            .setOnClickListener(
                View.OnClickListener {
                    Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        pickDate()
                    }
                })
        cancel.setOnClickListener(
            View.OnClickListener { destroy() })
        submit.setOnClickListener(
            View.OnClickListener { createNewPatient() })
        name.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    charSequence: CharSequence?,
                    i: Int,
                    i1: Int,
                    i2: Int
                ) {
                }

                override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {}
                override fun afterTextChanged(editable: Editable?) {
                    id.setText("Patient id: " + getCompleteId())
                }
            })
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun pickDate() {
        val datePickerDialog = DatePickerDialog(context)
        datePickerDialog.setOnDateSetListener { datePicker, year, month, day ->
            dob.setText("$day/$month/$year")
            date = Date(year, month, day)
        }
        datePickerDialog.show()
    }

    // COMPLETED(Danish) implement method
    fun createNewPatient() {
        /*
        * 1. Create new patient object
        * 2. Initiate server
        * 3. Upload data
        */
        val location: MutableList<String?> = ArrayList()
        location.add(floor.getText().toString().trim { it <= ' ' })
        location.add(bedNo.getText().toString().trim { it <= ' ' })
        location.add(roomNo.getText().toString().trim { it <= ' ' })
        val patient = Patient()
        patient.id = getCompleteId()
        patient.name = name.getText().toString().trim { it <= ' ' }
        patient.address = address.getText().toString().trim { it <= ' ' }
        patient.dob = dob.getText().toString().trim { it <= ' ' }
        patient.location = location
        patient.recentLog = "Log"
        patient.status = "Active"
        patient.age = calculateAge()
        val patientRef =
            mRef.collection("Hospital").document(HealthLog.Companion.ID).collection("Patient")
        patientRef
            .document(patient.id)
            .set(patient)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    patientRef
                        .document(patient.id)
                        .update("dateAdded", FieldValue.serverTimestamp())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // COMPLETED(DJ) call @updatePatientMetaData() and move below logic
                                // in that function
                                updatePatientMetaData(patientRef)
                            }
                        }
                    val activityIntent = Intent(context, MainActivity::class.java)
                    val contentIntent = PendingIntent.getActivity(
                        context, 0, activityIntent, 0
                    )
                    //                                        Incase we want to add some minor functionality to improve usage we can add an intent here
//                                        Intent broadcastIntent = new Intent(this, NotificationReceiver.class);
//                                        broadcastIntent.putExtra("toastMessage", message);
//                                        PendingIntent actionIntent = PendingIntent.getBroadcast(
//                                                this, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT
//                                        );

//                                        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.family_father);
                    val notification: Notification = NotificationCompat.Builder(
                        context, HealthLog.Companion.CHANNEL_1_ID
                    )
                        .setSmallIcon(R.mipmap.health_log)
                        .setContentTitle("@string/newpatient")
                        .setContentText("@string/notification_click") //                                                .setLargeIcon(largeIcon)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setColor(Color.RED)
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true) //                                                .addAction(R.mipmap.ic_launcher, "Toast", actionIntent)
                        .build()
                    notificationManager.notify(notificationCount++, notification)
                }
            }
    }

    // COMPLETED(DJ) update meta-data
    fun updatePatientMetaData(patientRef: CollectionReference?) {
        val metaDataRef = patientRef.document("meta-data")
        mRef.runTransaction(
            Transaction.Function<Void?> { transaction ->
                transaction.update(metaDataRef, "size", FieldValue.increment(1))
                transaction.update(metaDataRef, "active", FieldValue.increment(1))
                null
            })
            .addOnCompleteListener { fetchDoctor() }
    }

    fun fetchDoctor() {
        mRef.collection("Hospital")
            .document(HealthLog.Companion.ID)
            .collection("Doctor")
            .orderBy("noOfPatients", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val doctors: MutableList<Doctor?> = ArrayList()
                    for (d in task.result) {
                        doctors.add(d.toObject(Doctor::class.java))
                    }
                    setRoutine(doctors)
                }
            }
    }

    fun setRoutine(doctors: MutableList<Doctor?>?) {
        var currentDoctor: Doctor? = null
        for (i in 0 until doctors.size - 1) {
            if (doctors.get(i).getNoOfPatients() - 1 == doctors.get(i + 1).getNoOfPatients()
                && doctors.get(i + 1).getStatus() == "Available"
            ) {
                currentDoctor = doctors.get(i + 1)
                break
            }
        }
        if (currentDoctor == null) {
            currentDoctor = doctors.get(0)
        }
        toast("server initialising")
        initiateRoutineServer(currentDoctor)
    }

    fun initiateRoutineServer(doctor: Doctor?) {
        val doctorRef =
            mRef.collection("Hospital").document(HealthLog.Companion.ID).collection("Doctor")
        val patientRef = mRef.collection("Hospital")
            .document(HealthLog.Companion.ID)
            .collection("Patient")
            .document(getCompleteId())
        doctorRef
            .document(doctor.getId())
            .collection("Routine")
            .document("Routine")
            .update("patientList", FieldValue.arrayUnion(patientRef))
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    toast("REf added ")
                    doctorRef
                        .document(doctor.getId())
                        .update("noOfPatients", FieldValue.increment(1))
                        .addOnCompleteListener {
                            toast("done")
                            destroy()
                        }
                } else {
                    Log.i("NEWPATIENTHANDLER:>>", task.exception.getLocalizedMessage())
                    toast(task.exception.getLocalizedMessage())
                }
            }
    }

    fun toast(msg: String?) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun calculateAge(): String? {
        var result = ""
        val dob = Calendar.getInstance()
        val today = Calendar.getInstance()
        if (date == null) {
            return "0"
        }
        dob[date.getYear(), date.getMonth()] = date.getDay()
        var age = today[Calendar.YEAR] - dob[Calendar.YEAR]
        if (today[Calendar.DAY_OF_YEAR] < dob[Calendar.DAY_OF_YEAR]) {
            age--
        }
        result = age.toString()
        return result
    }

    fun getCompleteId(): String? {
        return patientId + name.getText().toString().trim { it <= ' ' }.split(" ").toTypedArray()[0]
    }

    fun fetchPatientMetaData() {
        mRef.collection("Hospital")
            .document(HealthLog.Companion.ID)
            .collection("Patient")
            .document("meta-data")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    size = task.result.getDouble("size").toInt()
                    patientId = "P" + size + "_"
                    id.setText("Patient id: $patientId")
                }
            }
    }

    fun setName(name: String?) {
        this.name.setText(name)
    }

    fun setAddress(address: String?) {
        this.address.setText(address)
    }

    fun setDob(dob: String?) {
        this.dob.setText(dob)
    }

    fun setFloor(floor: String?) {
        this.floor.setText(floor)
    }

    fun setRoomNo(roomNo: String?) {
        this.roomNo.setText(roomNo)
    }

    fun setBedNo(bedNo: String?) {
        this.bedNo.setText(bedNo)
    }

    fun init() {
        dialog.show()
    }

    fun destroy() {
        dialog.dismiss()
    }

    companion object {
        //every patient entry will require a new notification-id otherwise only one notification can be shown using one channel
        private var notificationCount = 1
    }

    init {
        notificationManager = NotificationManagerCompat.from(context)
        // this.activity = activity;
        setUp()
    }
}