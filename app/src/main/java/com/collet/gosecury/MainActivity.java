package com.collet.gosecury;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import static android.os.Process.SIGNAL_KILL;

public class MainActivity extends AppCompatActivity {

    private StorageReference mStorageRef;
    private Button captureImageBtn, detectTextBtn, checkIdentity, ownPhoto, quit;
    private ImageView imageView;
    private TextView textView;
    private Bitmap imageBitmap;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String lastnameResearch = "Nom";
    private String nameResearch = "Prénom";
    private String numberResearch = "";
    private String lastName;
    private String name;
    private String number;
    final String TAG = "MainActivity";
    File photoFile;
    static final int REQUEST_TAKE_PHOTO = 1;
    String mCurrentPhotoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captureImageBtn = findViewById(R.id.capture_image_btn);
        detectTextBtn = findViewById(R.id.detect_text_image_btn);
        checkIdentity = findViewById(R.id.check_identity);
        imageView = findViewById(R.id.image_view);
        textView = findViewById(R.id.text_display);
        ownPhoto = findViewById(R.id.own_photo);
        quit = findViewById(R.id.exit);

        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        captureImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
                textView.setText("Détectez le texte de l'image.");
                checkIdentity.setVisibility(View.INVISIBLE);
                detectTextBtn.setVisibility(View.VISIBLE);
                quit.setVisibility(View.INVISIBLE);
            }
        });

        ownPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        detectTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectTextFromImage();
            }
        });

        checkIdentity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIdentity();
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            switch (requestCode) {
                case 1:{
                    System.out.println("----------------------Case 1");
                    if (resultCode == RESULT_OK) {
                        File file = new File(mCurrentPhotoPath);
                        imageBitmap = MediaStore.Images.Media
                                .getBitmap(this.getContentResolver(), Uri.fromFile(file));
                        if (imageBitmap != null) {
                            imageView.setImageBitmap(imageBitmap);
                            sendToStorage();
                        }
                    }
                    break;
                }
                case 0: {
                    System.out.println("----------------------ERROR CASE 0 ");
                    break;
                }
                default:{
                    System.out.println("-----------------DEFAULT : " + requestCode);
                }
            }

        } catch (Exception error) {
            error.printStackTrace();
        }
    }
    private void dispatchTakePictureIntent() {
        if (photoFile!=null){
            photoFile.delete();
        }
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,"com.collet.gosecury", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void detectTextFromImage() {
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        firebaseVisionTextRecognizer.processImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                displayTextFormImage(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                Log.d("Error: ", e.getMessage());
            }
        });
    }
    private void displayTextFormImage(FirebaseVisionText firebaseVisionText) {
        String text = "";
        lastName="";
        name="";
        number="";
        List<FirebaseVisionText.TextBlock> blockList = firebaseVisionText.getTextBlocks();
        if (blockList.size() == 0) {
            Toast.makeText(this, "No text found", Toast.LENGTH_SHORT).show();
            photoFile.delete();
        } else {
            int i=0;
            int j=0;

            for (FirebaseVisionText.TextBlock block : blockList) {
                List<FirebaseVisionText.Line> LineList = block.getLines();
                for (FirebaseVisionText.Line line : LineList) {
                    if (i >= 2 && i <= 7) {
                        if (line.getText().contains(lastnameResearch)) {
                            lastName = researchWord(lastnameResearch, line);
                        }
                        if (line.getText().contains(name)) {
                            name = researchWord(nameResearch, line);
                            j=1;
                        }
                    }
                    if (i>12 && j==1){
                        numberResearch =name + "<<";
                        if (line.getText().contains(numberResearch)) {
                            number = researchNumber(line,numberResearch);
                        }
                    }
                    text = "identité : " + lastName + " " + name + " numéro : "+ number;
                    i += 1;
                }
            }
            photoFile.delete();
            checkIdentity.setVisibility(View.VISIBLE);
            textView.setText(text);
        }
    }
    private String researchWord(String Word, FirebaseVisionText.Line line){
        String result="";
            String[] tabTextBloc = line.getText().split(" ");
            for (int j = 0 ; j < tabTextBloc.length ; j++){
                if (tabTextBloc[j].contains(Word)){
                    result = tabTextBloc[j+1];
                    result = result.replace(",","");
                    result = removeAccents(result);
                }
            }
        return result;
    }
    private String researchNumber(FirebaseVisionText.Line line, String name){
        String result="";
        int m = 0;
        if (line.getText().contains(name)) {
            String[] tabTextBloc = line.getText().split("");
            if (tabTextBloc[0].isEmpty()){
                for (int j = 1; j < 13; j++) {
                    result += tabTextBloc[j];
                }
            }else {
                for (int j = 0; j < 12; j++) {
                    result += tabTextBloc[j];
                }
            }
            result = result.replace(" ","");
        }
        return result;
    }
    public static String removeAccents(String text){
        return text == null ? null
                : Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
    private void checkIdentity() {
        textView.clearComposingText();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference notebookRef = db.collection("Users");

        notebookRef.whereEqualTo("number", number)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        String data = "";
                        String id = "";
                        ArrayList<String> dateList = new ArrayList<String>();
                        String currentDate="";

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Users user = documentSnapshot.toObject(Users.class);

                            id = documentSnapshot.getId();
                            dateList = user.getDateList();
                            currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                            dateList.add(currentDate);

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            DocumentReference noteRef = db.document("Users/" + id);
                            String keyDate = "dateList";
                            noteRef.update(keyDate, dateList);
                        }

                        if (queryDocumentSnapshots.isEmpty()) {
                            data = "ACCES REFUSE";
                            ownPhoto.setVisibility(View.VISIBLE);
                            captureImageBtn.setVisibility(View.INVISIBLE);
                            checkIdentity.setVisibility(View.INVISIBLE);
                            detectTextBtn.setVisibility(View.INVISIBLE);
                            textView.setText("Pas autorisé: Prendre une photo de vous.");
                            quit.setVisibility(View.VISIBLE);
                        }else{
                            data = currentDate + "\n ACCES AUTORISE";
                        }
                    }
                });
    }
    private void sendToStorage(){
        mStorageRef = FirebaseStorage.getInstance().getReference();
        Uri file = Uri.fromFile(photoFile);
        StorageReference riversRef = mStorageRef.child("Picts/Bonsoir/"+file.getLastPathSegment());

        riversRef.putFile(file)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
