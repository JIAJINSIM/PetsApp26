package com.example.petsapp26

import android.content.Context
import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.navigation.NavigationView
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petsapp26.Record
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var toolbar: Toolbar // Define toolbar variable
    //private lateinit var recyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawerLayout = findViewById(R.id.drawer_layout)

        val userRole = getUserRole() // Retrieve the user's role
        updateNavigationView(userRole) // Update the navigation view based on the role


        toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        toggle.isDrawerIndicatorEnabled = false
        // Disable navigation drawer initially
        disableNavigationDrawer()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Login()).commit()
            navigationView.setCheckedItem(R.id.nav_login)
        }

        // Check if user is signed in (non-null) and update UI accordingly.

        // No user is signed in, handle the case appropriately
            // For example, navigate to the login screen

    }




    fun updateNavigationView(userRole: String?) {
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.menu.clear() // Clear existing items

        when (userRole) {
            "admin" -> {
                navigationView.inflateMenu(R.menu.nav_menu_admin) // load admin menu
            }
            "user" -> {
                navigationView.inflateMenu(R.menu.nav_menu_user) // load user menu
            }
            else -> {
                // Handle other roles or default case
                navigationView.inflateMenu(R.menu.nav_menu_admin) // default to user menu
            }
        }
    }

    private fun getUserRole(): String {
        val preferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return preferences.getString("userRole", "user") ?: "user" // Default to "user"
    }
    fun disableStaffListAdapter(){

    }



    fun disableNavigationDrawer() {
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        toggle.isDrawerIndicatorEnabled = false
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }


    fun enableNavigationDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        toggle.isDrawerIndicatorEnabled = true
        drawerLayout.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        toggle.syncState()
        // Enable the toggle
        //toggle.isDrawerIndicatorEnabled = true
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }



    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val contactsFragment = Contacts.newInstance("param1", "param2")

// Add this log before committing the transaction to add the ContactsFragment
        Log.d("MainActivity", "ContactsFragment added with tag: ContactsFragment, $contactsFragment" )
        when (item.itemId) {
            R.id.nav_contacts -> {
                val userRole = getUserRole()
                if (userRole == "admin") {


                    // For admin, prepare and show admin view
                    val contactsFragment = Contacts.newInstance("param1", "admin")
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, contactsFragment, "ContactsFragmentTag")
                        .commit()
                    //contactsFragment.fetchConversationsForAdmin()

                } else {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.fragment_container,
                            Contacts.newInstance("param1", "param2"),
                            "ContactsFragmentTag"
                        )
                        .commit()
                    //contactsFragment.fetchStaffMembers() // Call fetchStaffMembers function here
                    //contactsFragment.initRecyclerViewAdapter(emptyList()) // Initialize RecyclerView adapter here
                }
            // Handle other menu items...
            }
            R.id.nav_login -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Login()).commit()
            //R.id.nav_chat -> supportFragmentManager.beginTransaction()
                //.replace(R.id.fragment_container, Chat(),"CHAT_FRAGMENT_TAG").commit()
            R.id.nav_home -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            R.id.nav_search -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SearchFragment()).commit()
            R.id.nav_appts -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UserApptFragment()).commit()
            R.id.nav_share -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ShareFragment()).commit()
            R.id.nav_about -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AboutFragment()).commit()
            R.id.nav_profile -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Profile()).commit()

            // for staff
            R.id.nav_manage_appt -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ApptFragment()).commit()
            R.id.nav_records -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RecordFragment()).commit()
            R.id.nav_admin_home -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AdminHomeFragment()).commit()

            R.id.nav_logout -> {

                // Sign out from Firebase Authentication
                FirebaseAuth.getInstance().signOut()
                //val contactsFragment = supportFragmentManager.findFragmentByTag("ContactsFragmentTag") as? Contacts
                //contactsFragment?.clearAdapter()
                // Log to verify if clearStaffListAdapterData() is called
                //Log.d("MainActivity", "clearStaffListAdapterData() called: ${contactsFragment != null}")

                // Clear any saved user data (e.g., SharedPreferences)
                val sharedPreferences = getSharedPreferences("YourSharedPrefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().clear().apply()
                PreferencesUtil.clearUserId(this) // Assuming 'this' is a Context
                // Display logout message
                Toast.makeText(this, "Logged out!", Toast.LENGTH_SHORT).show()

                // Find the Chat fragment and clear its data
                val chatFragment = supportFragmentManager.findFragmentByTag("CHAT_FRAGMENT_TAG") as? Chat
                chatFragment?.clearChatData()


                // Disable navigation drawer
                disableNavigationDrawer()
                // Clear staff list adapter data




                // Replace the fragment with the login fragment
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, Login())
                    .commit()

                // Clear back stack to prevent going back to the secured fragments
                //supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                //removed the line above because after chat logout, the navigation ui and keyboard cannot type. 11_3_2024
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}