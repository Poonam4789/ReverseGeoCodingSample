package com.example.reversegeocodingpoc.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.reversegeocodingpoc.BuildConfig
import com.example.reversegeocodingpoc.R
import com.example.reversegeocodingpoc.constants.AppConstants
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.tasks.OnCompleteListener
import kotlinx.android.synthetic.main.activity_location_permission.*


class LocationPermissionActivity : AppCompatActivity(),
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    private var isAllowToCancelFlow = true


    private var permissionAllowedByUser = false
    var editPlotNewFoundationPruningDate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_permission)

        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this).build()
        mGoogleApiClient?.connect()

        isAllowToCancelFlow = intent.getBooleanExtra(
            AppConstants.IS_ALLOW_CANCEL_INTENT_ACTION,
            true
        )
        editPlotNewFoundationPruningDate = intent.getBooleanExtra(
            AppConstants.EDIT_PLOT_NEW_FOUNDATION_PRUNING_DATE,
            false
        )
        permissionsToBeAsked()
        Log.d("TAG", "intent : $intent")

        tv_allow.setOnClickListener {
            permissionAllowedByUser = true
            permissionsToBeAsked()
        }

        tv_cancel.setOnClickListener {

            if (intent.hasExtra(AppConstants.LAUNCH_SELECTED_RESULT_CODE)) {
                val intent1 = Intent()
                intent1.putExtra(
                    AppConstants.LAUNCH_SELECTED_RESULT_CODE,
                    intent.getIntExtra(AppConstants.LAUNCH_SELECTED_RESULT_CODE, -1)
                )
                setResult(Activity.RESULT_CANCELED, intent1)
                finish()
            } else {
                onBackPressed()
            }
        }
    }

    override fun onBackPressed() {
        if (isAllowToCancelFlow) {
            super.onBackPressed()
        } else {
            showDlgMandatoryDataSave(getString(R.string.location_required_msg))
        }
    }

    private fun showDlgMandatoryDataSave(message: String) {
        val dialog = Dialog(this, R.style.MyDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.logout_exit_dialog)
        dialog.setCancelable(true)
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        val btnNo = dialog.findViewById<TextView>(R.id.tv_no)
        val tvMsg = dialog.findViewById<TextView>(R.id.tv_description)
        val tvTitle = dialog.findViewById<TextView>(R.id.tv_title)
        val tvYes = dialog.findViewById<TextView>(R.id.tv_yes)
        //tv_no is invisible in this dialog
        tvTitle.visibility = View.GONE
        tvMsg.visibility = View.VISIBLE
        tvMsg.text = message
        btnNo.visibility = View.VISIBLE
        btnNo.text = getString(R.string.exit_anyway)
        tvYes.text = getString(R.string.continue_text)
        tvYes.setOnClickListener {
            dialog.dismiss()
        }
        btnNo.setOnClickListener {
            finishAffinity()
        }
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        if (permissionAllowedByUser) {
            permissionsToBeAsked()
        }
    }

    private fun continueLocationFlow() {
        if (intent.hasExtra(AppConstants.LAUNCH_SELECTED_RESULT_CODE)) {
            val intent1 = Intent()
            if (intent.hasExtra(AppConstants.LAUNCH_SELECTED_RESULT_CODE)) {
                intent1.putExtra(
                    AppConstants.LAUNCH_SELECTED_RESULT_CODE,
                    intent.getIntExtra(AppConstants.LAUNCH_SELECTED_RESULT_CODE, -1)
                )
            }
            setResult(Activity.RESULT_OK, intent1)
            finish()
        }
    }

    private fun permissionsToBeAsked() {
        val perms = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        when {
            (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED)
            -> {
                continueLocationFlow()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                AlertDialog.Builder(this)
                    .setTitle("Compulsory")
                    .setMessage("This app may not work correctly without the requested permissions.") // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(
                        R.string.yes
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
                 Log.d("TAG", "grantResults : $grantResults")
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    val intent = Intent("android.location.GPS_ENABLED_CHANGE")
                    intent.putExtra("enabled", true)
                    sendBroadcast(intent)

                } else {
                    AlertDialog.Builder(this)
                        .setTitle("Compulsory")
                        .setMessage("This app may not work correctly without the requested permissions.") // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(
                            R.string.yes
                        ) { dialog, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri: Uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
                            dialog.dismiss()
                            // Continue with delete operation
                        } // A null listener allows the button to dismiss the dialog and take no further action.
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()
                }
                // Explain to the user that the feature is unavailable because
                // the features requires a permission that the user has denied.
                // At the same time, respect the user's decision. Don't link to
                // system settings in an effort to convince the user to change
                // their decision.
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AppConstants.RESULT_ACTION_CUSTOM_UPDATE -> {
                    val intent1 = Intent()
                    if (intent.hasExtra(AppConstants.LAUNCH_SELECTED_RESULT_CODE)) {
                        intent1.putExtra(
                            AppConstants.LAUNCH_SELECTED_RESULT_CODE,
                            intent.getIntExtra(AppConstants.LAUNCH_SELECTED_RESULT_CODE, -1)
                        )
                    }
                    setResult(Activity.RESULT_OK, intent1)
                    finish()
                }
                REQUEST_LOCATION -> {
                    getCurrentLatlng()
                }
            }
        } else {
            isAllowToCancelFlow = true
            onBackPressed()
        }
    }

    var mFusedLocationProviderClient: FusedLocationProviderClient? = null

    private fun getCurrentLatlng() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            val locationResult = mFusedLocationProviderClient?.lastLocation

            locationResult?.addOnCompleteListener(this, OnCompleteListener {

                if (it.isSuccessful) {
                    // Set the map's camera position to the current location of the device.
                    val mLastKnownLocation = it.result
                    trackLastKnownLocation(mLastKnownLocation)

                } else {
                    Log.d("TAG", "it.isFail")
                }
            })
        } catch (e: SecurityException) {
            Log.e("TAG","error->"+e.message)
        }
    }

    private fun trackLastKnownLocation(mLastKnownLocation: Location) {
        if (mLastKnownLocation != null) {
            try {
                 Log.d("TAG", "mLastKnownLocation != null")
                 Log.d(
                    "TAG",
                    "$mLastKnownLocation.latitude : ${mLastKnownLocation.longitude}"
                )
            } catch (e: Exception) {
                 Log.d("TAG", "mLastKnownLocation Exception")
            }
        } else {
             Log.d("TAG", "mLastKnownLocation == null")
        }
    }


    var mLocationRequest: LocationRequest? = null
    var mGoogleApiClient: GoogleApiClient? = null
    var result: PendingResult<LocationSettingsResult>? = null
    val REQUEST_LOCATION = 199

    override fun onConnected(p0: Bundle?) {
        mLocationRequest = LocationRequest.create()
        mLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest?.interval = (30 * 1000).toLong()
        mLocationRequest?.fastestInterval = (5 * 1000).toLong()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequest!!)
        builder.setAlwaysShow(true)

        result =
            LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient!!, builder.build())
        result =
            LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient!!, builder.build())
        result!!.setResultCallback { result ->
            val status: Status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> {

                }
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        status.startResolutionForResult(
                            this@LocationPermissionActivity,
                            REQUEST_LOCATION
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    // Do something
                }
            }
        }
    }

    override fun onConnectionSuspended(p0: Int) {
         Log.d(AppConstants.TODO_IMPLEMENT, "TODO")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
         Log.d(AppConstants.TODO_IMPLEMENT, "TODO")
    }
}