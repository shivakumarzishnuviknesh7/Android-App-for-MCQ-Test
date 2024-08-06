# QuizApp Android

QuizApp is an Android application designed to present users with a series of quiz questions. Users can select their answers either by tapping the on-screen options or using voice commands. Additionally, the app incorporates accelerometer sensors to navigate through the questions by shaking the device.

## Features

- **Multiple Choice Questions**: Users can answer questions by selecting one of three on-screen options.
- **Voice Recognition**: Users can answer questions using voice commands.
- **Sensor-Based Navigation**: Users can navigate through questions by tilting the device left or right.
- **User-Friendly Interface**: Intuitive and easy-to-use interface with responsive design.

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/quizapp-android.git
   ```
2. Open the project in Android Studio.
3. Build and run the project on an Android device or emulator.

## Usage

1. **Launch the App**: Open the QuizApp on your device.
2. **Enter Your Name**: On the welcome screen, enter your name to start the quiz.
3. **Answer Questions**:
    - **On-Screen Options**: Tap on one of the three options to answer a question.
    - **Voice Commands**: Press the microphone button and speak your answer (e.g., "Option one").
    - **Sensor Navigation**: Tilt the device left to go to the previous question and right to go to the next question.
4. **View Results**: After completing the quiz, the results screen will display your score.

## Code Overview

### `QuestionsActivity.java`

This is the main activity that handles the quiz logic and user interaction.

- **UI Initialization**: Initializes UI components such as `TextView` for displaying questions and `Button` for options.
- **Speech Recognition**: Implements voice recognition using `SpeechRecognizer` and `ActivityResultLauncher`.
- **Sensor Handling**: Detects device tilts to navigate through questions.
- **Answer Checking**: Validates the user's answer and provides feedback by changing button colors.

### `activity_questions.xml`

Defines the layout for the `QuestionsActivity` including the question text, answer buttons, and the microphone button.

### `strings.xml`

Contains all the string resources used in the app, such as questions, answers, and prompts.

## Adding New Questions

To add new questions to the quiz:

1. Update the `questions`, `answers`, and `options` arrays in `strings.xml` with the new data.
2. Ensure the arrays are in sync, meaning each question should have a corresponding answer and three options.

## Known Issues

- The voice recognition may not be accurate in noisy environments.
- The accelerometer sensitivity might need adjustment based on the device used.
