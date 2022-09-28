package com.example.reversegeocodingpoc

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.reversegeocodingpoc.activity.LocationPermissionActivity
import com.example.reversegeocodingpoc.activity.PlotLocationActivity
import com.example.reversegeocodingpoc.apis.ApiAdapter
import com.example.reversegeocodingpoc.constants.AppConstants
import com.example.reversegeocodingpoc.databinding.ActivityMainBinding
import com.example.reversegeocodingpoc.models.Feature
import com.example.reversegeocodingpoc.models.Properties
import com.example.reversegeocodingpoc.utils.AppUtilities
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback, CoroutineScope by MainScope() {
    private lateinit var binding: ActivityMainBinding
    private var mMap: GoogleMap? = null

    //default lat lng center of india
    private var latitudeHere = 20.593778
    private var longitudeHere = 78.962945
    private var settingCurrentLocation = true
    private var zoomInMap = 14.0f
    private var etPlotLatitude = ""
    private var etPlotLongitude = ""
    private var textString = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.etPlotLatLng.setOnClickListener {
            intentAddPlotLocation()
        }

        binding.tvGetReverseGeoLocation.setOnClickListener {
            if (binding.etPlotLatLng.toString()
                    .isNotEmpty() && binding.rbTypes.checkedRadioButtonId != -1
            ) {
                when (binding.rbTypes.checkedRadioButtonId) {
                    R.id.rb2 -> {
                        getAddressFromLatLong(etPlotLatitude.toDouble(), etPlotLongitude.toDouble())
                    }
                    R.id.rb3 -> {
                        getReverseGeoCodingData()
                    }
                    else -> {
                        getReverseGeoCodingViaPlaces()
                    }
                }

            } else {
                AppUtilities.showShortToast(binding.pbLoader, getString(R.string.no_lat_long_err))
            }
        }
    }

    private fun getAddressFromLatLong(lat: Double, lng: Double) {
        try {
            val finalAddress: String
            val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
            val addresses: List<Address> = geocoder.getFromLocation(lat, lng, 1)
            finalAddress = if (addresses.isNotEmpty()) {
                addresses[0].getAddressLine(0).toString()
            } else {
                resources.getString(R.string.un_known)
            }
            binding.tvAddress.text = finalAddress
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun intentAddPlotLocation() {
        val plotLocationIntent: Intent?
        plotLocationIntent = Intent(
            this@MainActivity,
            PlotLocationActivity::class.java
        )

        plotLocationIntent.putExtra(AppConstants.IS_ALLOW_CANCEL_INTENT_ACTION, true)

        if (intent.hasExtra(AppConstants.INTENT_MAX_PLOT_TO_ADD_ONE)) {
            plotLocationIntent.putExtra(
                AppConstants.INTENT_MAX_PLOT_TO_ADD_ONE, intent?.extras?.getInt(
                    AppConstants.INTENT_MAX_PLOT_TO_ADD_ONE
                )
            )
        }
        if (intent.hasExtra(AppConstants.EDIT_PLOT_LOCATION_INTENT)) {

            plotLocationIntent.putExtra(
                AppConstants.EDIT_PLOT_LOCATION_INTENT, intent?.extras?.getBoolean(
                    AppConstants.EDIT_PLOT_LOCATION_INTENT
                )
            )
        }
        plotLocationIntent.putExtra(
            AppConstants.LAUNCH_SELECTED_RESULT_CODE,
            AppConstants.RESULT_ACTION_CUSTOM_UPDATE
        )
        launcherAddPlotLocationResult.launch(plotLocationIntent)

    }

    private val launcherAddPlotLocationResult =
        (this as ComponentActivity).registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { itResult ->
            if (itResult.resultCode == Activity.RESULT_OK && itResult != null) {
                when (itResult.data?.getIntExtra(AppConstants.LAUNCH_SELECTED_RESULT_CODE, -1)) {
                    AppConstants.RESULT_ACTION_CUSTOM_UPDATE -> {
                        itResult.data?.let { intentData ->
                            val lat: String =
                                intentData.getStringExtra(AppConstants.EDIT_PLOT_LAT)!!
                            val lng: String =
                                intentData.getStringExtra(AppConstants.EDIT_PLOT_LNG)!!
                            etPlotLatitude = lat
                            etPlotLongitude = lng
                            textString = "${etPlotLatitude},${etPlotLongitude}"
                            binding.etPlotLatLng.setText(textString)
                            if (lat.isNotEmpty() &&
                                lng.isNotEmpty()
                            ) {
                                binding.tvAddReset.text = getString(R.string.reset)
                            } else {
                                binding.tvAddReset.text = getString(R.string.add)
                            }
                        }
                    }
                }
            }
        }

    private fun intentLocationPermission() {
        val intent1: Intent?
        intent1 = Intent(
            this@MainActivity,
            LocationPermissionActivity::class.java
        )

        intent1.putExtra(
            AppConstants.LAUNCH_SELECTED_RESULT_CODE,
            AppConstants.RESULT_ACTION_CUSTOM_UPDATE
        )
        launcherLocationPermissionResult.launch(intent1)
    }

    private val launcherLocationPermissionResult =
        (this as ComponentActivity).registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { itResult ->
            if (itResult.resultCode == Activity.RESULT_OK && itResult != null) {
                when (itResult.data?.getIntExtra(AppConstants.LAUNCH_SELECTED_RESULT_CODE, -1)) {
                    AppConstants.RESULT_ACTION_CUSTOM_UPDATE -> {
                        getCurrentLatLng()
                        mMap?.let { onMapReady(it) }
                    }
                }
            } else if (itResult.resultCode == Activity.RESULT_CANCELED && itResult != null) {
                finish()
            }
        }

    private fun setLatLngLocation(latLng: LatLng) {
        if (mMap != null) {
            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomInMap))
        }
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

                if (it.isSuccessful) {
                    // Set the map's camera position to the current location of the device.
                    val mLastKnownLocation = it.result

                    if (mLastKnownLocation != null) {
                        try {
                            latitudeHere = mLastKnownLocation.latitude
                            longitudeHere = mLastKnownLocation.longitude
                            setLatLngLocation(LatLng(latitudeHere, longitudeHere))
                        } catch (e: java.lang.Exception) {
                            setLatLngLocation(LatLng(latitudeHere, longitudeHere))
                        }
                    } else {
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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.mapType = GoogleMap.MAP_TYPE_SATELLITE

        permissionsToBeAsked()

        mMap!!.setOnCameraIdleListener {
            mMap!!.uiSettings.isScrollGesturesEnabled = true
            if (!settingCurrentLocation) {
                latitudeHere = mMap!!.cameraPosition.target.latitude
                longitudeHere = mMap!!.cameraPosition.target.longitude

            }
        }
    }

    private fun getReverseGeoCodingData() {
        launch(Dispatchers.Main) {
            // Try catch block to handle exceptions when calling the API.
            try {
                val response =
                    ApiAdapter.getAPIClient(ApiAdapter.geoBaseUrl).getReverseGeoCodingData(
                        binding.etPlotLatLng.text.toString(),
                        getString(R.string.google_maps_api_key)
                    )
                // Check if response was successful.
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    Log.d("TAG", "Response-> $data")
                    // Check for null
                    data.results?.let { result ->
                        // Load URL into the ImageView using Coil.
                        binding.tvAddress.text = result.toString()
                    }
                    data.error_message?.let { error ->
                        // Load URL into the ImageView using Coil.
                        binding.tvAddress.text = error
                    }
                } else {
                    Log.e("TAG", response.message())
                    // Show API error.
                    AppUtilities.showShortToast(
                        binding.pbLoader,
                        "Error Occurred: ${response.message()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("TAG", e.message.toString())
                // Show API error. This is the error raised by the client.
                AppUtilities.showShortToast(binding.pbLoader, "Error Occurred: ${e.message}")
            }
        }
    }

    private fun getReverseGeoCodingViaPlaces() {
        launch(Dispatchers.Main) {
            // Try catch block to handle exceptions when calling the API.
            try {
                val response =
                    ApiAdapter.getAPIClient(ApiAdapter.placesBaseUrl).getReverseGeoCodingFromPlaces(
                        etPlotLongitude,
                        etPlotLatitude
                    )
                // Check if response was successful.
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    Log.d("TAG", "Response-> ${data.features}")
                    // Check for null
                    data.features.let { result ->
                        // Load URL into the ImageView using Coil.
                        val address: String = getAddress(result)
                        binding.tvAddress.text = address
                    }
                } else {
                    Log.e("TAG", response.message())
                    // Show API error.
                    AppUtilities.showShortToast(
                        binding.pbLoader,
                        "Error Occurred: ${response.message()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("TAG", e.message.toString())
                // Show API error. This is the error raised by the client.
                AppUtilities.showShortToast(binding.pbLoader, "Error Occurred: ${e.message}")
            }
        }
    }

    private fun getAddress(featureList: List<Feature>): String {
        val address: ArrayList<String> = ArrayList()
        featureList.forEachIndexed { index, feature ->
            val properties: Properties = feature.properties
            val address1 =
                "Address-${index}" + "\n" + "Country : " + properties.country + "\n" + "City : " + properties.city + "\n" + "District : " + properties.district + "\n" + "SubDistrict : " + properties.subdistrict + "\n" + "Pincode : " + properties.postcode + "\n\n\n"
            address.add(address1)
        }

        var finalAddresses = ""
        address.forEach {
            finalAddresses = "$finalAddresses $it"
        }
        return finalAddresses
    }

    private fun permissionsToBeAsked() {
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
                getCurrentLatLng()
                // You can use the API that requires the permission.
            }

            else -> {
                intentLocationPermission()
            }
        }
    }

}