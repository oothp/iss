package com.mig.iss.view

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
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
import com.mig.iss.viewmodel.MainViewModel
import com.mig.iss.viewmodel.ViewModelFactory
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var binding: ActivityMainBinding

    private lateinit var map: GoogleMap
    private var mapMarker: Marker? = null

    private val viewModel by lazy { ViewModelProvider(this, ViewModelFactory()).get(MainViewModel::class.java) }
    private val pagerAdapter by lazy { ViewPagerAdapter(onSwipeDownCallback) }

    private lateinit var transformer: PageTransformer

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        // binding.lifecycleOwner = this

        transformer = PageTransformer(binding.root.context, binding.pager)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val constraintSetPanel = ConstraintSet()

        // show dev label for dev builds.
        binding.isDebug = BuildConfig.DEBUG

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.pager.adapter = pagerAdapter
        binding.pager.offscreenPageLimit = 1
        binding.pager.post { binding.pager.setCurrentItem(1, false) } // workaround for pager.currentItem = x

        binding.pager.setPageTransformer(transformer)

        binding.infoContainer.setOnTouchListener(peopleBoxTouchListener)

        // region observe dynamic values
        observeDynamicData(constraintSetPanel)
        // endregion
        binding.executePendingBindings()
    }

    private fun observeDynamicData(constraintSetPanel: ConstraintSet) {
        viewModel.humansOnIss.bindAndFire { pagerAdapter.addPeople(it) }
        viewModel.coordinates.bindAndFire { updateIssPosition(it) }

        viewModel.peopleLoaded.bindAndFire { loaded ->
            if (loaded) {
                binding.pager.visibility = View.INVISIBLE
                binding.infoContainer.visibility = View.INVISIBLE
                binding.pager.post { changeConstraints(binding.pager, constraintSetPanel) }
                binding.infoContainer.post { changeConstraints(binding.infoContainer, constraintSetPanel) }
                binding.infoContainer.visibility = View.VISIBLE

                pagerViewWidth = (binding.pager.getChildAt(0) as RecyclerView).layoutManager?.findViewByPosition(1)?.width?.toFloat() ?: 0f
            }
        }

        viewModel.issDataLoaded.bindAndFire { loaded ->
            if (loaded) {
                val geocoder = Geocoder(this, Locale.getDefault())
                viewModel.getUpdatedTerritory(geocoder)
            }
        }

        viewModel.territory.bindAndFire { pagerAdapter.updateCurrentIssLocation(it ?: "") }

        viewModel.humanCount.bindAndFire {
            //            peopleViewBinding.headCountLabel.text = String.format(getString(R.string.humans), it)
        }
    }

    private fun changeConstraints(view: ViewGroup, constraintSet: ConstraintSet) {
        constraintSet.clone(binding.constraintLayout)
        constraintSet.clear(view.id, ConstraintSet.BOTTOM)
        constraintSet.connect(view.id, ConstraintSet.TOP, binding.guideline.id, ConstraintSet.TOP)
        val transition = AutoTransition()
        transition.duration = 50
        TransitionManager.beginDelayedTransition(binding.constraintLayout, transition)
        constraintSet.applyTo(binding.constraintLayout)
    }

    private var dY = 0f
    private var downY = 0f

    private var pagerViewWidth by Delegates.notNull<Float>()

    private var distanceToGo: Float = 0f
    private var distanceWent: Float = 0f
    private var percent: Float = 0f
    private var distanceToResize: Float = 0f

    @SuppressLint("ClickableViewAccessibility")
    private val peopleBoxTouchListener = View.OnTouchListener { v, event ->
        moveViews(event, v)
        true
    }

    private fun moveViews(event: MotionEvent?, v: View) {

        val a = ResizeAnimation(v)
        a.duration = 100

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                dY = v.y - event.rawY
                downY = event.y

                if (binding.pager.visibility == View.GONE || binding.pager.visibility == View.INVISIBLE) {
                    binding.pager.visibility = View.VISIBLE
                }

                // peopleViewBinding.handle.animation?.cancel() // stop animation if any
                distanceToGo = binding.guideline.y - (binding.root.height - binding.infoContainer.height)
                distanceToResize = binding.root.width - pagerViewWidth
            }
            MotionEvent.ACTION_MOVE -> {

                binding.pager.requestDisallowInterceptTouchEvent(true)

                if (downY < event.y) { // down

                    binding.infoContainer.visibility = View.VISIBLE

                    if (v.y <= binding.guideline.y) {
                        v.animate().y(event.rawY + dY).setDuration(0).start()
                        binding.pager.animate().y(event.rawY + dY).setDuration(0).start()
                    }

                } else { // up
                    if (v.y >= binding.root.height - binding.infoContainer.height + resources.getDimension(R.dimen.margin_default)) {
                        v.animate().y(event.rawY + dY).setDuration(0).start()
                        binding.pager.animate().y(event.rawY + dY).setDuration(0).start()
                    }
                }

                // calculating view drag distance
                distanceWent = v.y - (binding.root.height - binding.infoContainer.height)
                percent = distanceWent.div(distanceToGo / 100)

                // using the above to change view width
                val resizeBy: Int = distanceToResize.roundToInt() - (percent.times(distanceToResize / 100)).roundToInt()
                v.layoutParams.width = binding.root.width - resizeBy
                v.requestLayout()

                // animate page preview offset
                val fullOffset = resources.getDimension(R.dimen.page_offset)
                val offset1: Float = fullOffset - percent.times(fullOffset / 50) // 100
                transformer.updatePagePreviewOffset(offset1)
            }
            MotionEvent.ACTION_UP -> {
                // snap
                val dragAreaBottom = binding.root.height - (binding.root.height - binding.guideline.y)
                val dragAreaTop = binding.root.height - v.height

                if (v.y >= dragAreaTop + ((dragAreaBottom - dragAreaTop) / 2)) { // snap down
                    v.animate().y(binding.guideline.y).setDuration(100).start()
                    binding.pager.animate().y(binding.guideline.y).setDuration(100).start()
                    map.setPadding(0, binding.toolbar.height, 0, binding.constraintLayout.height - binding.guideline.y.toInt())

                    // snap view back to full width
                    a.setParams(v.layoutParams.width, binding.root.width)
                    v.startAnimation(a)

                    // snap viewpager pages out of view
                    Handler(Looper.getMainLooper()).postDelayed({ transformer.hidePagePreview() }, 100)
//                    binding.infoContainer.visibility = View.VISIBLE
                    binding.pager.requestDisallowInterceptTouchEvent(true)

                } else {
                    v.animate().y(binding.root.height - binding.infoContainer.height.toFloat()).setDuration(100).start()
                    binding.pager.animate().y(binding.root.height - binding.infoContainer.height.toFloat()).setDuration(100).start()
                    map.setPadding(0, binding.toolbar.height, 0, binding.infoContainer.height)

                    // snap view to pager width
                    a.setParams(v.layoutParams.width, pagerViewWidth.toInt())
                    v.startAnimation(a)

                    Handler(Looper.getMainLooper()).postDelayed({ transformer.hidePagePreview(false) }, 100) // show preview
                    //                    transformer.hidePagePreview(false) // show preview
                    binding.infoContainer.visibility = View.INVISIBLE

                    binding.pager.requestDisallowInterceptTouchEvent(false)
                }
            }
        }
    }

    private val onSwipeDownCallback = object : OnSwipeDownCallback { // takes touch event from viewpager pages.
        override fun onSwipeDown(e: MotionEvent) {
            moveViews(e, binding.infoContainer)
        }
    }

    override fun onMapReady(gmap: GoogleMap?) {
        map = gmap ?: return
        with(gmap) {

            setMapStyle(MapStyleOptions.loadRawResourceStyle(binding.root.context, Const.MAP_STYLE_AUBERGINE))
            setOnMarkerClickListener(this@MainActivity)
            setPadding(0, binding.toolbar.height, 0, binding.constraintLayout.height - binding.guideline.y.toInt())

            uiSettings.isMapToolbarEnabled = false
            uiSettings.isIndoorLevelPickerEnabled = true
            uiSettings.isCompassEnabled = true
            uiSettings.isZoomGesturesEnabled = true
            uiSettings.isZoomControlsEnabled = true

            mapMarker = map.addMarker(MarkerOptions().position(LatLng(0.0, 0.0))
                                          .anchor(0.5f, 0.5f)
                                          .icon(bitmapDescriptorFromVector())
                                          .title(viewModel.humanCount.value))

            Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate({ viewModel.refreshIssData() }, 1000, LOCATION_REFRESH_INTERVAL, TimeUnit.MILLISECONDS)
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
}
