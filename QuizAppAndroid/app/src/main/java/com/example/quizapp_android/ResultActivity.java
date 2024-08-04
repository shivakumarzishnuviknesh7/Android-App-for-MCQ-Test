package com.example.quizapp_android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {
    TextView tv, tv2, tv3;
    Button btnRestart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tv = findViewById(R.id.tvres);
        tv2 = findViewById(R.id.tvres2);
        tv3 = findViewById(R.id.tvres3);
        btnRestart = findViewById(R.id.btnRestart);

        StringBuffer sb = new StringBuffer();
        sb.append("Correct answers: ").append(QuestionsActivity.correct).append("\n");
        StringBuffer sb2 = new StringBuffer();
        sb2.append("Wrong Answers: ").append(QuestionsActivity.wrong).append("\n");
        StringBuffer sb3 = new StringBuffer();
        sb3.append("Final Score: ").append(QuestionsActivity.correct).append("\n");
        tv.setText(sb);
        tv2.setText(sb2);
        tv3.setText(sb3);

        QuestionsActivity.correct = 0;
        QuestionsActivity.wrong = 0;

        btnRestart.setOnClickListener(v -> {
            Intent in = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(in);
        });
    }

}