package com.example.healthlog.ui.dashboard

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

class DashboardFragment : Fragment() {
    // COMPLETED(DJ) add ID in patient_list_item
    // COMPLETED(DJ) create layout for detailed view of patient
    // COMPLETED(DJ) create search feature
    // COMPLETED(DJ) reformat ui file for this fragment
    private var dashboardViewModel: DashboardViewModel? = null
    private var dashboardAdapter: DashboardAdapter? = null
    private var dashboardRecyclerView: RecyclerView? = null
    var searchContainer: RelativeLayout? = null
    private var spinner: Spinner? = null
    private var searchEditText: EditText? = null
    private var category: Spinner? = null
    var root: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        Toast.makeText(activity, HealthLog.Companion.ID, Toast.LENGTH_SHORT).show()
        root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        setUpRecyclerView()
        setUpSpinner()

        // search edit text
        searchEditText = root.findViewById<EditText?>(R.id.dashboard_searchBox_editText)
        searchContainer =
            root.findViewById<RelativeLayout?>(R.id.dashboard_searchContainer_relativeLayout)
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
                    dashboardAdapter.filter(editable.toString().trim { it <= ' ' })
                }
            })


        // FAB
        val addPatient = root.findViewById<View?>(R.id.dashboard_add_fab) as FloatingActionButton?
        addPatient.setOnClickListener(
            View.OnClickListener { addNewPatient() })
        return root
    }

    fun addNewPatient() {
        val handler: NewPatientHandler<*> = NewPatientHandler<Any?>(context)
        handler.init()
    }

    fun setUpRecyclerView() {
        val patientViewHandler = PatientViewHandler(context, activity)
        dashboardAdapter = DashboardAdapter(
            context,
            ArrayList()
        ) { patient, v ->
            patientViewHandler.update(patient)
            patientViewHandler.init()
        }
        dashboardRecyclerView = root.findViewById(R.id.dashboard_showList_recycler)
        dashboardRecyclerView.setHasFixedSize(false)
        dashboardRecyclerView.setLayoutManager(LinearLayoutManager(context))
        dashboardRecyclerView.setAdapter(dashboardAdapter)
        dashboardRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    searchContainer.animate().alpha(0.1f).setDuration(500).start()
                } else {
                    searchContainer.animate().alpha(0.8f).setDuration(500).start()
                }
            }
        })
        dashboardViewModel = ViewModelProviders.of(requireActivity()).get(
            DashboardViewModel::class.java
        )
        dashboardViewModel.init(activity)
        dashboardViewModel
            .getPatientsList()
            .observe(
                viewLifecycleOwner,
                Observer { patientModels ->
                    for (p in patientModels) {
                        dashboardAdapter.add(p)
                    }
                })
    }

    fun setUpSpinner() {
        val sts = arrayOf<String?>("Active", "Cured", "Deceased", "All")
        spinner = root.findViewById<Spinner?>(R.id.dashboard_list_spinner)
        val adapter = ArrayAdapter(
            activity, R.layout.spinner_item, sts
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
                    spinner.setSelection(i)
                    dashboardAdapter.applyFilter(sts[i])
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_spinner, menu)
        inflater.inflate(R.menu.menu_logout, menu)
        val searchItem = menu.findItem(R.id.search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                dashboardAdapter.filter(newText.trim { it <= ' ' })
                return true
            }
        })
        val spinnerItem = menu.findItem(R.id.spinner)
        category = spinnerItem.actionView as Spinner
        val sts = arrayOf<String?>("Active", "Cured", "Deceased", "All")
        val adapter = ArrayAdapter(activity, R.layout.spinner_item, sts)
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(adapter)
        category.setOnItemSelectedListener(
            object : OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    i: Int,
                    l: Long
                ) {
                    category.setSelection(i)
                    dashboardAdapter.applyFilter(sts[i])
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            })
        super.onCreateOptionsMenu(menu, inflater)
    }
}