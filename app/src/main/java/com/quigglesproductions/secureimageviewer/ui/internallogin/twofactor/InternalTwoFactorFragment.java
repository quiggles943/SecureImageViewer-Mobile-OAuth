package com.quigglesproductions.secureimageviewer.ui.internallogin.twofactor;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.authentication.pogo.internal.InternalAuthResponse;
import com.quigglesproductions.secureimageviewer.databinding.FragmentInternalTwoFactorBinding;
import com.quigglesproductions.secureimageviewer.ui.SecureFragment;
import com.quigglesproductions.secureimageviewer.ui.data.model.LoggedInUser;
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist.EnhancedFolderListFragmentArgs;
import com.quigglesproductions.secureimageviewer.ui.internallogin.InternalLoggedInUserView;
import com.quigglesproductions.secureimageviewer.ui.internallogin.InternalLoginResult;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class InternalTwoFactorFragment extends SecureFragment {
    private InternalTwoFactorViewModel twoFactorViewModel;
    private FragmentInternalTwoFactorBinding binding;
    private LoggedInUser loggedInUser;
    private String requestId;

    private EditText twoFactorEditText;

    public InternalTwoFactorFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentInternalTwoFactorBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        twoFactorViewModel = new ViewModelProvider(this)
                .get(InternalTwoFactorViewModel.class);

        twoFactorEditText = binding.twofactor;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;
        InternalAuthResponse authResponse = getGson().fromJson(InternalTwoFactorFragmentArgs.fromBundle(getArguments()).getAuthResponse(), InternalAuthResponse.class);
        requestId = authResponse.id;
        twoFactorViewModel.getTwoFactorFormState().observe(getViewLifecycleOwner(), new Observer<InternalTwoFactorFormState>() {
            @Override
            public void onChanged(@Nullable InternalTwoFactorFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getTwoFactorError() != null) {
                    binding.twofactorError.setText(loginFormState.getTwoFactorError());
                    twoFactorEditText.setText(null);
                    loadingProgressBar.setVisibility(View.GONE);
                }
            }
        });

        twoFactorViewModel.getLoginResult().observe(getViewLifecycleOwner(), new Observer<InternalTwoFactorResult>() {
            @Override
            public void onChanged(@Nullable InternalTwoFactorResult loginResult) {
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
                twoFactorViewModel.loginDataChanged(twoFactorEditText.getText().toString());
            }
        };
        twoFactorEditText.addTextChangedListener(afterTextChangedListener);
        twoFactorEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    /*loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());*/
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                twoFactorViewModel.verifyTwoFactorCode(requestId,twoFactorEditText.getText().toString());
                loadingProgressBar.setVisibility(View.VISIBLE);
                binding.twofactorError.setText(null);
                /*loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());*/
            }
        });
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
}