package vn.vietanhnguyen.didong2.activities.login_forgot;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


import vn.vietanhnguyen.didong2.R;

public class ForgotPassword extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button btnForGot;
    private ImageView btnBack;
    private EditText edtEmail;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);
        btnForGot = findViewById(R.id.btnGui);
        btnBack = findViewById(R.id.btnBack);
        edtEmail = findViewById(R.id.edtNhapEmail);
        mAuth = FirebaseAuth.getInstance();

        btnForGot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtEmail.getText().toString();
                if (email.isEmpty()){
                    Toast.makeText(ForgotPassword.this, "Email không được bỏ trống!", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPassword.this, "Chúng tôi đã gửi mail đến hộp thư của bạn để đổi mật khẩu!" + email, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ForgotPassword.this, Login.class);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(intent);
                                }
                            },2000);
                        } else {
                            Toast.makeText(ForgotPassword.this, "Không thể gửi mail. Hãy kiểm tra lại địa chỉ email.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
