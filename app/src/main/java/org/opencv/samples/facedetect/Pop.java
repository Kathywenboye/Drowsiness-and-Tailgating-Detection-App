package org.opencv.samples.facedetect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;


public class Pop extends Activity implements OnTouchListener, Parcelable {
    public String TAG = "OCVSample::PopActivity";
    public Rect box;
    public boolean mouse_draw_box = false;
    public boolean face_done = false;
    ImageView imgView;
    public Mat faceimage;
    public Mat faceimagedrawn;
    public Mat lefteyedrawn;
    public Mat leftlefteye_tmp;
    public Mat leftlefteyedrawn;
    public Mat leftrighteye_tmp;
    public Mat leftrighteyedrawn;
    public FdActivity receiver;
    public Mat lefteye_load;
    public Mat leftlefteye_load;
    public Mat righteye_load;
    public Mat righteyedrawn;
    public Mat rightlefteye_tmp;
    public Mat rightlefteyedrawn;
    public Mat rightlefteye_load;
    public Mat rightrighteye_load;
    public Mat rightrighteye_tmp;
    public Mat rightrighteyedrawn;
    public Mat lefteye_tmp;
    public Mat righteye_tmp;
    public boolean rightlefteye_draw_done;
    public boolean face_draw_done = false;
    public boolean lefteye_draw_done = false;
    public boolean leftlefteye_draw_done = false;
    public boolean righteye_draw_done = false;
    public boolean rightlefteye_anchor_done = false;
    public boolean rightrighteye_draw_done = false;
    public boolean rightrighteye_anchor_done = false;
    public boolean left_pupil_done = false;
    public boolean right_pupil_done = false;

    public Rect rectLELE;
    public Rect rectLERE;
    public Rect rectRELE;
    public Rect rectRERE;
    public Point anchor = new Point();
    public Mat leftlefteyedrawn_aux;
    public Mat lefteyedrawn_aux;
    public int[] anchors = new int[8];
    public boolean leftlefteye_anchor_done = false;
    public boolean leftrighteye_draw_done = false;
    public Mat leftrighteye_load;
    public boolean leftrighteye_anchor_done = false;
    public boolean lefttopeye_anchor_done = false;
    public boolean leftbottomeye_anchor_done = false;
    public boolean righttopeye_anchor_done = false;
    public boolean rightbottomeye_anchor_done = false;
    public Mat lefteyetodraw;
    public Mat righteyetodraw;


    private int mData;
    public Button backbutton;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popwindow);

        backbutton = (Button) findViewById(R.id.back);
        backbutton.setVisibility(View.GONE);
        imgView = (ImageView) findViewById(R.id.face);
        imgView.setId(1);

        if (load_image("lefteye.jpg").empty())
            Log.w(TAG, "left eye is empty");
        else
            Log.w(TAG, "left eye is not empty");

        /*
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        Intent intent = getIntent();
        getWindow().setLayout((int)(width), (int)(height));
        */

        Bundle b = this.getIntent().getExtras();

        if (b != null) {

            receiver  = b.getParcelable("org.opencv.samples.facedetect.pass");
            //Log.w(TAG, "output is: " + receiver.r.width);
            /*
            if (receiver.r == null) {
                Log.w(TAG, "receiver.r is null");
                Log.w(TAG, "receiver is not null" + receiver.r.width + ", " + receiver.r.height);
            }
            */
            if (receiver.test != null) {
                imgView = (ImageView) findViewById(R.id.facetest);
                imgView.setId(12);
                Log.w(TAG, "I'm receiver test");
                Log.w(TAG, "receiver test size: " + receiver.zoomwindow_aux.width() + ", " + receiver.zoomwindow_aux.height());

                Mat zoomtest = receiver.zoomwindow_sec.clone();
                Core.rectangle(zoomtest, receiver.eyearea_right.tl(), receiver.eyearea_right.br(),
                        new Scalar(255, 0, 0, 255), 2);
                Core.circle(zoomtest, receiver.RE, 3, new Scalar(0, 255, 0, 255), 1);

/*
                Core.putText(receiver.test, "this is for test",
                        new Point(20, 20),
                        Core.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(255, 255, 255,
                                255));
                                */
                show_image(zoomtest);
            }

            else {
            Log.w(TAG, "faceimg size: " + receiver.r.width + ", " + receiver.r.height);
            Log.w(TAG, "window size is: " + receiver.mRgbart.rows() + ", " + receiver.mRgbart.cols());

            box = new Rect(0, 0, 40, 40);

                Mat faceimage2 = receiver.mRgbart.submat(receiver.r);
                faceimage = new Mat(receiver.mRgbart.rows(), receiver.mRgbart.rows(), receiver.mRgbart.type());

            //Log.w(TAG, "faceimg size: " + zoom.width() + ", " + zoom.height());
            //Log.w(TAG, "window size is: " + zoom.rows() + ", " + zoom.cols());

            Imgproc.resize(faceimage2, faceimage, faceimage.size());

            Bitmap img = Bitmap.createBitmap(faceimage.rows(), faceimage.cols(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(faceimage, img);
            imgView.setImageBitmap(img);
            Log.w(TAG, "face_done? " + face_done);
            imgView.setOnTouchListener(this);
            //imgView.setImageDrawable(null);
            Log.w(TAG, "face_done? after listener" + face_done);
            face_draw_done = false;

            }







        }
        /*
        cvSetMouseCallback("face", mouse_crop, imgFaceDrawn);

        cvShowImage("face", imgFaceDrawn);  // show face with box
        cout << "face size is: " << imgFaceDrawn.width << ", " << imgFaceDrawn.height << endl;

        if (mouse_draw_box) {               // draw new box is necessary
            mouse_draw_box = false;
            cvCopy(imgFace, imgFaceDrawn);
            write_text(imgFaceDrawn, "crop left eye");
            draw_box(imgFaceDrawn);

        }
        */


    }

    public void face_drawn() {
            // initialize the cropping box size
            faceimagedrawn = faceimage.clone();
            Core.putText(faceimagedrawn, "crop left eye",
                    new Point(20, 20),
                    Core.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(255, 255, 255,
                            255));

            imgView = (ImageView) findViewById(R.id.facedrawn);
            imgView.setId(2);
            show_image(faceimagedrawn);
    }

    public void left_pupil() {
        // initialize the cropping box size
        faceimagedrawn = faceimage.clone();
        Core.putText(faceimagedrawn, "crop left eye pupil",
                new Point(20, 20),
                Core.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(255, 255, 255,
                        255));
        imgView = (ImageView) findViewById(R.id.facedrawntmp1);
        imgView.setId(5);
        show_image(faceimagedrawn);
    }

    public void right_pupil() {
        // initialize the cropping box size

        faceimagedrawn = faceimage.clone();
        Core.putText(faceimagedrawn, "crop right eye pupil",
                new Point(20, 20),
                Core.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(255, 255, 255,
                        255));

        imgView = (ImageView) findViewById(R.id.facedrawntmp2);
        imgView.setId(8);
        show_image(faceimagedrawn);
    }

    public void face_drawn_sec() {
        faceimagedrawn = faceimage.clone();
        Core.putText(faceimagedrawn, "crop right eye",
                new Point(20, 20),
                Core.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(255, 255, 255,
                        255));

        imgView = (ImageView) findViewById(R.id.facedrawnsec);
        imgView.setId(7);
        show_image(faceimagedrawn);
    }

    /*

    public void leftleft_drawn() {
        // initialize the cropping box size
        lefteyedrawn = lefteye_load.clone();
        Core.putText(lefteyedrawn, "LELE",
                new Point(20, 20),
                Core.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(255, 255, 255,
                        255));
        Log.w(TAG, "left eye size: " + lefteyedrawn.width() + ", " + lefteyedrawn.height());
        imgView = (ImageView) findViewById(R.id.lefteye);
        imgView.setId(3);

        show_image_sec(lefteyedrawn);


    }

    public void leftright_drawn() {
        // initialize the cropping box size
        lefteyedrawn = lefteye_load.clone();
        Core.putText(lefteyedrawn, "LERE",
                new Point(20, 20),
                Core.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(255, 255, 255,
                        255));
        Log.w(TAG, "(right) left eye size: " + lefteyedrawn.width() + ", " + lefteyedrawn.height());
        imgView = (ImageView) findViewById(R.id.lefteyesec);
        imgView.setId(5);

        show_image_sec(lefteyedrawn);

    }

    public void rightleft_drawn() {
        // initialize the cropping box size
        righteyedrawn = righteye_load.clone();
        Core.putText(righteyedrawn, "RELE",
                new Point(20, 20),
                Core.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(255, 255, 255,
                        255));
        Log.w(TAG, "right eye size: " + righteyedrawn.width() + ", " + righteyedrawn.height());
        imgView = (ImageView) findViewById(R.id.righteye);
        imgView.setId(8);

        show_image_sec(righteyedrawn);

    }

    public void rightright_drawn() {
        // initialize the cropping box size
        righteyedrawn = righteye_load.clone();
        Core.putText(righteyedrawn, "RERE",
                new Point(20, 20),
                Core.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(255, 255, 255,
                        255));
        Log.w(TAG, "right eye size: " + righteyedrawn.width() + ", " + righteyedrawn.height());
        imgView = (ImageView) findViewById(R.id.righteyesec);
        imgView.setId(10);

        show_image_sec(righteyedrawn);

    }
*/

    public void left_anchor() {
        lefteye_tmp = lefteye_load.clone();
        lefteyedrawn = new Mat(160, 400, receiver.mRgbart.type());
        Imgproc.resize(lefteye_tmp, lefteyedrawn, lefteyedrawn.size());
        Bitmap img = Bitmap.createBitmap(lefteyedrawn.cols(), lefteyedrawn.rows(), Bitmap.Config.ARGB_8888);
        imgView = (ImageView) findViewById(R.id.lefteye);
        imgView.setId(4);

        show_image_sec(lefteyedrawn);

    }

    /*
    public void leftright_anchor() {
        lefteye_tmp = leftrighteye_load.clone();
        leftrighteyedrawn = new Mat(224, 224, receiver.mRgbart.type());
        Imgproc.resize(leftrighteye_tmp, leftrighteyedrawn, leftrighteyedrawn.size());
        Bitmap img = Bitmap.createBitmap(leftrighteyedrawn.cols(), leftrighteyedrawn.rows(), Bitmap.Config.ARGB_8888);
        imgView = (ImageView) findViewById(R.id.leftrighteye);
        imgView.setId(6);
        show_image_sec(leftrighteyedrawn);
    }

    public void lefttop_anchor() {
        leftrighteye_tmp = leftrighteye_load.clone();
        leftrighteyedrawn = new Mat(224, 224, receiver.mRgbart.type());
        Imgproc.resize(leftrighteye_tmp, leftrighteyedrawn, leftrighteyedrawn.size());
        Bitmap img = Bitmap.createBitmap(leftrighteyedrawn.cols(), leftrighteyedrawn.rows(), Bitmap.Config.ARGB_8888);
        imgView = (ImageView) findViewById(R.id.leftrighteye);
        imgView.setId(6);
        show_image_sec(leftrighteyedrawn);
    }
    */

    public void right_anchor() {
        righteye_tmp = righteye_load.clone();
        righteyedrawn = new Mat(160, 400, receiver.mRgbart.type());
        Imgproc.resize(righteye_tmp, righteyedrawn, righteyedrawn.size());
        Bitmap img = Bitmap.createBitmap(righteyedrawn.cols(), righteyedrawn.rows(), Bitmap.Config.ARGB_8888);
        imgView = (ImageView) findViewById(R.id.righteye);
        imgView.setId(14);
        show_image_sec(righteyedrawn);

    }
    /*
    public void rightright_anchor() {
        rightrighteye_tmp = rightrighteye_load.clone();
        rightrighteyedrawn = new Mat(224, 224, receiver.mRgbart.type());
        Imgproc.resize(rightrighteye_tmp, rightrighteyedrawn, rightrighteyedrawn.size());
        Bitmap img = Bitmap.createBitmap(rightrighteyedrawn.cols(), rightrighteyedrawn.rows(), Bitmap.Config.ARGB_8888);
        imgView = (ImageView) findViewById(R.id.rightrighteye);
        imgView.setId(11);
        show_image_sec(rightrighteyedrawn);
    }
    */


    @Override
    public boolean onTouch(View v, MotionEvent event) {


        if (v.getId() == 1 && event.getAction() == MotionEvent.ACTION_UP) {


            Log.w(TAG, "face coord is: " + (int)(event.getX()) + ", " + (int)( event.getY()));

        } else if (v.getId() == 2 && event.getAction() == MotionEvent.ACTION_UP) {
            box.width = 100;
            box.height = 40;
            box.x = (int)(event.getX()) - box.width/2;
            box.y = (int)( event.getY()) - box.height/2;
            //mouse_draw_box = true;
            //face_done = true;

            draw_box(faceimagedrawn);

            Log.w(TAG, "face drawn coord is: " + (int)(event.getX()) + ", " + (int)( event.getY()));

        } else if (v.getId() == 3 && event.getAction() == MotionEvent.ACTION_UP) {
            box.width = 32;
            box.height = 32;
            box.x = (int)(event.getX()) - box.width/2;
            box.y = (int)( event.getY()) - box.height/2;

            draw_box_sec(lefteyedrawn);
            Log.w(TAG, "left eye coord is: " + (int)(event.getX()) + ", " + (int)( event.getY()));


        } else if (v.getId() == 4 && event.getAction() == MotionEvent.ACTION_UP) {
            anchor.x = (int)(event.getX())+55;
            anchor.y = (int)(event.getY());
            //leftlefteyedrawn_aux = leftlefteyedrawn.clone();
            //lefteyedrawn_aux = lefteyedrawn.clone();
            //Core.circle(lefteyedrawn_aux, new Point(rectLELE.x + (int)(anchor.x/6.0), rectLELE.y + (int)(anchor.y/6.0)), 3, new Scalar(0, 255, 0, 255), 1);
            //show_image_sec(lefteyedrawn_aux);

            draw_anchor(lefteyedrawn);
            Log.w(TAG, "in side id = 4");
            Log.w(TAG, "pos: " + anchor.x + ", " + anchor.y);


        } else if (v.getId() == 14 && event.getAction() == MotionEvent.ACTION_UP) {
            anchor.x = (int)(event.getX())+55;
            anchor.y = (int)(event.getY());
        //leftlefteyedrawn_aux = leftlefteyedrawn.clone();
        //lefteyedrawn_aux = lefteyedrawn.clone();
        //Core.circle(lefteyedrawn_aux, new Point(rectLELE.x + (int)(anchor.x/6.0), rectLELE.y + (int)(anchor.y/6.0)), 3, new Scalar(0, 255, 0, 255), 1);
        //show_image_sec(lefteyedrawn_aux);
            draw_anchor(righteyedrawn);
            Log.w(TAG, "in side id = 14");
            Log.w(TAG, "pos: " + anchor.x + ", " + anchor.y);

        }

        else if (v.getId() == 5 && event.getAction() == MotionEvent.ACTION_UP) {
            box.width = 40;
            box.height = 40;
            box.x = (int) (event.getX()) - box.width / 2;
            box.y = (int) (event.getY()) - box.height / 2;

            draw_box_sec(faceimagedrawn);
            Log.w(TAG, "left eye coord is: " + (int) (event.getX()) + ", " + (int) (event.getY()));
        } else if (v.getId() == 6 && event.getAction() == MotionEvent.ACTION_UP) {
            anchor.x = (int)(event.getX());
            anchor.y = (int)(event.getY());
            //leftlefteyedrawn_aux = leftlefteyedrawn.clone();
            //lefteyedrawn_aux = lefteyedrawn.clone();
            //Core.circle(lefteyedrawn_aux, new Point(rectLELE.x + (int)(anchor.x/6.0), rectLELE.y + (int)(anchor.y/6.0)), 3, new Scalar(0, 255, 0, 255), 1);
            //show_image_sec(lefteyedrawn_aux);

            draw_anchor(leftrighteyedrawn);

        } else if (v.getId() == 13 && event.getAction() == MotionEvent.ACTION_UP) {
            anchor.x = (int)(event.getX());
            anchor.y = (int)(event.getY());
            //leftlefteyedrawn_aux = leftlefteyedrawn.clone();
            //lefteyedrawn_aux = lefteyedrawn.clone();
            //Core.circle(lefteyedrawn_aux, new Point(rectLELE.x + (int)(anchor.x/6.0), rectLELE.y + (int)(anchor.y/6.0)), 3, new Scalar(0, 255, 0, 255), 1);
            //show_image_sec(lefteyedrawn_aux);

            draw_anchor(leftrighteyedrawn);

        }

        else if (v.getId() == 7 && event.getAction() == MotionEvent.ACTION_UP) {
            box.width = 100;
            box.height = 40;
            box.x = (int)(event.getX()) - box.width/2;
            box.y = (int)( event.getY()) - box.height/2;
            //mouse_draw_box = true;
            //face_done = true;

            draw_box(faceimagedrawn);

            Log.w(TAG, "face drawn coord is: " + (int)(event.getX()) + ", " + (int)( event.getY()));

        } else if (v.getId() == 8 && event.getAction() == MotionEvent.ACTION_UP) {
            box.width = 40;
            box.height = 40;
            box.x = (int)(event.getX()) - box.width/2;
            box.y = (int)( event.getY()) - box.height/2;

            draw_box_sec(faceimagedrawn);
            Log.w(TAG, "right eye coord is: " + (int)(event.getX()) + ", " + (int)( event.getY()));

        } else if (v.getId() == 9 && event.getAction() == MotionEvent.ACTION_UP) {
            anchor.x = (int)(event.getX())+55;
            anchor.y = (int)(event.getY());
            //leftlefteyedrawn_aux = leftlefteyedrawn.clone();
            //lefteyedrawn_aux = lefteyedrawn.clone();
            //Core.circle(lefteyedrawn_aux, new Point(rectLELE.x + (int)(anchor.x/6.0), rectLELE.y + (int)(anchor.y/6.0)), 3, new Scalar(0, 255, 0, 255), 1);
            //show_image_sec(lefteyedrawn_aux);

            draw_anchor(righteyedrawn);

        } else if (v.getId() == 10 && event.getAction() == MotionEvent.ACTION_UP) {
            box.width = 32;
            box.height = 32;
            box.x = (int) (event.getX()) - box.width / 2;
            box.y = (int) (event.getY()) - box.height / 2;

            draw_box_sec(righteyedrawn);
            Log.w(TAG, "right eye coord is: " + (int) (event.getX()) + ", " + (int) (event.getY()));
        } else if (v.getId() == 11 && event.getAction() == MotionEvent.ACTION_UP) {
            anchor.x = (int)(event.getX());
            anchor.y = (int)(event.getY());
            //leftlefteyedrawn_aux = leftlefteyedrawn.clone();
            //lefteyedrawn_aux = lefteyedrawn.clone();
            //Core.circle(lefteyedrawn_aux, new Point(rectLELE.x + (int)(anchor.x/6.0), rectLELE.y + (int)(anchor.y/6.0)), 3, new Scalar(0, 255, 0, 255), 1);
            //show_image_sec(lefteyedrawn_aux);

            draw_anchor(rightrighteyedrawn);

        }


            return true;


    }

    public void ondoneClick(View v)
    {
        Log.w(TAG, "vID is: " + v.getId());
        if(face_draw_done == false) {
            imgView.setImageDrawable(null);
            left_pupil();
            //save_cropped_image(faceimagedrawn, "face.jpg");
            face_draw_done = true;
        } else if(left_pupil_done == false) {
            Mat lefteye_pupil = faceimage.submat(box);

            //Log.w(TAG, "save pic: " + lefteye_save.width() + ", " + lefteye_save.height());

            //lefteye_save.convertTo(lefteye_save_pass, receiver.mGray.type());

            save_cropped_image(lefteye_pupil, receiver.user_name + "leftpupil.jpg");
            imgView.setImageDrawable(null);
            face_drawn();
            left_pupil_done = true;
        }

        else if(lefteye_draw_done == false) {

            Mat lefteye_save = faceimage.submat(box);

            Log.w(TAG, "save pic: " + lefteye_save.width() + ", " + lefteye_save.height());

            //lefteye_save.convertTo(lefteye_save_pass, receiver.mGray.type());

            save_cropped_image(lefteye_save, receiver.user_name + "lefteye.jpg");
            lefteye_load = lefteye_save;
            imgView.setImageDrawable(null);

            Log.w(TAG, "load pic: " + lefteye_save.width() + ", " + lefteye_save.height());
            left_anchor();
            lefteye_draw_done = true;

        } else if(leftlefteye_anchor_done == false) {
            //FileOutputStream fileou= openFileOutput("mytextfile.txt", MODE_PRIVATE);
            //OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            anchors[0] = (int)(anchor.x/4.0);
            anchors[1] = (int)(anchor.y/4.0);
            Log.w(TAG, "just saved leftleft anchor");
            //imgView.setImageDrawable(null);
            Log.w(TAG, "about to load leftright anchor");
            //left_anchor("leftright");
            leftlefteye_anchor_done = true;

            /*
            try {
                File myDir = new File(getFilesDir().getAbsolutePath());
                FileWriter fw = new FileWriter(myDir + "/anchor.txt", false);
                fw.write("Hello World");
                fw.close();
            } catch(FileNotFoundException e){

            } catch (IOException e) {

            }
            try {

            }
            */
        } else if(leftrighteye_anchor_done == false) {
            //FileOutputStream fileou= openFileOutput("mytextfile.txt", MODE_PRIVATE);
            //OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            anchors[2] = (int) (anchor.x/4.0);
            anchors[3] = (int) (anchor.y/4.0);
            imgView.setImageDrawable(null);
            right_pupil();
            leftrighteye_anchor_done = true;
        } else if(right_pupil_done == false) {
            Mat righteye_pupil = faceimage.submat(box);

            //Log.w(TAG, "save pic: " + lefteye_save.width() + ", " + lefteye_save.height());

            //lefteye_save.convertTo(lefteye_save_pass, receiver.mGray.type());

            save_cropped_image(righteye_pupil, receiver.user_name + "rightpupil.jpg");
            imgView.setImageDrawable(null);
            face_drawn_sec();
            right_pupil_done = true;
        } else if(righteye_draw_done == false) {

            Mat righteye_save = faceimage.submat(box);

            Log.w(TAG, "save pic: " + righteye_save.width() + ", " + righteye_save.height());
            receiver.righteye_save_pass = receiver.mGray.submat(receiver.r).submat(box);
            Log.w(TAG, "righteye_save_pass type before is: " + receiver.righteye_save_pass.type());

            save_cropped_image(righteye_save, receiver.user_name + "righteye.jpg");
            righteye_load = righteye_save;
            imgView.setImageDrawable(null);

            Log.w(TAG, "load pic: " + righteye_save.width() + ", " + righteye_save.height());
            right_anchor();
            righteye_draw_done = true;
        } else if(rightlefteye_anchor_done == false) {
            //FileOutputStream fileou= openFileOutput("mytextfile.txt", MODE_PRIVATE);
            //OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            anchors[4] = (int) (anchor.x/4.0);
            anchors[5] = (int) (anchor.y/4.0);
            //imgView.setImageDrawable(null);
            //right_anchor();
            rightlefteye_anchor_done = true;
        } else if(rightbottomeye_anchor_done == false) {
            //FileOutputStream fileou= openFileOutput("mytextfile.txt", MODE_PRIVATE);
            //OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            anchors[6] = (int)(anchor.x/4.0);
            anchors[7] = (int)(anchor.y/4.0);
            save_anchors();
            imgView.setImageDrawable(null);
            rightbottomeye_anchor_done = true;
            receiver.state = receiver.liveState;
            //receiver.anchors_pass = anchors.clone();
            backbutton.setVisibility(View.VISIBLE);

            /*
            try {
                File myDir = new File(getFilesDir().getAbsolutePath());
                FileWriter fw = new FileWriter(myDir + "/anchor.txt", false);
                fw.write("Hello World");
                fw.close();
            } catch(FileNotFoundException e){

            } catch (IOException e) {

            }
            try {

            }
            */
        }

    }

    public void save_anchors() {
        String filename = receiver.user_name + "anchors.txt";
        String outputString = anchors[0] + " " + anchors[1] + " " + anchors[2] + " " + anchors[3] + " " +
                anchors[4] + " " + anchors[5] + " " + anchors[6] + " " + anchors[7];

        try {
            FileOutputStream outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(outputString.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onbackClick (View v) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        //if (FdActivity.this.r == null) {

        bundle.putParcelable("org.opencv.samples.facedetect.back", Pop.this);
        intent.putExtras(bundle);
        intent.setClass(Pop.this, FdActivity.class);
        startActivity(intent);
    }

    public void draw_box(Mat todraw){
        Mat draw_aux = todraw.clone();
        Core.rectangle(draw_aux, box.tl(), box.br(),
                new Scalar(255, 0, 0, 255), 2);
        show_image(draw_aux);

    }
    public void draw_box_sec(Mat todraw){
        Mat draw_aux = todraw.clone();
        Core.rectangle(draw_aux, box.tl(), box.br(),
                new Scalar(255, 0, 0, 255), 2);
        show_image_sec(draw_aux);

    }
    public void draw_anchor(Mat todraw) {
        Mat draw_aux = todraw.clone();
        Core.circle(draw_aux, new Point(anchor.x, anchor.y), 3, new Scalar(0, 255, 0, 255), 2);
        show_image_sec(draw_aux);
    }

    public void show_image(Mat toshow) {
        Mat zoomwindow = new Mat(receiver.mRgbart.rows(), receiver.mRgbart.rows(), receiver.mRgbart.type());
        Imgproc.resize(toshow, zoomwindow, zoomwindow.size());
        Bitmap img = Bitmap.createBitmap(zoomwindow.rows(), zoomwindow.cols(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(zoomwindow, img);
        imgView.setImageBitmap(img);
        imgView.setOnTouchListener(this);
    }

    public void show_image_sec(Mat toshow) {

        Bitmap img = Bitmap.createBitmap(toshow.cols(), toshow.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(toshow, img);

        imgView.setImageBitmap(img);
        imgView.setOnTouchListener(this);

    }

    public void show_image_gray(Mat toshow) {

        Bitmap img = Bitmap.createBitmap(toshow.cols(), toshow.rows(), Bitmap.Config.ALPHA_8);

        Utils.matToBitmap(toshow, img);

        imgView.setImageBitmap(img);
        imgView.setOnTouchListener(this);

    }

    public void save_cropped_image(Mat tosave, String name) {
        /*
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String filename = name;
        File file = new File(path, filename);
        filename = file.toString();
        Log.w(TAG, "filename is: " + filename);
        Highgui.imwrite(filename, tosave);
        */
        String filename = name;
        File file = new File(getApplicationContext().getFilesDir(), filename);
        filename = file.toString();
        Log.w(TAG, "filename is: " + filename);
        Highgui.imwrite(filename, tosave);
        if(load_image(name).empty() == false)
            Log.w(TAG, "load is not empty");
        else
            Log.w(TAG, "load is empty");
    }

    public Mat load_image(String name) {
        String filename = name;
        File file = new File(getApplicationContext().getFilesDir(), filename);
        filename = file.toString();
        //Log.w(TAG, "filename is: " + filename);
        return Highgui.imread(filename);
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
