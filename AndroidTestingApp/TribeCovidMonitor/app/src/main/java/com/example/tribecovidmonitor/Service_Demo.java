package com.example.tribecovidmonitor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.tribecovidmonitor.ui.dashboard.Dashboard;

import org.tensorflow.lite.Interpreter;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.text.BreakIterator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

public class Service_Demo extends Service {

    final String fnlStr_debug_tag = "Service Demo Debug";
    IBinder binder ;
    Interpreter interpreter;
    boolean allowRebind = false;
    float[][] similarity;
    public static final String
            ACTION_LOCATION_BROADCAST1 = Service_Demo.class.getName() + "simiBroadcast",
            healthy = "extra_latitude";
    public static final String
            ACTION_LOCATION_BROADCAST2 = Service_Demo.class.getName() + "simiBroadcast",
           mild = "extra_latitude";
    public static final String
            ACTION_LOCATION_BROADCAST3 = Service_Demo.class.getName() + "simiBroadcast",
            moderate = "extra_latitude";
    public static final String
            ACTION_LOCATION_BROADCAST4 = Service_Demo.class.getName() + "simiBroadcast",
            recovered = "extra_latitude";
    boolean stop_recording = false;
    Thread thr_rmCache , thr_recordSound;
    short[] recordingBuffer = new short[RECORDING_LENGTH];
    int recordingOffset = 0;
    boolean shouldContinue = true;
    private Thread recordingThread;
    boolean shouldContinueRecognition = true;
    private Thread recognitionThread;
    private final ReentrantLock recordingBufferLock = new ReentrantLock();
    private static final int SAMPLE_RATE = 48000;
    private static final int SAMPLE_DURATION_MS = 6390;
    private static final int RECORDING_LENGTH = (int) (SAMPLE_RATE * SAMPLE_DURATION_MS / 1000);

    private static final String OUTPUT_SCORES_NAME = "output";

    @Override
    public void
    onCreate() {
        binder = new LocalBinder ( ); }

    @Override
    public int
    onStartCommand (Intent intent , int flags , int startId ){
        if(intent == null ){
            /* The service has been killed , and the startMode
               has been set set to START_STICKY . As such
               the service is now being restarted , with a
               null Intent , so the original intent
               is not delivered again .
               When START_STICKY is set , the intent
               itself is not primordial , for the
               service to run .*/
            thr_rmCache = new Thread (new Purge_Cache (startId ) ) ;
            thr_rmCache .run();
            return START_STICKY; }
        else {
            /*An intent has been delivered . Either , the service
              is starting fresh , or it has been killed  , and
              its start mode has been set to START_REDELIVER_INTENT .
              So the original intent is being redelivered .*/
            switch (intent .getStringExtra ("do" ) ){
                case "Clear Cache":
                    if (thr_rmCache == null ){
                        thr_rmCache = new Thread (new Purge_Cache (startId ) ) ;
                        thr_rmCache .run();
                        return START_STICKY; }
                    break;
                case "Record Sound" :
                    if (thr_recordSound == null  ){
                        thr_recordSound = new Thread (new Record_Sound ( ) );
                        thr_recordSound .run ( );
                        return START_REDELIVER_INTENT; }
                    break; } }
        return START_NOT_STICKY; }

    @Override
    public IBinder
    onBind(Intent intent ){
        /* extras are not delivered
           on binding , unbinding , or
           rebinding  .
           The intent delivered ,
           on binding , unbinding ,
           rebiding , is the original
           intent delivered when
           binding .*/
        return binder; }

    @Override
    public boolean
    onUnbind(Intent intent) {
        return allowRebind; }

    @Override
    public void
    onRebind(Intent intent) { }

    @Override
    public void
    onDestroy( ) {
        if (thr_rmCache != null && thr_rmCache .isAlive ( ) )
            thr_rmCache .interrupt ( );
        if (thr_recordSound != null  )
            stop_recording = true ; }


    /* A class to Purge the cache directory  ,
     * it will show a toast when starting ,
     * the process , and when it is done
     * doing its job , it will show a
     * notification , and send a local
     * broadcast .*/
    class Purge_Cache implements Runnable {

        int startId;

        public
        Purge_Cache (int starId ){
            this .startId = starId; }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void
        run ( ){
            //show a toast on start
            Toast.makeText(Service_Demo .this ,
                    "Clearing the cache directory" , Toast .LENGTH_LONG ) .show ( );

            //purge the cache
            purge_Cache (getCacheDir ( ) .toPath ( ) );

            //show a notification after cash is purged
            cleared_Cache_Notification ( );

            //Create a local broadcast
            Intent intent = new Intent ("Cache Purged" );
            intent .putExtra ("msg"  , "The cache is purged" );
            LocalBroadcastManager.getInstance (Service_Demo .this .getApplicationContext ( ) )
                    .sendBroadcast (intent );

            //done
            thr_rmCache = null;
            if (thr_recordSound == null )
                stopSelf (startId ); }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void
        purge_Cache (Path pathD  ){
            //Do not remove the directory , only its content recursively
            Log.d (fnlStr_debug_tag , pathD .toString ( ) );
            if (Thread .interrupted ( ) ){
                return ;}
            try (DirectoryStream<Path > paths =
                         Files.newDirectoryStream
                                 (pathD ) ){
                for (Path td_path : paths ){
                    Log .d (fnlStr_debug_tag , td_path . toString ( ) );
                    if (Files .isDirectory
                            (td_path , LinkOption.NOFOLLOW_LINKS ) ){
                        purge_Cache (td_path ); }
                    Files .delete (td_path ); }}
            catch (IOException | SecurityException exception ){
                Log .d (fnlStr_debug_tag , exception .toString ( ) );}}


        @RequiresApi(api = Build.VERSION_CODES.O)
        public void
        cleared_Cache_Notification ( ){
            //Create notification channel
            String notification_channel_id = "coding_demo_cache_purged";
            NotificationChannel channel = new NotificationChannel (notification_channel_id ,
                    "coding demo cache purged" , NotificationManager.IMPORTANCE_DEFAULT );
            channel .setDescription ("Coding demo cache purged channel" );
            NotificationManager notificationManager = getSystemService (NotificationManager .class );
            notificationManager .createNotificationChannel (channel );

            //create notification
            int notification_id = 1;
            Notification .Builder notification_builder =  new Notification
                    .Builder (Service_Demo .this , notification_channel_id )
                    .setSmallIcon (R .mipmap .ic_launcher )
                    .setContentTitle ("Monitoring your status" )
                    .setContentText("The application cache is cleared" );

            notificationManager .notify (notification_id , notification_builder .build ( ) ); } }


    /* Record sound class
     */
    class Record_Sound implements Runnable{

        CountDownTimer audioCapture_countDownTimer;
        MediaRecorder audioCapture_recorder;


        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void
        run ( ) {
            Toast .makeText(Service_Demo .this , "Starting to record Sound !" ,
                    Toast .LENGTH_SHORT ) .show ( );

            startRecording();

            startForeground (2 , sound_Recording_Notification ( ) );
        }

//        public void
//        start_Recording ( ) {
//            try {
//                //Start recording
//                String audioCapture_fileName = getCacheDir().getAbsoluteFile()
//                        + "/" + UUID.randomUUID().toString() + ".wav";
//                audioCapture_recorder = new MediaRecorder();
//                audioCapture_recorder.setOutputFormat(AudioFormat.ENCODING_PCM_16BIT);
//                audioCapture_recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//                audioCapture_recorder.setAudioChannels(1);
//                audioCapture_recorder.setAudioEncodingBitRate(128000);
//                audioCapture_recorder.setAudioSamplingRate(48000);
//
//                audioCapture_recorder.setOutputFile(audioCapture_fileName);
//                audioCapture_recorder.prepare();
//                audioCapture_recorder.start();
//                int bufferSize =
//                        AudioRecord.getMinBufferSize(
//                                SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
//                if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
//                    bufferSize = SAMPLE_RATE * 2;
//                }
//                short[] audioBuffer = new short[bufferSize / 2];
//
//                AudioRecord record =
//                        new AudioRecord(
//                                MediaRecorder.AudioSource.DEFAULT,
//                                SAMPLE_RATE,
//                                AudioFormat.CHANNEL_IN_MONO,
//                                AudioFormat.ENCODING_PCM_16BIT,
//                                bufferSize);
//                if (record.getState() != AudioRecord.STATE_INITIALIZED) {
//                    Log.e("LOG_TAG", "Audio Record can't initialize!");
//                    return;
//                }
//
//                //Cancel recording after audioCapture_duration , or on interrupt
//            }
//            catch (Exception exception ){
//                Log .d (fnlStr_debug_tag , exception .toString ( ) );
//                Toast .makeText(Service_Demo .this , "Failed to record sound "
//                        , Toast .LENGTH_SHORT ); }}
//
//        public void
//        stop_Recording ( ){
//            audioCapture_recorder .stop( );
//            audioCapture_recorder .release( );
//            thr_recordSound = null ;
//            stop_recording = false ;
//            stopForeground (true ); }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public Notification
        sound_Recording_Notification ( ){
            // Create notification channel
            String notification_channel_id = "coding_demo_audio_capture";
            NotificationChannel channel = new NotificationChannel (
                    notification_channel_id , "coding demo audio capture" ,
                    NotificationManager .IMPORTANCE_DEFAULT );
            channel .setDescription("Coding demo audio capture channel" );
            NotificationManager notificationManager = getSystemService (NotificationManager .class );
            notificationManager .createNotificationChannel (channel );

            // Create the notification
            Notification .Builder notification_builder =  new Notification
                    .Builder (Service_Demo .this , notification_channel_id )
                    .setSmallIcon (R .mipmap .ic_launcher_round )
                    .setContentTitle ("Code demo" )
                    .setContentText ("Sound is being recorded" );
            return notification_builder .build ( ); }}

    class LocalBinder extends Binder {
        public int
        add (int ... vars_i ){
            int sum = 0;
            for (int var_i : vars_i ){
                sum += var_i ; }
            return sum ; }

//        public void
//        stopRecordSound (){
//            if(thr_recordSound != null  ){
//                stop_recording = true ;
//                Log .d (fnlStr_debug_tag , "Audio recording is stopped " );  }
//        }
    }



    private MappedByteBuffer loadModelFile() throws IOException {
        String MODEL_ASSETS_PATH = "siamese1.tflite";
        AssetFileDescriptor assetFileDescriptor =getApplicationContext().getAssets().openFd(MODEL_ASSETS_PATH) ;
        FileInputStream fileInputStream = new FileInputStream( assetFileDescriptor.getFileDescriptor() ) ;
        FileChannel fileChannel = fileInputStream.getChannel() ;
        long startoffset = assetFileDescriptor.getStartOffset() ;
        Log.v("LOG_TAG", "Model"+ startoffset);
        Log.v("LOG_TAG", "Model"+assetFileDescriptor);
        long declaredLength = assetFileDescriptor.getDeclaredLength() ;
        Log.v("LOG_TAG", "Model"+declaredLength);

        return fileChannel.map( FileChannel.MapMode.READ_ONLY , startoffset , declaredLength ) ;
    }
    private float[][] getRandomMediaFilePath(Context context, String assetSpecificFolderPath) {
        AssetManager assetManager;
        String[] fileList;
        String mediaFileName;
        String mediaFilePath = null;

        float[][] newarr = new float[0][];
        try {
            assetManager = context.getAssets();
            fileList = assetManager.list(assetSpecificFolderPath);
            mediaFileName = fileList[getRandomNumber(0, fileList.length - 1)];
            mediaFilePath = assetSpecificFolderPath + "/" + mediaFileName;
            newarr = fileContent(mediaFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return newarr;
    }


    private static int getRandomNumber(int min, int max)
    {
        // Usually this should be a field rather than a method variable so
        // that it is not re-seeded every call.
        Random random = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = random.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    public synchronized void startRecording() {
        if (recordingThread != null) {
            return;
        }
        shouldContinue = true;
        recordingThread =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                record();
                            }
                        });
        recordingThread.start();
    }

    private void record() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        // Estimate the buffer size we'll need for this device.
        int bufferSize =
                AudioRecord.getMinBufferSize(
                        SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }
        short[] audioBuffer = new short[bufferSize / 2];

        AudioRecord record =
                new AudioRecord(
                        MediaRecorder.AudioSource.DEFAULT,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize);

        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e("LOG_TAG", "Audio Record can't initialize!");
            return;
        }

        record.startRecording();

        Log.v("LOG_TAG", "Start recording");


        while (shouldContinue) {
            int numberRead = record.read(audioBuffer, 0, audioBuffer.length);
            Log.v("LOG_TAG", "read: " + numberRead);
            int maxLength = recordingBuffer.length;
            recordingBufferLock.lock();
            try {
                if (recordingOffset + numberRead < maxLength) {
                    System.arraycopy(audioBuffer, 0, recordingBuffer, recordingOffset, numberRead);
                } else {
                    shouldContinue = false;
                }
                recordingOffset += numberRead;
            } finally {
                recordingBufferLock.unlock();
            }
        }
        record.stop();
        record.release();
        startRecognition();
    }
    public synchronized void startRecognition() {
        if (recognitionThread != null) {
            return;
        }
        shouldContinueRecognition = true;
        recognitionThread =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    recognize();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
        recognitionThread.start();
    }

    private void recognize() throws IOException {
        try {
            interpreter = new Interpreter(loadModelFile(), null);

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.v("LOG_TAG", "Start recognition");

        short[] inputBuffer = new short[RECORDING_LENGTH];
        double[] doubleInputBuffer = new double[RECORDING_LENGTH];
        long[] outputScores = new long[4];
        String[] outputScoresNames = new String[]{OUTPUT_SCORES_NAME};


        recordingBufferLock.lock();
        try {
            int maxLength = recordingBuffer.length;
            System.arraycopy(recordingBuffer, 0, inputBuffer, 0, maxLength);
        } finally {
            recordingBufferLock.unlock();
        }

        // We need to feed in float values between -1.0 and 1.0, so divide the
        // signed 16-bit inputs.
        for (int i = 0; i < RECORDING_LENGTH; ++i) {
            doubleInputBuffer[i] = inputBuffer[i] / 32767.0;
        }

        //MFCC java library.
        MFCC mfccConvert = new MFCC();
        double[][] mfccInput = mfccConvert.process(doubleInputBuffer);

        float[][] floatmatrix = new float[mfccInput.length][mfccInput[0].length];
        for (int w = 0; mfccInput.length - 2 > w; w++) {
            for (int h = 0; (mfccInput[0].length) - 2 > h; h++) {
                floatmatrix[w][h] = (float) mfccInput[w][h];
            }
        }

        float[][][][] floatInputBuffer = new float[1][mfccInput.length][mfccInput[0].length][1];

        for (int i = 0; i < mfccInput.length - 2; i++) {

            for (int j = 0; j < (mfccInput[0].length) - 2; j++)
                floatInputBuffer[0][i][j][0] = floatmatrix[i][j];


        }


        Log.v("LOG_TAG", "MFCC Input======> " + floatInputBuffer[0][1][2].length);


        for (int xyz = 0; xyz < 4; xyz++) {
            float[][] newarr = getRandomMediaFilePath(getApplicationContext(), xyz+"/newcsv");
            Log.v("LOG_TAG", "MFCC Input======> " + newarr);


            float[][][][] floatInputBuffer1 = new float[1][newarr.length][newarr[0].length][1];

            for (int i = 0; i < newarr.length - 2; i++) {

                for (int j = 0; j < (newarr[0].length) - 2; j++)
                    floatInputBuffer1[0][i][j][0] = newarr[i][j];


            }

            Log.v("LOG_TAG", "MFCC check======> " + floatmatrix.getClass().getComponentType());
            Log.v("LOG_TAG", "MFCC check======> " + newarr.getClass().getComponentType());


            Object[] inputs = {floatInputBuffer, floatInputBuffer1};
            Log.v("LOG_TAG", "MFCC check======> " + floatInputBuffer.length);
            Log.v("LOG_TAG", "MFCC check======> " + floatInputBuffer[0].length);
            Log.v("LOG_TAG", "MFCC check======> " + floatInputBuffer[0][0].length);
            Log.v("LOG_TAG", "MFCC check======> " + floatInputBuffer1.length);
            Log.v("LOG_TAG", "MFCC check======> " + floatInputBuffer1[0].length);
            Log.v("LOG_TAG", "MFCC check======> " + floatInputBuffer1[0][0].length);
            Map<Integer, Object> outputs = new HashMap<>();
            outputs.put(0, new float[1][1]);

//        Log.v("LOG_TAG", "input " + Arrays.toString(new Map[]{outputs}));
            interpreter.runForMultipleInputsOutputs(inputs, outputs);

          similarity = (float[][]) outputs.get(0);
            Log.v("LOG_TAG", "End recognition: " + similarity[0][0]);

            switch(xyz) {
                case 0:

                    sendBroadcastMessage1(similarity[0][0]);
                    break;
                case 1:
                    sendBroadcastMessage2(similarity[0][0]);
                    break;
                case 2:
                    sendBroadcastMessage3(similarity[0][0]);
                    break;
                case 3:
                    sendBroadcastMessage4(similarity[0][0]);   break;
                default:
                    sendBroadcastMessage1(similarity[0][0]);
            }

        }
        recordingThread=null;
        startRecording();
    }

    public static float[] byteToFloat(byte[] input) {
        float[] ret = new float[input.length/4];
        for (int x = 0; x < input.length; x+=4) {
            ret[x/4] = ByteBuffer.wrap(input, x, 4).getFloat();
        }
        return ret;
    }


    private float[][] fileContent(String pathToCSVFile) throws IOException {
        final int ROWS = 40;
        final int COLUMNS = 600;
        float fileData[][] = new float[ROWS][COLUMNS];
        DataInputStream textFileStream = new DataInputStream(getAssets().open(pathToCSVFile));

        Scanner scanner = new Scanner(textFileStream);
        boolean done = false;
        int i=0;

        while (!done) {
            String str[] = scanner.nextLine().split(",");
            for (int element = 0; element < str.length-2; element++) {

                fileData[i][element] = Float.parseFloat(str[element]);

                if (i >= ROWS) {
                    Arrays.copyOf(fileData, fileData.length * 2);
                }
            }
            if (!scanner.hasNextLine())
                done = true;
            else
                i++;
        }
        return  fileData;
    }

    private void sendBroadcastMessage1(float similarity) {
        if (similarity != 0) {
            Intent intent = new Intent(ACTION_LOCATION_BROADCAST1);
            intent.putExtra(healthy, similarity);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
    private void sendBroadcastMessage2(float similarity) {
        if (similarity != 0) {
            Intent intent = new Intent(ACTION_LOCATION_BROADCAST2);
            intent.putExtra(mild, similarity);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
    private void sendBroadcastMessage3(float similarity) {
        if (similarity != 0) {
            Intent intent = new Intent(ACTION_LOCATION_BROADCAST3);
            intent.putExtra(moderate, similarity);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
    private void sendBroadcastMessage4(float similarity) {
        if (similarity != 0) {
            Intent intent = new Intent(ACTION_LOCATION_BROADCAST4);
            intent.putExtra(recovered, similarity);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
}