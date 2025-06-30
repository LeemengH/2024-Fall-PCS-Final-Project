package com.example.pcs;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.pcs.R;



public class ShopHome extends AppCompatActivity {

    EditText phonenumber,message;
    Button send;
    public static DatabaseHelper dbHandler;
    public String databaseTable = "smsUser";
    public String smTable = "smKeeper";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop_home);

        send = findViewById(R.id.button_send);
        phonenumber = findViewById(R.id.editText);
        message = findViewById(R.id.editText2);

        dbHandler = new DatabaseHelper(this);
        dbHandler.onCreate(dbHandler.getWritableDatabase());

        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String number = phonenumber.getText().toString();
                String msg = message.getText().toString();
                phonenumber.setText("");
                message.setText("");

                try {
                    if (dbHandler.findCandidate(number, msg)){
                        Toast.makeText(getApplicationContext(), "Find someone to send your message !", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "No User can send the short message...", Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(), "You need to send the short message by your own...", Toast.LENGTH_LONG).show();
                    }
                }catch (Exception e)
                {
                    String[] logg = e.toString().split(":");
                    for (String l: logg) {
                        Toast.makeText(getApplicationContext(), l, Toast.LENGTH_LONG).show();
                    }
                }

            }
        });

    }
}