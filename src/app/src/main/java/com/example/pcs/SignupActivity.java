package com.example.pcs;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Toast;
import android.widget.RadioGroup;
import android.widget.RadioButton;

import com.example.pcs.databinding.ActivitySignupBinding;

public class SignupActivity extends AppCompatActivity {

    ActivitySignupBinding binding;
    DatabaseHelper databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //TODO:  設置 RadioGroup 的事件監聽器 依照role 選擇註冊時需填寫的資料
        binding.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_provider) { // 如果選擇 "Message Provider"
                binding.signupPhone.setVisibility(View.VISIBLE); // 顯示 Phone 欄位
                binding.signupQuota.setVisibility(View.VISIBLE); // 顯示 Quota 欄位
            } else if (checkedId == R.id.radio_shop) { // 如果選擇 "Shop"
                binding.signupPhone.setVisibility(View.GONE); // 隱藏 Phone 欄位
                binding.signupQuota.setVisibility(View.GONE); // 隱藏 Quota 欄位
            }
        });

        databaseHelper = new DatabaseHelper(this);
        binding.signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = binding.signupUsername.getText().toString();
                String password = binding.signupPassword.getText().toString();
                String confirmPassword = binding.signupConfirm.getText().toString();

                // Get role from RadioGroup
                RadioGroup roleGroup = findViewById(R.id.radioGroup);
                int selectedId = roleGroup.getCheckedRadioButtonId();
                String role = "provider"; // 預設值，避免空指針
                if (selectedId == R.id.radio_provider) {
                    role = "provider";
                } else if (selectedId == R.id.radio_shop) {
                    role = "shop";
                }
                String phone = binding.signupPhone.getText().toString();
                String quotaString = binding.signupQuota.getText().toString();
                Integer quota = 0; // 預設為 null 以檢查空值情況
                if (!quotaString.isEmpty()) {
                    try {
                        quota = Integer.parseInt(quotaString);
                    } catch (NumberFormatException e) {
                        Toast.makeText(SignupActivity.this, "Quota must be a number", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if(username.equals("")||password.equals("")||confirmPassword.equals(""))
                    Toast.makeText(SignupActivity.this, "All fields are mandatory", Toast.LENGTH_SHORT).show();
                else{
                    if(password.equals(confirmPassword)){
                        Boolean checkUserUsername = databaseHelper.checkUsername(username);
                        if(checkUserUsername == false){

                            //TODO: insertData(username, password, role, phone quota)
                            databaseHelper.insertData(username, password, role, phone, quota);
                            Toast.makeText(SignupActivity.this, "Signup Successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                        }
                        else{
                            Toast.makeText(SignupActivity.this, "User already exists! Please login", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(SignupActivity.this, "Invalid Password!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        binding.loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}