package com.example.pcs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.database.Cursor;
import android.net.Uri;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.Manifest;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.widget.Toast;
import android.content.pm.PackageManager;

import java.util.ArrayList;

import com.example.pcs.databinding.ActivityMessageBinding;



public class MessageActivity extends AppCompatActivity {

    ActivityMessageBinding binding;
    private static final int REQUEST_READ_SMS_PERMISSION = 1;

    private Button fetchAndEncryptSmsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //filteredSmsEditText = findViewById(R.id.filtered_sms_edit_text);
        //encryptedSmsEditText = findViewById(R.id.encrypted_sms_edit_text);
        fetchAndEncryptSmsButton = findViewById(R.id.fetch_and_encrypt_sms_button);

        // permission to text
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, REQUEST_READ_SMS_PERMISSION);
        }

        //onclick to encrypt
        fetchAndEncryptSmsButton.setOnClickListener(v -> fetchAndEncryptSms());

        //onclick to login/sign up
        binding.messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(MessageActivity.this, "Welcome to Login!", Toast.LENGTH_SHORT).show();
                Intent intent  = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchAndEncryptSms();
            } else {
                Toast.makeText(this, "no permission to read text", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // read text
    private void fetchAndEncryptSms() {
        Uri smsUri = Uri.parse("content://sms/inbox");
        String[] projection = new String[]{"body"};
        ArrayList<String> filteredMessages = new ArrayList<>();

        try (Cursor cursor = getContentResolver().query(smsUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                    if (body.startsWith("#pcs#")) {  // text code
                        filteredMessages.add(body);
                    }
                } while (cursor.moveToNext());

                if (!filteredMessages.isEmpty()) {
                    //String originalMessages = String.join("\n\n", filteredMessages);

                    //filteredSmsEditText.setText(originalMessages);
                    // 解密
                    ArrayList<String> encryptedMessages = caesarDecrypt(filteredMessages);

                    //encryptedSmsEditText.setText(encryptedMessages);
                    LinearLayout containerLayout = findViewById(R.id.dynamic_text_container); // 主容器
                    containerLayout.removeAllViews(); // 確保不會重複添加
                    for (String message : filteredMessages) {
                        TextView textView = new TextView(this);
                        // 設定 Layout 參數
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                170  // 高度固定為 100dp
                        );
                        params.setMargins(0, 30, 0, 0); // 上方間距 30dp
                        textView.setLayoutParams(params);

                        // 設定樣式
                        textView.setBackgroundResource(R.drawable.lavender_border); // 背景樣式
                        textView.setPadding(15, 15, 15, 15); // Padding 8dp
                        textView.setGravity(Gravity.TOP); // 文字靠上對齊
                        textView.setTextSize(22); // 文字大小 22sp
                        textView.setText(message); // 訊息內容

                        containerLayout.addView(textView); // 添加到容器
                    }
                    for (String message : encryptedMessages) {
                        TextView textView = new TextView(this);
                        // 設定 Layout 參數
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                170  // 高度固定為 100dp
                        );
                        params.setMargins(0, 30, 0, 0); // 上方間距 30dp
                        textView.setLayoutParams(params);

                        // 設定樣式
                        textView.setBackgroundResource(R.drawable.lavender_border); // 背景樣式
                        textView.setPadding(8, 8, 8, 8); // Padding 8dp
                        textView.setGravity(Gravity.TOP); // 文字靠上對齊
                        textView.setTextSize(22); // 文字大小 22sp
                        textView.setText(message); // 訊息內容

                        containerLayout.addView(textView); // 添加到容器
                    }

                } else {
                    Toast.makeText(this, "No matching messages found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No text", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(this, "error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    public static ArrayList<String> caesarDecrypt(ArrayList<String> messages) {
        ArrayList<String> decryptedMessages = new ArrayList<>();

        for (String message : messages){
            message = message.substring(5);
            // Define the shift value (must match the encryption shift)
            int shift = 6;
            // Initialize a StringBuilder to store the decrypted result
            StringBuilder decrypted = new StringBuilder();
            // Iterate through each character in the input string
            for (char c : message.toCharArray()) {
                // Check if the character is a letter
                if (Character.isLetter(c)) {
                    // Determine the base ('A' for uppercase, 'a' for lowercase)
                    char base = Character.isUpperCase(c) ? 'A' : 'a';
                    // Reverse the shift and wrap around within the alphabet
                    char decryptedChar = (char) ((c - base - shift + 26) % 26 + base);
                    decrypted.append(decryptedChar);
                } else {
                    // For non-letter characters, append as-is
                    decrypted.append(c);
                }
            }
            decryptedMessages.add(decrypted.toString());
        }
        return decryptedMessages;
    }
}