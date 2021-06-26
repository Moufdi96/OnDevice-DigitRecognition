package com.example.digitrecognizer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.digitrecognizer.ml.DigitReco;
import com.example.digitrecognizer.ml.Mnist;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {

    private ImageView mImageView;
    private Button mSelectButton;
    private Button mPredictButton;
    private TextView mText;
    private Bitmap mInputBitmap;
    private PaintView mPaintView;
    private TextView mResult;

    private static final int PERMISSION_CODE = 1000;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mPaintView = new PaintView(this);
        setContentView(R.layout.activity_main);
        mPaintView = findViewById(R.id.p_view);
        this.mContext = this;
        this.mResult = findViewById(R.id.result_view);
        //mImageView = findViewById(R.id.image_view);

        mPredictButton = findViewById(R.id.predict_button);
        mSelectButton = findViewById(R.id.select_button);
        //mText = findViewById(R.id.text_view);

        this.mSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPaintView.clearView();
                mResult.setTextSize(20);
                mResult.setText("Result");
            }
        });

        this.mPredictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                try {
                    DigitReco model = DigitReco.newInstance(mContext);
                    Bitmap mInputBitmap = convertToBitmap(mPaintView);
                    mInputBitmap = resize(mInputBitmap);
                    // Creates inputs for reference.
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 28, 28}, DataType.FLOAT32);
                    //TensorImage tensorImage =  new TensorImage(DataType.FLOAT32);
                    //tensorImage.load(mInputBitmap); //= TensorImage.fromBitmap(mInputBitmap);
                    Log.d("-----inputBitmap-------",mInputBitmap.toString());
                    //Log.d("-----datatype-------",tensorImage.getDataType().toString());
                    //Log.d("-----Input-------", tensorImage.getHeight() + " " + tensorImage.getWidth());
                    ByteBuffer byteBuffer =  convertToByteBuffer(mInputBitmap); //tensorImage.getBuffer();
                    Log.d("-----length-------", byteBuffer.array().length + "");

                    inputFeature0.loadBuffer(byteBuffer);
                    // Runs model inference and gets result.
                    DigitReco.Outputs outputs = model.process(inputFeature0);
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
                    //mText.setText(outputFeature0.getFloatArray()[6] + "");
                    float [] mList = outputFeature0.getFloatArray();
                    int result = getMaxIndex(mList);
                    mResult.setTextSize(50);
                    mResult.setText(result + "");
                    //Toast.makeText(mContext,"The number you drew is" + " " +result,Toast.LENGTH_LONG).show();

                    Log.d("-----OUTPUT-------", outputFeature0.getFloatArray() + "");

                    // Releases model resources if no longer used.
                    model.close();
                } catch (IOException e) {
                    // TODO Handle the exception
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_CODE:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    pickImageFormGallery();
                }
                else {
                    Toast.makeText(this,"Permission denied !",Toast.LENGTH_SHORT).show();

                }
        }
    }

    private void pickImageFormGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == 100){
            //mImageView.setImageURI(data.getData());

            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);

                this.mInputBitmap = resize(bitmap);
                mImageView.setImageBitmap(this.mInputBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Bitmap resize(Bitmap bmpOriginal)
    {
        int width, height;
        //height = bmpOriginal.getHeight();
        //width = bmpOriginal.getWidth();

        //Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        //Canvas c = new Canvas(bmpGrayscale);
        //Paint paint = new Paint();
        //ColorMatrix cm = new ColorMatrix();
        //cm.setSaturation(0);
        //ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        //paint.setColorFilter(f);
        //c.drawBitmap(bmpOriginal, 0, 0, paint);
        Bitmap resizedInput = Bitmap.createScaledBitmap(bmpOriginal,28,28,true);
        //int w = resizedInput.getWidth();
        //int h = resizedInput.getHeight();
        //Log.d("-----Info-------", "getByteCount()" + resizedInput.getByteCount());
        //Log.d("-----Input-------", w + " " + h);
        return resizedInput;
    }

    public ByteBuffer convertToByteBuffer(Bitmap bitmap){
        int modelInputSize = 4*28*28*1;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(modelInputSize);
        byteBuffer.order(ByteOrder.nativeOrder());
        int [] pixels = new int[28*28];
        bitmap.getPixels(pixels,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
        for(int pixelvalue:pixels){
            int r = (pixelvalue >> 16) & 0xFF;
            int g = (pixelvalue >> 8) & 0xFF;
            int b = pixelvalue & 0xFF;
            float normalizedPixel = (r+g+b)/3.0f/255.0f;
            byteBuffer.putFloat(normalizedPixel);
        }
        return byteBuffer;
    }

    //deprecated
    protected Bitmap convertToBitmap(PaintView paintView) {
        Bitmap map;
        paintView.setDrawingCacheEnabled(true);
        paintView.buildDrawingCache();
        return map=paintView.getDrawingCache();
    }

    public int getMaxIndex(float [] mList){
        int maxIndex = 0;
        float max =0;
        for(int i=0;i< mList.length;i++){
            if(max < mList[i]){
                max = mList[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }


}