package com.example.healthlog.ui.doctor

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
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.junit.runner.RunWith
import java.util.ArrayList

class DoctorFragment : Fragment() {
    // COMPLETED(DJ) setOnItemClickListener to doctor_list_item
    // COMPLETED(Danish) create Application class and handle the shared preferences
    // COMPLETED(DJ) apply search filter
    // COMPLETED(DJ) implement layout file
    // COMPLETED(SHANK) implement recycler view and adapter
    private var doctorAdapter: DoctorAdapter? = null
    private var doctorRecyclerView: RecyclerView? = null
    private var doctorViewModel: DoctorViewModel? = null
    var searchContainer: LinearLayout? = null
    var currentSate = 0 //0: dim, 1:bright
    private var searchEditText: EditText? = null
    var root: View? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        root = inflater.inflate(R.layout.fragment_doctor, container, false)
        setUpRecyclerView()
        searchEditText = root.findViewById<EditText?>(R.id.doctor_searchBox_editText)
        searchContainer = root.findViewById<LinearLayout?>(R.id.doctor_searchContainer_linearLayout)
        searchEditText.addTextChangedListener(
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
                    doctorAdapter.filter(editable.toString().trim { it <= ' ' })
                }
            })
        searchEditText.setOnTouchListener(OnTouchListener { view, motionEvent ->
            if (currentSate == 0) {
                currentSate = 1
                searchContainer.animate().alpha(0.8f).setDuration(500).start()
            }
            false
        })
        root.findViewById<View?>(R.id.root).setOnClickListener(View.OnClickListener {
            if (currentSate == 1) {
                currentSate = 0
                searchContainer.animate().alpha(0.1f).setDuration(500).start()
            }
        })
        searchContainer.setOnClickListener(View.OnClickListener {
            if (currentSate == 0) {
                currentSate = 1
                searchContainer.animate().alpha(0.8f).setDuration(500).start()
            }
        })
        return root
    }

    // COMPLETED(SHANK) implement following method
    fun setUpRecyclerView() {
        doctorAdapter = DoctorAdapter(
            ArrayList()
        ) { d, v ->
            val intent = Intent(activity, DoctorActivity::class.java)
            intent.putExtra("doctor", d)
            startActivity(intent)
        }
        doctorRecyclerView = root.findViewById(R.id.doctor_showList_recycler)
        doctorRecyclerView.setHasFixedSize(false)
        doctorRecyclerView.setLayoutManager(LinearLayoutManager(context))
        doctorRecyclerView.setAdapter(doctorAdapter)
        doctorRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy >= 0) {
                    currentSate = 0
                    searchContainer.animate().alpha(0.1f).setDuration(500).start()
                } else {
                    currentSate = 1
                    searchContainer.animate().alpha(0.8f).setDuration(500).start()
                }
            }
        })
        doctorViewModel = ViewModelProviders.of(this).get(DoctorViewModel::class.java)
        doctorViewModel.init(activity)
        doctorViewModel
            .getDoctorsList()
            .observe(
                viewLifecycleOwner,
                Observer { doctors ->
                    for (d in doctors) {
                        doctorAdapter.add(d)
                    }
                })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_logout, menu)
        val searchItem = menu.findItem(R.id.search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                doctorAdapter.filter(newText.trim { it <= ' ' })
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }
}