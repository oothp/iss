package com.mig.iss.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.mig.iss.BuildConfig
import com.mig.iss.Const
import com.mig.iss.R
import com.mig.iss.databinding.ActivityMainBinding
import com.mig.iss.databinding.ViewPeopleBinding
import com.mig.iss.viewmodel.MainViewModel
import com.mig.iss.viewmodel.ViewModelFactory

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var binding: ActivityMainBinding
    private var map: GoogleMap? = null

    private val viewModel by lazy { ViewModelProvider(this, ViewModelFactory()).get(MainViewModel::class.java) }

    private val adapter by lazy { PeopleAdapter() }
    private val refreshHandler = Handler(Looper.getMainLooper())

    private val peopleViewBinding by lazy { ViewPeopleBinding.inflate(getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // show dev label for dev builds.
        binding.isDebug = BuildConfig.DEBUG

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // region prepare people view inflation
//        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        val peopleViewBinding = ViewPeopleBinding.inflate(inflater)

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        peopleViewBinding.peopleList.adapter = adapter
        peopleViewBinding.peopleList.layoutManager = linearLayoutManager
        peopleViewBinding.peopleList.hasFixedSize()
        // endregion

        // region observe dynamic values
        viewModel.items.bindAndFire { adapter.addItems(it) }
        viewModel.coordinates.bindAndFire { updateIssPosition() }

//        viewModel.showPeople.bindAndFire {
////        viewModel.progressPeople.bindAndFire {
////            binding.handle.isClickable = !it
//            when (it) {
//                true -> {
//                    if (binding.peopleContainer.childCount == 0)
////                        binding.peopleContainer.addView(
////                            peopleViewBinding.root, 0, ViewGroup.LayoutParams(
////                                ViewGroup.LayoutParams.MATCH_PARENT,
////                                ViewGroup.LayoutParams.WRAP_CONTENT
////                            )
////                        )
//                        binding.peopleContainer.addView(peopleViewBinding.root)
//                }
//                false -> binding.peopleContainer.removeView(peopleViewBinding.root)
//            }
//        }

//        viewModel.progress.bindAndFire {
//            binding.progress.visibility = when (it) {
//                true -> View.VISIBLE
//                false -> View.GONE
//            }
//        }
        // endregion

        // region map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        // endregion

//        viewModel.onHandleClicked = { togglePeople() }

        binding.executePendingBindings()
    }

    override fun onMapReady(gmap: GoogleMap) {
        map = gmap
        gmap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, Const.MAP_STYLE_NIGHT))
        gmap.setOnMarkerClickListener(this)
        gmap.uiSettings.isMapToolbarEnabled = false
        gmap.animateCamera(CameraUpdateFactory.zoomTo(4f))

        updateIssPosition()

        refreshHandler.postDelayed(object : Runnable {
            override fun run() {
                viewModel.refreshCurrentIssLocation()
                refreshHandler.postDelayed(this, Const.LOCATION_REFRESH_INTERVAL)
            }
        }, Const.INITIAL_REQUEST_DELAY) // get new coordinates
    }

    override fun onMarkerClick(marker: Marker): Boolean {
//        viewModel.togglePeople() // show hide ppl
        togglePeopleContainer()
        return false
    }

    private fun togglePeopleContainer() {
        when (viewModel.peopleVisible) {
            false -> {
                if (binding.peopleContainer.childCount == 0)
//                        binding.peopleContainer.addView(
//                            peopleViewBinding.root, 0, ViewGroup.LayoutParams(
//                                ViewGroup.LayoutParams.MATCH_PARENT,
//                                ViewGroup.LayoutParams.WRAP_CONTENT
//                            )
//                        )
                    binding.peopleContainer.addView(peopleViewBinding.root)
            }
            true -> binding.peopleContainer.removeView(peopleViewBinding.root)
        }
        viewModel.peopleVisible = !viewModel.peopleVisible
    }

    private fun updateIssPosition() {
        map?.let {
            it.clear()

            if (viewModel.coordinates.value.latitude != 0.0 && viewModel.coordinates.value.longitude != 0.0) {
                val markerOptions = MarkerOptions()
                    .position(viewModel.coordinates.value)
                    .icon(bitmapDescriptorFromVector(this))
                    .anchor(0.5f, 0.5f)

                it.addMarker(markerOptions)
                it.animateCamera(CameraUpdateFactory.newLatLng(viewModel.coordinates.value))
            }

            binding.progress.visibility = View.GONE
        }
    }

    private fun bitmapDescriptorFromVector(context: Context): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, R.drawable.vector_iss_light)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_info -> {
            Toast.makeText(binding.root.context, "INFO", Toast.LENGTH_SHORT).show()
            true
        }

        R.id.action_refresh -> {
            viewModel.getIssPeople()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

}
