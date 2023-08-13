package com.quigglesproductions.secureimageviewer.ui.internallogin.login;

import static com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.authentication.pogo.internal.InternalAuthResponse;
import com.quigglesproductions.secureimageviewer.databinding.FragmentLoginBinding;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.managers.SecurityManager;
import com.quigglesproductions.secureimageviewer.ui.SecureFragment;
import com.quigglesproductions.secureimageviewer.ui.data.model.LoggedInUser;
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist.EnhancedFolderListFragmentDirections;
import com.quigglesproductions.secureimageviewer.ui.internallogin.InternalLoggedInUserView;
import com.quigglesproductions.secureimageviewer.ui.internallogin.InternalLoginResult;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class InternalLoginFragment extends SecureFragment {
    private InternalLoginViewModel loginViewModel;
    private FragmentLoginBinding binding;
    private LoggedInUser loggedInUser;

    private EditText usernameEditText, passwordEditText;
    FusedLocationProviderClient fusedLocationClient;

    public InternalLoginFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loginViewModel = new ViewModelProvider(this)
                .get(InternalLoginViewModel.class);

        usernameEditText = binding.username;
        passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;
        final ImageButton fingerPrintButton = binding.fingerprintButton;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        loginViewModel.getLoginFormState().observe(getViewLifecycleOwner(), new Observer<InternalLoginFormState>() {
            @Override
            public void onChanged(@Nullable InternalLoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), new Observer<InternalLoginResult>() {
            @Override
            public void onChanged(@Nullable InternalLoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                    requireActivity().setResult(Activity.RESULT_OK);
                    requireActivity().finish();
                }
                if (loginResult.getInProgress() != null) {
                    InternalAuthResponse inProgress = loginResult.getInProgress();
                    switch (inProgress.getAuthenticationState()) {
                        case REQUIRES_2FA:
                            NavDirections action = InternalLoginFragmentDirections.actionInternalLoginFragmentToInternalTwoFactorFragment(getGson().toJson(inProgress));
                            Navigation.findNavController(binding.getRoot()).navigate(action);
                            break;
                        case COMPLETE:

                    }
                }
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    login(usernameEditText.getText().toString(), passwordEditText.getText().toString());

                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                login(usernameEditText.getText().toString(), passwordEditText.getText().toString());
            }
        });
        loggedInUser = SecurityManager.getInstance().getLoggedInUser();

        if (loggedInUser != null) {
            fingerPrintButton.setVisibility(View.VISIBLE);
        } else {
            fingerPrintButton.setVisibility(View.INVISIBLE);
        }
        fingerPrintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupBiometrics();
            }
        });

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
    }

    private void login(String username, String password) {
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(getSecureActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    loginViewModel.login(username,password, location);
                }
            });
        }
        else{
            loginViewModel.login(username,password,null);
        }


    }

    private void updateUiWithUser(InternalLoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(getContext().getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        }
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(
                    getContext().getApplicationContext(),
                    errorString,
                    Toast.LENGTH_LONG).show();
        }
    }
    private void showLoginFailed(String errorString) {
        if (getContext() != null && getContext().getApplicationContext() != null && errorString != null) {
            Toast.makeText(
                    getContext().getApplicationContext(),
                    errorString,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void checkUserLoginStatus(){
        LoggedInUser user = SecurityManager.getInstance().getLoggedInUser();
        if(user != null){
            usernameEditText.setText(user.getDisplayName());
            usernameEditText.setEnabled(false);
            passwordEditText.setText("********");
            passwordEditText.setEnabled(false);
            setupBiometrics();
        }

    }

    private void setupBiometrics(){
        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                SecurityManager.getInstance().callBiometricLogin(requiresSecureActivity(), new SecurityManager.BiometricResultCallback() {
                    @Override
                    public void BiometricResultReceived(boolean success, Exception exception) {
                        if(success){
                            requireActivity().setResult(Activity.RESULT_OK);
                            requireActivity().finish();
                        }
                        else{
                            if(exception != null)
                                showLoginFailed(exception.getMessage());
                        }
                    }
                });
                //Intent intent = new Intent(requiresSecureActivity(), EnhancedMainMenuActivity.class);
                //SecurityManager.getInstance().setupBiometricsForResult(requiresSecureActivity(), intent);
            }
        });

    }


}