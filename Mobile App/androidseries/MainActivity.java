package com.example.androidseries;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.*;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.dnn.Dnn;
import org.opencv.utils.Converters;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

   CameraBridgeViewBase cameraBridgeViewBase;
   BaseLoaderCallback baseLoaderCallback;
   boolean startSpp = false;
   boolean startTiny = false;
   boolean firstTimeYolo_spp = false;
   boolean firstTimeYolo_tiny = false;
   Net tinyYolo;
   Net SppYolo;


   private static final String TAG = "OpenCV/Sample/MobileNet";


   public void YOLO(View Button){
       if ( startSpp == false){
           startSpp= true;

           if (firstTimeYolo_spp == false){

               firstTimeYolo_spp = true;
               //String path = context.getExternalFilesDir(null).getAbsolutePath() ;
               String YoloCfg = getPath("yolov3-tiny-flower.cfg", this);
               String YoloWeights = getPath("yolov3-tiny-flower_best.weights", this);
               SppYolo = Dnn.readNetFromDarknet(YoloCfg, YoloWeights);

           }

       }
       else{
           startSpp = false;
       }
   }
   /*
   public void YOLO_tiny(View Button){
       if ( startTiny == false){
           startTiny= true;

           if (firstTimeYolo_tiny == false){
               firstTimeYolo_tiny = true;
               //String path = context.getExternalFilesDir(null).getAbsolutePath() ;
               String YoloCfg = getPath("yolov3-tiny.cfg", this);
               String YoloWeights = getPath("yolov3-tiny.weights", this);
               tinyYolo = Dnn.readNetFromDarknet(YoloCfg, YoloWeights);
           }

       }
       else{
           startTiny = false;
       }
   }
*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);

                switch(status){

                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }


            }

        };
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        long startTime = System.nanoTime();
        Mat frame = inputFrame.rgba();
        //count the number of flowers
        int NumofPotato = 0;
        int NumofSweetPotato = 0;
        int NumofEggplant = 0;
        int NumofIT = 0;

        int imgSize = 416;
        if (startTiny) {
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);
            imgSize = 416;
            Mat imageBlob = Dnn.blobFromImage(frame, 0.00392, new Size(imgSize, imgSize), new Scalar(0, 0, 0),
                    /*swapRB*/false, /*crop*/false);
            tinyYolo.setInput(imageBlob);
            java.util.List<Mat> result = new java.util.ArrayList<Mat>(2);
            List<String> outBlobNames = new java.util.ArrayList<>();
            outBlobNames.add(0, "yolo_16");
            outBlobNames.add(1, "yolo_23");
            tinyYolo.forward(result, outBlobNames);

            float confThreshold = 0.3f;

            List<Integer> clsIds = new ArrayList<>();//classId
            List<Float> confs = new ArrayList<>();//confidences > confThreshold
            List<Rect> rects = new ArrayList<>();//boundary boxes

            for (int i = 0; i < result.size(); ++i)
            {
                Mat level = result.get(i);
                for (int j = 0; j < level.rows(); ++j)
                {
                    Mat row = level.row(j);

                    Mat scores = row.colRange(5, level.cols());

                    Core.MinMaxLocResult mm = Core.minMaxLoc(scores);
                    //coords: 1,2,3,...,80 (MSCOCO)
                    float confidence = (float)mm.maxVal;

                    Point classIdPoint = mm.maxLoc;

                    if (confidence > confThreshold)
                    {
                        int centerX = (int)(row.get(0,0)[0] * frame.cols());
                        int centerY = (int)(row.get(0,1)[0] * frame.rows());
                        int width   = (int)(row.get(0,2)[0] * frame.cols());
                        int height  = (int)(row.get(0,3)[0] * frame.rows());


                        int left    = centerX - width  / 2;
                        int top     = centerY - height / 2;

                        clsIds.add((int)classIdPoint.x);
                        confs.add((float)confidence);
                        rects.add(new Rect(left, top, width, height));
                    }
                }
            }
            int ArrayLength = confs.size();

            if (ArrayLength>=1) {
                // Apply non-maximum suppression procedure.
                float nmsThresh = 0.2f;
                MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(confs));
                Rect[] boxesArray = rects.toArray(new Rect[0]);

                MatOfRect boxes = new MatOfRect(boxesArray);

                MatOfInt indices = new MatOfInt();

                Dnn.NMSBoxes(boxes, confidences, confThreshold, nmsThresh, indices);

                // Draw result boxes:
                int[] ind = indices.toArray();
                for (int i = 0; i < ind.length; ++i) {

                    int idx = ind[i];
                    Rect box = boxesArray[idx];

                    int idGuy = clsIds.get(idx);

                    float conf = confs.get(idx);


                    List<String> cocoNames = Arrays.asList("person", "bicycle", "car", "motorbike", "aeroplane", "bus", "train", "truck",
                            "boat", "traffic light", "fire hydrant", "stop sign", "parking meter", "bench",
                            "bird", "cat", "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe",
                            "backpack", "umbrella", "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard",
                            "sports ball", "kite", "baseball bat", "baseball glove", "skateboard", "surfboard",
                            "tennis racket", "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana",
                            "apple", "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake",
                            "chair", "sofa", "pottedplant", "bed", "diningtable", "toilet", "tvmonitor", "laptop", "mouse",
                            "remote", "keyboard", "cell phone", "microwave", "oven", "toaster", "sink", "refrigerator",
                            "book", "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush");

                    int intConf = (int) (conf * 100);
                    Imgproc.putText(frame, cocoNames.get(idGuy) + " " + intConf + "%", box.tl(), Core.FONT_HERSHEY_SIMPLEX, 2, new Scalar(255, 0, 0), 4);

                    Imgproc.rectangle(frame, box.tl(), box.br(), new Scalar(255, 0, 0), 4);
                }
            }
        }
        else if (startSpp == true) {
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);
            imgSize = 416;
            Mat imageBlob = Dnn.blobFromImage(frame, 0.00392, new Size(imgSize, imgSize), new Scalar(0, 0, 0),
                    /*swapRB*/false, /*crop*/false);
            SppYolo.setInput(imageBlob);
            java.util.List<Mat> result = new java.util.ArrayList<Mat>(2);
            List<String> outBlobNames = new java.util.ArrayList<>();
            outBlobNames.add(0, "yolo_16");
            outBlobNames.add(1, "yolo_23");
            //outBlobNames.add(2, "yolo_106");

            SppYolo.forward(result, outBlobNames);

            float confThreshold = 0.3f;

            List<Integer> clsIds = new ArrayList<>();//classId
            List<Float> confs = new ArrayList<>();//confidences > confThreshold
            List<Rect> rects = new ArrayList<>();//boundary boxes

            for (int i = 0; i < result.size(); ++i)
            {
                Mat level = result.get(i);
                for (int j = 0; j < level.rows(); ++j)
                {
                    Mat row = level.row(j);

                    Mat scores = row.colRange(5, level.cols());

                    Core.MinMaxLocResult mm = Core.minMaxLoc(scores);
                    //coords: 1,2,3,...,80 (MSCOCO)
                    float confidence = (float)mm.maxVal;

                    Point classIdPoint = mm.maxLoc;

                    if (confidence > confThreshold)
                    {
                        int centerX = (int)(row.get(0,0)[0] * frame.cols());
                        int centerY = (int)(row.get(0,1)[0] * frame.rows());
                        int width   = (int)(row.get(0,2)[0] * frame.cols());
                        int height  = (int)(row.get(0,3)[0] * frame.rows());


                        int left    = centerX - width  / 2;
                        int top     = centerY - height / 2;

                        clsIds.add((int)classIdPoint.x);
                        confs.add((float)confidence);
                        rects.add(new Rect(left, top, width, height));
                    }
                }
            }
            int ArrayLength = confs.size();


            if (ArrayLength>=1) {
                // Apply non-maximum suppression procedure.
                float nmsThresh = 0.2f;
                MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(confs));
                Rect[] boxesArray = rects.toArray(new Rect[0]);

                MatOfRect boxes = new MatOfRect(boxesArray);

                MatOfInt indices = new MatOfInt();

                Dnn.NMSBoxes(boxes, confidences, confThreshold, nmsThresh, indices);

                // Draw result boxes:
                int[] ind = indices.toArray();

                for (int i = 0; i < ind.length; ++i) {

                    int idx = ind[i];
                    Rect box = boxesArray[idx];

                    int idGuy = clsIds.get(idx);

                    float conf = confs.get(idx);


                    List<String> cocoNames = Arrays.asList("Ipomoea_triloba",
                            "eggplant",
                            "potato",
                            "sweet_potato"
                    );

                    int intConf = (int) (conf * 100);
                    if (cocoNames.get(idGuy) == "potato" || cocoNames.get(idGuy) == "sweet_potato"){
                        Imgproc.putText(frame, cocoNames.get(idGuy) + " " + intConf + "%", box.tl(), Core.FONT_HERSHEY_SIMPLEX, 2, new Scalar(0, 255, 0), 4);
                        Imgproc.rectangle(frame, box.tl(), box.br(), new Scalar(0, 255, 0), 4);
                        if (cocoNames.get(idGuy) == "potato") {NumofPotato++;}
                        else if(cocoNames.get(idGuy) == "sweet_potato"){NumofSweetPotato++;}
                    }
                    else {
                        Imgproc.putText(frame, cocoNames.get(idGuy) + " " + intConf + "%", box.tl(), Core.FONT_HERSHEY_SIMPLEX, 2, new Scalar(255, 0, 0), 4);
                        Imgproc.rectangle(frame, box.tl(), box.br(), new Scalar(255, 0, 0), 4);
                        if (cocoNames.get(idGuy) == "eggplant") {NumofEggplant++;}
                        else if(cocoNames.get(idGuy) == "Ipomoea_triloba"){NumofIT++;}
                    }
                }

            }
        }

        String[] stringNames = new String[]{"Ipomoea_triloba",
                "eggplant",
                "potato",
                "sweet_potato"
        };
        int[] NumArray = new int[]{NumofIT,
                NumofEggplant,
                NumofPotato,
                NumofSweetPotato};
        for (int i = 0; i <NumArray.length; i++){
            Imgproc.putText (
                    frame,                          // Matrix obj of the image
                    stringNames[i] + ": "+ NumArray[i],          // Text to be added
                    new Point(1, (i+1)*30+20),               // point
                    Core.FONT_HERSHEY_SIMPLEX ,      // front face
                    1,                               // front scale
                    new Scalar(194, 136, 67),             // Scalar object for color
                    4                                // Thickness
            );
        }

        long elapsedTime = System.nanoTime() - startTime;
        System.out.println("Execution time in millis: "
                + elapsedTime/1000000.00);
        return frame;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(),"There's a problem!", Toast.LENGTH_SHORT).show();
        }

        else
        {
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        if(cameraBridgeViewBase!=null){

            cameraBridgeViewBase.disableView();
        }
    }


    private static String getPath(String file, Context context) {
        AssetManager assetManager = context.getAssets();
        BufferedInputStream inputStream = null;
        try {
            // Read data from assets.
            inputStream = new BufferedInputStream(assetManager.open(file));
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
            // Create copy file in storage.
            File outFile = new File(context.getFilesDir(), file);
            FileOutputStream os = new FileOutputStream(outFile);
            os.write(data);
            os.close();
            // Return a path to file which may be read in common way.
            return outFile.getAbsolutePath();
        } catch (IOException ex) {
            Log.i(TAG, "Failed to upload a file");
        }
        return "";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (cameraBridgeViewBase!=null){
            cameraBridgeViewBase.disableView();
        }
    }
}