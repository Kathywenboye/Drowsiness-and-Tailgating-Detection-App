package org.opencv.samples.facedetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

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
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Parcelable;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.widget.Toast;
import android.widget.Button;
import android.os.Parcel;
import android.os.Environment;
import android.content.Intent;



public class FdActivity extends Activity implements CvCameraViewListener2, Parcelable {

	public String TAG = "OCVSample::Activity";
	private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
	public static final int JAVA_DETECTOR = 0;
	private static final int TM_SQDIFF = 0;
	private static final int TM_SQDIFF_NORMED = 1;
	private static final int TM_CCOEFF = 2;
	private static final int TM_CCOEFF_NORMED = 3;
	private static final int TM_CCORR = 4;
	private static final int TM_CCORR_NORMED = 5;


	private int learn_frames = 0;
	private Mat teplateR;
	private Mat teplateL;
	int method = 1;
	int eye_open = 0;
	int eye_close = 0;
	double min_val = 9999;
	double maxarea_prev = 0;
	double maxarea_this = 0;
	boolean first = true;
	boolean first_closed = true;
	boolean has_timer = false;

	private MenuItem mItemFace50;
	private MenuItem mItemFace40;
	private MenuItem mItemFace30;
	private MenuItem mItemFace20;
	private MenuItem mItemType;

	public Userpage user;
	public static Mat mRgba;
	public static Mat mGray;
	public static Mat mRgbart;
	public static Mat test = null;
	public static Mat righteye_save_pass = null;

	// matrix for zooming
	public static Mat mZoomWindow;
	private Mat mZoomWindow2 = null;

	public static CvCameraViewFrame cameraframe;

	public Mat leftEye_template;
	public Mat leftpupil_template;
	public Mat rightEye_template;
	public Mat rightpupil_template;
	public Mat RERE_template;
	public Mat RELE_template;
	public Mat LERE_template;
	public Mat LELE_template;
	public Mat pupil_template;
	public Mat imgray;
	public Rect eye_right;
	public Rect eye_right_closed;
	public Rect eye_left;
	public Rect eye_left_closed;
	public Mat zoom_eye_right;
	public Mat zoom_eye_right_closed;
	public Mat zoom_eye_left;
	public Mat zoom_eye_left_closed;

	public static Mat zoomwindow_aux;
	public static Rect eyearea_right;
	public static Mat imrgb;
	public static Mat zoomwindow_sec;
	public Button b;
	public static String user_name;
	public Button popbutton;
	public Button popbutton2;
	List<Integer> oneminuteclosed = new ArrayList<>();
	List<Integer> oneminuteopen = new ArrayList<>();

	public boolean firstminute = true;
	public int seconds_curr = 0;
	public int seconds_prev = 0;
	public int closedcount = 0;
	public int opencount = 0;
	public int totalclosedcount = 0;
	public int totalopencount = 0;
	public int totalClosedCount2=0;
	public int totalOpenCount2=0;
	public double closedpercentage = 0.0;
	public double closedPercentage2 = 0.0;
	public boolean odd = false;
	public long begintime = 0;
	public long starttime = 0;

	public int count = 0;
	public int count2 = 0;
	private MediaPlayer mp;


	private File mCascadeFile;
	private CascadeClassifier mJavaDetector;
	private CascadeClassifier mJavaDetectorEye;


	private int mDetectorType = JAVA_DETECTOR;
	private String[] mDetectorName;

	private float mRelativeFaceSize = 0.2f;
	private int mAbsoluteFaceSize = 0;

	private CameraBridgeViewBase mOpenCvCameraView;

	private SeekBar mMethodSeekbar;
	private TextView mValue;
	private int mData;


	double xCenter = -1;
	double yCenter = -1;
	public static Rect r = null;
	public static int[] anchors_pass = new int[8];
	public TextView output;
	public Point center;

	//
	//

	public static int getImageState = 0;
	int leftEyeState = 1;
	int LELEState = 2;
	int LELEanchorState = 3;
	int LEREState = 4;
	int LEREanchorState = 5;
	int RightEyeState = 6;
	int RELEState = 7;
	int RELEanchorState = 8;
	int REREState = 9;
	int REREanchorState = 10;
	public static int liveState = 11;

	Point RE, LE, FACE, REP, LEP;
	Point LELE, LERE, RELE, RERE, RP, LP;
	int matchType = 3;
//         0 = square difference
//         1 = square difference normalized
//         2 = correlation matching
//         3 = correlation matching normalized ----
//         4 = correlation coefficient - inconsisten
//         5 = correlation coefficient normalized - inconsisten


	// global variables
	Rect box;
	Point anchor;
	boolean mouse_draw_anchor = false;
	boolean mouse_draw_box = false;

	// LELE, LERE, RELE, RERE

	Timer timer;
	//Timer timer_sec;
	TimerTask timerTask;
	//TimerTask timerTasksec;

	//we are going to use a handler to be able to run in our TimerTask
	Handler handler = new Handler();
	//Handler handlersec = new Handler();


	public static int state = getImageState; // live state if you arent creating a new profile
	double scale = 1.0/10; // how far do we want to scale down the haar detect objects images for speed

	boolean init = true;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS: {
					Log.i(TAG, "OpenCV loaded successfully");


					try {
						// load cascade file from application resources
						InputStream is = getResources().openRawResource(
								R.raw.haarcascade_frontalface_alt);
						File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
						mCascadeFile = new File(cascadeDir,
								"haarcascade_frontalface_alt.xml");
						FileOutputStream os = new FileOutputStream(mCascadeFile);

						byte[] buffer = new byte[4096];
						int bytesRead;
						while ((bytesRead = is.read(buffer)) != -1) {
							os.write(buffer, 0, bytesRead);
						}
						is.close();
						os.close();

					/*
					// --------------------------------- load left eye
					// classificator -----------------------------------
					InputStream iser = getResources().openRawResource(
							R.raw.haarcascade_lefteye_2splits);
					//InputStream iser = getResources().openRawResource(
					//		R.raw.haarcascade_mcs_lefteye);

					File cascadeDirER = getDir("cascadeER",
							Context.MODE_PRIVATE);
					File cascadeFileER = new File(cascadeDirER,
							"haarcascade_eye_right.xml");
					//File cascadeFileER = new File(cascadeDirER,
					//		"haarcascade_eye_left.xml");
					FileOutputStream oser = new FileOutputStream(cascadeFileER);

					byte[] bufferER = new byte[4096];
					int bytesReadER;
					while ((bytesReadER = iser.read(bufferER)) != -1) {
						oser.write(bufferER, 0, bytesReadER);
					}
					iser.close();
					oser.close();
					*/

						mJavaDetector = new CascadeClassifier(
								mCascadeFile.getAbsolutePath());
						if (mJavaDetector.empty()) {
							Log.e(TAG, "Failed to load cascade classifier");
							mJavaDetector = null;
						} else
							Log.i(TAG, "Loaded cascade classifier from "
									+ mCascadeFile.getAbsolutePath());

					/*
					mJavaDetectorEye = new CascadeClassifier(
							cascadeFileER.getAbsolutePath());
					if (mJavaDetectorEye.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mJavaDetectorEye = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from "
								+ mCascadeFile.getAbsolutePath());
					*/


						cascadeDir.delete();
						Bundle b = getIntent().getExtras();
						user = b.getParcelable("org.opencv.samples.facedetect.start");
						user_name = user.username;
						if (state == liveState) {
							user.currstate = user.live;
						}

						popbutton = (Button) findViewById(R.id.pop);
						//popbutton2 = (Button) findViewById(R.id.pop2);

						if (user.currstate == user.live || state == liveState) {
							timer = new Timer();
							initializeTimerTask();
							timer.schedule(timerTask, 1000, 1000);

							output = (TextView) findViewById(R.id.outputtext);

							popbutton.setVisibility(View.GONE);
							//popbutton2.setVisibility(View.GONE);
							load_AND_display();
							String anchorstring = load_anchors(user.username + "anchors.txt");
							int pos = 0;
							int start = 0;
							int end = 0;
							int j = 0;
							Log.w(TAG, "anchorstring is: " + anchorstring);
							while (pos <= anchorstring.length()){
								Log.w(TAG, "position is: " + pos);
								if (pos == anchorstring.length()) {
									anchors_pass[j] = Integer.parseInt(anchorstring.substring(start));
									Log.w(TAG, "anchor_pass " + j + " is: " + anchors_pass[j]);
								}
								else if (anchorstring.charAt(pos) == ' ') {
									end = pos;
									Log.w(TAG, "start is: " + start);
									Log.w(TAG, "end is: " + end);
									Log.w(TAG, "substring is: " + anchorstring.substring(start, end));
									anchors_pass[j++] = Integer.parseInt(anchorstring.substring(start, end));
									start = pos + 1;
									Log.w(TAG, "anchor_pass " + j + " is: " + anchors_pass[j]);
								}
								pos++;
							}
							Log.w(TAG, "anchor RERE is: " + anchors_pass[6] + ", " + anchors_pass[7]);
							Log.w(TAG, "anchor RELE is: " + anchors_pass[4] + ", " + anchors_pass[5]);
							Log.w(TAG, "anchor LERE is: " + anchors_pass[2] + ", " + anchors_pass[3]);
							Log.w(TAG, "anchor LELE is: " + anchors_pass[0] + ", " + anchors_pass[1]);

						} else {
							popbutton.setVisibility(View.VISIBLE);
							//popbutton2.setVisibility(View.VISIBLE);
						}
					} catch (IOException e) {
						e.printStackTrace();
						Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
					}
					mOpenCvCameraView.setCameraIndex(1);
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

	public String load_anchors(String name) {
		try {
			FileInputStream inputStream = openFileInput(name);
			BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder total = new StringBuilder();
			String line;
			while ((line = r.readLine()) != null) {
				total.append(line);
			}
			r.close();
			inputStream.close();
			Log.w(TAG, "anchor points: " + total);
			return total.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}


	public void load_AND_display() {

		leftEye_template = load_image(user.username + "lefteye.jpg");
		leftpupil_template = load_image(user.username + "leftpupil.jpg");

		//test = load_image_sec(user.username + "rightrighteye.jpg");
		//if (leftEye_template.empty())
		//	Log.w(TAG, "left eye from main is empty");
		rightEye_template = load_image(user.username + "righteye.jpg");
		rightpupil_template = load_image(user.username + "rightpupil.jpg");

		//RERE_template = load_image(user.username + "rightrighteye.jpg");
		//RELE_template = load_image(user.username + "rightlefteye.jpg");
		//LERE_template = load_image(user.username + "leftrighteye.jpg");
		//LELE_template = load_image(user.username + "leftlefteye.jpg");
		try{
			pupil_template = Utils.loadResource(this, R.drawable.pupil, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (leftEye_template.empty() || rightEye_template.empty() ||  pupil_template.empty())
			Log.w(TAG, "Failed to load the image");
		else
			Log.w(TAG, "successfully loading image");

	}

	public Mat load_image(String name) {
		String filename = name;
		File file = new File(getApplicationContext().getFilesDir(), filename);
		filename = file.toString();
		Log.w(TAG, "filename is: " + filename);
		//storage/emulated/0/Pictures/lefteye.jpg
		return Highgui.imread(filename, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		//Highgui.CV_LOAD_IMAGE_GRAYSCALE

	}

	public Mat load_image_sec(String name) {
		String filename = name;
		File file = new File(getApplicationContext().getFilesDir(), filename);
		filename = file.toString();
		Log.w(TAG, "filename is: " + filename);
		//storage/emulated/0/Pictures/lefteye.jpg
		return Highgui.imread(filename);
		//Highgui.CV_LOAD_IMAGE_GRAYSCALE

	}

	public FdActivity() {
		mDetectorName = new String[2];
		mDetectorName[JAVA_DETECTOR] = "Java";

		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** Called when the activity is first created. */
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.face_detect_surface_view);

		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
		mOpenCvCameraView.setCvCameraViewListener(this);

		/*
		mMethodSeekbar = (SeekBar) findViewById(R.id.methodSeekBar);
		mValue = (TextView) findViewById(R.id.method);


		mMethodSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				method = progress;
				switch (method) {
				case 0:
					mValue.setText("TM_SQDIFF");
					break;
				case 1:
					mValue.setText("TM_SQDIFF_NORMED");
					break;
				case 2:
					mValue.setText("TM_CCOEFF");
					break;
				case 3:
					mValue.setText("TM_CCOEFF_NORMED");
					break;
				case 4:
					mValue.setText("TM_CCORR");
					break;
				case 5:
					mValue.setText("TM_CCORR_NORMED");
					break;
				}


			}
		});
		*/
	}


	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null) {
			mOpenCvCameraView.disableView();
		}

		if (mp!=null){
			mp.release();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);

	}

	public void onDestroy() {
		super.onDestroy();
		mOpenCvCameraView.disableView();
		if (mp!=null){
			mp.release();
		}
	}

	public void onCameraViewStarted(int width, int height) {

	}

	public void onCameraViewStopped() {

	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();
		mRgbart = mRgba.clone();



		if (mAbsoluteFaceSize == 0) {
			int height = mGray.rows();
			if (Math.round(height * mRelativeFaceSize) > 0) {
				mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
			}
		}

		if (mZoomWindow == null || mZoomWindow2 == null)
			CreateAuxiliaryMats();

		MatOfRect faces = new MatOfRect();

		if (mJavaDetector != null)
			mJavaDetector.detectMultiScale(mGray, faces, 1.2, 2,
					2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
					new Size(mAbsoluteFaceSize, mAbsoluteFaceSize),
					new Size());

		Rect[] facesArray = faces.toArray();
		double area = 0;
		int index = 0;
		double maxarea = 0;
		if (facesArray.length > 0) {
			index = 0;
			maxarea = facesArray[0].width * facesArray[0].height;
		}
		for (int i = 1; i < facesArray.length; i++) {
			area = facesArray[i].width * facesArray[i].height;
			if (area > maxarea) {
				maxarea = area;
				index = i;
			}
		}



		if (facesArray.length > 0) {
			if (first == true) {
				first = false;
				maxarea_this = maxarea;
			} else {
				maxarea_prev = maxarea_this;
				maxarea_this = maxarea;
				if ((maxarea_this < maxarea_prev * 0.7)) {
					Log.w(TAG, "bad capture");
					maxarea_this = maxarea_prev;
					return mRgba;
				}
			}
			//Log.w(TAG, "maxarea_this is: " + maxarea_this);
			//Log.w(TAG, "maxarea_prev is: " + maxarea_prev);




			Core.rectangle(mRgba, facesArray[index].tl(), facesArray[index].br(),
					FACE_RECT_COLOR, 3);


			xCenter = (facesArray[index].x + facesArray[index].width + facesArray[index].x) / 2;
			yCenter = (facesArray[index].y + facesArray[index].y + facesArray[index].height) / 2;
			center = new Point(xCenter, yCenter);


			/*
			Core.putText(mRgba, "[" + center.x + "," + center.y + "]",
					new Point(center.x + 20, center.y + 20),
					Core.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(255, 255, 255,
							255));
							*/

			r = facesArray[index];

			if (FdActivity.this.r == null) {
				Log.w(TAG, "transmitter.r is null");
			} else {
				Log.w(TAG, "transmitter.r is not null" + r.width + ", " + r.height);

			}
			Log.w(TAG, "RGba" + mRgba.width() + ", " + mRgba.height());

			double s = ((double)(r.width))/480.0;


			Rect facerect = r.clone();
			imgray = mGray.submat(facerect);
			imrgb = mRgbart.submat(facerect);
			zoomwindow_sec = new Mat(mRgbart.rows(), mRgbart.rows(), mRgbart.type());
			Imgproc.resize(imrgb, zoomwindow_sec, zoomwindow_sec.size());

			zoomwindow_aux = new Mat(mGray.rows(), mGray.rows(), mGray.type());
			Imgproc.resize(imgray, zoomwindow_aux, zoomwindow_aux.size());

			// compute the eye area
			Rect eyearea = new Rect(r.x + r.width / 8,
					(int) (r.y + (r.height / 4.5)), r.width - 2 * r.width / 8,
					(int) (r.height / 3.0));
			// split it

			int side = 480;
			eyearea_right = new Rect(side / 16,
					2*side / 7,
					side/2 - side / 16, side / 5);
			Rect eyearea_left = new Rect(side / 2,
					2*side / 7,
					side/2 - side / 16, side / 5);


			Rect eyearea_right_draw = new Rect(r.x + r.width / 16,
					(int) (r.y + (2*r.height / 7)),
					(r.width - 2 * r.width / 16) / 2, (int) (r.height / 5.0));
			Rect eyearea_left_draw = new Rect(r.x + r.width / 16
					+ (r.width - 2 * r.width / 16) / 2,
					(int) (r.y + (2*r.height / 7)),
					(r.width - 2 * r.width / 16) / 2, (int) (r.height / 5.0));
			// draw the area - mGray is working grayscale mat, if you want to
			// see area in rgb preview, change mGray to mRgba
			Core.rectangle(mRgba, eyearea_left_draw.tl(), eyearea_left_draw.br(),
					new Scalar(255, 0, 0, 255), 2);
			Core.rectangle(mRgba, eyearea_right_draw.tl(), eyearea_right_draw.br(),
					new Scalar(255, 0, 0, 255), 2);

			//detect_eye_open_close(eyearea_left, method, eyearea_left.tl(), eyearea_left.br(), open_left_eye, "open");
			//detect_eye_open_close(eyearea_left, method, eyearea_left.tl(), eyearea_left.br(), closed_left_eye, "closed");
			//min_val = 9999;

			Log.w(TAG, "face size: " + r.width + ", " + r.height);         // 364 * 364
			Log.w(TAG, "right eye size: " + eyearea_right.width + ", " + eyearea_right.height);   // 159 * 121

			if (user.currstate == user.live || state == liveState) {



				RE = new Point(0, 0);
				LE = new Point(0, 0);
				REP = new Point(0, 0);
				LEP = new Point(0, 0);

				RE.x += side / 16;
				RE.y += 2*side / 7;
				LE.x += side / 2;
				LE.y += 2*side / 7;

				REP.x += side / 16;
				REP.y += 2*side / 7;
				LEP.x += side / 2;
				LEP.y += 2*side / 7;


				Core.circle(mRgba, r.tl(), 10, FACE_RECT_COLOR, 2);

				Core.circle(mRgba, new Point(r.x + (int)(((double)(RE.x)) * s), r.y + (int)(((double)(RE.y)) * s)), 5, new Scalar(255, 0, 0, 255), 2);

				Point tp = find_eye(eyearea_right, method, "right","notpupil");
				Point tmp = find_eye(eyearea_right, method, "right","ispupil");
				RE.x += tp.x;
				RE.y += tp.y;
				REP.x += tmp.x;
				REP.y += tmp.y;

				//Core.circle(mRgba, center, 10, new Scalar(255, 0, 0, 255), 3);





				//Core.circle(mRgba, new Point(r.x + (int)(((double)(RE.x)) * s), r.y + (int)(((double)(RE.y)) * s)), 5, new Scalar(255, 0, 0, 255), 2);


				Log.w(TAG, "find right eye corr point: " + tp.x + ", " + tp.y);
				Log.w(TAG, "scale is: " + s);
				tp = find_eye(eyearea_left, method, "left", "notpupil");
				tmp = find_eye(eyearea_left, method, "left", "ispupil");
				LE.x += tp.x;
				LE.y += tp.y;
				LEP.x += tmp.x;
				LEP.y += tmp.y;

				Rect eyearea_right_right = new Rect(0, 0,
						eye_right.width / 2, eye_right.height);
				Rect eyearea_right_left = new Rect(eye_right.width / 2, 0,
						eye_right.width / 2, eye_right.height);
				Rect eyearea_left_right = new Rect(0, 0,
						eye_left.width / 2, eye_left.height);
				Rect eyearea_left_left = new Rect(eye_left.width / 2, 0,
						eye_left.width / 2, eye_left.height);

				int[] RERE_anchor_points = {anchors_pass[6], anchors_pass[7]};
				Log.w(TAG, "anchor RERE is: " + anchors_pass[6] + ", " + anchors_pass[7]);
				RERE = new Point();
				RERE.x = RERE_anchor_points[0];
				RERE.y = RERE_anchor_points[1];

				RERE.x += RE.x;
				RERE.y += RE.y;


				int[] RELE_anchor_points = {anchors_pass[4], anchors_pass[5]};
				Log.w(TAG, "anchor RELE is: " + anchors_pass[4] + ", " + anchors_pass[5]);
				RELE = new Point();

				RELE.x = RELE_anchor_points[0];
				RELE.y = RELE_anchor_points[1];
				RELE.x += RE.x;
				RELE.y += RE.y;

				int[] LERE_anchor_points = {anchors_pass[2], anchors_pass[3]};
				Log.w(TAG, "anchor LERE is: " + anchors_pass[2] + ", " + anchors_pass[3]);
				LERE = new Point();

				LERE.x = LERE_anchor_points[0];
				LERE.y = LERE_anchor_points[1];
				LERE.x += LE.x;
				LERE.y += LE.y;

				int[] LELE_anchor_points = {anchors_pass[0], anchors_pass[1]};
				Log.w(TAG, "anchor LELE is: " + anchors_pass[0] + ", " + anchors_pass[1]);
				LELE = new Point();

				LELE.x = LELE_anchor_points[0];
				LELE.y = LELE_anchor_points[1];
				LELE.x += LE.x;
				LELE.y += LE.y;

				RP = findPupil(zoom_eye_right, pupil_template, method, "right");
				Log.w(TAG, "find right pupil corr point: " + RP.x + ", " + RP.y);

				RP.x += REP.x;
				RP.y += REP.y;

				LP = findPupil(zoom_eye_left, pupil_template, method, "left");
				LP.x += LEP.x;
				LP.y += LEP.y;

				RERE.x = r.x + (int)(((double)(RERE.x)) * s);
				RERE.y = r.y + (int)(((double)(RERE.y)) * s);

				RELE.x = r.x + (int)(((double)(RELE.x)) * s);
				RELE.y = r.y + (int)(((double)(RELE.y)) * s);

				LERE.x = r.x + (int)(((double)(LERE.x)) * s);
				LERE.y = r.y + (int)(((double)(LERE.y)) * s);

				LELE.x = r.x + (int)(((double)(LELE.x)) * s);
				LELE.y = r.y + (int)(((double)(LELE.y)) * s);

				RP.x = r.x + (int)(((double)(RP.x)) * s);
				RP.y = r.y + (int)(((double)(RP.y)) * s);

				RE.x = r.x + (int)(((double)(RE.x)) * s);
				RE.y = r.y + (int)(((double)(RE.y)) * s);

				LP.x = r.x + (int)(((double)(LP.x)) * s);
				LP.y = r.y + (int)(((double)(LP.y)) * s);

				REP.x = r.x + (int)(((double)(REP.x)) * s);
				REP.y = r.y + (int)(((double)(REP.y)) * s);

				//Core.rectangle(mRgba, RE, new Point(RE.x+rightEye_template.width(),RE.y+rightEye_template.height()), new Scalar(255, 0, 255, 255));

				//Core.circle(mRgba, RE, 3, new Scalar(255, 0, 255, 255), 2);

				//Core.rectangle(mRgba, REP, new Point(REP.x+pupil_template.width(),REP.y+pupil_template.height()), new Scalar(0, 191, 255, 255));

				//Core.circle(mRgba, REP, 3, new Scalar(0, 191, 255, 255), 2);

				boolean err = false;
				//left eye
				int width, height;
				width = (int) (Math.abs(LERE.x - LELE.x));
				height = (int) (Math.abs(LERE.y - LELE.y));

				// make sure the edges arent too unaligned
				// modified
				//width = (int) (Math.abs(LERE.x - LELE.x));
				//height = (int) (Math.abs(LERE.y - LELE.y));

				/*
				if (LP.x - LE.x < leftEye_template.width() / 4 || LP.x - LE.x > leftEye_template.width() - leftEye_template.width() / 4) {
					err = true;
				}


				if (RP.x - RE.x < rightEye_template.width() / 4 || RP.x - RE.x > rightEye_template.width() - rightEye_template.width() / 4) {
					err = true;
				}
				*/


				if (height > 10) {
					err = true;
				}

				// make sure the pupil is near the center
				if (LELE.x - LP.x < width / 5 || LELE.x - LP.x > width - width / 5) {
					err = true;
				}
				if (Math.abs(LP.y - LERE.y) > 30) {
					err = true;
				}

				// right eye
				// if widths are different
				if (Math.abs(width - (RELE.x - RERE.x)) > 20) {
					err = true;
				}
				width = (int) (Math.abs(RERE.x - RELE.x));
				height = (int) (Math.abs(RERE.y - RELE.y));
				// make sure the edges arent too unaligned

				if (height > 10) {
					err = true;
				}

				// make sure the pupil is near the center
				if (RELE.x - RP.x < width / 5 || RELE.x - RP.x > width - width / 5) {
					err = true;
				}
				if (Math.abs(RP.y - RELE.y) > 30) {
					err = true;
				}






				if (err) {
					Log.w(TAG, "eye closed");
					closedcount++;

					Core.putText(mRgba, "other state",
							new Point(xCenter+20, yCenter+20),
							Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 255, 255,
									255));

					// not good
				} else {
					Log.w(TAG, "eye open");
					opencount++;

					Core.putText(mRgba, "eye open",
							new Point(xCenter+20, yCenter+20),
							Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 255, 255,
									255));

					// draw the features on
					//Core.circle(mRgba, RERE, 3, new Scalar(0, 255, 0, 255), 1);
					//Core.circle(mRgba, RELE, 3, new Scalar(0, 255, 0, 255), 1);
					//Core.circle(mRgba, RP, 3, new Scalar(255, 0, 0, 255), 3);
					//Core.circle(mRgba, LERE, 3, new Scalar(0, 255, 0, 255), 1);
					//Core.circle(mRgba, LELE, 3, new Scalar(0, 255, 0, 255), 1);
					//Core.circle(mRgba, LP, 3, new Scalar(255, 0, 0, 255), 3);
					//Core.circle(mRgba, RE, 3, new Scalar(0, 255, 255, 255), 3);
				}
				Core.circle(mRgba, RERE, 3, new Scalar(255, 215, 0, 255), 2);
				Core.circle(mRgba, RELE, 3, new Scalar(255, 215, 0, 255), 2);
				Core.line(mRgba, new Point(RP.x-4,RP.y), new Point(RP.x+4,RP.y),new Scalar(0, 255, 0, 255), 2);
				Core.line(mRgba, new Point(RP.x,RP.y-4), new Point(RP.x,RP.y+4),new Scalar(0, 255, 0, 255), 2);
				//Core.circle(mRgba, RP, 3, new Scalar(255, 0, 0, 255), 3);
				Core.circle(mRgba, LERE, 3, new Scalar(255, 215, 0, 255), 2);
				Core.circle(mRgba, LELE, 3, new Scalar(255, 215, 0, 255), 2);
				Core.line(mRgba, new Point(LP.x-4,LP.y), new Point(LP.x+4,LP.y),new Scalar(0, 255, 0, 255), 2);
				Core.line(mRgba, new Point(LP.x,LP.y-4), new Point(LP.x,LP.y+4),new Scalar(0, 255, 0, 255), 2);
				//Core.circle(mRgba, LP, 3, new Scalar(255, 0, 0, 255), 3);
				//Core.circle(mRgba, RE, 3, new Scalar(0, 255, 255, 255), 3);

			}


		}

		return mRgba;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.w(TAG, "called onCreateOptionsMenu");
		mItemFace50 = menu.add("Face size 50%");
		mItemFace40 = menu.add("Face size 40%");
		mItemFace30 = menu.add("Face size 30%");
		mItemFace20 = menu.add("Face size 20%");
		mItemType = menu.add(mDetectorName[mDetectorType]);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.w(TAG, "called onOptionsItemSelected; selected item: " + item);
		if (item == mItemFace50)
			setMinFaceSize(0.5f);
		else if (item == mItemFace40)
			setMinFaceSize(0.4f);
		else if (item == mItemFace30)
			setMinFaceSize(0.3f);
		else if (item == mItemFace20)
			setMinFaceSize(0.2f);
		else if (item == mItemType) {
			int tmpDetectorType = (mDetectorType + 1) % mDetectorName.length;
			item.setTitle(mDetectorName[tmpDetectorType]);
		}
		return true;
	}

	private void setMinFaceSize(float faceSize) {
		mRelativeFaceSize = faceSize;
		mAbsoluteFaceSize = 0;
	}


	private void CreateAuxiliaryMats() {
		if (mGray.empty())
			return;

		int rows = mGray.rows();
		int cols = mGray.cols();

		if (mZoomWindow == null) {
			mZoomWindow = mRgbart.submat(0, 0, rows, rows);
			mZoomWindow2 = mRgbart.submat(0, rows / 2 - rows / 10, cols / 2
					+ cols / 10, cols);
		}

	}

	private Point find_eye(Rect area, int type, String left_right, String pupil) {
		Mat mTemplate;
		Mat mTemplate2;
		if (left_right == "right" && pupil == "ispupil") {
			mTemplate = rightpupil_template;
			//mTemplate2 = rightEyeclosed_template;

		} else if (left_right == "left" && pupil == "ispupil") {
			mTemplate = leftpupil_template;
			//mTemplate2 = leftEyeclosed_template;
		} else if (left_right == "right" && pupil == "notpupil") {
			mTemplate = rightEye_template;
		} else {
			mTemplate = leftEye_template;
		}

		Point matchLoc = new Point(0, 0);
		Mat mROI = zoomwindow_aux.submat(area);
		Log.w(TAG, "mROI size: " + mROI.rows() + ", " + mROI.cols());
		Log.w(TAG, "mTemplate size: " + mTemplate.rows() + ", " + mTemplate.cols());
		Log.w(TAG, "ROI type is: " + mROI.type());
		Log.w(TAG, "template type is: " + rightEye_template.type());
		Log.w(TAG, "mRgb type is: " + mRgbart.type());
		Log.w(TAG, "mGray type is: " + mGray.type());
		Log.w(TAG, "CV_8U type is: " + CvType.CV_8U);
		Log.w(TAG, "CV_32FC1 type is: " + CvType.CV_32FC1);


		int result_cols = mROI.cols() - mTemplate.cols() + 1;
		int result_rows = mROI.rows() - mTemplate.rows() + 1;
		// Check for bad template size

		Mat mResult = new Mat(result_cols, result_rows, CvType.CV_8U);

		switch (TM_CCOEFF_NORMED) {
			case TM_SQDIFF:
				Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_SQDIFF);
				break;
			case TM_SQDIFF_NORMED:
				Imgproc.matchTemplate(mROI, mTemplate, mResult,
						Imgproc.TM_SQDIFF_NORMED);
				break;
			case TM_CCOEFF:
				Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCOEFF);
				break;
			case TM_CCOEFF_NORMED:
				Imgproc.matchTemplate(mROI, mTemplate, mResult,
						Imgproc.TM_CCOEFF_NORMED);
				break;
			case TM_CCORR:
				Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCORR);
				break;
			case TM_CCORR_NORMED:
				Imgproc.matchTemplate(mROI, mTemplate, mResult,
						Imgproc.TM_CCORR_NORMED);
				break;
		}
		Mat mResult_open = new Mat();
		Core.normalize( mResult, mResult_open, 0, 1, Core.NORM_MINMAX, -1);
		Core.MinMaxLocResult mmres_open = Core.minMaxLoc(mResult_open);
		// there is difference in matching methods - best match is max/min value

		/*
		Mat mResult2 = new Mat(result_cols, result_rows, CvType.CV_8U);
		Imgproc.matchTemplate(mROI, mTemplate2, mResult2,
				Imgproc.TM_CCOEFF_NORMED);
		Mat mResult_closed = new Mat();
		Core.normalize( mResult2, mResult_closed, 0, 1, Core.NORM_MINMAX, -1);
		Core.MinMaxLocResult mmres_closed = Core.minMaxLoc(mResult_closed);
		*/


		/*
		if (mmres_open.minVal < mmres_closed.minVal) {
			Log.w(TAG, "Eye Open");
			Core.putText(mRgba, "eye_open",
					new Point(xCenter+20, yCenter+20),
					Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 255, 255,
							255));
		} else if (mmres_open.minVal > mmres_closed.minVal){
			Log.w(TAG, "Eye closed");
			Core.putText(mRgba, "eye_closed",
					new Point(xCenter+70, yCenter+70),
					Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 255, 255,
							255));
		}
		*/

		matchLoc = mmres_open.maxLoc;


		if (left_right == "right"&& pupil == "ispupil") {
			eye_right = new Rect((int) (mmres_open.maxLoc.x),
					(int) (mmres_open.maxLoc.y),
					mTemplate.width(), mTemplate.height());
			zoom_eye_right = mROI.submat(eye_right);

			/*
			eye_right_closed = new Rect((int) (mmres_closed.maxLoc.x),
					(int) (mmres_closed.maxLoc.y),
					mTemplate.width(), mTemplate.height());
			zoom_eye_right_closed = mROI.submat(eye_right_closed);
			*/

		} else if (left_right == "left"&& pupil == "ispupil"){
			eye_left = new Rect((int) (mmres_open.maxLoc.x),
					(int) (mmres_open.maxLoc.y),
					mTemplate.width(), mTemplate.height());
			zoom_eye_left = mROI.submat(eye_left);

			/*
			eye_left_closed = new Rect((int) (mmres_closed.maxLoc.x),
					(int) (mmres_closed.maxLoc.y),
					mTemplate.width(), mTemplate.height());
			zoom_eye_left_closed = mROI.submat(eye_right_closed);
			*/

		}

		return matchLoc;

	}

	private Point findPupil(Mat area, Mat mTemplate, int type, String left_right) {
		Point anchor = new Point(mTemplate.width()/2, mTemplate.height()/2);
		Log.w(TAG, "pupil template width is: " + mTemplate.width() + ", " + mTemplate.height());

		Point matchLoc;
		Mat mROI = area;
		int result_cols = mROI.cols() - mTemplate.cols() + 1;
		int result_rows = mROI.rows() - mTemplate.rows() + 1;
		// Check for bad template size

		Mat mResult = new Mat(result_cols, result_rows, CvType.CV_8U);

		switch (TM_CCOEFF_NORMED) {
			case TM_SQDIFF:
				Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_SQDIFF);
				break;
			case TM_SQDIFF_NORMED:
				Imgproc.matchTemplate(mROI, mTemplate, mResult,
						Imgproc.TM_SQDIFF_NORMED);
				break;
			case TM_CCOEFF:
				Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCOEFF);
				break;
			case TM_CCOEFF_NORMED:
				Imgproc.matchTemplate(mROI, mTemplate, mResult,
						Imgproc.TM_CCOEFF_NORMED);
				break;
			case TM_CCORR:
				Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCORR);
				break;
			case TM_CCORR_NORMED:
				Imgproc.matchTemplate(mROI, mTemplate, mResult,
						Imgproc.TM_CCORR_NORMED);
				break;
		}
		Mat mResult_open = new Mat();
		Core.normalize( mResult, mResult_open, 0, 1, Core.NORM_MINMAX, -1);
		Core.MinMaxLocResult mmres_open = Core.minMaxLoc(mResult_open);

		// there is difference in matching methods - best match is max/min value
		/*
		if (left_right == "right") {
			mROI = zoom_eye_right_closed;
		} else {
			mROI = zoom_eye_left_closed;
		}
		Mat mResult2 = new Mat(result_cols, result_rows, CvType.CV_8U);
		Imgproc.matchTemplate(mROI, mTemplate, mResult2,
				Imgproc.TM_CCOEFF_NORMED);
		Mat mResult_closed = new Mat();
		Core.normalize( mResult2, mResult_closed, 0, 1, Core.NORM_MINMAX, -1);
		Core.MinMaxLocResult mmres_closed = Core.minMaxLoc(mResult_closed);


		Log.w(TAG, "open minval is :" + mmres_open.minVal);
		Log.w(TAG, "closed minval is :" + mmres_closed.minVal);

		if(mmres_open.minVal < 0)
		{
			eye_open=eye_open+1;
			if(eye_open == 1)
			{
				Log.w(TAG, "Eye Open");
				Core.putText(mRgba, "eye_open",
						new Point(xCenter+20, yCenter+20),
						Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 255, 255,
								255));
				eye_open=0;
				eye_close=0;
			}
		}
		if(mmres_closed.minVal < 0) {
			eye_close = eye_close + 1;
			if (eye_close == 1) {
				Log.w(TAG, "Eye closed");
				Core.putText(mRgba, "eye_closed",
						new Point(xCenter+70, yCenter+70),
						Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 255, 255,
								255));
				eye_close = 0;
				//system("python send_arduino.py");
			}
		}
		*/

		matchLoc = mmres_open.maxLoc;

		matchLoc.x += anchor.x;
		matchLoc.y += anchor.y;
		//matchLoc.x += anchor_points[0];
		//matchLoc.y += anchor_points[1];
		return matchLoc;

	}

	private Point findKernel(Mat area, Mat mTemplate, int type, int[] anchor_points) {
		Point matchLoc;
		Mat mROI = area;
		int result_cols = mROI.cols() - mTemplate.cols() + 1;
		int result_rows = mROI.rows() - mTemplate.rows() + 1;
		// Check for bad template size

		Mat mResult = new Mat(result_cols, result_rows, CvType.CV_8U);

		switch (TM_CCOEFF_NORMED) {
			case TM_SQDIFF:
				Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_SQDIFF);
				break;
			case TM_SQDIFF_NORMED:
				Imgproc.matchTemplate(mROI, mTemplate, mResult,
						Imgproc.TM_SQDIFF_NORMED);
				break;
			case TM_CCOEFF:
				Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCOEFF);
				break;
			case TM_CCOEFF_NORMED:
				Imgproc.matchTemplate(mROI, mTemplate, mResult,
						Imgproc.TM_CCOEFF_NORMED);
				break;
			case TM_CCORR:
				Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCORR);
				break;
			case TM_CCORR_NORMED:
				Imgproc.matchTemplate(mROI, mTemplate, mResult,
						Imgproc.TM_CCORR_NORMED);
				break;
		}

		Core.MinMaxLocResult mmres = Core.minMaxLoc(mResult);
		// there is difference in matching methods - best match is max/min value


			matchLoc = mmres.maxLoc;

		matchLoc.x += anchor_points[0];
		matchLoc.y += anchor_points[1];
		return matchLoc;

	}

	/*
	public void detect_eye_open_close(Rect rec, int type, Point matchLoc_tx, Point matchLoc_ty, Mat open_close_temp, String open_close) {

		Point matchLoc2;
		Mat mROI2 = mGray.submat(rec);

		int result_cols2 = mROI2.cols() - open_close_temp.cols() + 1;
		int result_rows2 = mROI2.rows() - open_close_temp.rows() + 1;
		// Check for bad template size
		if (open_close_temp.cols() == 0 || open_close_temp.rows() == 0) {
			return ;
		}
		Mat mResult2 = new Mat(result_cols2, result_rows2, CvType.CV_8U);

		switch (type) {
			case TM_SQDIFF:
				Imgproc.matchTemplate(mROI2, open_close_temp, mResult2, Imgproc.TM_SQDIFF);
				break;
			case TM_SQDIFF_NORMED:
				Imgproc.matchTemplate(mROI2, open_close_temp, mResult2,
						Imgproc.TM_SQDIFF_NORMED);
				break;
			case TM_CCOEFF:
				Imgproc.matchTemplate(mROI2, open_close_temp, mResult2, Imgproc.TM_CCOEFF);
				break;
			case TM_CCOEFF_NORMED:
				Imgproc.matchTemplate(mROI2, open_close_temp, mResult2,
						Imgproc.TM_CCOEFF_NORMED);
				break;
			case TM_CCORR:
				Imgproc.matchTemplate(mROI2, open_close_temp, mResult2, Imgproc.TM_CCORR);
				break;
			case TM_CCORR_NORMED:
				Imgproc.matchTemplate(mROI2, open_close_temp, mResult2,
						Imgproc.TM_CCORR_NORMED);
				break;
		}
		Core.normalize( mResult2, mResult2, 0, 1, Core.NORM_MINMAX, -1, new Mat() );

		/// Localizing the best match with minMaxLoc
		//double minVal; double maxVal; Point minLoc; Point maxLoc;


		Core.MinMaxLocResult mmres2 = Core.minMaxLoc(mResult2);
		// there is difference in matching methods - best match is max/min value
		if (type == TM_SQDIFF || type == TM_SQDIFF_NORMED) {
			matchLoc2 = mmres2.minLoc;
		} else {
			matchLoc2 = mmres2.maxLoc;
		}

		//Log.w(TAG, "minVal is: " + mmres2.minVal);

		///Justing checkin the match template value reaching the threashold
		if(open_close == "open" && (mmres2.minVal < 0))
		{
			min_val = mmres2.minVal;
			Log.w(TAG, "open_minVal is: " + mmres2.minVal);

			if(eye_open == 3)
			{
				Core.putText(mRgba, "eye_open",
						new Point(matchLoc_tx.x - 20, matchLoc_tx.y - 20),
						Core.FONT_HERSHEY_SIMPLEX, 0.9, new Scalar(255, 255, 255,
								255));
				Log.w(TAG, "eye is open");
				eye_open=0;
				eye_close=0;
			}

		}
		if(open_close == "closed" && (mmres2.minVal < 0)) {
			if (mmres2.minVal < min_val) {
				Log.w(TAG, "min value is: " + min_val);
				Log.w(TAG, "eye is closed");
				Core.putText(mRgba, "eye_closed",
						new Point(matchLoc_tx.x - 20, matchLoc_tx.y - 20),
						Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 255, 255,
								255));
				Log.w(TAG, "closed_minVal is: " + mmres2.minVal);
			} else if(min_val < 0){
				Log.w(TAG, "eye is open");
				Core.putText(mRgba, "eye_open",
						new Point(matchLoc_tx.x - 20, matchLoc_tx.y - 20),
						Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 255, 255,
								255));
			}
		}
		else {
			Log.w(TAG, "eye is open");
			Core.putText(mRgba, "eye_open",
					new Point(matchLoc_tx.x - 20, matchLoc_tx.y - 20),
					Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 255, 255,
							255));
		}
		if(eye_close == 3)
		{
			Core.putText(mRgba, "eye_closed",
					new Point(matchLoc_tx.x - 20, matchLoc_tx.y - 20),
					Core.FONT_HERSHEY_SIMPLEX, 0.9, new Scalar(255, 255, 255,
							255));
			Log.w(TAG, "eye is closed");
			eye_close=0;
			//system("python send_arduino.py");
		}

		//Log.w(TAG, "matchLoc2 location is: " + "x: " + matchLoc2.x + "y: " + matchLoc2.y);
		//Log.w(TAG, "matchLoc_tx location is: " + "x: " + matchLoc_tx.x + "y: " + matchLoc_tx.y);

		Point matchLoc_tx2 = new Point(matchLoc_tx.x, matchLoc_tx.y);
		Point matchLoc_ty2 = new Point(matchLoc_tx.x + open_close_temp.cols(),matchLoc_tx.y + open_close_temp.rows());
		//Log.w(TAG, "matchLoc_ty2 location is: " + "x: " + matchLoc_ty2.x + "y: " + matchLoc_ty2.y);


		Core.rectangle(mRgba, matchLoc_tx2, matchLoc_ty2, new Scalar(125, 255, 255,
				255));


	}

	private Mat get_template(CascadeClassifier clasificator, int type, Rect area, int size_x, int size_y) {
		Mat template = new Mat();
		Mat mROI = mGray.submat(area);
		MatOfRect eyes = new MatOfRect();
		Point iris = new Point();
		Rect eye_template = new Rect();
		clasificator.detectMultiScale(mROI, eyes, 1.1, 2,
				Objdetect.CASCADE_FIND_BIGGEST_OBJECT
						| Objdetect.CASCADE_SCALE_IMAGE, new Size(20, 20),
				new Size());

		Rect[] eyesArray = eyes.toArray();
		if (eyesArray.length == 0) {
			Log.w(TAG, "eye is closed_main");
			Core.putText(mRgba, "eye_closed",
					new Point(area.x - 20, area.y - 20),
					Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 255, 255,
							255));
			if (first_closed == true) {
				first_closed = false;
				has_timer = true;
				timer = new Timer();
				initializeTimerTask();
				timer.schedule(timerTask, 3000); //
			}


		} else {
			Log.w(TAG, "eye is open_main");
			Core.putText(mRgba, "eye_open",
					new Point(area.x - 20, area.y - 20),
					Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 255, 255,
							255));
			if (has_timer == true) {
				timer.cancel();
				has_timer = false;
			}
			first_closed = true;
		}
		for (int i = 0; i < eyesArray.length;) {
			Rect e = eyesArray[i];
			e.x = area.x + e.x;
			e.y = area.y + e.y;
			Rect eye_only_rectangle = new Rect((int) e.tl().x,
					(int) (e.tl().y + e.height * 0.4), (int) e.width,
					(int) (e.height * 0.6));
			mROI = mGray.submat(eye_only_rectangle);
			Mat vyrez = mRgba.submat(eye_only_rectangle);
			
			
			Core.MinMaxLocResult mmG = Core.minMaxLoc(mROI);

			Core.circle(vyrez, mmG.minLoc, 2, new Scalar(255, 255, 255, 255), 2);
			iris.x = mmG.minLoc.x + eye_only_rectangle.x;
			iris.y = mmG.minLoc.y + eye_only_rectangle.y;
			eye_template = new Rect((int) iris.x - size_x / 2, (int) iris.y
					- size_y / 2, size_x, size_y);
			Core.rectangle(mRgba, eye_template.tl(), eye_template.br(),
					new Scalar(255, 0, 0, 255), 2);
			template = (mGray.submat(eye_template)).clone();
			//detect_eye_open_close(eye_template, type, eye_template.tl(), eye_template.br(), open_left_eye, "open");
			//detect_eye_open_close(eye_template, type, eye_template.tl(), eye_template.br(), closed_left_eye, "closed");
			return template;
		}

		return template;
	}
	*/
	public void new_profile (Rect facerect) {
		Imgproc.resize(mRgba.submat(facerect), mZoomWindow2,
				mZoomWindow2.size());
		Imgproc.resize(mRgba.submat(facerect), mZoomWindow,
				mZoomWindow.size());
	}

	public void initializeTimerTask() {

		 timerTask = new TimerTask() {
			public void run() {

				//use a handler to run a toast that shows the current timestamp
				handler.post(new Runnable() {
					public void run() {
						//get the current timeStamp
						//Calendar calendar = Calendar.getInstance();
						//SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd:MMMM:yyyy HH:mm:ss a");
						//final String strDate = simpleDateFormat.format(calendar.getTime());

						//show the toast


						/*if (firstminute == true) {
							if (count < 10){
								oneminuteclosed.add(closedcount);
								oneminuteopen.add(opencount);
								totalclosedcount += closedcount;
								totalopencount += opencount;
								//Log.w(TAG, "closedcount is: " + closedcount);
								//Log.w(TAG, "opencout is: " + opencount);
								//Log.w(TAG, "total closed is: " + totalclosedcount);
								//Log.w(TAG, "total open is: " + totalopencount);
								closedcount = 0;
								opencount = 0;
							}
							else {
								oneminuteclosed.add(closedcount);
								oneminuteopen.add(opencount);
								totalclosedcount += closedcount;
								totalopencount += opencount;
								closedpercentage = (double) (((double) totalclosedcount) / (double) (totalclosedcount + totalopencount + 0.0001));
								//Log.w(TAG, "first PERCLOS is: " + closedpercentage);
								//Log.w(TAG, "closedcount is: " + closedcount);
								//Log.w(TAG, "opencout is: " + opencount);
								//Log.w(TAG, "total closed is: " + totalclosedcount);
								//Log.w(TAG, "total open is: " + totalopencount);
								output.setText("PERCLOS: " + closedpercentage);
								firstminute = false;
								closedcount = 0;
								opencount = 0;
							}
							Log.w(TAG, "count is: " + count);
							count++;
						} else {
							oneminuteclosed.add(closedcount);
							oneminuteopen.add(opencount);
							totalclosedcount += closedcount - oneminuteclosed.get(0);
							totalopencount += opencount - oneminuteopen.get(0);
							closedpercentage = (double) (((double) totalclosedcount) / (double) (totalclosedcount + totalopencount + 0.0));
							Log.w(TAG, "PERCLOS is: " + closedpercentage);
							Log.w(TAG, "closedcount is: " + closedcount);
							Log.w(TAG, "opencout is: " + opencount);
							Log.w(TAG, "total closed is: " + totalclosedcount);
							Log.w(TAG, "total open is: " + totalopencount);
							closedcount = 0;
							opencount = 0;
							output.setText("PERCLOS: " + closedpercentage);
							oneminuteclosed.remove(0);
							oneminuteopen.remove(0);
						}

						if (closedpercentage >= 0.8) {
							int duration = Toast.LENGTH_SHORT;
							Toast toast = Toast.makeText(getApplicationContext(), "Drowsy! Stop the car!!!", duration);
							toast.show();
						}
						*/

						if (count < 10){
							//oneminuteclosed.add(closedcount);
							//oneminuteopen.add(opencount);
							totalclosedcount += closedcount;
							totalopencount += opencount;
							//Log.w(TAG, "closedcount is: " + closedcount);
							//Log.w(TAG, "opencout is: " + opencount);
							//Log.w(TAG, "total closed is: " + totalclosedcount);
							//Log.w(TAG, "total open is: " + totalopencount);
							closedcount = 0;
							opencount = 0;
						}
						else {
							if(count==10) {
								//oneminuteclosed.add(closedcount);
								//oneminuteopen.add(opencount);
								totalclosedcount += closedcount;
								totalopencount += opencount;
								closedpercentage = (double) (((double) totalclosedcount) / (double) (totalclosedcount + totalopencount + 0.0001));
								//Log.w(TAG, "first PERCLOS is: " + closedpercentage);
								//Log.w(TAG, "closedcount is: " + closedcount);
								//Log.w(TAG, "opencout is: " + opencount);
								//Log.w(TAG, "total closed is: " + totalclosedcount);
								//Log.w(TAG, "total open is: " + totalopencount);
								output.setText("PERCLOS: " + closedpercentage);
								//firstminute = false;
								closedcount = 0;
								opencount = 0;
							}
						}
						Log.w(TAG, "count is: " + count);
						count++;

						if (closedpercentage >= 0.28/*0.6*/) {
							int timeLength2=10;

							if (count2 < timeLength2){
								//oneminuteclosed.add(closedcount);
								//oneminuteopen.add(opencount);
								totalClosedCount2 += closedcount;
								totalOpenCount2 += opencount;
								//Log.w(TAG, "closedcount is: " + closedcount);
								//Log.w(TAG, "opencout is: " + opencount);
								//Log.w(TAG, "total closed is: " + totalclosedcount);
								//Log.w(TAG, "total open is: " + totalopencount);
								closedcount = 0;
								opencount = 0;
							}
							else {
								if(count2==timeLength2) {
									//oneminuteclosed.add(closedcount);
									//oneminuteopen.add(opencount);
									totalClosedCount2 += closedcount;
									totalOpenCount2 += opencount;
									closedPercentage2 = (double) (((double) totalClosedCount2) / (double) (totalClosedCount2 + totalOpenCount2 + 0.0001));
									//Log.w(TAG, "first PERCLOS is: " + closedpercentage);
									//Log.w(TAG, "closedcount is: " + closedcount);
									//Log.w(TAG, "opencout is: " + opencount);
									//Log.w(TAG, "total closed is: " + totalclosedcount);
									//Log.w(TAG, "total open is: " + totalopencount);
									output.setText("PERCLOS: " + closedPercentage2);
									//firstminute = false;
									closedcount = 0;
									opencount = 0;
								}
							}
							Log.w(TAG, "count2 is: " + count2);
							count2++;
							if (closedPercentage2>=0.28 &&count2==timeLength2+1){
								int duration = Toast.LENGTH_SHORT;
								Toast toast = Toast.makeText(getApplicationContext(), "Drowsy! Stop the car!!!", duration);
								toast.show();
								mediaAlert();

							}else{
								if(count2==timeLength2+1) {
									//not Drowsy 2
									Intent GoToCarDetection = new Intent(FdActivity.this, CarActivity.class);
									startActivity(GoToCarDetection);
									FdActivity.this.finish();
								}

							}
						}else{
							if (count==11) {

								//not Drowsy 1
								Intent GoToCarDetection = new Intent(FdActivity.this, CarActivity.class);
								startActivity(GoToCarDetection);
								FdActivity.this.finish();
							}

						}


					}
				});
			}
		};
	}

	/*
	public void initializeTimerTasksec() {
		starttime = System.currentTimeMillis();
		timerTasksec = new TimerTask() {
			public void run() {

				//use a handler to run a toast that shows the current timestamp
				handlersec.post(new Runnable() {
					public void run() {
						//get the current timeStamp
						//Calendar calendar = Calendar.getInstance();
						//SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd:MMMM:yyyy HH:mm:ss a");
						//final String strDate = simpleDateFormat.format(calendar.getTime());

						//show the toast
							firstminute = false;
							timer.cancel();
							long millis_curr = System.currentTimeMillis() - starttime;
							int seconds_end = (int) (millis_curr / 1000);
							Log.w(TAG, "seconds is: " + seconds_end);

							if (seconds_end == 1) {
								oneminuteclosed.add(closedcount);
								oneminuteopen.add(opencount);
								totalclosedcount += closedcount - oneminuteclosed.get(0);
								totalopencount += opencount - oneminuteopen.get(0);
								closedcount = 0;
								opencount = 0;
								closedpercentage = (double) (((double) totalclosedcount) / (double) (totalclosedcount + totalopencount + 0.0));
								Log.w(TAG, "PERCLOS is: " + closedpercentage);
								output.setText("PERCLOS: " + closedpercentage);
								oneminuteclosed.remove(0);
								oneminuteopen.remove(0);
								seconds_end = 0;
								timer = new Timer();
								initializeTimerTask();
								timer.schedule(timerTask, 0);
							}

						Log.w(TAG, "total closed: " + totalclosedcount);
						Log.w(TAG, "total open: " + totalclosedcount);

							if (closedpercentage >= 0.8) {
								int duration = Toast.LENGTH_SHORT;
								Toast toast = Toast.makeText(getApplicationContext(), "Drowsy! Stop the car!!!", duration);
								toast.show();
							}
					}
				});
			}
		};
	}
	*/

	public void onexitClick(View v){
		Intent intent = new Intent(getApplicationContext(), Userpage.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("EXIT", true);
		startActivity(intent);
	}

	public void onPopClick(View v){
		if (r!= null && mRgba!= null) {
			Intent myintent = new Intent();
			Bundle mybundle = new Bundle();
			//if (FdActivity.this.r == null) {
			Log.w(TAG, "transmitter.r is null");
			//}
			mybundle.putParcelable("org.opencv.samples.facedetect.pass", FdActivity.this);
			myintent.putExtras(mybundle);
			myintent.setClass(FdActivity.this, Pop.class);
			startActivity(myintent);
		}
	}



	public void onRecreateClick(View v)
	{
		learn_frames = 0;
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
