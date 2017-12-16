package kr.ac.dju.biobeacon;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class RegisterActivity extends AppCompatActivity {

    //Constant 선언
    public final static int REQUEST_TAKE_PHOTO = 0;

    //Model 선언
    /*!
    @brief 학과 타이틀 목록
     */
    private ArrayList<String> _departmentTitles;
    /*!
    @brief 학과 타이틀 ID 맵
     */
    private HashMap<String, Integer> _departmentsMap;

    /*!
    @breif 현재 사진 경로
     */
    private String _currentPhotoPath;
    /*!
    @brief 이미지 id
     */
    private int _uploadedImageId;


    // View 선언
    /*!
    @breif 성
     */
    private EditText _lastnameEditText;
    /*!
    @brief 이름
     */
    private EditText _firstnameEditText;
    /*!
    @brief 아이디
     */
    private EditText _usernameEditText;
    /*!
   @brief 이메일
    */
    private EditText _emailEditText;
    /*
    @brief 비밀번호
     */
    private EditText _passwordEditText;
    /*!
    @brief 비밀번호 확인
     */
    private EditText _passwordConfirmEditText;
    /*!
    @breif 학번
     */
    private EditText _idNumEditText;
    /*!
    @brief 프로필 이미지 버튼
     */
    private ImageButton _profileImageButton;
    /*!
    @brief 학과
     */
    private Spinner _departmentsSpinner;
    /*!
    @brief 회원가입 버튼
     */
    private Button _registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initModelElements();
        initViewElements();
    }

    /*!
    @brief 모델요소 초기화
     */
    private void initModelElements() {
        _departmentTitles = new ArrayList<String>();
        _departmentsMap = new HashMap<String, Integer>();
        _currentPhotoPath = null;
        _uploadedImageId = 0;
    }

    /*!
    @brief 뷰 요소 초기화
     */
    private void initViewElements() {
        _lastnameEditText = (EditText) findViewById(R.id.lastname_editText);
        _firstnameEditText = (EditText) findViewById(R.id.firstname_editText);
        _usernameEditText = (EditText) findViewById(R.id.username_editText);
        _emailEditText = (EditText) findViewById(R.id.email_editText);
        _passwordEditText = (EditText) findViewById(R.id.password_editText);
        _passwordConfirmEditText = (EditText) findViewById(R.id.password_confirm_editText);
        _idNumEditText = (EditText) findViewById(R.id.idNum_editText);
        _profileImageButton = (ImageButton) findViewById(R.id.profile_imageButton);
        _profileImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectProfileImage();
            }
        });

        _departmentsSpinner = (Spinner) findViewById(R.id.departments_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, _departmentTitles);
        _departmentsSpinner.setAdapter(adapter);
        String selectInfo = "학과를 선택해주세요.";
        _departmentTitles.add(selectInfo);
        _departmentsMap.put(selectInfo, 0);
        adapter.notifyDataSetChanged();
        _departmentsSpinner.setSelection(adapter.getPosition(selectInfo));
        loadDepartmentListFromServer();
        _registerButton = (Button) findViewById(R.id.register_button);
        _registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doRegister();
            }
        });
    }

    /*!
    @brief 회원가입 입력 중 누락 확인
     */
    private boolean checkBlank() {
        boolean result = false;

        if (result) return true;

        result = _lastnameEditText.getText().toString().isEmpty();
        if (result) return true;
        result = _firstnameEditText.getText().toString().isEmpty();
        if (result) return true;
        result = _usernameEditText.getText().toString().isEmpty();
        if (result) return true;
        result = _emailEditText.getText().toString().isEmpty();
        if (result) return true;
        result = _passwordEditText.getText().toString().isEmpty();
        if (result) return true;
        result = _passwordConfirmEditText.getText().toString().isEmpty();
        if (result) return true;
        result = _passwordConfirmEditText.getText().toString().equals(_passwordEditText.getText().toString()) ? false : true;
        if (result) return true;
        result = _idNumEditText.getText().toString().isEmpty();
        if (result) return true;
        result = _uploadedImageId == 0 ? true : false;
        if (result) return true;
        result = _departmentsSpinner.getSelectedItemPosition() == 0 ? true : false;

        return result;
    }

    /*!
    @brief 회원가입 하기
     */
    private void doRegister() {
        if (checkBlank()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("회원가입 오류");
            alertDialogBuilder.setMessage("입력하지 않은 값이 있습니다.");
            alertDialogBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //확인을 눌렀을 때

                }
            });
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.show();

            return;
        }

        //아이디, 성,이름, 이메일 서버로 전송

        AsyncHttpClient uploadClient = new AsyncHttpClient();

        RequestParams params = new RequestParams();

        try {
            params.put("first_name", _firstnameEditText.getText().toString());
            params.put("last_name", _lastnameEditText.getText().toString());
            params.put("username", _usernameEditText.getText().toString());
            params.put("email", _emailEditText.getText().toString());
            params.put("password", _passwordEditText.getText().toString());
            params.put("confirm-password", _passwordConfirmEditText.getText().toString());
            params.put("is_staff", "false");
            params.put("id", _idNumEditText.getText().toString());
            params.put("department", _departmentsMap.get(_departmentsSpinner.getSelectedItem()));
            params.put("profile_image_id", _uploadedImageId);


        } catch (Exception e) {
            e.printStackTrace();
        }

        String uploadURL = getString(R.string.server_url) + getString(R.string.profile_upload);

        uploadClient.put(this, uploadURL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                        dialog();
                } catch (Exception e) {
                    //응답은 성공하였으나 값을 제대로 못 받음
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }







    /*!
    @brief 프로필 사진 받기
     */
    private void selectProfileImage() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){
        //사진파일 읽어오기
            Bitmap bitmap = BitmapUtil.rotateBitmapOrientation(_currentPhotoPath);
            Bitmap resizedBitmap = BitmapUtil.minimizeBitmap(bitmap);
            uploadProfileImageToServer(BitmapUtil.bitmapToInputStream(resizedBitmap));
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
        _currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /*!
    @breif 학과 목록 서버로부터 불러오기
     */
    private void loadDepartmentListFromServer() {
        AsyncHttpClient client = new AsyncHttpClient();

        String loadLectureListURL = getString(R.string.server_url) + getString(R.string.department_list_url);
        client.get(this, loadLectureListURL, null, "application/json", new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    JSONArray departments = response.getJSONArray("departments");
                    for ( int i=0; i < departments.length(); i++ ){
                        JSONObject department = departments.getJSONObject(i);
                        _departmentTitles.add(department.getString("name"));
                        _departmentsMap.put(department.getString("name"), department.getInt("id"));
                    }
                }catch (Exception e) {
                    //응답에는 성공하였으나 적절한 값이 없을 때
                    e.printStackTrace();
                }

            }

            //응답 실패
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    private void uploadProfileImageToServer(InputStream inputStream){
        //this alias
        final RegisterActivity self = this;

        AsyncHttpClient uploadClient = new AsyncHttpClient();


        uploadClient.addHeader(getString(R.string.auth_key), CookieManager.getInstance().getCookie(getString(R.string.token_key)));
        RequestParams params = new RequestParams();

        try {
            params.put("file", inputStream, "image/png");
        } catch (Exception e){
            e.printStackTrace();
        }

        String uploadURL = getString(R.string.server_url) + getString(R.string.profile_image_upload_url);

        uploadClient.post(this, uploadURL, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    Picasso.with(self).load(getString(R.string.server_url) + response.getString("uploaded_url"))
                            .into(_profileImageButton);
                    _uploadedImageId = response.getInt("image_id");
                } catch (Exception e){
                    //응답은 성공하였으나 값을 제대로 못 받음
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }


    private void dialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("축하합니다.");
        alertDialogBuilder.setMessage("회원가입이 완료되었습니다.");
        alertDialogBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //확인을 눌렀을 때
                Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);

            }
        });
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.show();

    }

}
