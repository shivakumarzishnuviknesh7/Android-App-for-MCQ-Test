package com.example.quizapp_android;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText nametext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the ActivityResultLauncher
        ActivityResultLauncher<Intent> writeSettingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleWriteSettingsResult
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                writeSettingsLauncher.launch(intent);
            }
        }

        nametext = findViewById(R.id.userName);
        Button startbutton = findViewById(R.id.buttonStart);
        Button aboutbutton = findViewById(R.id.buttonAbout);

        startbutton.setOnClickListener(v -> {
            String name = nametext.getText().toString();
            Intent intent = new Intent(MainActivity.this, QuestionsActivity.class);
            intent.putExtra("name", name);
            startActivity(intent);
        });

        aboutbutton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });
    }

    private void handleWriteSettingsResult(ActivityResult result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                Toast.makeText(this, "Permission denied to write system settings", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SensorService.getInstance(this).registerLightSensorListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SensorService.getInstance(this).unregisterLightSensorListener();
    }
}
