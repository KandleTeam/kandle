package ch.epfl.sdp.kandle.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ch.epfl.sdp.kandle.R;
import ja.burhanrashid52.photoeditor.OnSaveBitmap;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;

public class PhotoEditorActivity extends AppCompatActivity {

    private Button mFinishButton;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_editor);

        ImageButton mBrushButton = findViewById(R.id.brushButton);
        mFinishButton = findViewById(R.id.finishButton);

        PhotoEditorView mPhotoEditorView = findViewById(R.id.photoEditorView);
        ImageView imageView = mPhotoEditorView.getSource();

        Intent intent = getIntent();
        imageUri = intent.getData();
        Picasso.get().load(imageUri).into(imageView);


        PhotoEditor mPhotoEditor = new PhotoEditor.Builder(this, mPhotoEditorView)
                .build();

        final ColorPicker cp = new ColorPicker(PhotoEditorActivity.this, 0, 0, 0);
        cp.enableAutoClose();
        cp.setCallback(mPhotoEditor::setBrushColor);

        mBrushButton.setOnClickListener(v -> {
            cp.show();
            mPhotoEditor.setBrushDrawingMode(true);
        });

        mFinishButton.setOnClickListener(v -> {

            mPhotoEditor.saveAsBitmap(new OnSaveBitmap() {

                @Override
                public void onBitmapReady(Bitmap saveBitmap) {


                    String path = MediaStore.Images.Media.insertImage(getContentResolver(), saveBitmap,
                             "image", "edited from Kandle");

                    Intent i = new Intent();
                    i.setData(Uri.parse(path));
                    setResult(RESULT_OK, i);
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    finish();
                }
            });


        });
    }
}
