package com.quigglesproductions.secureimageviewer.ui.internallogin;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.databinding.ActivityInternalLoginBinding;
import com.quigglesproductions.secureimageviewer.managers.SecurityManager;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.data.model.LoggedInUser;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class InternalLoginActivity extends SecureActivity {
    ActivityInternalLoginBinding binding;
    Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInternalLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mContext = this;
        LoggedInUser user = SecurityManager.getInstance().getLoggedInUser();
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        /*mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_overviewFragment, R.id.nav_enhancedFolderListFragment, R.id.nav_enhancedOfflineFolderListFragment,R.id.nav_settingsFragment)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_navigation);*/
        //NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        //NavigationUI.setupWithNavController(navigationView, navController);
        /*NavDestination onlineFolderListDestination = navController.getGraph().findNode(R.id.nav_enhancedFolderListFragment);
        onlineFolderListDestination.addArgument("state", new NavArgument.Builder()
                .setType(NavType.StringType)
                .setDefaultValue("online")
                .build());
        NavDestination offlineFolderListDestination = navController.getGraph().findNode(R.id.nav_enhancedOfflineFolderListFragment);
        offlineFolderListDestination.addArgument("state", new NavArgument.Builder()
                .setType(NavType.StringType)
                //.setDefaultValue("offline")
                .setDefaultValue("offline-room")
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
        getWindow().setNavigationBarColor(context.getColor(R.color.transparent));*/

    }
}
