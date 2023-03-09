package com.quigglesproductions.secureimageviewer.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavArgument;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavType;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.databinding.ActivityMainNavigationBinding;
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager;

public class EnhancedMainMenuActivity extends SecureActivity{
    ActivityMainNavigationBinding binding;
    Context mContext;
    EnhancedMainMenuViewModel viewModel;
    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainNavigationBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(EnhancedMainMenuViewModel.class);
        viewModel.getIsOnline().observe(this,this::setOnlineEnabled);
        setContentView(binding.getRoot());
        mContext = this;
        setSupportActionBar(binding.appBarNavigation.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_enhancedMainMenuFragment, R.id.nav_enhancedFolderListFragment, R.id.nav_enhancedOfflineFolderListFragment,R.id.nav_settingsFragment)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_navigation);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        NavDestination onlineFolderListDestination = navController.getGraph().findNode(R.id.nav_enhancedFolderListFragment);
        onlineFolderListDestination.addArgument("state", new NavArgument.Builder()
                .setType(NavType.StringType)
                .setDefaultValue("online")
                .build());
        NavDestination offlineFolderListDestination = navController.getGraph().findNode(R.id.nav_enhancedOfflineFolderListFragment);
        offlineFolderListDestination.addArgument("state", new NavArgument.Builder()
                .setType(NavType.StringType)
                .setDefaultValue("offline")
                .build());
        viewModel.getIsOnline().setValue(ViewerConnectivityManager.getInstance().isConnected());

    }
    @Override
    public void onConnectionRestored() {
        super.onConnectionRestored();
        viewModel.getIsOnline().setValue(true);
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
}
