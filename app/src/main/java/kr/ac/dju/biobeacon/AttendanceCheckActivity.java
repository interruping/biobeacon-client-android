package kr.ac.dju.biobeacon;

import android.hardware.fingerprint.FingerprintManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mattprecious.swirl.SwirlView;
import com.multidots.fingerprintauth.AuthErrorCodes;
import com.multidots.fingerprintauth.FingerPrintAuthCallback;
import com.multidots.fingerprintauth.FingerPrintAuthHelper;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class AttendanceCheckActivity extends AppCompatActivity implements FingerPrintAuthCallback{

    //모델 선언
    FingerPrintAuthHelper _fingerprintAuthHelper;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_check);
        initModelElements();
        initViewElements();
    }

    /*!
    @brief 모델 요소 초기화
     */
    private void initModelElements() {
        _fingerprintAuthHelper = FingerPrintAuthHelper.getHelper(this, this);
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
        _fingerprintAuthHelper.startAuth();
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
    }

    // 등록된 지문이 없을 때
    @Override
    public void onNoFingerPrintRegistered() {
        if ( _fingerprintSwirlView != null )
            _fingerprintSwirlView.setState(SwirlView.State.ERROR);
        //There are no finger prints registered on this device.
    }

    // 마시멜로우 미만 버전일 때
    @Override
    public void onBelowMarshmallow() {
        //Device running below API 23 version of android that does not support finger print authentication.
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
}
