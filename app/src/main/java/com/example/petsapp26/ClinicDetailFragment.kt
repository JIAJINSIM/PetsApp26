package com.example.petsapp26

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ClinicDetailFragment : Fragment() {

    private lateinit var veterinarianRecyclerView: RecyclerView
    private lateinit var vetStaffAdapter: VetStaffAdapter
    private var vetStaffList: ArrayList<VetStaff> = ArrayList()
    private val db = FirebaseFirestore.getInstance()

    companion object {
        private const val ARG_CLINIC_ID = "clinicId"

        fun newInstance(clinicId: String): ClinicDetailFragment {
            val fragment = ClinicDetailFragment()
            val args = Bundle()
            args.putString(ARG_CLINIC_ID, clinicId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_clinicdetail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val clinicId = arguments?.getString(ARG_CLINIC_ID) ?: return

        veterinarianRecyclerView = view.findViewById(R.id.staff_list_recycler_view)
        veterinarianRecyclerView.layoutManager = LinearLayoutManager(context)
        vetStaffAdapter = VetStaffAdapter(vetStaffList, requireContext())
        veterinarianRecyclerView.adapter = vetStaffAdapter

        fetchClinicDetails(clinicId)
        fetchVeterinarians(clinicId)
    }

    private fun fetchClinicDetails(clinicId: String) {
        db.collection("veterinaries").document(clinicId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val clinicName = document.getString("name") ?: ""
                    val location = document.toObject(Location::class.java) ?: Location()
                    val clinicLocation = location.address
                    view?.findViewById<TextView>(R.id.clinic_name)?.text = clinicName
                    view?.findViewById<TextView>(R.id.clinic_location)?.text = clinicLocation
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors here
            }
    }

    private fun fetchVeterinarians(clinicId: String) {
        db.collection("veterinaries").document(clinicId).collection("teamMembers").get()
            .addOnSuccessListener { result ->
                vetStaffList.clear()
                for (document in result) {
                    val vetStaff = document.toObject(VetStaff::class.java)
                    vetStaffList.add(vetStaff)
                }
                vetStaffAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle any errors here
            }
    }
}
