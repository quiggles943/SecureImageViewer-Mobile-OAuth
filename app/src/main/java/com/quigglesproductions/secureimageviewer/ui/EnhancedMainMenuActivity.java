package com.quigglesproductions.secureimageviewer.ui;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavArgument;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavType;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.navigation.NavigationView;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.aurora.authentication.AuroraUser;
import com.quigglesproductions.secureimageviewer.databinding.ActivityMainNavigationBinding;
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager;
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist.FolderListType;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EnhancedMainMenuActivity extends SecureActivity{
    ActivityMainNavigationBinding binding;
    Context mContext;
    EnhancedMainMenuViewModel viewModel;
    private AppBarConfiguration mAppBarConfiguration;
    SupportActionBarSetListener mActionBarSetListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainNavigationBinding.inflate(getLayoutInflater());
        getViewModel().getIsOnline().observe(this,this::setOnlineEnabled);
        setContentView(binding.getRoot());
        mContext = this;
        setSupportActionBar(binding.appBarNavigation.toolbar);
        final TextView usernameView = binding.navView.getHeaderView(0).findViewById(R.id.user_name);
        final TextView userEmailView = binding.navView.getHeaderView(0).findViewById(R.id.user_email);
        AuroraUser user = getAuroraAuthenticationManager().getUser();
        if(user != null) {
            usernameView.setText(user.userName);
            userEmailView.setText(user.emailAddress);
        }

        if(mActionBarSetListener != null)
            mActionBarSetListener.SupportActionBarSet();
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_overviewFragment, R.id.nav_enhancedFolderListFragment, R.id.nav_enhancedOfflineFolderListFragment,R.id.nav_settingsFragment)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_navigation);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        NavDestination onlineFolderListDestination = navController.getGraph().findNode(R.id.nav_enhancedFolderListFragment);
        onlineFolderListDestination.addArgument("state", new NavArgument.Builder()
                .setType(NavType.StringType)
                .setDefaultValue(FolderListType.ONLINE.name())
                .build());
        NavDestination offlineFolderListDestination = navController.getGraph().findNode(R.id.nav_enhancedOfflineFolderListFragment);
        offlineFolderListDestination.addArgument("state", new NavArgument.Builder()
                .setType(NavType.StringType)
                //.setDefaultValue("offline")
                .setDefaultValue(FolderListType.DOWNLOADED.name())
                .build());
        getViewModel().getIsOnline().setValue(ViewerConnectivityManager.getInstance().isConnected());
        getViewModel().getAppBarTitle().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(getSupportActionBar() != null)
                    if(!s.isEmpty())
                        getSupportActionBar().setTitle(s);
            }
        });
        getWindow().setNavigationBarColor(context.getColor(R.color.transparent));
        navigationView.getMenu().findItem(R.id.nav_logout).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                getAuroraAuthenticationManager().logout();
                return true;
            }
        });

        ImageView userIcon = binding.navView.getHeaderView(0).findViewById(R.id.user_icon);
        GlideUrl glideUrl = new GlideUrl("https://quigleyid.ddns.net/v2/oauth/userinfo/thumbnail", new LazyHeaders.Builder()
                //.addHeader("Authorization", "Bearer " + accessToken)
                .build());
        Glide.with(this).addDefaultRequestListener(new RequestListener<Object>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Object> target, boolean isFirstResource) {
                //Log.e("Image Load Fail", e.getMessage());
                //e.logRootCauses("Image Load Fail");
                return false;
            }

            @Override
            public boolean onResourceReady(Object resource, Object model, Target<Object> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        }).load(glideUrl).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.ic_launcher_foreground).fitCenter().into(userIcon);
    }
    private EnhancedMainMenuViewModel getViewModel(){
        if(viewModel == null){
            viewModel = new ViewModelProvider(this).get(EnhancedMainMenuViewModel.class);
        }
        return viewModel;
    }
    @Override
    public void onConnectionRestored() {
        super.onConnectionRestored();
        getViewModel().getIsOnline().setValue(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_navigation);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void setOnlineEnabled(boolean enabled){
        MenuItem onlineMenuItem = binding.navView.getMenu().getItem(1);
        onlineMenuItem.setEnabled(enabled);
    }

    public void setActionBarTitle(String title){
        getViewModel().getAppBarTitle().setValue(title);
    }

    public void overrideActionBarTitle(String title){
        if(getSupportActionBar() == null){

        }
        getSupportActionBar().setTitle(title);
        //setTitle(title);
    }

    public void overrideActionBarColorFromInt(@ColorInt int color){
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));
    }
    public void overrideActionBarColor(@ColorRes int color) {
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context,color)));
    }

    public void hideStatusBar(){
        this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
    public void showStatusBar(){
        this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
    public void registerActionBarSetListener(SupportActionBarSetListener listener){
        mActionBarSetListener = listener;
    }
}
