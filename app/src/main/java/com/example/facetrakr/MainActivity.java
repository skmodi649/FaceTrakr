package com.example.facetrakr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {


    private ImageView originalImageIv;
    private Button detectFaceBtn;
    private ImageView croppedImageIv;

    // TAG for debugging
    private static final String TAG = "FACE_DETECT_TAG";

    // Factor used to make the detecting image smaller, to make the process faster
    private static final int SCALING_FACTOR = 10;

    private FaceDetector detector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        originalImageIv = findViewById(R.id.originalImageIv);
        detectFaceBtn = findViewById(R.id.detectFaceBtn);
        croppedImageIv = findViewById(R.id.croppedImageIv);

        FaceDetectorOptions realTimeFdo = new FaceDetectorOptions.Builder()
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .build();


            final int min = 1;
            final int max = 5;
            final int random = new Random().nextInt((max - min) + 1) + min;
            String picName = "pic" + Integer.toString(random);

          detector = FaceDetection.getClient(realTimeFdo);

        // Handling the click action, detect/cropping face from the original image
        detectFaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Here we have three methods of taking images:
                // **************** Method1: MethodBitmap from drawable ************************

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pic4);

                // ************Method2: Bitmap from Uri, in case you want to detect face in images picked from gallery/camera *********
             /*   Uri imageUri = null;
                try{
                    Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                }catch(IOException e){
                    e.printStackTrace();
                }
              */
                // *********** Method 3: Bitmap from ImageView, in case the images present in ImageView are from URL/Web ***********
             /*   BitmapDrawable bitmapDrawable = (BitmapDrawable) originalImageIv.getDrawable();
                Bitmap bitmap1 = bitmapDrawable.getBitmap(); */

                // Calling the analyze method
                analyzeImage(bitmap);
            }
        });
    }

   private void analyzeImage(Bitmap bitmap){
        Log.d(TAG, "analyzePhoto: ");
        Bitmap smallerBitmap = Bitmap.createScaledBitmap(
                bitmap,
                bitmap.getWidth() / SCALING_FACTOR,
                bitmap.getHeight() / SCALING_FACTOR,
                false
        );

        InputImage inputImage = InputImage.fromBitmap(smallerBitmap, 0);

        // Start the detection process
        detector.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                    @Override
                    public void onSuccess(List<Face> faces) {
                        Log.d(TAG, "onSuccess: No of Faces detected: "+faces.size());
                        for(Face face: faces){
                            // Getting detected face as rectangle
                            Rect rect = face.getBoundingBox();
                            rect.set(rect.left*SCALING_FACTOR,
                                    rect.top*(SCALING_FACTOR - 1),
                                    rect.right*SCALING_FACTOR,
                                    (rect.bottom*SCALING_FACTOR)+90);
                        }

                        cropDetectedFaces(bitmap, faces);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Detection failed
                        Log.e(TAG, "onFailure: ", e);
                        Toast.makeText(MainActivity.this, "Detection failed due to "+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void cropDetectedFaces(Bitmap bitmap, List<Face> faces) {
        Log.d(TAG, "cropDetectedFaces: ");
        Rect rect = faces.get(0).getBoundingBox();

        int x = Math.max(rect.left, 0);
        int y = Math.max(rect.top, 0);
        int width = rect.width();
        int height = rect.height();

        Bitmap croppedBitmap = Bitmap.createBitmap(
                bitmap,
                x,
                y,
                (x + width > bitmap.getWidth()) ? bitmap.getWidth() - x : width,
                (y + height > bitmap.getHeight()) ? bitmap.getHeight() - y : height
        );

        // Setting the cropped image to the respected image view
        croppedImageIv.setImageBitmap(croppedBitmap);

    }
}