package com.example.reversegeocodingpoc.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.reversegeocodingpoc.BuildConfig
import com.example.reversegeocodingpoc.R
import com.example.reversegeocodingpoc.constants.AppConstants
import com.example.reversegeocodingpoc.databinding.ActivityPlotLocationBinding
import com.example.reversegeocodingpoc.utils.AppUtilities
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_plot_location.*
import java.util.*


class PlotLocationActivity : AppCompatActivity(), OnMapReadyCallback {
    
    private var mMap: GoogleMap? = null
    private lateinit var binding: ActivityPlotLocationBinding
    private lateinit var mapFragment: SupportMapFragment

    //default lat lng center of india
    private var latitudeHere = 20.593778
    private var longitudeHere = 78.962945
    private var settingCurrentLocation = true
    private var mapTypeNormal = true
    private var zoomInMap = 14.0f

    private var mLatitude: Double? = null
    private var mLongitude: Double? = null


    @SuppressLint("StaticFieldLeak")
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            location = locationResult.lastLocation!!
            onSuccess(location)
        }
    }
    lateinit var onSuccess: (location: Location) -> Unit
    lateinit var onError: () -> Unit
    lateinit var location: Location

    private fun init() {
        initiateLocationProvider()

        intentDataDetails()

        binding.ivMapType.setOnClickListener {
            changeMapStyle()
        }

        binding.ivBack.setOnClickListener {
            finish()
        }

        getAddressFromLatLong2()

        binding.ivEditLatlng.setOnClickListener {
            makeLatLngEditable(true)
        }
        allTextWatcher()

    }

    private fun allTextWatcher() {
        binding.etPlotLat.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(cs: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
                Log.d(AppConstants.TODO_IMPLEMENT, "TODO")
            }

            override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
                Log.d(AppConstants.TODO_IMPLEMENT, "TODO")
            }

            @RequiresApi(Build.VERSION_CODES.N)
            override fun afterTextChanged(arg0: Editable) {
                latLngTextValidation(arg0, binding.etPlotLat)
            }
        })
        binding.etPlotLng.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(cs: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
                Log.d(AppConstants.TODO_IMPLEMENT, "TODO")
            }

            override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
                Log.d(AppConstants.TODO_IMPLEMENT, "TODO")
            }

            @RequiresApi(Build.VERSION_CODES.N)
            override fun afterTextChanged(arg0: Editable) {
                latLngTextValidation(arg0, binding.etPlotLng)
            }
        })
    }

    private fun initiateLocationProvider() {
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = AppConstants.LOCATION_UPDATE_INTERVAL
            fastestInterval = AppConstants.LOCATION_FASTEST_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun intentDataDetails() {

        if (!intent.getStringExtra(AppConstants.EDIT_PLOT_LNG).isNullOrEmpty()) {

            if (!intent.getStringExtra(AppConstants.EDIT_PLOT_LAT).isNullOrEmpty()) {
                binding.etPlotLat.setText(intent.getStringExtra(AppConstants.EDIT_PLOT_LAT))
                mLatitude = intent.getStringExtra(AppConstants.EDIT_PLOT_LAT).toString().toDouble()
            }
            if (!intent.getStringExtra(AppConstants.EDIT_PLOT_LNG).isNullOrEmpty()) {
                binding.etPlotLng.setText(intent.getStringExtra(AppConstants.EDIT_PLOT_LNG))
                mLongitude = intent.getStringExtra(AppConstants.EDIT_PLOT_LNG).toString().toDouble()
            }
        }

        if (intent.extras?.getString(AppConstants.INTENT_VALUE)
                .equals(AppConstants.INTENT_LOCATION_PLOT)
        ) {
            makeLatLngEditable(true)
            binding.cvLatLng.visibility = View.VISIBLE
            binding.tvTitle.text = resources.getString(R.string.enter_latlng)
        } else {
            makeLatLngEditable(false)
            binding.cvLatLng.visibility = View.VISIBLE
        }
    }

    private fun changeMapStyle() {
        if (mapTypeNormal) {
            setSatelliteMapView()
        } else {
            setHybridMapView()
        }
    }

    private fun setHybridMapView() {
        mapTypeNormal = true
        if (mMap != null) {
            mMap!!.mapType = GoogleMap.MAP_TYPE_HYBRID
        }
        binding.ivMapType.setImageResource(R.drawable.map_normal)
    }

    private fun setSatelliteMapView() {
        mapTypeNormal = false
        if (mMap != null) {
            mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        }
        binding.ivMapType.setImageResource(R.drawable.map_satellite)
    }

    private fun checkLocationStatusAndGetLocation(activity: Activity) {
        when {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> LocationServices.getSettingsClient(
                activity
            ).checkLocationSettings(
                LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                    .setAlwaysShow(true).build()
            ).addOnCompleteListener { task ->
                try {
                    task.getResult(ApiException::class.java)
                    fusedLocationProviderClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                } catch (exception: ApiException) {
                    handleExceptionApiException(exception)
                }
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> activity.runOnUiThread {
                promptShowLocation()
            }
            else -> ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                7024
            )
        }
    }


    private fun handleExceptionApiException(exception: ApiException) {
        when (exception.statusCode) {
            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                try {
                    (exception as ResolvableApiException).startResolutionForResult(
                        this,
                        7025
                    )
                } catch (ex: Exception) {
                    promptShowLocation()
                }
            }
            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                promptShowLocation()
            }
        }
    }

    private fun onRequestPermissionsResult(
        activity: Activity,
        requestCode: Int,
        grantResults: IntArray
    ) {
        if (requestCode == 7024) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationStatusAndGetLocation(activity)
            } else {
                onError()
            }
        }
    }

    private fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int) {
        if (requestCode == 7025) {
            if (resultCode == Activity.RESULT_OK) {
                checkLocationStatusAndGetLocation(activity)
            } else {
                onError()
            }
        }
    }

    private fun promptShowLocation() {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Compulsory")
                .setMessage("To continue, allow the device to use location, witch uses Google's Location Service")  // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(
                    R.string.yes
                ) { dialog, _ ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    onError()
                    dialog.dismiss()
                    // Continue with delete operation
                } // A null listener allows the button to dismiss the dialog and take no further action.
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            DataBindingUtil.setContentView(
                this@PlotLocationActivity,
                R.layout.activity_plot_location
            )
        init()



        binding.cvSaveLocationEnable.setOnClickListener {
            if (tv_save_location_enable.text.toString() == resources.getString(R.string.pick_plot_location)
            ) {
                AppUtilities.hideSoftInputKeyboard(this@PlotLocationActivity)
                pickPlotValidLatLng()
                if (et_plot_lat.text.toString().isNotEmpty() &&
                    et_plot_lng.text.toString().isNotEmpty() &&
                    getAddressFromLatLong(
                        et_plot_lat.text.toString().toDouble(),
                        et_plot_lng.text.toString().toDouble()
                    )
                ) {
                    makeLatLngEditable(false)

                    settingCurrentLocation = true
                    setLatLngLocation(
                        LatLng(
                            et_plot_lat.text.toString().toDouble(),
                            et_plot_lng.text.toString().toDouble()
                        )
                    )
                }
                settingCurrentLocation = false
            } else {
                makeLatLngEditable(false)
                sendLocationIfCorrect()
            }
        }
    }

    private fun pickPlotValidLatLng() {
        when {
            binding.etPlotLat.text.toString().trim().isEmpty() -> {
                AppUtilities.showShortToast(
                    et_plot_lat,
                    resources.getString(R.string.please_enter_lat)
                )
                return
            }
            binding.etPlotLng.text.toString().trim().isEmpty() -> {
                AppUtilities.showShortToast(
                    et_plot_lat,
                    resources.getString(R.string.please_enter_lng)
                )
                return
            }
            (!AppUtilities.validLatLng(et_plot_lat.text.toString())) -> {
                AppUtilities.showShortToast(
                    et_plot_lat,
                    resources.getString(R.string.please_enter_valid_lat)
                )
                return
            }
            (!AppUtilities.validLatLng(et_plot_lng.text.toString())) -> {
                AppUtilities.showShortToast(
                    et_plot_lat,
                    resources.getString(R.string.please_enter_valid_lng)
                )
                return
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun latLngTextValidation(arg0: Editable?, editText: EditText) {
        try {
            val convertedArg0 =
                AppUtilities.convertingNumbersToEnglish(arg0.toString()).toString()
            var beforeDecimalLengthFormatted = 7
            if (convertedArg0.contains("-")) {
                beforeDecimalLengthFormatted += 1
            }
            if (convertedArg0.contains(".")) {
                containsDecimal(
                    convertedArg0,
                    editText,
                    beforeDecimalLengthFormatted
                )

            } else {
                if (convertedArg0.length > beforeDecimalLengthFormatted) {
                    editText.setText(
                        convertedArg0.substring(
                            0,
                            beforeDecimalLengthFormatted
                        )
                    )
                    editText.setSelection(editText.length())
                }
            }
        } catch (e: Exception) {
            Log.e("tag", "error->$e")
        }
    }

    private fun containsDecimal(
        convertedArg0: String,
        editText: EditText,
        beforeDecimalLengthFormatted: Int
    ) {
        val afterDecimalLength = 6
        val beforeDecimal =
            AppUtilities.splitFromDotAcresValidation(convertedArg0)[0]
        val afterDecimal =
            AppUtilities.splitFromDotAcresValidation(convertedArg0)[1]
        var afterDecimalFinal = afterDecimal
        var beforeDecimalFinal = beforeDecimal
        if (afterDecimal.length > afterDecimalLength) {
            afterDecimalFinal = afterDecimal.substring(0, afterDecimalLength)
            val setValue = "$beforeDecimalFinal.$afterDecimalFinal"
            editText.setText(setValue)
            editText.setSelection(editText.length())
        }
        if (beforeDecimal.length > beforeDecimalLengthFormatted) {
            beforeDecimalFinal =
                beforeDecimal.substring(0, beforeDecimalLengthFormatted)
            val setValue = "$beforeDecimalFinal.$afterDecimalFinal"
            editText.setText(setValue)
            editText.setSelection(editText.length())
        }
    }

    private fun sendLocationIfCorrect() {
        if (et_plot_lat.text.toString().isNotEmpty() && et_plot_lng.text.toString()
                .isNotEmpty()
        ) {
            if (getAddressFromLatLong(
                    et_plot_lat.text.toString().toDouble(),
                    et_plot_lng.text.toString().toDouble()
                )
                && et_plot_lat.text.toString().isNotEmpty() && et_plot_lng.text.toString()
                    .isNotEmpty()
            ) {
                val resultIntent = Intent()
                resultIntent.putExtra(AppConstants.EDIT_PLOT_LAT, et_plot_lat.text.toString())
                resultIntent.putExtra(AppConstants.EDIT_PLOT_LNG, et_plot_lng.text.toString())
                if (intent.hasExtra(AppConstants.LAUNCH_SELECTED_RESULT_CODE)) {
                    resultIntent.putExtra(
                        AppConstants.LAUNCH_SELECTED_RESULT_CODE,
                        intent.getIntExtra(AppConstants.LAUNCH_SELECTED_RESULT_CODE, -1)
                    )
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                AppUtilities.showShortToast(
                    binding.cvSaveLocationEnable.rootView,
                    resources.getString(R.string.not_valid_lat_lng)
                )
            }
        }
    }

    private fun setLatLngLocation(latLng: LatLng) {
        if (mMap != null) {
            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomInMap))
        }
    }


    private fun getAddressFromLatLong(lat: Double, lng: Double): Boolean {
        try {
            val geocoder = Geocoder(this@PlotLocationActivity, Locale.getDefault())
            val addresses: List<Address> = geocoder.getFromLocation(lat, lng, 1)
            return if (addresses.isNotEmpty()) {
                addresses[0].getAddressLine(0).toString()
                true
            } else {
                resources.getString(R.string.un_known)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        resources.getString(R.string.un_known)
        return true
    }

    private fun getAddressFromLatLong2(): Boolean {
        try {
            val geoCoder = Geocoder(this@PlotLocationActivity, Locale.getDefault())
            val addresses: List<Address> = geoCoder.getFromLocation(21.24, 81.66, 1)
            return if (addresses.isNotEmpty()) {
                addresses[0].getAddressLine(0).toString()
                true
            } else {
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        AppUtilities.showShortToast(
            binding.cvSaveLocationEnable.rootView,
            getString(R.string.not_valid_lat_lng)
        )
        return false
    }

    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null

    private fun getCurrentLatLng() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            val locationResult = mFusedLocationProviderClient?.lastLocation

            locationResult?.addOnCompleteListener(this) {
                val mLastKnownLocation = it.result
                if (it.isSuccessful && mLastKnownLocation != null) {
                    // Set the map's camera position to the current location of the device.
                    try {
                        latitudeHere = mLastKnownLocation.latitude
                        longitudeHere = mLastKnownLocation.longitude
                        binding.ivEditLatlng.isEnabled = true
                        setLatLngLocation(LatLng(latitudeHere, longitudeHere))
                    } catch (e: java.lang.Exception) {
                        setLatLngLocation(LatLng(latitudeHere, longitudeHere))
                    }
                } else {
                    setLatLngLocation(LatLng(latitudeHere, longitudeHere))
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun makeLatLngEditable(value: Boolean) {
        binding.etPlotLat.isEnabled = value
        binding.etPlotLng.isEnabled = value
        if (mMap != null) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            mMap?.isMyLocationEnabled = true
            mMap?.uiSettings?.isMyLocationButtonEnabled = true
            mMap?.setPadding(0, 400, 50, 0)
//            mapView = mapFragment.view as MapView
//            val myLocationButton: View = mapView.findViewWithTag("GoogleMapMyLocationButton")
//            val rlp = myLocationButton.layoutParams as RelativeLayout.LayoutParams
//            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
//            rlp.setMargins(0, 200, 30, 0)

            val userLocation = mMap?.myLocation
            if (userLocation != null) {
                Log.d("TAG", "Location direct latitude : " + userLocation.latitude)
                Log.d("TAG", "Location direct longitude : " + userLocation.longitude)
            }
        }
        if (value) {
            binding.ivPlaceholder.visibility = View.GONE
            if (mMap != null) {
                mMap!!.uiSettings.isScrollGesturesEnabled = false
            }
            zoomInMap = 5.0f
            tv_save_location_enable.text = resources.getString(R.string.pick_plot_location)
        } else {
            binding.ivPlaceholder.visibility = View.VISIBLE
            if (mMap != null) {
                mMap!!.uiSettings.isScrollGesturesEnabled = true
            }
            zoomInMap = 14.0f
            settingCurrentLocation = false
            tv_save_location_enable.text = resources.getString(R.string.save_next)
        }
    }

    private fun permissionsToBeAsked() {
        val perms = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        when {
            (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    ) -> {
                if (mLatitude != null && mLongitude != null) {
                    if (mLatitude != 0.0 && mLongitude != 0.0) {
                        et_plot_lat.setText(
                            mLatitude.toString()
                        )
                        et_plot_lng.setText(
                            mLongitude.toString()
                        )

                        makeLatLngEditable(false)

                        setLatLngLocation(
                            LatLng(
                                mLatitude!!,
                                mLongitude!!
                            )
                        )

                        settingCurrentLocation = false
                        latitudeHere = mLatitude!!
                        longitudeHere = mLongitude!!
                    }
                } else {
                    getCurrentLatLng()
                }
                // You can use the API that requires the permission.
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                AlertDialog.Builder(this)
                    .setTitle(resources.getString(R.string.compulsory))
                    .setMessage(resources.getString(R.string.request_permission_compulsory))  // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(
                        resources.getString(R.string.yes)
                    ) { dialog, _ ->
                        startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                            )
                        )
                        dialog.dismiss()
                        // Continue with delete operation
                    } // A null listener allows the button to dismiss the dialog and take no further action.
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                ActivityCompat.requestPermissions(this, perms, 1)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    permissionsToBeAsked()
                } else {
                    promptShowLocation()
                }
            }

            else -> {
                onRequestPermissionsResult(this, requestCode, grantResults)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AppConstants.RESULT_ACTION_CUSTOM_UPDATE -> {
                    val intent1 = Intent()
                    setResult(Activity.RESULT_OK, intent1)
                    finish()
                }
                else -> onActivityResult(this, requestCode, resultCode)
            }
        } else {
            onBackPressed()
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.mapType = GoogleMap.MAP_TYPE_SATELLITE

        permissionsToBeAsked()

        mMap!!.setOnCameraIdleListener {
            mMap!!.uiSettings.isScrollGesturesEnabled = !binding.ivEditLatlng.isEnabled
            if (!settingCurrentLocation) {
                makeLatLngEditable(false)
                latitudeHere = mMap!!.cameraPosition.target.latitude
                longitudeHere = mMap!!.cameraPosition.target.longitude

                binding.etPlotLat.text = Editable.Factory.getInstance().newEditable(
                    "" + AppUtilities.decimalValidation(
                        latitudeHere.toString(),
                        7,
                        6
                    )
                )
                binding.etPlotLng.text = Editable.Factory.getInstance().newEditable(
                    "" + AppUtilities.decimalValidation(
                        longitudeHere.toString(),
                        7,
                        6
                    )
                )
            }
        }
    }
}