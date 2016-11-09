package org.opencv.samples.facedetect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.openxc.VehicleManager;
import com.openxc.measurements.BrakePedalStatus;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.VehicleSpeed;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.LogRecord;


public class CarActivity extends Activity implements CvCameraViewListener2 {

	private static final String TAG = "OCVSample::Activity";
//	private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
	public static final int JAVA_DETECTOR = 0;

	private static final int UPDATE_TAG = 0x01;//


	private int CaseIndex=1,activityIndex =0;
	private boolean goBruins = true;
	private boolean Drowsy =false;
	private int distanceCount =0;
	private int tailgatingCount=0;
	private float safetyDistanceLong  = 80;
	private double speed=0;
	private double d;



	private VehicleManager mVehicleManager;
	private TextView mVehicleSpeedView;
	private TextView brakeStatus;
	private Button backButton;
	private Button exitButton;

	private AlertDialog alert = null;
	private AlertDialog.Builder builder = null;
	private Context mContext;

	private MediaPlayer mp;
	TimerTask timerTask;
	Timer timer;
	Handler handler = new Handler();
	private boolean brake = false;


	private Mat mRgba;
	private Mat mGray;

	private File mCascadeFile;
	private CascadeClassifier mJavaDetector;
	private CascadeClassifier mJavaDetectorEye;
	
	
	private int mDetectorType = JAVA_DETECTOR;
	private String[] mDetectorName;


	private CameraBridgeViewBase mOpenCvCameraView;

	// vehicle
	int iter = 0;
	long current_frame = 0;
	Mat temp_frame;
	int SCAN_STEP = 2;		  // in pixels
	int LINE_REJECT_DEGREES = 10; // in degrees Changed!!!
	int BW_TRESHOLD = 250;		  // edge response strength to recognize for 'WHITE'
	int BORDERX = 10;			  // px, skip this much from left & right borders
	int MAX_RESPONSE_DIST = 5;	  // px
	int CANNY_MIN_TRESHOLD = 1;	  // edge detector minimum hysteresis threshold
	int CANNY_MAX_TRESHOLD = 100; // edge detector maximum hysteresis threshold

	int HOUGH_TRESHOLD = 50;		// line approval vote threshold
	int HOUGH_MIN_LINE_LENGTH = 50;	// remove lines shorter than this treshold
	int HOUGH_MAX_LINE_GAP = 100;   // join lines to one with smaller than this gaps

	//int CAR_DETECT_LINES = 3;    // minimum lines for a region to pass validation as a 'CAR'4 // TED CHANGED THIS!!! NOT SURE
	int CAR_DETECT_LINES = 3;
	//int CAR_H_LINE_LENGTH = 5;  // minimum horizontal line length from car body in px10
	int CAR_H_LINE_LENGTH = 1;

	int MAX_VEHICLE_SAMPLES = 30;      // max vehicle detection sampling history
	int CAR_DETECT_POSITIVE_SAMPLES = MAX_VEHICLE_SAMPLES-2; // probability positive matches for valid car
	int MAX_VEHICLE_NO_UPDATE_FREQ = 15; // remove car after this much no update frames

	int HEIGHT = 1500;
	int FOCAL_LENGTH = 28;//24-28mm

	double K_VARY_FACTOR = 0.2;
	double B_VARY_FACTOR = 40.0;
	double MAX_LOST_FRAMES = 20.0;

	Scalar GREEN = new Scalar(0,255,0,255); /////
	Scalar RED = new Scalar(255,0,0,255);
	Scalar BLUE = new Scalar(0,0,255,255);
	Scalar PURPLE = new Scalar(255,0,255,255);

	public ArrayList<VehicleSample> samples = new ArrayList<VehicleSample>();
	public ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();


	public Status laneR = new Status();
	public Status laneL = new Status();

	public ImageView imgView;

	public int counter = 0;
	public Mat saveframe = null;

	public Mat return_frame = null;
	public Mat half_frame = null;
	public int s = 5;
	double distance=0;
	public int index =0;
	public int frame = 0;

	public TextView output;
	public Handler texthandler = new Handler();

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");


				try {
					// load cascade file from application resources
					InputStream is = getResources().openRawResource(
							R.raw.cars3);
					File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
					mCascadeFile = new File(cascadeDir,
							"cars3.xml");
					FileOutputStream os = new FileOutputStream(mCascadeFile);

					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
					}
					is.close();
					os.close();

					mJavaDetector = new CascadeClassifier(
							mCascadeFile.getAbsolutePath());
					if (mJavaDetector.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mJavaDetector = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from "
								+ mCascadeFile.getAbsolutePath());


					cascadeDir.delete();

				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
				}
				mOpenCvCameraView.setCameraIndex(0);
				mOpenCvCameraView.enableFpsMeter();
				mOpenCvCameraView.enableView();

			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	public CarActivity() {
		mDetectorName = new String[2];
		mDetectorName[JAVA_DETECTOR] = "Java";

		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.car_detect_surface_view);
		imgView = (ImageView) findViewById(R.id.halfframe);
		output = (TextView) findViewById(R.id.outputtext);

		Intent intent = getIntent();
		//Drowsy = intent.getBooleanExtra("Drowsy",false);
		//show vehicle speed and brakeStatus
		mVehicleSpeedView = (TextView) findViewById(R.id.vehicle_speed);
		brakeStatus = (TextView) findViewById(R.id.brakeStatus);
		mContext = CarActivity.this;

		backButton = (Button) findViewById(R.id.backButton);
		exitButton = (Button) findViewById(R.id.exitButton);
		backButton.setOnClickListener(backButtonListener);
		exitButton.setOnClickListener(exitButtonListener);

		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
		mOpenCvCameraView.setCvCameraViewListener(this);


	}


	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
		if(mVehicleManager == null) {
			Intent intent = new Intent(this, VehicleManager.class);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		}


	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
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
		if (mp!=null){
			mp.release();
		}
	}


	public void onDestroy() {
		super.onDestroy();
		mOpenCvCameraView.disableView();

		if (mp!=null){
			mp.release();
		}
	}

	public void onCameraViewStarted(int width, int height) {
		mGray = new Mat();
		mRgba = new Mat();
	}

	public void onCameraViewStopped() {
		mGray.release();
		mRgba.release();
		//mZoomWindow.release();
		//mZoomWindow2.release();
	}
	public void getdistance() {
		texthandler.post(new Runnable() {
			public void run() {
				output.setText("The following distance: " + String.valueOf(distance)+"m");
			}

		});
	}
	private Button.OnClickListener backButtonListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent myintent = new Intent();
			Bundle mybundle = new Bundle();
			//mybundle.putParcelable("org.opencv.samples.facedetect.user", Userpage.this);
			myintent.putExtras(mybundle);
			myintent.setClass(CarActivity.this, FdActivity.class);
			startActivity(myintent);
			CarActivity.this.finish();

		}
	};

	private Button.OnClickListener exitButtonListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent exitToHomepage = new Intent(getApplicationContext(), Userpage.class);
			startActivity(exitToHomepage);
		}
	};

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

		distanceCount++;

		if (distanceCount==100){

			Intent myintent = new Intent();
			Bundle mybundle = new Bundle();
			//mybundle.putParcelable("org.opencv.samples.facedetect.user", Userpage.this);
			myintent.putExtras(mybundle);
			myintent.setClass(CarActivity.this, FdActivity.class);
			startActivity(myintent);
			CarActivity.this.finish();
		}
		counter++;
		if (counter < 2 && return_frame != null) {
			return return_frame;
		} else if(counter < 2 && return_frame == null) {
			return inputFrame.rgba();
		}
		counter = 0;


		Log.w(TAG, "iter num: " + iter);
		iter++;
		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();
		return_frame = mRgba.clone();

		//int size = (int) (mGray.total() * mGray.channels());
		//byte[] temp = new byte[size];
		//mGray.get(0, 0, temp);
		//Log.w(TAG, "temp size is: " + temp.length);
		/*
		for (int i=0; i < temp.length; i++) {
			if (temp[i] >= 0)
				Log.w(TAG, "pixel " + "i is: " + temp[i]);
		}
		*/

		/*

		for (int i = 0; i < 5; i++)
			for (int j = 0; j < 5; j++) {
				double[] data = mGray.get(i, j);
				Log.w(TAG, "pixel " + "i is: " + data[0]);
				//Log.w(TAG, "data length is: " + data[0]);
			}
			*/

		half_frame = new Mat(mRgba.width()/s, mRgba.height()/s, mRgba.type());
		Mat temp_frame = new Mat(mGray.width()/s, mGray.height()/s, mGray.type());
		Mat grey = new Mat(mGray.width()/8, mGray.height()/8, mGray.type());
		Mat edges = new Mat(mGray.width()/s, mGray.height()/s, mGray.type());

		Imgproc.resize(mRgba, half_frame, new Size(mRgba.width()/s, mRgba.height()/s));
		Imgproc.resize(mGray, temp_frame, new Size(mRgba.width()/s, mRgba.height()/s));
		//half_frame = mRgba.clone();
		//temp_frame = mGray.clone();
		grey = temp_frame.clone();
		//Imgproc.cvtColor(temp_frame, grey, Imgproc.COLOR_BGR2GRAY);
		Imgproc.GaussianBlur(grey, grey, new Size(5, 5), 0);
		Imgproc.Canny(grey, edges, CANNY_MIN_TRESHOLD, CANNY_MAX_TRESHOLD);


		double rho = 1.0;
		double theta = Math.PI/180;
		Mat lines = new Mat();

		Imgproc.HoughLinesP(edges, lines, rho, theta, HOUGH_TRESHOLD, HOUGH_MIN_LINE_LENGTH, HOUGH_MAX_LINE_GAP);

		Point[] laneEnds = processLanes(lines, edges, half_frame);

		//Log.w(TAG, "array needs initialized?");
		vehicleDetection(half_frame, mJavaDetector);
		drawVehicles(half_frame, laneEnds);
		//show_image(half_frame);
		/*
		saveframe = new Mat(mRgba.width(), mRgba.height(), mRgba.type());
		Imgproc.resize(half_frame, saveframe, new Size(mRgba.width(), mRgba.height()));
		*/
		//saveframe = half_frame.clone();

		return return_frame;
	}


	public void drawVehicles(Mat half_frame, Point[] laneEnds) {


		// show vehicles in the same lane
		for (int i = 0; i < vehicles.size(); i++) {
			Vehicle v = vehicles.get(i);

			if (v.valid == true) {

				boolean leftLaneInVehicle=laneEnds[0].x>=v.bmin.x-1&&laneEnds[0].x<=v.bmax.x+1&&laneEnds[0].y>=v.bmin.y-1&&laneEnds[0].y<=v.bmax.y+1;
				boolean RightLaneInVehicle=laneEnds[1].x>=v.bmin.x-1&&laneEnds[1].x<=v.bmax.x+1&&laneEnds[1].y>=v.bmin.y-1&&laneEnds[1].y<=v.bmax.y+1;
				if(leftLaneInVehicle==false&&RightLaneInVehicle==false)
					continue;

				Core.rectangle(return_frame, new Point(s*v.bmin.x, s*v.bmin.y), new Point(s*v.bmax.x, s*v.bmax.y), GREEN, 1);

				double midY = ((v.bmin.y + v.bmax.y) / 2);
				double distY = (v.bmax.y - v.bmin.y);

				Core.line(return_frame, new Point(s*v.symmetryX, s*half_frame.height()/2.0), new Point(s*v.symmetryX, s*(midY+distY/4.0)), PURPLE);
				Core.circle(return_frame, new Point(s*v.symmetryX, s*half_frame.height()/2.0), 2,PURPLE);
				Core.circle(return_frame, new Point(s*v.symmetryX, s*(midY+distY/4.0)), 2,PURPLE);
				Core.circle(return_frame, new Point(s*v.symmetryX, s*(half_frame.height()-2)), 2,PURPLE);

				double d1=s*(half_frame.height()/2.0-2)*0.0132; // 1 px = 0.264583 mm /0.0132
				double d2=s*(midY+distY/4.0-half_frame.height()/2.0)*0.0132;
				//Log.w(TAG, "~~~~~~~~~~~frame height: " + s*half_frame.height() + "~~~~~~~~~~~");
				Log.w(TAG, "~~~~~~~~~~~d1: " + d1 + "~~~~~~~~~~~");
				Log.w(TAG, "~~~~~~~~~~~d2: " + d2 + "~~~~~~~~~~~");
				distance=(1/d2-1/d1)*HEIGHT*FOCAL_LENGTH/1000.0;
				Log.w(TAG, "The following distance: " + distance + "m");

				getdistance();

				timer = new Timer();

				timerTask = new TimerTask() {
					@Override
					public void run() {
						handler.post(new Runnable() {
							@Override
							public void run() {
								if (d<safetyDistanceLong){
									if(speed>80){
										tailgatingCount++;
										distanceCount=0;
										if (tailgatingCount==50){
											//do something;
											int duration = Toast.LENGTH_SHORT;
											Toast toast = Toast.makeText(getApplicationContext(), "Tailgating !", duration);
											toast.show();
											mediaAlert();
										}
										if (brake&&tailgatingCount<2000){
											if (mp!=null) {
												mp.release();
											}
										}
										if (tailgatingCount==2000){
											mediaAlert();
										}



									}else{
										tailgatingCount=0;
									}

								}

							}
						});
					}
				};

				timer.schedule(timerTask, 1000, 1000);


			}


		}

		// show vehicle position sampling
	/*for (int i = 0; i < samples.size(); i++) {
		cvCircle(half_frame, cvPoint(samples[i].center.x, samples[i].center.y), samples[i].radi, RED);
	}*/
	}



	public void removeOldVehicleSamples(int currentFrame) {
		// statistical sampling - clear very old samples
		ArrayList<VehicleSample> sampl = new ArrayList<VehicleSample>();

		for (int i = 0; i < samples.size(); i++) {
			if (currentFrame - samples.get(i).frameDetected < MAX_VEHICLE_SAMPLES) {
				sampl.add(((VehicleSample)(samples.get(i))));
			}
		}
		samples = (ArrayList<VehicleSample>)sampl.clone();
	}

	public int findSymmetryAxisX(Mat half_frame, Point bmin, Point bmax) {
//		Log.w(TAG, "inside findSymmetryAxisX");
		//find symmtry x in the frame from point bmin to bmax
		double value = 0.0;
		int axisX = -1; // not found

		int xmin = (int)bmin.x;
		int ymin = (int)bmin.y;
		int xmax = (int)bmax.x;
		int ymax = (int)bmax.y;
		int half_width = half_frame.width()/2;
		int maxi = 1;


		for(int x=xmin, j=0; x<xmax; x++, j++) {
			float HS = 0;
			for(int y=ymin; y<ymax; y++) {
				int row = y;
				for(int step=1; step<half_width; step++) {
					int neg = x-step;
					int pos = x+step;
					int Gneg = (neg < xmin) ? 0 : (int)(half_frame.get(row,neg)[0]);
					int Gpos = (pos >= xmax) ? 0 : (int)(half_frame.get(row,pos)[0]);
					HS += Math.abs(Gneg-Gpos);
				}
			}

			if (axisX == -1 || value > HS) { // find minimum
				axisX = x;
				value = HS;
			}
		}

		return axisX;
	}

	public int pixel(Mat img, int x, int y) {
		//int size = (int) (img.total() * img.channels());
		//int[] temp = new int[size];
		//img.get(0, 0, temp);

		return (int)(img.get(y,x)[0]);
	}

	public boolean hasVertResponse(Mat edges, int x, int y, int ymin, int ymax) {
		//return true if (x,y) is a white pixel in the vertical black line from ymin to ymax
		boolean has = (pixel(edges, x, y) > BW_TRESHOLD);
		if (y-1 >= ymin) has &= (pixel(edges, x, y-1) < BW_TRESHOLD);
		if (y+1 < ymax) has &= (pixel(edges, x, y+1) < BW_TRESHOLD);
		return has;
	}

	public int horizLine(Mat edges, int x, int y, Point bmin, Point bmax, int maxHorzGap) {
		// find number of horizontal line pixels at y in the left and right half of frame from point bmin to bmax with maxHorzGap
		// scan to right
		int right = 0;
		int gap = maxHorzGap;
		for (int xx=x; xx<bmax.x; xx++) {
			if (hasVertResponse(edges, xx, y, (int)bmin.y, (int)bmax.y)) {
				right++;
				gap = maxHorzGap; // reset
			} else {
				gap--;
				if (gap <= 0) {
					break;
				}
			}
		}
		//scan to left
		int left = 0;
		gap = maxHorzGap;
		for (int xx=x-1; xx>=bmin.x; xx--) {
			if (hasVertResponse(edges, xx, y, (int)bmin.y, (int)bmax.y)) {
				left++;
				gap = maxHorzGap; // reset
			} else {
				gap--;
				if (gap <= 0) {
					break;
				}
			}
		}

		return left+right;
	}

	public boolean vehicleValid(Mat half_frame, Mat edges, Vehicle v, Integer index) {
		//Log.w(TAG, "inside vehicleValid");
		index = -1;

		// first step: find horizontal symmetry axis
		v.symmetryX = findSymmetryAxisX(half_frame, v.bmin.clone(), v.bmax.clone());
		//Log.w(TAG, "after findSymmetryAxisX");

		if (v.symmetryX == -1) return false;

		// second step: cars tend to have a lot of horizontal lines, find number of horizontal lines
		int hlines = 0;
		for (int y = (int)(v.bmin.y); y < v.bmax.y; y++) {
			if (horizLine(edges, v.symmetryX, y, v.bmin.clone(), v.bmax.clone(), 2) > CAR_H_LINE_LENGTH) {
//				Log.w(TAG, "after horizLine");
				//Core.circle(return_frame, new Point(s*v.symmetryX, s*y), 2, PURPLE);
				hlines++;
			}
		}
		//Log.w(TAG, "after all horizLine");

		int midy = (int)((v.bmax.y + v.bmin.y)/2);

		// third step: check with previous detected samples if car already exists
		int numClose = 0;
		double closestDist = 0;
		for (int i = 0; i < samples.size(); i++) {
			int dx = (int)(samples.get(i).center.x - v.symmetryX);
			int dy = (int)(samples.get(i).center.y - midy);
			double Rsqr = dx*dx + dy*dy;

			if (Rsqr <= samples.get(i).radi*samples.get(i).radi) { //if vehicle center is within the sample radius
				numClose++;  //increase the number of how many samples the vehicle is close to
				if (index == -1 || Rsqr < closestDist) {
					index = samples.get(i).vehicleIndex; //set index to the vehicleIndex of the closest sample
					closestDist = Rsqr;
				}
			}
		}
		Log.w(TAG, "hlines: "+hlines+" numClose: "+numClose+" Sample size: "+samples.size()+" index: "+index);
		return (hlines >= CAR_DETECT_LINES || numClose >= CAR_DETECT_POSITIVE_SAMPLES);

	}


	public void vehicleDetection(Mat half_frame, CascadeClassifier cascade) {
		int frame = 0;
		frame++;
		Log.w(TAG, "*** vehicle detector frame: ***" + frame);

		removeOldVehicleSamples(frame);

		// Haar Car detection
		double scale_factor = 1.05; // every iteration increases scan window by 5%
		int min_neighbours = 2; // minus 1, number of rectangles, that the object consists of

		MatOfRect rects_mat = new MatOfRect();
		cascade.detectMultiScale(half_frame, rects_mat, scale_factor, min_neighbours,
				1, new Size(0, 0),
				half_frame.size().clone());
		Rect[] rects = rects_mat.toArray();

		if (rects.length > 0) {
			Log.w(TAG, "haar detected " + rects.length + "car hypotheses");
			Mat edges = new Mat(half_frame.width(), half_frame.height(), mGray.type());
			Imgproc.Canny(half_frame, edges, CANNY_MIN_TRESHOLD, CANNY_MAX_TRESHOLD); //find edges in an image using Canny86 algorithm

		/* validate vehicles */
			for (int i = 0; i < rects.length; i++) {
				Rect rc = rects[i]; //return a pointer to a sequence element rects according to its index i
				Vehicle v = new Vehicle();
				v.bmin = new Point(rc.x, rc.y);
				v.bmax = new Point(rc.x + rc.width, rc.y + rc.height);
				v.valid = true;

				Integer index = new Integer(0);
				//Log.w(TAG, "before calling vehiclevalid");
				if (vehicleValid(half_frame, edges, v, index)) { // put a sample on that position
					//Log.w(TAG, "after calling vehiclevalid");

					if (index == -1) { // new car detected

						v.lastUpdate = frame;

						// re-use already created but inactive vehicles
						for(int j=0; j<vehicles.size(); j++) {
							if (vehicles.get(i).valid == false) {
								index = j;
								break;
							}
						}
						if (index == -1) { // all space used, push back new car into vehicles
							index = vehicles.size();
							vehicles.add(v);
						}
						Log.w(TAG, "new car detected, index = " + index);
					} else {
						//Log.w(TAG, "index is: " + index);

						// update the position from new data
						if (vehicles.size() == 0) {
							vehicles.add(v);
						} else {
							vehicles.set(index, v);
						}
						//Log.w(TAG, "v is null?: " + (v == null));
						vehicles.get(index).lastUpdate = frame;
						Log.w(TAG, "old car updated, index = " + index);
					}
					//push back new vehicle into samples
					VehicleSample vs = new VehicleSample();
					vs.frameDetected = frame;
					vs.vehicleIndex = index;
					vs.radi = (Math.max(rc.width, rc.height))/4; // radius twice smaller - prevent false positives
					vs.center = new Point((v.bmin.x+v.bmax.x)/2, (v.bmin.y+v.bmax.y)/2);
					samples.add(vs);
				}
				//Log.w(TAG, "before calling removelostvehicle in if clause");

			}
		} else {
			Log.w(TAG, "no vehicles detected in current frame!");
		}

		removeLostVehicles(frame);

		Log.w(TAG, "total vehicles on screen: " + vehicles.size());

	}

	public void removeLostVehicles(int currentFrame) {
		// remove old unknown/false vehicles & their samples, if any
		for (int i=0; i<vehicles.size(); i++) {
			if (vehicles.get(i).valid && currentFrame - vehicles.get(i).lastUpdate >= MAX_VEHICLE_NO_UPDATE_FREQ) {
				Log.w(TAG, "removing inactive car, index = "+ i);
				removeSamplesByIndex(i);
				vehicles.get(i).valid = false;
			}
		}
	}

	public void removeSamplesByIndex(int index) {
		// statistical sampling - clear samples with certain index
		ArrayList<VehicleSample> sampl = new ArrayList<VehicleSample>();
		for (int i = 0; i < samples.size(); i++) {
			if (samples.get(i).vehicleIndex != index) {
				sampl.add(samples.get(i));
			}
		}
		samples = (ArrayList<VehicleSample>)sampl.clone();
	}


	public Point[] processLanes(Mat lines, Mat edges, Mat temp_frame) {
		//Log.w(TAG, "I'm inside processLanes");
		ArrayList<Lane> left = new ArrayList<Lane>();
		ArrayList<Lane> right = new ArrayList<Lane>();
		Point[] laneEnds = new Point[2];
//		Log.w(TAG, "lines col is:" + lines.cols());
		for(int i = 0; i < lines.cols(); i++) {
			double[] val = lines.get(0, i);
			Point[] line = new Point[2];
			line[0] = new Point();
			line[1] = new Point();

			line[0].x = val[0];
			line[0].y = val[1];
			line[1].x = val[2];
			line[1].y = val[3];
			double dx = line[1].x - line[0].x;
			double dy = line[1].y - line[0].y;
			double angle = Math.atan2(dy, dx) * 180/Math.PI;

			if (Math.abs(angle) <= LINE_REJECT_DEGREES) { // reject near horizontal lines
				continue;
			}

			// assume that vanishing point is close to the image horizontal center
			// calculate line parameters: y = kx + b;
			dx = (((int)dx) == 0) ? 1 : dx; // prevent DIV/0!
			double k = dy/(double)dx;
			double b = line[0].y - k*line[0].x;
			int midx = (int)(Math.round((line[0].x + line[1].x) / 2));
			if (midx < temp_frame.width()/2) {
				left.add(new Lane(line[0].clone(), line[1].clone(), angle, k, b));
			} else if (midx > temp_frame.width()/2) {
				right.add(new Lane(line[0].clone(), line[1].clone(), angle, k, b));
			}

		}
		//ArrayList<Lane> leftcopy = (ArrayList<Lane>)left.clone();
		//ArrayList<Lane> rightcopy = (ArrayList<Lane>)right.clone();
		//Log.w(TAG, "before calling processSide");

		processSide(left, edges, false);
		processSide(right, edges, true);
		//Log.w(TAG, "after calling processSide");

		double x = (temp_frame.width() * 0.52);
		double x2 = (temp_frame.width());

		Core.line(return_frame, new Point(s*x, s*laneR.k.get()*x + s*laneR.b.get()),
				new Point(s*x2, s*laneR.k.get() * x2 + s*laneR.b.get()), PURPLE, 1);
		laneEnds[0]=new Point(x, laneR.k.get() * x + laneR.b.get());
		//Log.w(TAG, "landEnds[0] is: " + s*laneEnds[0].x + ", " + s*laneEnds[0].y);
		Core.circle(return_frame, new Point(s*x, s*(laneR.k.get() * x + laneR.b.get())), 2, PURPLE);


		x = 0;
		x2 = (temp_frame.width() * 0.48);
		Core.line(return_frame, new Point(s*x, s*laneL.k.get()*x + s*laneL.b.get()),
				new Point(s*x2, s*laneL.k.get() * x2 + s*laneL.b.get()), PURPLE, 1);
		laneEnds[1]= new Point(x2, laneL.k.get() * x2 + laneL.b.get());
		//Log.w(TAG, "landEnds[1] is: " + s*laneEnds[1].x + ", " + s*laneEnds[1].y);
		Core.circle(return_frame, new Point(s*x2, s*(laneL.k.get() * x2 + laneL.b.get())), 2,PURPLE);


		return laneEnds;

	}

	public void FindResponses(Mat img, int startX, int endX, int y, ArrayList<Integer> list) {
		int row = y;
		//int size = (int) (img.total() * img.channels());
		//byte[] temp = new byte[size];
		//img.get(0, 0, temp);
		//Log.w(TAG, "temp size is: " + temp.length);
		/*
		for (int i=0; i < temp.length; i++) {
			if (temp[i] > 0)
			Log.w(TAG, "pixel " + "i is: " + temp[i]);
		}
		*/
		//Log.w(TAG, "in find responses");
		int step = (endX < startX) ? -1: 1;
		int range = (endX > startX) ? endX-startX+1 : startX-endX+1;

		int startX_test = startX;
		int range_test = range;

		for(int x = startX; range>0 && Math.abs(x-startX) < 40; x += step, range--)
		{
			if(row < 0 || row > img.height() - 1) {
				Log.w(TAG, "out of bound");
				Log.w(TAG, "access row is: " + row + "but height is: " + (img.height()-1));
				Log.w(TAG, "access column is: " + x + "but width is: " + (img.width()-1));
				Log.w(TAG, "endX is: " + endX);
				Log.w(TAG, "initial start is: " + startX_test);
				Log.w(TAG, "initial range is:" + range_test);
				Log.w(TAG, "final range is:" + range);
				Log.w(TAG, "final x is:" + x);

			}
			//Log.w(TAG, "final range is:" + range);
			//Log.w(TAG, "final x is:" + x);
			//Log.w(TAG, "width is:" + (img.width()-1));

			if (img.get(row,x) == null) {
				Log.w(TAG, "pixel is null");
				continue;
			}
			if (img.get(row,x).length == 0) {
				Log.w(TAG, "array lenght is zero");
				continue;
			}

			if((int)(img.get(row,x)[0]) <= BW_TRESHOLD) continue; // skip black: loop until white pixels show up

			// first response found
			int idx = x + step;

			// skip same response(white) pixels
			while(range > 0 && (int)(img.get(row,idx)[0]) > BW_TRESHOLD){
				idx += step;
				range--;
			}

			// reached black again
			if((int)(img.get(row,idx)[0]) <= BW_TRESHOLD) {
				list.add(x);
			}

			x = idx; // begin from new pos
		}
		//Log.w(TAG, "about to return from find responses");

	}


	// dist2line() implementation
	public Point sub(Point b, Point a) { return new Point(b.x-a.x, b.y-a.y); }
	public Point mul(Point b, Point a) { return new Point(b.x*a.x, b.y*a.y); }
	public Point add(Point b, Point a) { return new Point(b.x+a.x, b.y+a.y); }
	public Point mul(Point b, double t) { return new Point(b.x*t, b.y*t); }
	public double dot(Point a, Point b) { return (b.x*a.x + b.y*a.y); }
	public double dist(Point v) { return Math.sqrt(v.x*v.x + v.y*v.y); }

	public Point point_on_segment(Point line0, Point line1, Point pt){
		//return projection of pt on segment line0 to line1
		Point v = sub(pt.clone(), line0.clone());
		Point dir = sub(line1.clone(), line0.clone());
		double len = dist(dir.clone());
		double inv = 1.0/(len+0.000001);
		dir.x *= inv;
		dir.y *= inv;

		double t = dot(dir.clone(), v.clone()); //v.x*dir.x/len+v.y*dir.y/len
		if(t >= len) return line1; //if pt is beyond line1
		else if(t <= 0) return line0; //if pt is below line0

		return add(line0.clone(), mul(dir.clone(),t)); //if pt is between line0 and line1, return line0 + v.x*dir.x^2/len^2+v.y*dir.y^2/len^2
	}

	public double dist2line(Point line0, Point line1, Point pt){
		//return distance from pt to segment line0 to line1
		return dist(sub(point_on_segment(line0.clone(), line1.clone(), pt.clone()), pt.clone()));
	}
	// dist2line implementation done


	public void processSide(ArrayList<Lane> lanes_list, Mat edges, boolean right) {
		//Log.w(TAG, "inside processSide");
		Status side = right ? laneR : laneL;

		// response search
		int w = edges.width();
		int h = edges.height();
		int BEGINY = 0;
		int ENDY = h-1;
		int ENDX = right ? (w-BORDERX) : BORDERX;
		int midx = w/2;
		int midy = edges.height()/2;
		//Log.w(TAG, "edges total element is: " + edges.total());
		//int size = (int) (edges.total() * edges.channels());
		//byte[] temp = new byte[size];
		//edges.get(0, 0, temp);

		//Lane[] a = new Lane[3];
		//Lane[] lanes = (lanes_list.toArray(a));
		Lane[] lanes_type = new Lane[lanes_list.size()];
		Lane[] lanes = lanes_list.toArray(lanes_type);
//		if (lanes.length == lanes_list.size()) {
//			Log.w(TAG, "lane size is right");
//		}
//
//		int[] votes = new int[lanes.length];
//		for(int i=0; i<lanes.length; i++) votes[i++] = 0;
//
//		//Log.w(TAG, "before calling FindResponses");
//
//		for(int y=ENDY; y>=BEGINY; y-=SCAN_STEP) {
//			ArrayList<Integer> rsp = new ArrayList<Integer>();
//			FindResponses(edges, midx, ENDX, y, rsp);
//
//			if (rsp.size() > 0) {
//				int response_x = rsp.get(0); // use first reponse (closest to screen center)
//
//				double dmin = 9999999;
//				double xmin = 9999999;
//				int match = -1;
//				for (int j=0; j<lanes.length; j++) {
//					// compute response point distance to current line
//					double d = dist2line(
//							new Point(lanes[j].p0.x, lanes[j].p0.y),
//							new Point(lanes[j].p1.x, lanes[j].p1.y),
//							new Point(response_x, y));
//
//					// point on line at current y line
//					double xline = (y - lanes[j].b) / lanes[j].k;
//					double dist_mid = Math.abs(midx - xline); // distance to midpoint
//
//					// pick the best closest match to line & to screen center
//					if (match == -1 || (d <= dmin && dist_mid < xmin)) {
//						dmin = d;
//						match = j;
//						xmin = dist_mid;
//						break;
//					}
//				}
//
//				// vote for each line
//				if (match != -1) {
//					votes[match] += 1;
//				}
//			}
//		}
		//Log.w(TAG, "after calling FindResponses");


		Log.w(TAG, "lanes length: " + lanes.length);

		int bestMatch = -1;
		int mini = 9999999;
		double maxi = -9999999;
		for (int i=0; i<lanes.length; i++) {

			if(right){
				if(lanes[i].angle<0)
					continue;
			}else
			if(lanes[i].angle>0)
				continue;

			//Log.w(TAG, "i'm in for loop");
			int xline = (int)(Math.round((midy - lanes[i].b) / lanes[i].k));
			int distx = Math.abs(midx - xline); // x distance to midpoint
			int yline = (int)(Math.round(midx*lanes[i].k+lanes[i].b));
			int disty = Math.abs(midy - yline); // y distance to midpoint
			//Log.w(TAG, "dist to midpoint: " + dist);
			Log.w(TAG, "lane angle: " + lanes[i].angle);
			//if (bestMatch == -1 || (votes[i] > votes[bestMatch] && distx < mini)) { //find the best match lane
			if (bestMatch == -1 || distx+disty < mini) {//find the best match lane

				if(Math.abs(lanes[i].angle)>maxi-2)
					bestMatch = i;
				mini = distx+disty;
//				Log.w(TAG,  "distx: " +distx+" disty: " +disty+" mini: "+mini);
			}

			if(bestMatch == -1 ||Math.abs(lanes[i].angle)>maxi){

				if(distx+disty<mini+5)
					bestMatch = i;
				maxi = Math.abs(lanes[i].angle);
//				Log.w(TAG,  "distx: " +distx+" disty: " +disty+" angle: "+maxi);
			}
			Log.w(TAG,  "distx: " +distx+" disty: " +disty+" angle: "+lanes[i].angle+" mini: "+mini);
		}

		Lane best;
		if (bestMatch != -1) {
			best = lanes[bestMatch];

			double k_diff = Math.abs(best.k - side.k.get());
			double b_diff = Math.abs(best.b - side.b.get());

			boolean update_ok = (k_diff <= K_VARY_FACTOR && b_diff <= B_VARY_FACTOR) || side.reset;

			//Log.w(TAG, "side: %s, k vary: %.4f, b vary: %.4f, lost: %s" +
			//		(right? + "RIGHT":"LEFT"), k_diff, b_diff, (update_ok?"no":"yes"));

			if (update_ok == true) {
				// update is in valid bounds
				side.k.add(best.k);
				side.b.add(best.b);
				side.reset = false;
				side.lost = 0;
			} else {
				// can't update, lanes flicker periodically, start counter for partial reset!
				side.lost++;
				if (side.lost >= MAX_LOST_FRAMES && !side.reset) {
					side.reset = true;
				}
			}

		} else {
			Log.w(TAG, "no lanes detected - lane tracking lost! counter increased");
			side.lost++;
			if (side.lost >= MAX_LOST_FRAMES && !side.reset) {
				// do full reset when lost for more than N frames
				side.reset = true;
				side.k.clear();
				side.b.clear();
			}
		}

	}

	VehicleSpeed.Listener vSpeedListener = new VehicleSpeed.Listener(){


		@Override
		public void receive(Measurement measurement2) {

			final VehicleSpeed vSpeed = (VehicleSpeed) measurement2;

			CarActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mVehicleSpeedView.setText("Vehicle Speed (km/hr): "
							+ vSpeed.getValue().doubleValue());
					speed = vSpeed.getValue().doubleValue();
				}
			});

		}
	};

	BrakePedalStatus.Listener brakeStatusListener = new BrakePedalStatus.Listener(){

		@Override
		public void receive(Measurement measurement4) {

			final BrakePedalStatus brakePedalStatus = (BrakePedalStatus) measurement4;

			CarActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {

					if (brakePedalStatus.getValue().booleanValue()){
						brakeStatus.setText(R.string.On_Brake);
						brake = true;
					}else{
						brakeStatus.setText(R.string.No_Brake);
						brake = false;
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


	/*public Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == UPDATE_TAG ) {


				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(getApplicationContext(), "Tailgating !", duration);
				toast.show();

			}
		}
	};*/


	private void mediaAlert(){

		mp = new MediaPlayer();
		try {
			mp.setDataSource(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			mp.prepare();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mp.start();

	}
}
