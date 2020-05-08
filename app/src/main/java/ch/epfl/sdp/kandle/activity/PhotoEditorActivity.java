package ch.epfl.sdp.kandle.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.squareup.picasso.Picasso;

import ch.epfl.sdp.kandle.R;
import ja.burhanrashid52.photoeditor.OnSaveBitmap;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;

public class PhotoEditorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_editor);

        ImageButton mBrushButton = findViewById(R.id.brushButton);
        Button mFinishButton = findViewById(R.id.finishButton);

        PhotoEditorView mPhotoEditorView = findViewById(R.id.photoEditorView);
        ImageView imageView = mPhotoEditorView.getSource();

        Intent intent = getIntent();
        Uri imageUri = intent.getData();
        if (imageUri != null)
            Picasso.get().load(imageUri).into(imageView);
        else
            imageView.setImageDrawable(getDrawable(R.drawable.logo));

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
