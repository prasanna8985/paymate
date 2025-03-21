package com.project.Paymate.presentationLayer.user

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.project.Paymate.R
import com.project.Paymate.databinding.DialogexchangerequestBinding
import com.project.Paymate.databinding.UserMainBinding
import com.project.Paymate.presentationLayer.MainActivity
import com.project.Paymate.responsiveLayer.RetrofitC
import com.project.Paymate.responsiveLayer.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserMainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: UserMainBinding
    private val fused by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }
    private var latLng: LatLng? = null
    private var arrayUser = ArrayList<User>()
    private val shared by lazy { getSharedPreferences("user", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.floatingActionButton.setOnClickListener {
            startActivity(Intent(this, SendRequestActivity::class.java).apply {
                putExtra("data", arrayUser)
            })
        }
        binding.logout.setOnClickListener {
            dialog()
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Request SMS permission at startup
        requestSmsPermission()
    }

    private fun dialog() {
        MaterialAlertDialogBuilder(this).apply {
            setTitle("Do you want to Logout ?")
            setPositiveButton("Yes") { p, _ ->
                getSharedPreferences("user", MODE_PRIVATE).edit().clear().apply()
                finishAffinity()
                startActivity(Intent(this@UserMainActivity, MainActivity::class.java))
                p.dismiss()
            }
            setNegativeButton("No") { p, _ ->
                p.dismiss()
            }
            show()
        }
    }

    private val request =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                runMyCurrentLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val array = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (!checkPermission()) {
            request.launch(array)
        } else {
            runMyCurrentLocation()
        }

        // Set marker click listener
        mMap.setOnMarkerClickListener { marker ->
            val user = marker.tag as? User
            user?.let {
                showExchangeRequestDialog(it)
                true // Consume the click event
            } ?: false // Let default behavior occur if no user is found
        }

        // Load users from API
        CoroutineScope(IO).launch {
            val response = try {
                RetrofitC.api.getLocation(condition = "getLocations")
            } catch (e: Exception) {
                null
            }
            response?.body()?.data?.let { users ->
                withContext(Main) {
                    setDistance(users)
                }
            }
        }
    }

    private fun setDistance(users: ArrayList<User>) {
        arrayUser.clear()
        latLng?.let { currentLocation ->
            val locationA = Location("current")
            locationA.latitude = currentLocation.latitude
            locationA.longitude = currentLocation.longitude

            users.forEach { user ->
                user.location?.split(",")?.let { coords ->
                    if (coords.size == 2) {
                        val locationB = Location("user")
                        locationB.latitude = coords[0].toDouble()
                        locationB.longitude = coords[1].toDouble()
                        val distance =
                            locationA.distanceTo(locationB) / 1000 // Distance in kilometers

                        if (distance < 50) {
                            val userLatLng = LatLng(coords[0].toDouble(), coords[1].toDouble())
                            val marker = mMap.addMarker(
                                MarkerOptions()
                                    .position(userLatLng)
                                    .title(user.name)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin2))
                            )
                            marker?.tag = user // Attach user object to marker
                            arrayUser.add(user)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun runMyCurrentLocation() {
        mMap.isMyLocationEnabled = true
        fused.lastLocation.addOnSuccessListener { location ->
            location?.let {
                latLng = LatLng(it.latitude, it.longitude)
                mMap.addMarker(
                    MarkerOptions()
                        .position(latLng!!)
                        .title("Current Location")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
                )
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng!!, 16f))
            }
        }
    }

    private fun showExchangeRequestDialog(user: User) {
        val dialog = Dialog(this)
        val dialogBinding = DialogexchangerequestBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Set title
        dialogBinding.tvDialogTitle.text = "Request Exchange with ${user.name}"

        // Populate spinners (uncommented and fixed)
        val currencies = arrayOf("USD", "EUR", "GBP", "JPY")
        dialogBinding.spinnerCurrencyFrom.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        dialogBinding.spinnerCurrencyTo.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)

        // Time picker setup
        dialogBinding.etMeetTime.setOnClickListener {
            val timePicker = TimePickerDialog(this, { _, hour, minute ->
                dialogBinding.etMeetTime.setText(String.format("%02d:%02d", hour, minute))
            }, 12, 0, true)
            timePicker.show()
        }

        dialogBinding.etPhoneNumber.setText("${shared.getString("mobile", "")}")

        // Toggle visibility based on radio selection
        dialogBinding.rgExchangeMethod.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_meet_in_person -> {
                    dialogBinding.tilMeetLocation.visibility = View.VISIBLE
                    dialogBinding.tilMeetTime.visibility = View.VISIBLE
                    dialogBinding.tilUpiId.visibility = View.GONE
                }

                R.id.rb_upi_payment -> {
                    dialogBinding.tilMeetLocation.visibility = View.GONE
                    dialogBinding.tilMeetTime.visibility = View.GONE
                    dialogBinding.tilUpiId.visibility = View.VISIBLE
                }
            }
        }

        // Cancel button
        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }

        // Submit button with SMS logic
        dialogBinding.btnSubmitRequest.setOnClickListener {
            val amount = dialogBinding.etAmount.text.toString().toDoubleOrNull()
            val phoneNumber = dialogBinding.etPhoneNumber.text.toString()
            val note = dialogBinding.etNote.text.toString()

            if (amount == null || amount <= 0) {
                dialogBinding.etAmount.error = "Enter valid amount"
                return@setOnClickListener
            }
            if (phoneNumber.isEmpty()) {
                dialogBinding.etPhoneNumber.error = "Enter phone number"
                return@setOnClickListener
            }

            var meetLocation: String? = null
            var meetTime: String? = null
            var upiId: String? = null
            var isUpiPayment = false

            when (dialogBinding.rgExchangeMethod.checkedRadioButtonId) {
                R.id.rb_meet_in_person -> {
                    meetLocation = dialogBinding.etMeetLocation.text.toString()
                    meetTime = dialogBinding.etMeetTime.text.toString()
                    if (meetLocation.isEmpty()) {
                        dialogBinding.etMeetLocation.error = "Enter meeting location"
                        return@setOnClickListener
                    }
                    if (meetTime.isEmpty()) {
                        dialogBinding.etMeetTime.error = "Select meeting time"
                        return@setOnClickListener
                    }
                }

                R.id.rb_upi_payment -> {
                    upiId = dialogBinding.etUpiId.text.toString()
                    if (upiId.isEmpty()) {
                        dialogBinding.etUpiId.error = "Enter UPI ID"
                        return@setOnClickListener
                    }
                    isUpiPayment = true
                }
            }

            CoroutineScope(IO).launch {
                RetrofitC.api.exchange(
                    requesterId = "${shared.getString("id","")}",
                    receiverId = "${user.id}",
                    amount = "$amount",
                    currencyFrom = "",
                    currencyTo = "",
                    status = "Pending",
                    timestamp = "${System.currentTimeMillis()}",
                    phoneNumber = "${shared.getString("mobile","")}",
                    note = note,
                    meetLocation = "$meetLocation",
                    meetTime = "$meetTime",
                    upiId = "$upiId",
                    isUpiPayment = "true"
                ).execute().body()?.let {
                    val checkerro = it.error!!
                    if (!checkerro) {
                        runOnUiThread {


                            // Construct SMS message
                            val smsMessage = StringBuilder()
                            smsMessage.append("Currency Exchange Request\n")
                            smsMessage.append("From: ${shared.getString("name", "")}\n")
                            smsMessage.append("Amount: $amount")
                            smsMessage.append("Phone: ${shared.getString("mobile", "")}\n")
                            if (isUpiPayment) {
                                smsMessage.append("Payment Method: UPI\n")
                                smsMessage.append("UPI ID: $upiId\n")
                            } else {
                                smsMessage.append("Payment Method: In-Person\n")
                                smsMessage.append("Location: $meetLocation\n")
                                smsMessage.append("Time: $meetTime\n")
                            }
                            if (!dialogBinding.etNote.text.isNullOrEmpty()) {
                                smsMessage.append("Note: ${dialogBinding.etNote.text}\n")
                            }

                            // Send SMS
                            sendSms(phoneNumber, smsMessage.toString())

                            Toast.makeText(
                                this@UserMainActivity,
                                "Request sent to $phoneNumber",
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                this@UserMainActivity,
                                "Failed to send request",
                                Toast.LENGTH_SHORT
                            ).show()

                        }

                    }
                }

            }

        }

        dialog.show()
    }

    private fun sendSms(phoneNumber: String, message: String) {
        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send SMS: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                SMS_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            SMS_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show()
                }
            }

            100 -> { // Location permission
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    runMyCurrentLocation()
                }
            }
        }
    }

    companion object {
        private const val SMS_PERMISSION_REQUEST_CODE = 100
    }
}