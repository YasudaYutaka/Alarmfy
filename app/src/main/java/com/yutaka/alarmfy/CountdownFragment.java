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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class CountdownFragment extends Fragment {

    private EditText mEditTextInputHours;
    private EditText mEditTextInputMinutes;
    private EditText mEditTextInputSeconds;
    private TextView mTextViewCountdown;
    private Button mButtonSet;
    private Button mButtonStartPause;
    private Button mButtonReset;
    private ProgressBar mProgressBar1;
    private int progressBarMax;
    private int currentProgress;

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

        mEditTextInputHours = getView().findViewById(R.id.editTextInputHours);
        mEditTextInputMinutes = getView().findViewById(R.id.editTextInputMinutes);
        mEditTextInputSeconds = getView().findViewById(R.id.editTextInputSeconds);
        mTextViewCountdown = getView().findViewById(R.id.textViewCountdown);
        mButtonSet = getView().findViewById(R.id.buttonSet);
        mButtonStartPause = getView().findViewById(R.id.buttonStartPause);
        mButtonReset = getView().findViewById(R.id.buttonReset);
        mProgressBar1 = getView().findViewById(R.id.progressBar1);

        mButtonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String checkerH = mEditTextInputHours.getText().toString();
                String checkerM = mEditTextInputMinutes.getText().toString();
                String checkerS = mEditTextInputSeconds.getText().toString();

                if(checkerH.equals("") && checkerM.equals("") && checkerS.equals("")) {
                    Toast.makeText(getActivity(), "Fields can´t be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                long inputNumber = 0;

                if(!checkerH.equals("")) {
                    inputNumber += Long.parseLong(checkerH) * 60 * 60 * 1000;
                }
                if(!checkerM.equals("")) {
                    inputNumber += Long.parseLong(checkerM) * 60000;
                }
                if(!checkerS.equals("")) {
                    inputNumber += Long.parseLong(checkerS) * 1000;
                }

                /*if(!checkerM.equals("") && checkerS.equals("")) {
                    inputNumber = Long.parseLong(checkerM) * 60000;
                } else if(checkerM.equals("") && !checkerS.equals("")) {
                    inputNumber = Long.parseLong(checkerS) * 1000;
                } else {
                    inputNumber = (Long.parseLong(checkerM) * 60000) + (Long.parseLong(checkerS) * 1000);
                }  */

                if(inputNumber == 0) {
                    Toast.makeText(getActivity(), "Enter a positive number!", Toast.LENGTH_SHORT).show();
                    return;
                }

                setTime(inputNumber);
                mEditTextInputHours.setText("");
                mEditTextInputMinutes.setText("");
                mEditTextInputSeconds.setText("");
            }
        });

        mButtonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timerRunning) {
                    pauseCountdown();
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
        progressBarMax = (int) (long) time;
        mProgressBar1.setMax(progressBarMax);
    }

    public void startCountdown() {
        endTime = System.currentTimeMillis() + timeLeft; // tempo que passou + tempo que falta
        countDownTimer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                updateCountdownText();
                mProgressBar1.incrementProgressBy(1000);
                currentProgress = mProgressBar1.getProgress();
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

    public void pauseCountdown() {
        countDownTimer.cancel();
        timerRunning = false;
        updateButtons();
    }

    public void resetCountdown() {
        timeLeft = startTime;
        updateCountdownText();
        updateButtons();
        mProgressBar1.setMax((int)(long)startTime); // teste2
        mProgressBar1.setProgress(0); // TESTEEEEEEEE
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
            mEditTextInputHours.setVisibility(View.INVISIBLE);
            mEditTextInputMinutes.setVisibility(View.INVISIBLE);
            mEditTextInputSeconds.setVisibility(View.INVISIBLE);
            mButtonSet.setVisibility(View.INVISIBLE);
          //  mButtonReset.setVisibility(View.INVISIBLE);
            // mButtonStartPause.setText("Pause");
            mButtonStartPause.setBackgroundResource(R.drawable.ic_pause_circle_outline_pink_24dp);
        } else {
            mEditTextInputHours.setVisibility(View.VISIBLE);
            mEditTextInputMinutes.setVisibility(View.VISIBLE);
            mEditTextInputSeconds.setVisibility(View.VISIBLE);
            mButtonSet.setVisibility(View.VISIBLE);
           // mButtonStartPause.setText("Start");
            mButtonStartPause.setBackgroundResource(R.drawable.ic_play_circle_outline_pink_24dp);

            if(timeLeft < 1000) {
                mButtonStartPause.setVisibility(View.INVISIBLE);
            } else {
                mButtonStartPause.setVisibility(View.VISIBLE);
            }

       //     if(timeLeft < startTime) {
          //      mButtonReset.setVisibility(View.VISIBLE);
     //       } else {
               // mButtonReset.setVisibility(View.INVISIBLE);
         //   }
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
        editor.putInt("progressBarMax", progressBarMax);
        editor.putInt("currentProgress", currentProgress);

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
        progressBarMax = pref.getInt("progressbarMax", (int)(long)startTime); // VERIFICAR SE O DEFVALUE ESTÁ CORRETO
        currentProgress = pref.getInt("currentProgress", 0);
        mProgressBar1.setMax(progressBarMax);
        mProgressBar1.setProgress(currentProgress);

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
