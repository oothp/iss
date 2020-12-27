package com.mig.iss.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
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
import com.mig.iss.R
import com.mig.iss.databinding.ActivityMainBinding
import com.mig.iss.databinding.ViewPeopleBinding
import com.mig.iss.model.enums.Direction
import com.mig.iss.viewmodel.MainViewModel
import com.mig.iss.viewmodel.ViewModelFactory
import kotlin.math.atan2
import kotlin.math.hypot

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var binding: ActivityMainBinding
    private var map: GoogleMap? = null

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelFactory()
        ).get(MainViewModel::class.java)
    }
    private val adapter by lazy { PeopleAdapter() }
    private val peopleViewBinding by lazy { ViewPeopleBinding.inflate(getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater) }

    private val refreshHandler = Handler(Looper.getMainLooper())

    private val constraint2 = ConstraintSet()

    private lateinit var gestureScanner: GestureDetector

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        gestureScanner = GestureDetector(binding.root.context, gestureListener)
        val constraintSetPeople = ConstraintSet()

        // show dev label for dev builds.
        binding.isDebug = BuildConfig.DEBUG

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // region people
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        peopleViewBinding.peopleList.adapter = adapter
        peopleViewBinding.peopleList.layoutManager = linearLayoutManager
        peopleViewBinding.peopleList.hasFixedSize()
        binding.peopleContainer.addView(peopleViewBinding.root)
        // endregion

        // region gesture reg
//        binding.gestureView.setOnTouchListener { _, event -> gestureScanner.onTouchEvent(event) }
        // endregion

        // region observe dynamic values
        viewModel.items.bindAndFire { adapter.addItems(it) }
        viewModel.coordinates.bindAndFire { updateIssPosition() }

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

//                startChevronAnimation()
            }
        }
        // endregion

        // region map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        // endregion

        binding.executePendingBindings()
    }

    private fun startChevronAnimation(delay: Long = 0) {
        binding.gestureView.visibility = View.VISIBLE

        val dur: Long = 260

        binding.include.chevron1.animate().apply {
            interpolator = LinearInterpolator()
            duration = dur
            startDelay = delay
            alpha(1f)
            start()
        }

        binding.include.chevron2.animate().apply {
            interpolator = LinearInterpolator()
            duration = dur
            startDelay = dur + delay
            alpha(1f)
            setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                    super.onAnimationEnd(animation)
                    binding.include.chevron3.alpha = 1f
                }
            })
            start()
        }

        binding.include.chevron3.animate().apply {
            interpolator = LinearInterpolator()
            duration = dur.times(2)
            startDelay = dur.times(2).plus(delay)
            alpha(10f)
            translationY(-(binding.include.chevron3.height.toFloat() / 2))
            setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {

                    binding.include.chevron1.alpha = 0f
                    binding.include.chevron2.alpha = 0f
                    binding.include.chevron3.alpha = 0f

                    binding.include.chevron3.translationY = 0f

                    startChevronAnimation(150)
                }
            })
            start()
        }
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
        }, Const.INITIAL_REQUEST_DELAY)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
//        togglePeopleContainer()
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
        val anim = ViewAnimationUtils.createCircularReveal(
            binding.peopleContainer,
            cx.toInt(),
            cy.toInt(),
            0f,
            finalRadius
        )
        // make the view visible and start the animation
        binding.peopleContainer.visibility = View.VISIBLE
        anim.start()

        // === animate map
//        constraint2.clone(binding.constraintLayout)
//        constraint2.connect(
//            binding.map.id,
//            ConstraintSet.BOTTOM,
//            binding.peopleContainer.id,
//            ConstraintSet.TOP
//        )
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
        val anim = ViewAnimationUtils.createCircularReveal(
            binding.peopleContainer,
            cx,
            cy,
            initialRadius,
            0f
        )

        // make the view invisible when the animation is done
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                binding.peopleContainer.visibility = View.INVISIBLE
            }
        })
        anim.start()

        constraint2.clone(binding.constraintLayout)
        constraint2.connect(
            binding.map.id,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM
        )

        val transition = AutoTransition()
//        transition.duration = 1000
        TransitionManager.beginDelayedTransition(binding.constraintLayout, transition)
        constraint2.applyTo(binding.constraintLayout)

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

    // region gesture listener
    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
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

    var x = 0f
    var y = 0f
}
