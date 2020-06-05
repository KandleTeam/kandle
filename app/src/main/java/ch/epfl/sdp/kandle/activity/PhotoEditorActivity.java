package ch.epfl.sdp.kandle.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
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
import ch.epfl.sdp.kandle.fragment.TextDialogFragment;
import ja.burhanrashid52.photoeditor.OnSaveBitmap;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.TextStyleBuilder;

public class PhotoEditorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_editor);

        ImageButton mBrushButton = findViewById(R.id.brushButton);
        ImageButton mTextButton = findViewById(R.id.textButton);
        ImageButton mEraserButton = findViewById(R.id.eraserButton);
        ImageButton mUndoButton = findViewById(R.id.undoButton);
        ImageButton mRedoButton = findViewById(R.id.redoButton);
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

        final ColorPicker brushCp = new ColorPicker(PhotoEditorActivity.this, 0, 0, 0);
        brushCp.enableAutoClose();
        brushCp.setCallback(mPhotoEditor::setBrushColor);

        final ColorPicker textCp = new ColorPicker(PhotoEditorActivity.this, 0, 0, 0);
        textCp.enableAutoClose();
        textCp.setCallback(color -> {
            TextDialogFragment textDialogFragment = TextDialogFragment.show(this, color);
            textDialogFragment.setOnTextEditorListener(inputText -> {
                final TextStyleBuilder styleBuilder = new TextStyleBuilder();
                styleBuilder.withTextColor(color);
                styleBuilder.withTextSize(40f);
                styleBuilder.withTextFont(Typeface.DEFAULT);
                styleBuilder.withTextAppearance(144);

                mPhotoEditor.addText(inputText, styleBuilder);
            });
        });

        mBrushButton.setOnClickListener(v -> {
            brushCp.show();
            mPhotoEditor.setBrushDrawingMode(true);
        });

        mTextButton.setOnClickListener(v -> textCp.show());

        mEraserButton.setOnClickListener(v -> mPhotoEditor.brushEraser());

        mUndoButton.setOnClickListener(v -> mPhotoEditor.undo());

        mRedoButton.setOnClickListener(v -> mPhotoEditor.redo());

        mFinishButton.setOnClickListener(v -> mPhotoEditor.saveAsBitmap(new OnSaveBitmap() {

            @Override
            public void onBitmapReady(Bitmap saveBitmap) {


                String path = MediaStore.Images.Media.insertImage(getContentResolver(), saveBitmap,
                        "image", "edited from Kandle");

                Intent i = new Intent();
                if (path != null)
                    i.setData(Uri.parse(path));
                setResult(RESULT_OK, i);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                finish();
            }
        }));
    }
}
