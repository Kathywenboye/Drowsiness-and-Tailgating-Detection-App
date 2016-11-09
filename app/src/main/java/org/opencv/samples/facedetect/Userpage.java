package org.opencv.samples.facedetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.FileWriter;
import java.io.FileNotFoundException;


import org.opencv.android.Utils;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.video.Video;

import android.app.Activity;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.widget.Toast;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.os.Parcel;
import android.os.Environment;

public class Userpage extends Activity implements Parcelable {

    public EditText userinput;
    public TextView outputtext;
    public static int currstate;
    public static int getimage = 0;
    public static int live = 11;
    public static String username;

    public int mData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user);
        userinput = (EditText) findViewById(R.id.userinput);
        userinput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        outputtext = (TextView) findViewById(R.id.output);
        outputtext.setText("Please enter your username. Or click start to create your profile");
        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("EXIT", false)) {
            finish();
        }

    }

    public void ondoneenterclick(View v) {
        hideKeyboard(this);
        String inputstring = userinput.getText().toString();
        String filename = inputstring + "anchors.txt";
        File file = new File(getApplicationContext().getFilesDir(), filename);
        if (!file.exists()) {
            outputtext.setText("Username doesn't exit. Please click start to create your profile");
        } else {
            outputtext.setText("Welcome " + inputstring + ". Click start to begin your journey");
        }
    }

    public void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        userinput.setFocusableInTouchMode(false);
        userinput.setFocusable(false);
        userinput.setFocusableInTouchMode(true);
        userinput.setFocusable(true);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void ondeleteclick(View v) {
        String inputstring = userinput.getText().toString();
        String filename = inputstring + "anchors.txt";
        File file = new File(getApplicationContext().getFilesDir(), filename);
        if (file.exists()) {
            file.delete();
        }
        filename = inputstring + "lefteye.jpg";
        file = new File(getApplicationContext().getFilesDir(), filename);
        if (file.exists()) {
            file.delete();
        }
        filename = inputstring + "righteye.jpg";
        file = new File(getApplicationContext().getFilesDir(), filename);
        if (file.exists()) {
            file.delete();
        }
        filename = inputstring + "leftpupil.jpg";
        file = new File(getApplicationContext().getFilesDir(), filename);
        if (file.exists()) {
            file.delete();
        }
        filename = inputstring + "rightpupil.jpg";
        file = new File(getApplicationContext().getFilesDir(), filename);
        if (file.exists()) {
            file.delete();
        }

    }

    public void onstartclick(View v) {
        username = userinput.getText().toString();
        String filename = username + "anchors.txt";
        File file = new File(getApplicationContext().getFilesDir(), filename);
        if (!file.exists()) {
            currstate = getimage;
        } else {
            currstate = live;
        }
        Intent myintent = new Intent();
        Bundle mybundle = new Bundle();
        mybundle.putParcelable("org.opencv.samples.facedetect.user", Userpage.this);
        myintent.putExtras(mybundle);
        myintent.setClass(Userpage.this, FdActivity.class);
        startActivity(myintent);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mData);
    }

    public static final Parcelable.Creator<FdActivity> CREATOR
            = new Parcelable.Creator<FdActivity>() {
        public FdActivity createFromParcel(Parcel in) {
            return new FdActivity();
        }

        public FdActivity[] newArray(int size) {
            return new FdActivity[size];
        }
    };
}
