package com.mig.iss.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.mig.iss.BuildConfig
import com.mig.iss.Const
import com.mig.iss.Const.LOCATION_REFRESH_INTERVAL
import com.mig.iss.R
import com.mig.iss.databinding.ActivityMainBinding
import com.mig.iss.databinding.ViewPeopleBinding
import com.mig.iss.model.enums.Direction
import com.mig.iss.viewmodel.MainViewModel
import com.mig.iss.viewmodel.ViewModelFactory
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.hypot

//val Int.dp: Int
//    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
//val Int.px: Int
//    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var binding: ActivityMainBinding

    private lateinit var map: GoogleMap
    private var mapMarker: Marker? = null

    private val viewModel by lazy { ViewModelProvider(this, ViewModelFactory()).get(MainViewModel::class.java) }
    private val adapter by lazy { PeopleAdapter() }
    private val peopleViewBinding by lazy { ViewPeopleBinding.inflate(getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater) }

    private val constraint2 = ConstraintSet()

    private lateinit var gestureScanner: GestureDetector

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
//        binding.lifecycleOwner = this

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        gestureScanner = GestureDetector(binding.root.context, gestureListener)

        val constraintSetPeople = ConstraintSet()

        // show dev label for dev builds.
        binding.isDebug = BuildConfig.DEBUG

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // region people recyclerview setup
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        peopleViewBinding.peopleList.adapter = adapter
        peopleViewBinding.peopleList.layoutManager = linearLayoutManager
        peopleViewBinding.peopleList.suppressLayout(true)
        peopleViewBinding.peopleList.hasFixedSize()
        binding.peopleContainer.addView(peopleViewBinding.root)
        // endregion

        // region gesture reg
        //        binding.peopleContainer.setOnTouchListener { _, event -> gestureScanner.onTouchEvent(event) }
        binding.peopleContainer.setOnTouchListener(peopleBoxTouchListener)
        // endregion

        // region observe dynamic values
        viewModel.humansOnIss.bindAndFire { adapter.addItems(it) }
        viewModel.coordinates.bindAndFire { updateIssPosition(it) }

        viewModel.peopleLoaded.bindAndFire { loaded ->
            if (loaded) {
                // people list ready -
                binding.peopleContainer.visibility = View.INVISIBLE

                binding.peopleContainer.post {
                    constraintSetPeople.clone(binding.constraintLayout)
                    constraintSetPeople.clear(binding.peopleContainer.id, ConstraintSet.BOTTOM)
                    constraintSetPeople.connect(binding.peopleContainer.id, ConstraintSet.TOP, binding.guideline.id, ConstraintSet.TOP)
                    val transition = AutoTransition()
                    transition.duration = 50
                    TransitionManager.beginDelayedTransition(binding.constraintLayout, transition)
                    constraintSetPeople.applyTo(binding.constraintLayout)

                    binding.peopleContainer.visibility = View.VISIBLE
                    peopleViewBinding.handle.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse))
                }
            }
        }

        viewModel.issDataLoaded.bindAndFire { loaded ->
            if (loaded) {
                val geocoder = Geocoder(this, Locale.getDefault())
                viewModel.getUpdatedTerritory(geocoder)
            }
        }

        viewModel.territory.bindAndFire {
//            peopleViewBinding.territoryLabel.text = it?.let { territory ->
            Log.e("====>>", "territory: $it")
            peopleViewBinding.countryLabel.text = it
//                resources.getString(R.string.currently).plus(territory)
//            } ?: ""
        }

//        viewModel.country.bindAndFire { peopleViewBinding.countryLabel.text = it }
        viewModel.humanCount.bindAndFire { peopleViewBinding.headCountLabel.text = String.format(getString(R.string.humans), it) }
        // endregion

        binding.executePendingBindings()
    }

    private var dY = 0f
    private var downY = 0f

    @SuppressLint("ClickableViewAccessibility")
    private val peopleBoxTouchListener = View.OnTouchListener { v, event ->
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                dY = v.y - event.rawY
                downY = event.y

                peopleViewBinding.handle.animation?.cancel() // stop animation if any
            }
            MotionEvent.ACTION_MOVE -> {
                if (downY < event.y) { // down
                    if (v.y <= binding.guideline.y)
                        v.animate()
                                .y(event.rawY + dY)
                                .setDuration(0)
                                .start()

                } else { // up
                    if (v.y >= binding.root.height - binding.peopleContainer.height)
                        v.animate()
                                .y(event.rawY + dY)
                                .setDuration(0)
                                .start()
                }
            }
            MotionEvent.ACTION_UP -> {
                // snap
                if (v.y >= binding.root.height - v.height.div(2)) {
                    v.animate()
                            .y(binding.guideline.y)
                            .setDuration(100)
                            .start()
                    map.setPadding(0, binding.toolbar.height, 0, binding.constraintLayout.height - binding.guideline.y.toInt())

                } else {
                    v.animate()
                            .y(binding.root.height - binding.peopleContainer.height.toFloat())
                            .setDuration(100)
                            .start()
                    map.setPadding(0, binding.toolbar.height, 0, binding.peopleContainer.height)
                }
            }
        }
        true
    }

    override fun onMapReady(gmap: GoogleMap?) {
        map = gmap ?: return

        with(gmap) {

            setMapStyle(MapStyleOptions.loadRawResourceStyle(binding.root.context, Const.MAP_STYLE_AUBERGINE))
            setOnMarkerClickListener(this@MainActivity)
            setPadding(0, binding.toolbar.height, 0, binding.constraintLayout.height - binding.guideline.y.toInt())

            uiSettings.isMapToolbarEnabled = false
            uiSettings.isIndoorLevelPickerEnabled  = true
            uiSettings.isCompassEnabled = true
            uiSettings.isZoomGesturesEnabled  = true
            uiSettings.isZoomControlsEnabled = true

            mapMarker = map.addMarker(MarkerOptions()
                    .position(LatLng(0.0, 0.0))
                    .anchor(0.5f, 0.5f)
                    .icon(bitmapDescriptorFromVector())
                    .title(viewModel.humanCount.value))

            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
                viewModel.refreshIssData()
            }, 1000, LOCATION_REFRESH_INTERVAL, TimeUnit.MILLISECONDS)
        }
    }

    private fun checkReadyThen(function: () -> Unit) {
        if (!::map.isInitialized) {
            Toast.makeText(this, "map not ready", Toast.LENGTH_SHORT).show()
        } else {
            function()
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        // nothing yet
        return false
    }

    private fun togglePeopleContainer(swipeDirection: Direction?) {
        swipeDirection?.let {
            when (it) {
                Direction.UP -> {
//                    if (!viewModel.peopleVisible)
                    revealPeople()
//                    viewModel.peopleVisible = !viewModel.peopleVisible
                }
                Direction.DOWN -> {
//                    if (viewModel.peopleVisible)
                    hidePeople()
//                    viewModel.peopleVisible = !viewModel.peopleVisible
                }
            }
        }
    }

    private fun revealPeople() {
//        get the center for the clipping circle
        val cx = binding.peopleContainer.width / 2
        val cy = binding.root.height
//        val cx = x.toDouble()
//        val cy = y.toDouble()

        // get the final radius for the clipping circle
        val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

        // create the animator for this view (the start radius is zero)
        val anim = ViewAnimationUtils.createCircularReveal(binding.peopleContainer, cx, cy, 0f, finalRadius)
        // make the view visible and start the animation
        binding.peopleContainer.visibility = View.VISIBLE
        anim.start()

        // === animate map
//        constraint2.clone(binding.constraintLayout)
//        constraint2.connect(binding.map.id, ConstraintSet.BOTTOM, binding.peopleContainer.id, ConstraintSet.TOP )
//
//        val transition = AutoTransition()
////        transition.duration = 1000
//        TransitionManager.beginDelayedTransition(binding.constraintLayout, transition)
//        constraint2.applyTo(binding.constraintLayout)
        // =====
    }

    private fun hidePeople() {
        // get the center for the clipping circle
        val cx = binding.peopleContainer.width / 2
        val cy = binding.peopleContainer.height / 2

        // get the initial radius for the clipping circle
        val initialRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

        // create the animation (the final radius is zero)
        val anim = ViewAnimationUtils.createCircularReveal(binding.peopleContainer, cx, cy, initialRadius, 0f)

        // make the view invisible when the animation is done
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                binding.peopleContainer.visibility = View.INVISIBLE
            }
        })
        anim.start()

        constraint2.clone(binding.constraintLayout)
        constraint2.connect(binding.map.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

        val transition = AutoTransition()
//        transition.duration = 1000
        TransitionManager.beginDelayedTransition(binding.constraintLayout, transition)
        constraint2.applyTo(binding.constraintLayout)

    }

    private fun updateIssPosition(latlng: LatLng) {
        checkReadyThen {
            binding.progress.visibility = View.GONE
            mapMarker?.position = latlng
            map.animateCamera(CameraUpdateFactory.newLatLng(latlng))
        }
    }

    private fun bitmapDescriptorFromVector(): BitmapDescriptor? {
        return ContextCompat.getDrawable(binding.root.context, R.drawable.vector_iss_light)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
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

    // region gesture listener
    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            super.onFling(e1, e2, velocityX, velocityY)

            // https://stackoverflow.com/a/26387629
//            if (isSwipeUp(e1.x, e1.y, e2.x, e2.y)) togglePeopleContainer()
            togglePeopleContainer(getSwipeDirection(e1.x, e1.y, e2.x, e2.y))

            return false
        }
    }

    private fun getSwipeDirection(x1: Float, y1: Float, x2: Float, y2: Float): Direction? {
        val angle = getAngle(x1, y1, x2, y2)
        return when {
            angle >= 45f && angle < 135f -> Direction.UP
            angle >= 225f && angle < 315f -> Direction.DOWN
            else -> null
        }
//        if (inRange(angle, 45f, 135f)) { UP
//        } else if (inRange(angle, 0f, 45f) || inRange(angle, 315f, 360f)) { RIGHT
//        } else if (inRange(angle, 225f, 315f)) { DOWN
//        } else { LEFT }
    }

    /**
     *
     * Finds the angle between two points in the plane (x1,y1) and (x2, y2)
     * The angle is measured with 0/360 being the X-axis to the right, angles
     * increase counter clockwise.
     *
     * @param x1 the x position of the first point
     * @param y1 the y position of the first point
     * @param x2 the x position of the second point
     * @param y2 the y position of the second point
     * @return the angle between two points
     */
    private fun getAngle(x1: Float, y1: Float, x2: Float, y2: Float): Double {
        val rad = atan2((y1 - y2).toDouble(), (x2 - x1).toDouble()) + Math.PI
        return (rad * 180 / Math.PI + 180) % 360
    }
    // endregion
}
