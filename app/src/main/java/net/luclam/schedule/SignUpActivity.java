package net.luclam.schedule;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    private static String TAG = "SignUpActivity";
    private Button btSignUp;
    private EditText etEmail, etPassword, etConfirmPassword;
    private ProgressBar pbSignUp;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btSignUp = findViewById(R.id.btSignUp);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        pbSignUp = findViewById(R.id.pbSignUp);

        btSignUp.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();

    }

    @Override
    public void onClick(View v) {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();
        if (!AppHelper.isValidEmail(email)) {
            etEmail.setError(getString(R.string.invalid_email));
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError(getString(R.string.password_cannot_be_empty));
            return;
        }
        if (password.length() < 6) {
            etPassword.setError(getString(R.string.password_least_six_characters));
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.password_incorrect));
            return;
        }
        if (!AppHelper.isNetworkConnected(SignUpActivity.this)) {
            AppHelper.showToastError(SignUpActivity.this, R.string.no_network_connection);
            return;
        } else {
            createAccount(email, password);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void createAccount(String email, String password) {
        pbSignUp.setVisibility(View.VISIBLE);
        btSignUp.setVisibility(View.INVISIBLE);
        password = AppHelper.hashingMd5(password);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        pbSignUp.setVisibility(View.INVISIBLE);
                        btSignUp.setVisibility(View.VISIBLE);
                        if (task.isSuccessful()) {
                            AppHelper.showToastSuccess(SignUpActivity.this, R.string.sign_up_success);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            }, Toast.LENGTH_SHORT);
                        } else {
                            Log.d(TAG, "Error create account: " + task.getException().getMessage());
                            AppHelper.showToastError(SignUpActivity.this, R.string.email_already_use_another_account);
                        }
                    }
                });
    }
}
