package net.luclam.schedule;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btLogin;
    private TextView tvSignUp;
    private EditText etEmail, etPassword;
    private ProgressBar pbLogin;
    private FirebaseAuth auth;
    private static String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btLogin = findViewById(R.id.btLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        pbLogin = findViewById(R.id.pbLogin);

        btLogin.setOnClickListener(this);
        tvSignUp.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btLogin:
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                if (!AppHelper.isValidEmail(email)) {
                    etEmail.setError(getString(R.string.invalid_email));
                    return;
                }
                if (password.isEmpty()) {
                    etPassword.setError(getString(R.string.password_cannot_be_empty));
                    return;
                }
                if (!AppHelper.isNetworkConnected(LoginActivity.this)) {
                    AppHelper.showToastError(LoginActivity.this, R.string.no_network_connection);
                    return;
                } else {
                    signIn(email, password);
                }
                break;
            case R.id.tvSignUp:
                startActivity(new Intent(this, SignUpActivity.class));
                break;
        }
    }

    private void signIn(String email, String password) {
        pbLogin.setVisibility(View.VISIBLE);
        btLogin.setVisibility(View.INVISIBLE);
        tvSignUp.setEnabled(false);
        password = AppHelper.hashingMd5(password);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        pbLogin.setVisibility(View.INVISIBLE);
                        btLogin.setVisibility(View.VISIBLE);
                        tvSignUp.setEnabled(true);
                        if (task.isSuccessful()) {
                            AppHelper.showToastSuccess(LoginActivity.this, R.string.login_success);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                }
                            }, Toast.LENGTH_SHORT);
                        } else {
                            Log.d(TAG, "Error login: " + task.getException().getMessage());
                            AppHelper.showToastError(LoginActivity.this, R.string.invalid_password_or_account_not_exist);
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}
