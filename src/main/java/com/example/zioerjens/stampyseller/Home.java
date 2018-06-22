package com.example.zioerjens.stampyseller;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.TypefaceSpan;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import static com.google.zxing.BarcodeFormat.QR_CODE;

public class Home extends AppCompatActivity {

    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    @Override
    protected void onStart() {
        super.onStart();

        LinearLayout btnGenerate = (LinearLayout) findViewById(R.id.btnGenerate);
        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateCode();
            }
        });
        setScanVaucherListener();
        setupSeekBar();
        this.activity = (Activity) this;
    }

    public void generateCode(){

        String code = generateNewCode(25);
        code = getMultiCode(code);
        insertCodeInDatabase(code);

        ImageView imageView = (ImageView) findViewById(R.id.qrCode);
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(code, BarcodeFormat.QR_CODE,1000,1000);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            imageView.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }

        resetSeekBar();
    }

    public String generateNewCode(int length){
        final String CHAR_LIST ="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        String code = "";
        for (int i = 0; i < length; i++){
            int randomPos = (int)(Math.random() * CHAR_LIST.length());
            code = code + CHAR_LIST.charAt(randomPos);
        }
        return code;
    }

    public void insertCodeInDatabase(String code){

        //Saves a stamp and adds it to the Database
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("freeStamp");
        DatabaseReference ref2 = ref.push();
        ref2.setValue(new FreeStamp(code));
    }

    public void setScanVaucherListener(){
        resetSeekBar();
        Button btnScan = (Button) findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.setOrientationLocked(true);
                integrator.setCaptureActivity(CaptureActivityPortrait.class);
                integrator.initiateScan();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);

        if (result != null){
            //Happens if you cancel the scanning
            if (result.getContents() == null) {
                Toast.makeText(this, R.string.scanCanceled, Toast.LENGTH_SHORT).show();
            }
            //Happens if you scan a QR-Code
            else {
                deleteVaucherIfValid(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void deleteVaucherIfValid(String code){

        final String scannedCode = code;
        final FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("voucher");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Boolean valid = false;

                for(DataSnapshot grSnapshot : dataSnapshot.getChildren()) {

                    Voucher voucher = grSnapshot.getValue(Voucher.class);

                    if (voucher.code.equals(scannedCode)) {
                        valid = true;
                        DatabaseReference ref = db.getReference("voucher/" + grSnapshot.getKey());
                        ref.removeValue();
                        showValidPopUp(true);
                    }
                }

                if (!valid){
                    showValidPopUp(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void showValidPopUp(Boolean valid){

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        View mView = getLayoutInflater().inflate(R.layout.show_success,null);

        if (valid) {
            mView.findViewById(R.id.success).setBackgroundResource(R.drawable.success);
        } else {
            mView.findViewById(R.id.success).setBackgroundResource(R.drawable.failure);
        }

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();

        new CountDownTimer(5000, 1000) { // 5000 = 5 sec

            public void onTick(long millisUntilFinished) {}

            public void onFinish() {
                dialog.dismiss();
            }
        }.start();
    }

    public void setupSeekBar(){
       SeekBar seekBar = findViewById(R.id.seekBar);
       seekBar.setMax(9);
       final LinearLayout btnGenerate = (LinearLayout) findViewById(R.id.btnGenerate);

       seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
           @Override
           public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

               TextView s2 = btnGenerate.findViewById(R.id.txtAmount);
               TextView s3 = btnGenerate.findViewById(R.id.txtCoffee);

               s2.setText((progress+1)+"");

               if (progress == 0) {
                   s3.setText(R.string.coffee);
               } else {
                   s3.setText(R.string.coffees);
               }
           }

           @Override
           public void onStartTrackingTouch(SeekBar seekBar) {

           }

           @Override
           public void onStopTrackingTouch(SeekBar seekBar) {

           }
       });
    }

    public String getMultiCode(String code){

        SeekBar seekbar = findViewById(R.id.seekBar);
        int multiplicator = seekbar.getProgress();

        code = code+multiplicator;

        return code;
    }

    public void resetSeekBar(){
        SeekBar seekbar = findViewById(R.id.seekBar);
        seekbar.setProgress(0);
    }
}
