package com.example.jailson.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

@SuppressLint("SimpleDateFormat")

public class MainActivity extends AppCompatActivity
{

    MediaPlayer mediaPlayer = null;

    private static final String TAG = MainActivity.class.getName();

    //wakelock to keep screen on
    protected PowerManager.WakeLock mWakeLock;

    //speach recognizer for callbacks
    private SpeechRecognizer mSpeechRecognizer;

    //handler to post changes to progress bar
    private Handler mHandler = new Handler();

    //ui textview
    TextView responseText;

    //intent for speech recogniztion
    Intent mSpeechIntent;

    //this bool will record that it's time to kill P.A.L.
    boolean killCommanded = false;

    //legel commands
    private static final String[] VALID_COMMANDS = {
            "ho are you",
            "are you doing",
            "test",
            "exit"
    };

    private static final int VALID_COMMANDS_SIZE = VALID_COMMANDS.length;

    AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        responseText = (TextView) findViewById(R.id.responseText);
    }

    @Override
    protected void onStart()
    {
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
        audioManager.setStreamMute(AudioManager.STREAM_ALARM, true);
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        audioManager.setStreamMute(AudioManager.STREAM_RING, true);
        audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        SpeechListener mRecognitionListener = new SpeechListener();
        mSpeechRecognizer.setRecognitionListener(mRecognitionListener);
        mSpeechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.example.jailson.myapplication");

        // Given an hint to the recognizer about what the user is going to say
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Specify how many results you want to receive. The results will be sorted
        // where the first result is the one with higher confidence.
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 20);


        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        //aqcuire the wakelock to keep the screen on until user exits/closes app
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
        this.mWakeLock.acquire();
        mSpeechRecognizer.startListening(mSpeechIntent);

        //Toast.makeText(getApplicationContext(), "onStart", Toast.LENGTH_LONG).show();

        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //
        return false;
    }

    private String getResponse(int command)
    {
        Calendar c = Calendar.getInstance();

        String retString =  "I'm sorry, Not Possible for me";
        SimpleDateFormat dfDate_day;
        int resID;
        switch (command)
        {
            case 0:
                resID = getApplicationContext().getResources().getIdentifier("desculpe", "raw", getApplicationContext().getPackageName());
                tocarSon(resID);
                retString = "ho are you";
                //responseText.setOnClickListener(new View.OnClickListener() {
                //    @Override
                //    public void onClick(View arg0) {
                //        Intent i = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://madeinandroid.net/"));
                //        startActivity(i);
                //   }
                //});
                break;
            case 1:
                retString = "are you doing";
                resID = getApplicationContext().getResources().getIdentifier("partiu", "raw", getApplicationContext().getPackageName());
                tocarSon(resID);
                //dfDate_day = new SimpleDateFormat("dd/MM/yyyy");
                //retString= " Today is " + dfDate_day.format(c.getTime());
                break;
            case 2:
                retString = "test";
                resID = getApplicationContext().getResources().getIdentifier("nome_jarbas", "raw", getApplicationContext().getPackageName());
                tocarSon(resID);
                //mediaPlayer.release();
//                mediaPlayer.reset();
                break;
            case 3:
                killCommanded = true;
                break;
            default:
                resID = getApplicationContext().getResources().getIdentifier("nome_jarbas", "raw", getApplicationContext().getPackageName());
                tocarSon(resID);
                //mediaPlayer.release();
                //mediaPlayer.reset();
                break;
        }
        return retString;
    }

    @Override
    protected void onPause()
    {
        //kill the voice recognizer
        if(mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
            mSpeechRecognizer = null;
        }
        this.mWakeLock.release();
        super.onPause();
    }

    private void processCommand(ArrayList<String> matchStrings)
    {
        String response = "não entendi";

        int maxStrings = matchStrings.size();
        boolean resultFound = false;

        for(int i = 0; i < VALID_COMMANDS_SIZE && !resultFound; i++)
        {
            for(int j=0; j < maxStrings && !resultFound; j++)
            {
                if(StringUtils.getLevenshteinDistance(matchStrings.get(j), VALID_COMMANDS[i])
                            < (VALID_COMMANDS[i].length() / 3))
                {
                    response = getResponse(i);
                }
            }
        }

        final String finalResponse = response;
        mHandler.post(new Runnable() {
            public void run() {
                responseText.setText(finalResponse);
            }
        });
    }

    class SpeechListener implements RecognitionListener
    {
        public void onBufferReceived(byte[] buffer)
        {
            Log.d(TAG, "buffer recieved ");
        }
        public void onError(int error) {
            //if critical error then exit
            if(error == SpeechRecognizer.ERROR_CLIENT || error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS)
            {
                Log.d(TAG, "[jaja] client error");
            }
            //else ask to repeats
            else
            {
                Log.d(TAG, "other error");
                mSpeechRecognizer.startListening(mSpeechIntent);
            }
        }
        public void onEvent(int eventType, Bundle params)
        {
            Log.d(TAG, "onEvent");
        }
        public void onPartialResults(Bundle partialResults)
        {
            Log.d(TAG, "partial results");
        }
        public void onReadyForSpeech(Bundle params)
        {
            Log.d(TAG, "on ready for speech");
            audioManager.setStreamVolume(AudioManager.RINGER_MODE_SILENT, 0 , 0);
        }
        public void onResults(Bundle results)
        {
            Log.d(TAG, "on results");

            ArrayList<String> matches = null;

            if(results != null){
                matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if(matches != null){
                    Log.d(TAG, "results are " + matches.toString());
                    final ArrayList<String> matchesStrings = matches;
                    processCommand(matchesStrings);
                    if(!killCommanded)
                        mSpeechRecognizer.startListening(mSpeechIntent);
                    else
                        finish();

                }
            }

        }
        public void onRmsChanged(float rmsdB) {
            //   Log.d(TAG, "rms changed");
        }
        public void onBeginningOfSpeech() {
            Log.d(TAG, "speach begining");
        }
        public void onEndOfSpeech() {
            Log.d(TAG, "speach done");
        }

    }

    public void tocarSon(int id)
    {

        mediaPlayer = MediaPlayer.create(getApplicationContext(), id);

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
        audioManager.setStreamMute(AudioManager.STREAM_ALARM, false);
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        audioManager.setStreamMute(AudioManager.STREAM_RING, false);
        audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.release();
                mp = null;


                audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
                audioManager.setStreamMute(AudioManager.STREAM_ALARM, true);
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                audioManager.setStreamMute(AudioManager.STREAM_RING, true);
                audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);

            }
        });
        mediaPlayer.start();

    }
}
