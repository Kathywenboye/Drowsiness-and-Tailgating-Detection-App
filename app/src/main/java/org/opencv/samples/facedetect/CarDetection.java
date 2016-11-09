package org.opencv.samples.facedetect;

import android.app.Activity;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.VehicleManager;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.BrakePedalStatus;
import com.openxc.units.Boolean;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class CarDetection extends Activity {

    private static final int UPDATE_TAG = 0x01;//tag for switch camera
    private static final String TAG = "StarterActivity";

    private int CaseIndex=1,activityIndex =0;
    private boolean goBruins = true;
    private boolean Drowsy =false;


    private VehicleManager mVehicleManager;
    private TextView mVehicleSpeedView;
    private TextView brakeStatus;
    private Button backButton;
    private Button exitButton;

    private AlertDialog alert = null;
    private AlertDialog.Builder builder = null;
    private Context mContext;

    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_detection);

        Intent intent = getIntent();
        //Drowsy = intent.getBooleanExtra("Drowsy",false);
        //show vehicle speed and brakeStatus
        mVehicleSpeedView = (TextView) findViewById(R.id.vehicle_speed);
        brakeStatus = (TextView) findViewById(R.id.brakeStatus);
        mContext = CarDetection.this;

        backButton = (Button) findViewById(R.id.backButton);
        exitButton = (Button) findViewById(R.id.exitButton);
        backButton.setOnClickListener(backButtonListener);
        exitButton.setOnClickListener(exitButtonListener);
        //DetectDrowsy();

        //FdAndTailgating();

    }

    @Override
    protected void onResume(){
        super.onResume();
        if(mVehicleManager == null) {
            Intent intent = new Intent(this, VehicleManager.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        // When the activity goes into the background or exits, we want to make
        // sure to unbind from the service to avoid leaking memory
        if(mVehicleManager != null) {
            Log.i(TAG, "Unbinding from Vehicle Manager");
            // Remember to remove your listeners, in typical Android
            // fashion.

            mVehicleManager.removeListener(VehicleSpeed.class,
                    vSpeedListener);
            mVehicleManager.removeListener(BrakePedalStatus.class,
                    brakeStatusListener);

            unbindService(mConnection);
            mVehicleManager = null;
        }
    }


   /* private void FdAndTailgating(){

        //main function from here
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (goBruins) {

                    mHandler.sendEmptyMessage(UPDATE_TAG);

                    try {
                        Thread.currentThread().sleep(40000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();



    }
*/

    private Button.OnClickListener backButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent myintent = new Intent();
            Bundle mybundle = new Bundle();
            //mybundle.putParcelable("org.opencv.samples.facedetect.user", Userpage.this);
            myintent.putExtras(mybundle);
            myintent.setClass(CarDetection.this, FdActivity.class);
            startActivity(myintent);
            CarDetection.this.finish();

        }
    };

    private Button.OnClickListener exitButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent exitToHomepage = new Intent(getApplicationContext(), Userpage.class);
            startActivity(exitToHomepage);
        }
    };
    /*private Button.OnClickListener stopListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            goBruins=false;
            if (FdActivity.instance!=null) {
                FdActivity.instance.finish();
            }
        }
    };*/



    /*public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_TAG ) {
                switch (CaseIndex) {
                    case 1: {
                        CaseIndex = 0;
                        Intent ToFdActivity = new Intent(Working_page.this,FdActivity.class);
                        startActivity(ToFdActivity);
                        Working_page.this.finish();
                        System.exit(0);
                        break;


                    }
                    case 0: {

                        Toast.makeText(Working_page.this, "Java", Toast.LENGTH_SHORT).show();
                        CaseIndex = 1;
                        break;
                    }
                }
            }
        }
    };*/

    VehicleSpeed.Listener vSpeedListener = new VehicleSpeed.Listener(){


        @Override
        public void receive(Measurement measurement2) {

            final VehicleSpeed vSpeed = (VehicleSpeed) measurement2;

            CarDetection.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mVehicleSpeedView.setText("Vehicle Speed (km/hr): "
                            + vSpeed.getValue().doubleValue());
                }
            });

        }
    };

    BrakePedalStatus.Listener brakeStatusListener = new BrakePedalStatus.Listener(){

        @Override
        public void receive(Measurement measurement4) {

            final BrakePedalStatus brakePedalStatus = (BrakePedalStatus) measurement4;

            CarDetection.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (brakePedalStatus.getValue().booleanValue()){
                        brakeStatus.setText(R.string.On_Brake);



                    }else{
                        brakeStatus.setText(R.string.No_Brake);



                    }

                }
            });

        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the VehicleManager service is
        // established, i.e. bound.
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            // When the VehicleManager starts up, we store a reference to it
            // here in "mVehicleManager" so we can call functions on it
            // elsewhere in our code.
            mVehicleManager = ((VehicleManager.VehicleBinder) service)
                    .getService();

            // We want to receive updates whenever the EngineSpeed changes. We
            // have an EngineSpeed.Listener (see above, mSpeedListener) and here
            // we request that the VehicleManager call its receive() method
            // whenever the EngineSpeed changes

            mVehicleManager.addListener(VehicleSpeed.class,vSpeedListener);
            mVehicleManager.addListener(BrakePedalStatus.class,brakeStatusListener);
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleManager Service  disconnected unexpectedly");
            mVehicleManager = null;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.starter, menu);
        return true;
    }

    /*private void DetectDrowsy(){

        Log.v(TAG,"Drowsy");

        if(Drowsy){

            mp = new MediaPlayer();
            try {
                mp.setDataSource(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mp.start();



            alert = null;
            builder = new AlertDialog.Builder(mContext);
            alert = builder.setMessage("Drowsy! Stop the car!!!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (mp != null) {
                                mp.stop();
                                mp.release();
                                mp = null;
                            }

                        }
                    }).create();
            alert.show();

            goBruins=false;
            if (FdActivity.instance!=null) {
                FdActivity.instance.finish();
            }


        }
    }
*/

}
