package kr.ac.dju.biobeacon;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.mattprecious.swirl.SwirlView;
import com.multidots.fingerprintauth.AuthErrorCodes;
import com.multidots.fingerprintauth.FingerPrintAuthCallback;
import com.multidots.fingerprintauth.FingerPrintAuthHelper;
import com.vistrav.ask.Ask;
import com.vistrav.ask.annotations.AskDenied;
import com.vistrav.ask.annotations.AskGranted;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class AttendanceCheckActivity extends AppCompatActivity implements FingerPrintAuthCallback{

    //상수 선언
    public static int REQUEST_TAKE_PHOTO = 0;
    /*!
    @brief 비콘 권한 설정 로그 텍스트
     */
    private static final String TAG = AttendanceCheckActivity.class.getSimpleName();

    //모델 선언
    /*!
    @brief 지문인식 헬퍼
     */
    FingerPrintAuthHelper _fingerprintAuthHelper;
    /*!
    @brief 프로필 이미지 base64 인코딩
     */
    String _profileImageBase64;
    /*!
    @brief 프로필 아이디
     */
    String _profileUsername;
    /*!
    @breif 사용자로부터 받은 카메라 얼굴 사진 경로
     */
    String _currentPhotoPath;
    /*!
    @breif 얼굴 등록 완료 여부 플래그
     */
    boolean _isEnrolled;
    /*!
    @breif 얼굴 등록 실패 여부 플래그
     */
    boolean _enrollFail;

    //뷰 선언
    /*!
    @brief 비콘 검색 상태 텍스트
     */
    TextView _beaconSearchStatusTextView;
    /*!
    @brief pulsatorLayout
     */
    PulsatorLayout _beaconSearchPulsatorLayout;
    /*!
    @brief 얼굴인식 프로그레스바
     */
    ProgressBar _faceverifyProgressBar;
    /*!
    @brief 얼굴인식 상태 텍스트
     */
    TextView _faceverifyStatusTextView;
    /*!
    @brief 지문 아이콘 프레임
     */
    FrameLayout _swirlFrameLayout;
    /*!
    @breif 지문 아이콘
     */
    SwirlView _fingerprintSwirlView;
    /*!
    @breif 지문 인식 안내 텍스트
     */
    TextView _fingerprintInfoTextView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_check);
        initModelElements();
        initViewElements();
        _fingerprintAuthHelper.startAuth();

        Ask.on(this)
                .forPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .withRationales("비콘 감지를 위한 권한을 요청합니다.") //optional
                .go();
    }

    /*!
    @brief 비콘 권한 설정
     */
    //optional
    @AskGranted(Manifest.permission.ACCESS_FINE_LOCATION)
    public void mapAccessGranted(int id) {
        Log.i(TAG, "MAP GRANTED");
    }

    //optional
    @AskDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    public void mapAccessDenied(int id) {
        Log.i(TAG, "MAP DENIED");
    }



    /*!
    @brief 모델 요소 초기화
     */
    private void initModelElements() {
        _fingerprintAuthHelper = FingerPrintAuthHelper.getHelper(this, this);

        Intent intent = getIntent();

        if ( Base64EncodedImagePassHelper.isSaved() != true){
            //프로필 이미지를 불러오지 못하였을 때 오류.
        }

        _profileImageBase64 = Base64EncodedImagePassHelper.get();
        _profileUsername = intent.getStringExtra("username");
        _isEnrolled = false;
        _enrollFail = false;

    }

    /*!
    @breif 뷰 요소 초기화
     */
    private void initViewElements() {
        _beaconSearchStatusTextView = (TextView) findViewById(R.id.beacon_searh_status_textView);
        _beaconSearchPulsatorLayout = (PulsatorLayout) findViewById(R.id.beacon_search_pulsatorLayout);
        _beaconSearchPulsatorLayout.start();
        _faceverifyProgressBar = (ProgressBar) findViewById(R.id.faceverify_progressBar);

        _faceverifyStatusTextView = (TextView) findViewById(R.id.faceverify_status_textView);
        _swirlFrameLayout = (FrameLayout) findViewById(R.id.swirl_frameLayout);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            //마시멜로우 이상만 지문인식 아이콘 사용
            _fingerprintSwirlView = new SwirlView(this);
            _fingerprintSwirlView.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            _swirlFrameLayout.addView(_fingerprintSwirlView);
        }
        _fingerprintInfoTextView = (TextView) findViewById(R.id.fingerprint_info_textView);
        hideFaceVerifyInfo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 지문 인식 종료
        _fingerprintAuthHelper.stopAuth();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 지문인식 시작
        //_fingerprintAuthHelper.startAuth();
    }

    /*!
        @breif 얼굴인식 안내 보이게 하기
         */
    private void showFaceVerifyInfo() {
        _faceverifyProgressBar.setVisibility(View.VISIBLE);
        _faceverifyStatusTextView.setVisibility(View.VISIBLE);
        _swirlFrameLayout.setVisibility(View.INVISIBLE);
        _fingerprintInfoTextView.setVisibility(View.INVISIBLE);
    }

    /*!
    @breif 얼굴인식 안내 숨기기
     */
    private void hideFaceVerifyInfo() {
        _faceverifyProgressBar.setVisibility(View.INVISIBLE);
        _faceverifyStatusTextView.setVisibility(View.INVISIBLE);
        _swirlFrameLayout.setVisibility(View.VISIBLE);
        _fingerprintInfoTextView.setVisibility(View.VISIBLE);
    }

    // 지문인식 하드웨어가 없을 때 호출
    @Override
    public void onNoFingerPrintHardwareFound() {
        //Device does not have finger print scanner.
        showFaceVerifyInfo();
        notifyFaceVerifyAndStart();
        _fingerprintAuthHelper.stopAuth();

    }

    // 등록된 지문이 없을 때
    @Override
    public void onNoFingerPrintRegistered() {
        if ( _fingerprintSwirlView != null )
            _fingerprintSwirlView.setState(SwirlView.State.ERROR);
        //There are no finger prints registered on this device.

        _fingerprintAuthHelper.stopAuth();

    }

    // 마시멜로우 미만 버전일 때
    @Override
    public void onBelowMarshmallow() {
        //Device running below API 23 version of android that does not support finger print authentication.
        showFaceVerifyInfo();
        notifyFaceVerifyAndStart();
        _fingerprintAuthHelper.stopAuth();
    }

    //지문인식 성공했을 때
    @Override
    public void onAuthSuccess(FingerprintManager.CryptoObject cryptoObject) {
        //Authentication sucessful.
        if ( _fingerprintSwirlView != null )
            _fingerprintSwirlView.setState(SwirlView.State.ON);
        _fingerprintInfoTextView.setText("지문 인식 성공");

    }

    @Override
    public void onAuthFailed(int errorCode, String errorMessage) {
        _fingerprintInfoTextView.setText("지문 인식 실패");
        switch (errorCode) {    //Parse the error code for recoverable/non recoverable error.
            case AuthErrorCodes.CANNOT_RECOGNIZE_ERROR:
                //Cannot recognize the fingerprint scanned.
                if ( _fingerprintSwirlView != null )
                    _fingerprintSwirlView.setState(SwirlView.State.ERROR);
                break;
            case AuthErrorCodes.NON_RECOVERABLE_ERROR:
                //This is not recoverable error. Try other options for user authentication. like pin, password.
                break;
            case AuthErrorCodes.RECOVERABLE_ERROR:
                //Any recoverable error. Display message to the user.
                break;
        }
    }

    /*!
    @brief 얼굴인식을 시작하기 전에 물어보는 매서드
     */
    private void notifyFaceVerifyAndStart() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("지문인식 미지원 기기");
        alertDialogBuilder.setMessage("얼굴인식을 시작합니다.");
        alertDialogBuilder.setPositiveButton("시작", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //확인을 눌렀을 때
                startFaceVerify();
            }
        });

        alertDialogBuilder.create().setCanceledOnTouchOutside(false);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.show();
    }

    /*!
    @brief
     */
    private void startFaceVerify() {
        //alias this
        final AttendanceCheckActivity self = this;

        AsyncHttpClient enrollClient = new AsyncHttpClient();

        enrollClient.addHeader("app_id", getString(R.string.kairos_app_id));
        enrollClient.addHeader("app_key", getString(R.string.kairos_key));

        JSONObject jsonParams = new JSONObject();
        StringEntity entity = null;

        try{
            jsonParams.put("image", _profileImageBase64);
            jsonParams.put("subject_id", _profileUsername);
            jsonParams.put("gallery_name", _profileUsername + getString(R.string.karios_gallery));
            entity = new StringEntity(jsonParams.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String enrollURL = getString(R.string.karios_base_url) + getString(R.string.karios_enroll_url);

        enrollClient.post(this, enrollURL, entity, "application/json", new JsonHttpResponseHandler(){
            //얼굴 등록 성공
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                _isEnrolled = true;

            }
            //얼굴 등록 실패
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                _enrollFail = true;
            }
        });
        startGetFaceImageFromUser();
    }

    /*!
    @breif 얼굴 사진을 얻기 위해 카메라 액티비티 띄움
     */
    private void startGetFaceImageFromUser() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "kr.ac.dju.biobeacon.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    //이미지 파일 placeholder
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
        _currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){
            //사진파일 읽어오기
            Bitmap bitmap = BitmapUtil.rotateBitmapOrientation(_currentPhotoPath);
            Bitmap resizedBitmap = BitmapUtil.minimizeBitmap(bitmap);
            startVerifyFace(resizedBitmap);
        }
    }

    private void startVerifyFace(final Bitmap bitmap){
        _faceverifyStatusTextView.setText("방금 찍은 얼굴을 프로필 사진 얼굴과 비교 중");
        if ( _isEnrolled != true ){
            if ( _enrollFail == true) {
                _faceverifyStatusTextView.setText("얼굴 등록 실패");
                return;
            }
            new Handler(getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    startVerifyFace(bitmap);
                }
            }, 20);
            return;
        }

        final AttendanceCheckActivity self = this;



        AsyncHttpClient verifyClient = new AsyncHttpClient();

        verifyClient.addHeader("app_id", getString(R.string.kairos_app_id));
        verifyClient.addHeader("app_key", getString(R.string.kairos_key));

        JSONObject jsonParams = new JSONObject();
        StringEntity entity = null;

        try{
            jsonParams.put("image", BitmapUtil.BitmapToBase64(bitmap));
            jsonParams.put("subject_id", _profileUsername);
            jsonParams.put("gallery_name", _profileUsername + getString(R.string.karios_gallery));
            entity = new StringEntity(jsonParams.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String enrollURL = getString(R.string.karios_base_url) + getString(R.string.karios_verify_url);

        verifyClient.post(this, enrollURL, entity, "application/json", new JsonHttpResponseHandler(){
            //얼굴 검증 응답 성공
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    JSONArray images = response.getJSONArray("images");
                    JSONObject first = images.getJSONObject(0);
                    JSONObject transaction = first.getJSONObject("transaction");
                    double confidence = transaction.getDouble("confidence");

                    if ( confidence > 0.6f) {
                        //얼굴 일치
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(self);
                        alertDialogBuilder.setTitle("얼굴 인식 성공");
                        alertDialogBuilder.setMessage("프로필 사진의 얼굴과 일치하는 것을 확인하였습니다");
                        alertDialogBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //확인을 눌렀을 때
                            }
                        });

                        alertDialogBuilder.create().setCanceledOnTouchOutside(false);
                        alertDialogBuilder.setCancelable(false);
                        alertDialogBuilder.show();
                    } else {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(self);
                        alertDialogBuilder.setTitle("얼굴 인식 실패");
                        alertDialogBuilder.setMessage("프로필 사진의 얼굴과 " + String.valueOf((int)(confidence*100)) + "% 일치 했습니다.");
                        alertDialogBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //확인을 눌렀을 때
                            }
                        });
                        alertDialogBuilder.create().setCanceledOnTouchOutside(false);
                        alertDialogBuilder.setCancelable(false);
                        alertDialogBuilder.show();
                        //얼굴 불일치
                    }
                } catch ( Exception  e) {
                    //얼굴 자체 인식 실패
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(self);
                    alertDialogBuilder.setTitle("얼굴 찾기 실패");
                    alertDialogBuilder.setMessage("사진에서 얼굴을 찾을 수 없습니다.");
                    alertDialogBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //확인을 눌렀을 때
                        }
                    });

                    alertDialogBuilder.create().setCanceledOnTouchOutside(false);
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.show();
                }
            }
            //얼굴 검증 응답 실패
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }
}
