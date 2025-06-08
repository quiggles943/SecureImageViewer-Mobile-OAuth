package com.quigglesproductions.secureimageviewer.ui

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.base.activity.SecureActivity
import com.quigglesproductions.secureimageviewer.aurora.authentication.device.ConnectivityState
import com.quigglesproductions.secureimageviewer.databinding.ActivityMainNavigationBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EnhancedMainMenuActivity : SecureActivity() {
    private var binding: ActivityMainNavigationBinding? = null
    var mContext: Context? = null
    private val viewModel by viewModels<EnhancedMainMenuViewModel>()
    private var mAppBarConfiguration: AppBarConfiguration? = null
    private var mActionBarSetListener: SupportActionBarSetListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainNavigationBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)
        mContext = this
        setSupportActionBar(binding!!.appBarNavigation.toolbar)
        val usernameView = binding!!.navView.getHeaderView(0).findViewById<TextView>(R.id.user_name)
        val userEmailView =
            binding!!.navView.getHeaderView(0).findViewById<TextView>(R.id.user_email)
        val user = getAuroraAuthenticationManager().user
        if (user != null) {
            usernameView.text = user.userName
            userEmailView.text = user.emailAddress
        }

        if (mActionBarSetListener != null) mActionBarSetListener!!.SupportActionBarSet()
        if (binding!!.drawerLayout is DrawerLayout) setupModalNavigationView()
        else setupStandardNavigationView()
        viewModel.appBarTitle?.observe(
            this
        ) { s: String ->
            if (supportActionBar != null) if (!s.isEmpty()) supportActionBar!!.title = s
        }
        window.navigationBarColor = this.getColor(R.color.transparent)

        val userIcon = binding!!.navView.getHeaderView(0).findViewById<ImageView>(R.id.user_icon)
        val glideUrl = GlideUrl(
            "https://quigleyid.ddns.net/v2/oauth/userinfo/thumbnail",
            LazyHeaders.Builder() //.addHeader("Authorization", "Bearer " + accessToken)
                .build()
        )
        Glide.with(this).addDefaultRequestListener(object : RequestListener<Any?> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Any?>,
                isFirstResource: Boolean
            ): Boolean {
                //Log.e("Image Load Fail", e.getMessage());
                //e.logRootCauses("Image Load Fail");
                return false
            }

            override fun onResourceReady(
                resource: Any,
                model: Any,
                target: Target<Any?>,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }
        }).load(glideUrl).diskCacheStrategy(DiskCacheStrategy.ALL)
            .error(R.drawable.ic_launcher_foreground).fitCenter().into(userIcon)
    }

    private fun setupModalNavigationView() {
        val drawer = binding!!.drawerLayout as DrawerLayout
        val navigationView = binding!!.navView
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = AppBarConfiguration.Builder(
            R.id.nav_overviewFragment,
            R.id.nav_enhancedFolderListFragment,
            R.id.nav_SearchFragment,
            R.id.nav_settingsFragment
        )
            .setOpenableLayout(drawer)
            .build()
        val navController = findNavController(this, R.id.nav_host_fragment_content_navigation)
        setupActionBarWithNavController(this, navController, mAppBarConfiguration!!)
        setupWithNavController(navigationView, navController)

        navigationView.menu.findItem(R.id.nav_logout).setOnMenuItemClickListener {
            getAuroraAuthenticationManager().logout()
            true
        }
    }

    private fun setupStandardNavigationView() {
        val navigationView = binding!!.navView
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = AppBarConfiguration.Builder(
            R.id.nav_overviewFragment,
            R.id.nav_enhancedFolderListFragment,
            R.id.nav_SearchFragment,
            R.id.nav_settingsFragment
        )
            .build()
        val navController = findNavController(this, R.id.nav_host_fragment_content_navigation)
        setupActionBarWithNavController(this, navController, mAppBarConfiguration!!)
        setupWithNavController(navigationView, navController)

        navigationView.menu.findItem(R.id.nav_logout).setOnMenuItemClickListener {
            getAuroraAuthenticationManager().logout()
            true
        }
    }

    override fun onConnectionRestored() {
        super.onConnectionRestored()
    }

    override fun onConnectionStateUpdated(connectivityState: ConnectivityState) {
        super.onConnectionStateUpdated(connectivityState)
        viewModel.connectivityState.postValue(connectivityState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(this, R.id.nav_host_fragment_content_navigation)
        return navigateUp(navController, mAppBarConfiguration!!)
                || super.onSupportNavigateUp()
    }

    fun setOnlineEnabled(enabled: Boolean) {
        val onlineMenuItem = binding!!.navView.menu.getItem(1)
        onlineMenuItem.setEnabled(enabled)
    }

    fun setActionBarTitle(title: String?) {
        viewModel.appBarTitle.value = title
    }

    fun overrideActionBarTitle(title: String?) {
        if (supportActionBar == null) {
        }
        supportActionBar!!.title = title
        //setTitle(title);
    }

    fun overrideActionBarColorFromInt(@ColorInt color: Int) {
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(color))
    }

    fun overrideActionBarColor(@ColorRes color: Int) {
        supportActionBar!!.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    this,
                    color
                )
            )
        )
    }

    fun hideNavigationDrawer() {
        binding!!.navView.visibility = View.GONE
    }

    fun showNavigationDrawer() {
        binding!!.navView.visibility = View.VISIBLE
    }

    fun hideStatusBar() {
        this.window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    fun showStatusBar() {
        this.window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    fun registerActionBarSetListener(listener: SupportActionBarSetListener?) {
        mActionBarSetListener = listener
    }
}
