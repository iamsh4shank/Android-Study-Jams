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

class PatientLogHandler {
    var dialogClickListener: DialogClickListener? = null
    var context: Context?
    var log: String? = null
    var logEdit: EditText? = null
    var save: Button? = null
    var verifyCodeEt: EditText? = null
    var verifyCodeBtn: Button? = null
    var radioGroup: RadioGroup? = null
    var checkedRadioButton: RadioButton? = null
    var verifyCode: String? = null
    var patient: Patient? = null

    constructor(context: Context?) {
        this.context = context
        if (dialog == null) {
            setUp()
        }
    }

    constructor(dialogClickListener: DialogClickListener?, context: Context?) {
        this.dialogClickListener = dialogClickListener
        this.context = context
    }

    var dialog: Dialog? = null
    fun setUp() {
        dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_add_log)
        dialog.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog
            .getWindow()
            .setLayout(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT
            )
        logEdit = dialog.findViewById<EditText?>(R.id.doctor_activity_addlog_editText)
        radioGroup =
            dialog.findViewById<RadioGroup?>(R.id.doctor_Activity_dialog_patientStatus_radioGroup)
        save = dialog.findViewById<Button?>(R.id.doctor_activity_addlog_btn)
        verifyCodeEt = dialog.findViewById<EditText?>(R.id.doctor_activity_verifyCode_eT)
        verifyCodeBtn = dialog.findViewById<Button?>(R.id.doctor_activity_verifyCode_btn)
        radioGroup.setOnCheckedChangeListener(
            RadioGroup.OnCheckedChangeListener { radioGroup, i ->
                checkedRadioButton = dialog.findViewById<RadioButton?>(i)
            })
        save.setOnClickListener(
            View.OnClickListener {
                if (checkedRadioButton == null) {
                    dialogClickListener.onSaveClicked(
                        logEdit.getText().toString().trim { it <= ' ' }, null
                    )
                } else {
                    dialogClickListener.onSaveClicked(
                        logEdit.getText().toString().trim { it <= ' ' },
                        checkedRadioButton.getText().toString()
                    )
                }
            })
        verifyCodeBtn.setOnClickListener(
            View.OnClickListener {
                verifyCode(
                    verifyCodeEt.getText().toString().trim { it <= ' ' })
            })
        dialog.setOnCancelListener(
            DialogInterface.OnCancelListener { resetCode() })
    }

    fun init() {
        dialog.show()
    }

    fun destroyDialog() {
        dialog.dismiss()
    }

    fun getLog(): String? {
        return log
    }

    fun setDialogClickListener(dialogClickListener: DialogClickListener?) {
        this.dialogClickListener = dialogClickListener
    }

    // COMPLETED(DJ) verify code and update the ui
    fun verifyCode(code: String?) {
        if (code == verifyCode) {
            verifyCodeEt.setVisibility(View.GONE)
            verifyCodeBtn.setVisibility(View.GONE)
            logEdit.setVisibility(View.VISIBLE)
            radioGroup.setVisibility(View.VISIBLE)
            save.setVisibility(View.VISIBLE)
        } else {
            verifyCodeEt.setError("Invalid code")
        }
    }

    fun setVerifyCode(verifyCode: String?) {
        this.verifyCode = verifyCode
    }

    fun resetCode() {
        verifyCodeEt.setVisibility(View.VISIBLE)
        verifyCodeBtn.setVisibility(View.VISIBLE)
        logEdit.setVisibility(View.GONE)
        radioGroup.setVisibility(View.GONE)
        save.setVisibility(View.GONE)
    }
}