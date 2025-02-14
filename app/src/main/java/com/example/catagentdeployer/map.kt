package com.example.catagentdeployer

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.catagentdeployer.databinding.ActivityMapBinding
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker

class map : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var marker: Marker? = null
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var binding: ActivityMapBinding

    private fun getLastLocation() {
        Log.d("MapsActivity", "getLastLocation() called.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getLastLocation()
            } else {
                showPermissionRationale { requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapClickListener { latLng ->
            addOrMoveSelectedPositionMarker(latLng)
        }
        // Add a marker in Sydney and move the camera
        val binalbagan = LatLng(10.193946, 122.859213)
        mMap.addMarker(
            MarkerOptions()
                .position(binalbagan)
                .title("Binalbagan")

        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(binalbagan))

        when {
            hasLocationPermission() -> getLastLocation()
            ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                showPermissionRationale {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
            else -> requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getBitmapDescriptorFromVector(@DrawableRes vectorDrawableResourceId: Int): BitmapDescriptor? {
        val drawable: Drawable? = ContextCompat.getDrawable(this, vectorDrawableResourceId)
        drawable?.let {
            val bitmap = Bitmap.createBitmap(
                it.intrinsicWidth,
                it.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            it.setBounds(0, 0, canvas.width, canvas.height)
            it.draw(canvas)
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }
        return null
    }

    annotation class DrawableRes

    private fun addMarkerAtLocation(
        location: LatLng,
        title: String,
        markerIcon: BitmapDescriptor? = null): Marker {
        return mMap.addMarker(
            MarkerOptions()
                .title(title)
                .position(location)
                .apply {
                    markerIcon?.let { icon(markerIcon) }
                }
        )!!
    }

    private fun addOrMoveSelectedPositionMarker(latLng: LatLng) {
        if (marker == null) {
            marker = addMarkerAtLocation(
                latLng, "Deploy here",
                getBitmapDescriptorFromVector(R.drawable.baseline_directions_car_24)
            )
        } else {
            marker?.apply {
                position = latLng
            }
        }
    }

    private fun hasLocationPermission() = ActivityCompat.checkSelfPermission(this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun showPermissionRationale(positiveAction: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Location permission")
            .setMessage("This app will not work without knowing your current location")
            .setPositiveButton(android.R.string.ok) { _, _ -> positiveAction() }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}


