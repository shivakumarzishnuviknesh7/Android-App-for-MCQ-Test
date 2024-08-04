package com.example.quizapp_android;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QuestionsActivity extends AppCompatActivity {

    private TextView questionText;
    private Button option1Button, option2Button, option3Button;
    private ImageButton voiceButton;

    private String[] questions;
    private String[] answers;
    private String[] options;
    private int flag = 0;
    public static int marks = 0, correct = 0, wrong = 0;
    private static final float SHAKE_THRESHOLD = 2.0f;
    private SpeechRecognizer speechRecognizer;
    private ActivityResultLauncher<Intent> voiceRecognitionLauncher;

    private Map<String, String> optionMapping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        // Initialize the ActivityResultLauncher for voice recognition
        voiceRecognitionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleVoiceRecognitionResult
        );

        // Initialize UI elements
        questionText = findViewById(R.id.questionText);
        TextView dispNameText = findViewById(R.id.DispName);
        option1Button = findViewById(R.id.option1Button);
        option2Button = findViewById(R.id.option2Button);
        option3Button = findViewById(R.id.option3Button);
        Button quitButton = findViewById(R.id.buttonQuit);
        voiceButton = findViewById(R.id.voiceButton);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        dispNameText.setText(name == null || name.trim().isEmpty() ? "Hello User" : "Hello " + name);


        questions = getResources().getStringArray(R.array.questions);
        answers = getResources().getStringArray(R.array.answers);
        options = getResources().getStringArray(R.array.options);

        updateQuestion();

        option1Button.setOnClickListener(v -> checkAnswer(option1Button.getText().toString(), option1Button, null));
        option2Button.setOnClickListener(v -> checkAnswer(option2Button.getText().toString(), option2Button, null));
        option3Button.setOnClickListener(v -> checkAnswer(option3Button.getText().toString(), option3Button, null));


        quitButton.setOnClickListener(v -> {
            Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent1);
        });

        voiceButton.setOnClickListener(v -> startVoiceRecognition());

        // Initialize the SpeechRecognizer
        initializeSpeechRecognizer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register accelerometer sensor listener when activity is resumed
        SensorService.getInstance(this).registerAccelerometerListener(this);
        SensorService.getInstance(this).registerLightSensorListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister accelerometer sensor listener when activity is paused
        SensorService.getInstance(this).unregisterAccelerometerListener();
        SensorService.getInstance(this).unregisterLightSensorListener();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }

    private void initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int error) {
                Toast.makeText(QuestionsActivity.this, "Speech recognition error: " + error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0);
                    checkAnswer(spokenText, null, voiceButton);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say the option (e.g., Option one)");
        voiceRecognitionLauncher.launch(intent);
    }

    private void handleVoiceRecognitionResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && !matches.isEmpty()) {
                String spokenText = matches.get(0);
                checkAnswer(spokenText, null, voiceButton);
            }
        }
    }

    public <K, V> K findKeyByValue(Map<K, V> map, V valueToFind, ImageButton selectedButton) {
        // Convert the value to find to a string and normalize its case
        String normalizedValueToFind = valueToFind != null ? valueToFind.toString().toLowerCase() : null;

        for (Map.Entry<K, V> entry : map.entrySet()) {
            // Convert the map's value to a string and normalize its case
            String normalizedMapValue = entry.getValue() != null ? entry.getValue().toString().toLowerCase() : null;

            assert normalizedMapValue != null;
            assert normalizedValueToFind != null;
            if (selectedButton == voiceButton) {
                if (isPartialMatch(normalizedValueToFind, normalizedMapValue)) {
                    return entry.getKey();
                }
            } else {
                if (normalizedMapValue.equals(normalizedValueToFind)) {
                    return entry.getKey();
                }
            }
        }

        return null; // Return null if the value is not found
    }


    private void checkAnswer(String answerText, Button selectedButton, ImageButton imageButton) {
        if (optionMapping == null) {
            initializeOptionMapping();
        }

        // Normalize and tokenize the spoken text
        String normalizedText = normalizeOptionText(answerText);
        Log.d("checkAnswer", "Normalized Text: " + normalizedText);

        // Get the correct answer for the current question
        String correctAnswer = answers[flag];
        boolean isCorrect = false;

        // Normalize and tokenize the correct answer
        String normalizedCorrectAnswer = normalizeOptionText(correctAnswer);

        // Check for exact or partial matches
        if (optionMapping.containsKey(normalizedText)) {
            String selectedOption = optionMapping.get(normalizedText);
            if (selectedOption != null && isPartialMatch(selectedOption, normalizedCorrectAnswer)) {
                isCorrect = true;
            }
        } else if (isPartialMatch(normalizedText, normalizedCorrectAnswer)) {
            isCorrect = true;
        }

        if (isCorrect) {
            correct++;
        } else {
            wrong++;
        }
        // Check if the selected button is valid
        if (imageButton != null && imageButton == voiceButton) {
            if (optionMapping.containsKey(normalizedText)) {
                String ansButton = findKeyByValue(optionMapping, correctAnswer, null);
                updateButtonColors(normalizedText, ansButton, isCorrect);

            } else {
                String selButton = findKeyByValue(optionMapping, normalizedText, imageButton);
                if (selButton != null) {
                    String ansButton = findKeyByValue(optionMapping, correctAnswer, imageButton);
                    updateButtonColors(selButton, ansButton, isCorrect);
                } else {
                    // Option not found, show message and stay on the same question
                    Toast.makeText(getApplicationContext(), "Option not found", Toast.LENGTH_SHORT).show();
                    updateOptionButtonColors(false, null); // Reset button colors
                    return; // Exit the method to stay on the same question
                }
            }

        } else {
            updateOptionButtonColors(isCorrect, selectedButton);
        }

        // Add a delay before updating the question
        new Handler().postDelayed(() -> {
            flag++;

            if (flag < questions.length) {
                updateQuestion();
            } else {
                marks = correct;
                Intent in = new Intent(getApplicationContext(), ResultActivity.class);
                startActivity(in);
            }
        }, 700);
    }

    private static boolean isPartialMatch(String text, String correctAnswer) {
        text = text.toLowerCase().replaceAll("\\s+", "").trim();
        correctAnswer = correctAnswer.toLowerCase().replaceAll("\\s+", "").trim();

        return correctAnswer.toLowerCase().contains(text);
    }


    private void updateButtonColors(String selButton, String ansButton, boolean isCorrect) {
        int color = isCorrect ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"); // Green for correct, Red for incorrect
        int defaultColor = Color.parseColor("#323b60"); // Default color
        int wrongColor = Color.parseColor("#4CAF50");
        option1Button.setBackgroundColor(defaultColor);
        option2Button.setBackgroundColor(defaultColor);
        option3Button.setBackgroundColor(defaultColor);
        Button selectedButton = getButtonFromMap(selButton);
        Button answerButton = getButtonFromMap(ansButton);

        if (isCorrect) {
            assert selectedButton != null;
            selectedButton.setBackgroundColor(color);
        } else {
            assert selectedButton != null;
            selectedButton.setBackgroundColor(color);
            assert answerButton != null;
            answerButton.setBackgroundColor(wrongColor);
        }

    }

    private void updateOptionButtonColors(boolean isCorrect, Button selectedButton) {
        int color = isCorrect ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"); // Green for correct, Red for incorrect
        int defaultColor = Color.parseColor("#323b60"); // Default color
        int wrongColor = Color.parseColor("#4CAF50");
        option1Button.setBackgroundColor(defaultColor);
        option2Button.setBackgroundColor(defaultColor);
        option3Button.setBackgroundColor(defaultColor);
        if (selectedButton != null) {
            if (isCorrect) {
                selectedButton.setBackgroundColor(color);
            } else {
                selectedButton.setBackgroundColor(color);
                Button correctAnsButton = getSelectedButton();
                if (correctAnsButton != null) {
                    correctAnsButton.setBackgroundColor(wrongColor);
                }
            }
        }
    }

    private Button getSelectedButton() {
        if (option1Button.getText().toString().equalsIgnoreCase(answers[flag])) {
            return option1Button;
        } else if (option2Button.getText().toString().equalsIgnoreCase(answers[flag])) {
            return option2Button;
        } else if (option3Button.getText().toString().equalsIgnoreCase(answers[flag])) {
            return option3Button;
        }
        return null;
    }

    private Button getButtonFromMap(String key) {
        switch (key) {
            case "option one":
                return option1Button;
            case "option two":
                return option2Button;
            case "option three":
                return option3Button;
        }
        return null;
    }

    private String normalizeOptionText(String spokenText) {
        spokenText = spokenText.toLowerCase();
        spokenText = spokenText.replaceAll("\\s+", " ");
        spokenText = spokenText.replaceAll("1", "one");
        spokenText = spokenText.replaceAll("2", "two");
        spokenText = spokenText.replaceAll("3", "three");
        return spokenText.trim();
    }

    private void initializeOptionMapping() {
        optionMapping = new HashMap<>();
        optionMapping.put("option one", options[0]);
        optionMapping.put("option two", options[1]);
        optionMapping.put("option three", options[2]);
    }

    private void updateQuestion() {
        updateOptionButtonColors(false, null);
        if (flag < questions.length) {
            questionText.setText(questions[flag]);
            option1Button.setText(options[flag * 3]);
            option2Button.setText(options[flag * 3 + 1]);
            option3Button.setText(options[flag * 3 + 2]);
            optionMapping = new HashMap<>();
            optionMapping.put("option one", options[flag * 3]);
            optionMapping.put("option two", options[flag * 3 + 1]);
            optionMapping.put("option three", options[flag * 3 + 2]);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.no_more_questions), Toast.LENGTH_SHORT).show();
        }
    }

    void onShakeDetected(float xAcc) {
        if (xAcc > SHAKE_THRESHOLD) {
            // Tilted to the right (next question)
            showNextQuestion();
        } else if (xAcc < -SHAKE_THRESHOLD) {
            // Tilted to the left (previous question)
            showPreviousQuestion();
        }
    }

    public void showNextQuestion() {
        if (flag < questions.length - 1) {
            flag++;
            updateQuestion();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.no_more_questions), Toast.LENGTH_SHORT).show();
        }
    }

    public void showPreviousQuestion() {
        if (flag > 0) {
            flag--;
            updateQuestion();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.first_question), Toast.LENGTH_SHORT).show();
        }
    }
}
