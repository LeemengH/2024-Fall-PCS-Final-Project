package com.example.pcs;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class ProviderHome extends AppCompatActivity {

    DatabaseHelper databaseHelper;
    private TextView textText;
    private static String user_id;
    private static long sm_id;
    public static int count = 0;
    public String userTable = "smsUser";
    public String smTable = "smKeeper";

    public static String caesarEncrypt(String input) {
        // Define the shift value
        int shift = 6;
        // Initialize a StringBuilder to store the encrypted result
        StringBuilder encrypted = new StringBuilder();

        // Iterate through each character in the input string
        for (char c : input.toCharArray()) {
            // Check if the character is a letter
            if (Character.isLetter(c)) {
                // Determine the base ('A' for uppercase, 'a' for lowercase)
                char base = Character.isUpperCase(c) ? 'A' : 'a';
                // Apply the shift and wrap around within the alphabet
                char encryptedChar = (char) ((c - base + shift) % 26 + base);
                encrypted.append(encryptedChar);
            } else {
                // For non-letter characters, append as-is
                encrypted.append(c);
            }
        }
        return encrypted.toString();
    }

    private final Runnable checkTask_and_send = new Runnable() {

        ArrayList<String> phone_Messages = new ArrayList<>();
        @Override
        public void run() {

            ArrayList<Integer> task_quota = databaseHelper.getTaskQuota(user_id);
            if (task_quota.get(0) != 0) {

                sm_id = task_quota.get(0);
                phone_Messages = databaseHelper.getPhoneMessage(sm_id);

                try {
                    //send
                    String number = phone_Messages.get(0);
                    String msg = "#pcs#" + caesarEncrypt(phone_Messages.get(1));
                    //check message
                    SmsManager smsManager = SmsManager.getDefault();
                    ActivityCompat.requestPermissions(ProviderHome.this, new String[]{android.Manifest.permission.SEND_SMS}, 1);
                    smsManager.sendTextMessage(number, null, msg, null, null);

                    //update quota
                    databaseHelper.updateContentValues(user_id, task_quota.get(1));
                    count += 1;
                } catch (Exception e) {
                    Log.d("Provider", e.toString());
                }
            }
        }
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_provider_home);
        databaseHelper = new DatabaseHelper(this);

        user_id = (String)getIntent().getExtras().getString("username");


        TextView T1 = findViewById(R.id.textView1);
        T1.setText("Hello "+ user_id + " !");

        TextView T2 = findViewById(R.id.textView2);
        T2.setText("Your quota: "+ databaseHelper.getQuota(user_id));

        TextView T3 = findViewById(R.id.textView3);
        T3.setText("You have sent "+ Integer.toString(count) + " short messages in this month!");


        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(checkTask_and_send, 0, 5, TimeUnit.SECONDS);

    }
}