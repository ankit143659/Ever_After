package com.example.ever_after

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.math.*
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.SettingsClient
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex

class Setting : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var Gender: String
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val database = FirebaseDatabase.getInstance().getReference("Users")
    private val userLocations = mutableListOf<Triple<String, Pair<String, String>, GeoPoint>>()
    private var currentUserLocation: GeoPoint? = null
    private lateinit var btnToggleMapView: ImageView
    private var isSatelliteView = false

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val GPS_ENABLE_REQUEST_CODE = 1002

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        Configuration.getInstance().load(requireContext(), requireContext().getSharedPreferences("osmdroid", 0))

        mapView = view.findViewById(R.id.mapView)
        mapView.setMultiTouchControls(true)

        val btnToggleMapView = view.findViewById<ImageView>(R.id.btnToggleMapView)

        setNormalMapView()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupLocationRequest()
        requestLocationPermission()

        btnToggleMapView.setOnClickListener {
            if (isSatelliteView) {
                setNormalMapView()
            } else {
                setSatelliteMapView()
            }
            isSatelliteView = !isSatelliteView
        }
        return view
    }

    private fun setNormalMapView() {
        val normalTileSource = object : OnlineTileSourceBase(
            "OpenStreetMap",
            0, 18, 256, "",
            arrayOf("https://tile.openstreetmap.org/")
        ) {
            override fun getTileURLString(pMapTileIndex: Long): String {
                val zoom = MapTileIndex.getZoom(pMapTileIndex)
                val x = MapTileIndex.getX(pMapTileIndex)
                val y = MapTileIndex.getY(pMapTileIndex)
                return "$baseUrl$zoom/$x/$y.png"
            }
        }
        mapView.setTileSource(normalTileSource)
    }

    private fun setSatelliteMapView() {
        val appPackageName = requireContext().packageName
        Configuration.getInstance().userAgentValue = appPackageName

        // Define the custom Esri tile source
        val esriTileSource = object : OnlineTileSourceBase(
            "Esri World Imagery",
            0, 18, 256, "",
            arrayOf("https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/")
        ) {
            override fun getTileURLString(pMapTileIndex: Long): String {
                val zoom = MapTileIndex.getZoom(pMapTileIndex)
                val x = MapTileIndex.getX(pMapTileIndex)
                val y = MapTileIndex.getY(pMapTileIndex)
                return "$baseUrl$zoom/$y/$x"
            }
        }

        // Apply the custom tile source to the MapView
        mapView.setTileSource(esriTileSource)
    }

    private fun setupLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000
            fastestInterval = 2000
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    currentUserLocation = GeoPoint(latitude, longitude)
                    Log.d("LocationDebug", "Updated User Location: $latitude, $longitude")

                    saveUserLocationToFirebase(latitude, longitude)
                    fetchUserLocations()
                    updateMapView()
                    stopLocationUpdates()
                }
            }
        }
    }

    private fun updateMapView() {
        if (currentUserLocation != null) {
            mapView.controller.setZoom(20.0)
            mapView.controller.setCenter(currentUserLocation)
            mapView.invalidate()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            checkGPSAndRequestLocationUpdates()
        }
    }

    private fun checkGPSAndRequestLocationUpdates() {
        val sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val gpsPromptShown = sharedPreferences.getBoolean("gpsPromptShown", false)

        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.e("GPSCheck", "GPS is OFF, requesting user to enable GPS...")
            requestEnableGPS() // Request GPS enable from user
        } else {
            startLocationUpdates()
        }
    }

    private fun requestEnableGPS() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true) // Forces GPS prompt

        val settingsClient: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task = settingsClient.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            Log.d("GPSCheck", "GPS is already enabled.")
            startLocationUpdates()
        }.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(requireActivity(), GPS_ENABLE_REQUEST_CODE)
                } catch (sendEx: Exception) {
                    Log.e("GPSCheck", "Failed to show GPS enable dialog", sendEx)
                }
            } else {
                Log.e("GPSCheck", "GPS is not enabled, and cannot be resolved automatically.")
                fetchLastKnownLocationFromFirebase()
            }
        }
    }



    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkGPSAndRequestLocationUpdates()
            } else {
                Log.e("PermissionError", "Location permission denied. Fetching last known location from Firebase.")
                fetchLastKnownLocationFromFirebase()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GPS_ENABLE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("GPSCheck", "GPS enabled by the user.")
                startLocationUpdates()
            } else {
                Log.e("GPSCheck", "User denied GPS enable request.")
                fetchLastKnownLocationFromFirebase()
            }
        }
    }


    private fun saveUserLocationToFirebase(lat: Double, lng: Double) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userRef = database.child(currentUser.uid)
            val userData = mapOf(
                "latitude" to lat,
                "longitude" to lng
            )
            userRef.updateChildren(userData)
                .addOnSuccessListener {
                    Log.d("FirebaseUpdate", "User location saved successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseError", "Failed to save user location", e)
                }
        } else {
            Log.e("FirebaseError", "User is not logged in")
        }
    }

    private fun fetchLastKnownLocationFromFirebase() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            database.child(currentUser.uid).get().addOnSuccessListener { snapshot ->
                val lat = snapshot.child("latitude").getValue(Double::class.java) ?: 0.0
                val lng = snapshot.child("longitude").getValue(Double::class.java) ?: 0.0

                if (lat != 0.0 && lng != 0.0) {
                    currentUserLocation = GeoPoint(lat, lng)
                    Log.d("LocationDebug", "Fetched last known location: $lat, $lng")

                    // Now fetch other users' locations and update the map
                    fetchUserLocations()
                    updateMapView()
                } else {
                    Log.e("LocationDebug", "No last known location found in Firebase.")
                }
            }.addOnFailureListener {
                Log.e("FirebaseError", "Failed to fetch last known location", it)
            }
        }
    }


    private fun fetchUserLocations() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userLocations.clear()

                val currentUser = FirebaseAuth.getInstance().currentUser
                val currentUserId = currentUser?.uid  // Get the Current User ID

                if (currentUserLocation == null) {
                    Log.e("MapFragment", "Current user location is not available!")
                    return
                }

                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key  // Each user in Firebase has a unique UID
                    val name = userSnapshot.child("name").getValue(String::class.java) ?: "Unknown"
                    val gender = userSnapshot.child("Details").child("Gender").getValue(String::class.java) ?: "Unknown"  // Get Gender
                    val lat = userSnapshot.child("latitude").getValue(Double::class.java) ?: 0.0
                    val lng = userSnapshot.child("longitude").getValue(Double::class.java) ?: 0.0

                    if (lat != 0.0 && lng != 0.0) {
                        val location = GeoPoint(lat, lng)
                        val distance = calculateDistance(currentUserLocation!!, location)

                        if (distance <= 30.0) { // Only show users within 30 km
                            userLocations.add(Triple(userId!!, name to gender, location))  // Storing Gender with Name
                        }
                    }
                }

                addUserMarkers(currentUserId)  // Passing Current User ID to Markers
                updateMapView()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MapFragment", "Error fetching locations", error.toException())
            }
        })
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun addUserMarkers(currentUserId: String?) {
        if (currentUserLocation == null) {
            Log.e("MapFragment", "Current user location is not available!")
            return
        }

        mapView.overlays.clear()

        for ((userId, nameGender, location) in userLocations) {
            val (name, gender) = nameGender  // Extract Name and Gender

            val marker = Marker(mapView)
            marker.position = location
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

            // **Select icon based on gender**
            val drawableRes = when (gender) {
                "Man" -> R.drawable.man_icon
                "Woman" -> R.drawable.woman_icon  // Make sure you have this drawable
                else -> R.drawable.man_icon  // Fallback icon
            }

            val drawable = context?.let { ContextCompat.getDrawable(it, drawableRes) }
            if (drawable is BitmapDrawable) {
                val scaledDrawable = Bitmap.createScaledBitmap(
                    drawable.bitmap,
                    60, 60,
                    false
                )
                marker.icon = BitmapDrawable(resources, scaledDrawable)
            }

            val distance = calculateDistance(currentUserLocation!!, location)

            // **If this is the current user, show only the name**
            marker.title = if (currentUserId != null && userId == currentUserId) {
                name
            } else {
                "$name\n(${String.format("%.2f", distance)} km)"
            }
            marker.setOnMarkerClickListener { marker, mapView ->
                if (currentUserId != null && userId == currentUserId) {
                    // Current user ke marker pe click hone pe kuch nahi hoga
                    return@setOnMarkerClickListener true
                }

                // Sirf dusre users ka profile bottom sheet khulega
                val bottomSheet = ProfileBottomSheetFragment.newInstance(userId)
                bottomSheet.show(parentFragmentManager, "ProfileBottomSheetFragment")
                true
            }



            // **Text Overlay (To make the name clear)**
            val textOverlay = object : org.osmdroid.views.overlay.Overlay() {
                override fun draw(c: Canvas, osmv: MapView, shadow: Boolean) {
                    val projection = osmv.projection.toPixels(location, null)
                    val paint = Paint().apply {
                        color = Color.WHITE
                        textSize = 50f
                        typeface = Typeface.DEFAULT_BOLD
                        isAntiAlias = true
                        textAlign = Paint.Align.CENTER
                        setShadowLayer(10f, 0f, 0f, Color.BLACK)
                    }

                    val text = if (currentUserId != null && userId == currentUserId) {
                        name  // Only name
                    } else {
                        "$name (${String.format("%.2f", distance)} km)"  // Name + Distance
                    }

                    c.drawText(text, projection.x.toFloat(), projection.y.toFloat() - 60, paint)
                }
            }

            mapView.overlays.add(marker)
            mapView.overlays.add(textOverlay)
        }

        mapView.invalidate()
    }






    private fun calculateDistance(start: GeoPoint, end: GeoPoint): Double {
        val R = 6371.0
        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.toRadians(end.latitude)
        val lon2 = Math.toRadians(end.longitude)

        val dlat = lat2 - lat1
        val dlon = lon2 - lon1

        val a = sin(dlat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dlon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        checkGPSAndRequestLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDetach()
        stopLocationUpdates()
    }
}
