package com.example.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    EditText nom, motpass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        nom = findViewById(R.id.username);
        motpass = findViewById(R.id.password);
    }

    public void login(View view) {
        String n, p;
        n = nom.getText().toString().trim();
        p = motpass.getText().toString().trim();
        if (n.equals("admin") && p.equals("admin")) {
            startActivity(new Intent(this, MainActivity.class));
            this.finish();

        } else {
            Toast.makeText(this, "Nom ou Mot de pass incorrect!", Toast.LENGTH_LONG).show();
        }
    }
}