package com.yutaka.alarmfy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class CountdownFragment extends Fragment {

    private EditText mEditTextInput;
    private TextView mTextViewCountdown;
    private Button mButtonSet;
    private Button mButtonStartPause;
    private Button mButtonReset;

    private CountDownTimer countDownTimer;
    private Boolean timerRunning = false;
    private long timeLeft; // milliseconds
    private long startTime;
    private long endTime;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_countdown, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEditTextInput = getView().findViewById(R.id.editTextInput);
        mTextViewCountdown = getView().findViewById(R.id.textViewCountdown);
        mButtonSet = getView().findViewById(R.id.buttonSet);
        mButtonStartPause = getView().findViewById(R.id.buttonStartPause);
        mButtonReset = getView().findViewById(R.id.buttonReset);

        mButtonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String checker = mEditTextInput.getText().toString();

                if(checker.equals("")) {
                    Toast.makeText(getActivity(), "Field can´t be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                long inputNumber = Long.parseLong(checker) * 60000;

                if(inputNumber == 0) {
                    Toast.makeText(getActivity(), "Enter a positive number!", Toast.LENGTH_SHORT).show();
                    return;
                }

                setTime(inputNumber);
                mEditTextInput.setText("");
            }
        });

        mButtonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timerRunning) {
                    pauseCountDown();
                } else {
                    startCountdown();
                }
            }
        });

        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetCountdown();
            }
        });
    }

    public void setTime(Long time) {
        startTime = time;
        resetCountdown();
    }

    public void startCountdown() {
        endTime = System.currentTimeMillis() + timeLeft; // tempo que passou + tempo que falta
        countDownTimer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                updateCountdownText();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                updateButtons();
            }
        }.start();

        timerRunning = true;
        updateButtons();
    }

    public void pauseCountDown() {
        countDownTimer.cancel();
        timerRunning = false;
        updateButtons();
    }

    public void resetCountdown() {
        timeLeft = startTime;
        updateCountdownText();
        updateButtons();
    }

    public void updateCountdownText() {
        int hours = (int) (timeLeft / 1000) / 3600; // ms / 1000 = s -> s/3600 = hr
        int minutes = (int) ((timeLeft / 1000) % 3600) / 60;
        int seconds = (int) (timeLeft / 1000) % 60;

        String timeLeftFormatted;

            if(hours > 0 ) {
                timeLeftFormatted = String.format(Locale.getDefault(),"%d:%02d:%02d", hours, minutes, seconds);
            } else {
                timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
            }

        mTextViewCountdown.setText(timeLeftFormatted);
    }

    public void updateButtons() {
        if(timerRunning) {
            mEditTextInput.setVisibility(View.INVISIBLE);
            mButtonSet.setVisibility(View.INVISIBLE);
            mButtonReset.setVisibility(View.INVISIBLE);
            mButtonStartPause.setText("Pause");
        } else {
            mEditTextInput.setVisibility(View.VISIBLE);
            mButtonSet.setVisibility(View.VISIBLE);
            mButtonStartPause.setText("Start");

            if(timeLeft < 1000) {
                mButtonStartPause.setVisibility(View.INVISIBLE);
            } else {
                mButtonStartPause.setVisibility(View.VISIBLE);
            }

            if(timeLeft < startTime) {
                mButtonReset.setVisibility(View.VISIBLE);
            } else {
                mButtonReset.setVisibility(View.INVISIBLE);
            }
        }
    }


    @Override
    public void onStop() { // EVITA RESETAR QUANDO MINIMIZA OU GIRA CELULAR
        super.onStop();

        SharedPreferences pref = this.getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putLong("startTime", startTime);
        editor.putLong("timeLeft", timeLeft);
        editor.putLong("endTime", endTime);
        editor.putBoolean("timerRunning", timerRunning);

        editor.apply();

        if(countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences pref = this.getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);

        startTime = pref.getLong("startTime", 600000);
        timeLeft = pref.getLong("timeLeft", startTime);
        timerRunning = pref.getBoolean("timerRunning", false);

        updateCountdownText();
        updateButtons();

        if(timerRunning) {
            endTime = pref.getLong("endTime", 0);
            timeLeft = endTime - System.currentTimeMillis(); // quantidade para acabar - tempo atual corrido

            if(timeLeft < 0) {
                timeLeft = 0;
                timerRunning = false;
                updateCountdownText();
                updateButtons();
            } else {
                startCountdown();
            }
        }
    }
}
