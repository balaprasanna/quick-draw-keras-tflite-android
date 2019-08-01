package com.bala.quickdraw;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class Classifier {
    private static final String LOG_TAG = Classifier.class.getSimpleName();

    // For HASYv2
    private static final String MODEL_NAME = "HASYv2-1chan-acc84.tflite"; // "HASYv2-1chan.tflite";

    private static final int BATCH_SIZE = 1;

//     For HASYv2
    public static final int IMG_HEIGHT = 32;// 28;
    public static final int IMG_WIDTH = 32; //28
    private static final int NUM_CHANNEL = 1; //1;
    private static final int NUM_CLASSES =  369; //82; //10;


    private final Interpreter.Options options = new Interpreter.Options();
    private final Interpreter mInterpreter;
    private final ByteBuffer mImageData;
    private final int[] mImagePixels = new int[IMG_HEIGHT * IMG_WIDTH];
    private final float[][] mResult = new float[1][NUM_CLASSES];

    // For HASYv2
    public  JSONObject labelsJsonFile = null;
    public  JSONObject symbolsJsonFile = null;

    public Classifier(Activity activity) throws IOException {
        mInterpreter = new Interpreter(loadModelFile(activity), options);
        mImageData = ByteBuffer.allocateDirect(
                4 * BATCH_SIZE * IMG_HEIGHT * IMG_WIDTH * NUM_CHANNEL);
        mImageData.order(ByteOrder.nativeOrder());

        try {

            JSONObject lablesobj = new JSONObject( loadJSONFromAsset(activity, "HASYv2-lables-i2c.json"));
            labelsJsonFile = lablesobj;
            JSONObject symbobj = new JSONObject( loadJSONFromAsset(activity, "HASYv2_symbol.json"));
            symbolsJsonFile = symbobj;

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public String loadJSONFromAsset(Activity activity, String jsonfilename ) {
        String json = null;
        try {
            InputStream is = activity.getAssets().open(jsonfilename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public Result classify(Bitmap bitmap) {
        convertBitmapToByteBuffer(bitmap);
        long startTime = SystemClock.uptimeMillis();
        mInterpreter.run(mImageData, mResult);
        long endTime = SystemClock.uptimeMillis();
        long timeCost = endTime - startTime;
        Log.v(LOG_TAG, "classify(): result = " + Arrays.toString(mResult[0])
                + ", timeCost = " + timeCost);

        Result res = new Result(mResult[0], timeCost);
        try{
            //For HASYv2
            Log.v(LOG_TAG, "labelsJsonFile -> "+ labelsJsonFile.getString("0"));
            Log.v(LOG_TAG, "labelsJsonFile "+ labelsJsonFile.getString( String.valueOf( res.getNumber()) )  );
            Log.v(LOG_TAG, "Symbol -> " + symbolsJsonFile.getJSONObject("latex").getString( String.valueOf( res.getNumber()) ) );

            // SET LABLES
            String lab = labelsJsonFile.getString(String.valueOf( res.getNumber()) )
                    + " - "
                    + symbolsJsonFile.getJSONObject("latex").getString( String.valueOf( res.getNumber()) ) ;
            //res.setLabel( labelsJsonFile.getString(String.valueOf( res.getNumber()) ) );
            res.setLabel(  lab );

        } catch (Exception e){
            e.printStackTrace();
        }

        return res;
    }

    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_NAME);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (mImageData == null) {
            return;
        }
        mImageData.rewind();

        bitmap.getPixels(mImagePixels, 0, bitmap.getWidth(), 0, 0,
                bitmap.getWidth(), bitmap.getHeight());

        int pixel = 0;
        for (int i = 0; i < IMG_WIDTH; ++i) {
            for (int j = 0; j < IMG_HEIGHT; ++j) {
                int value = mImagePixels[pixel++];
                mImageData.putFloat(convertPixel(value));
            }
        }
    }

    private static float convertPixel(int color) {
        return (255 - (((color >> 16) & 0xFF) * 0.299f
                + ((color >> 8) & 0xFF) * 0.587f
                + (color & 0xFF) * 0.114f)) / 255.0f;
    }
}


