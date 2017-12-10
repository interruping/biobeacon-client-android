package kr.ac.dju.biobeacon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

public class RegisterActivity extends AppCompatActivity {
    /*!
    @breif 성
     */
    EditText _lastnameEditText;
    /*!
    @brief 이름
     */
    EditText _firstnameEditText;
    /*!
    @brief 아이디
     */
    EditText _usernameEditText;
    /*!
    @brief 비밀번호
     */
    EditText _passwordEditText;
    /*!
    @brief 비밀번호 확인
     */
    EditText _passwordConfirmEditText;
    /*!
    @breif 학번
     */
    EditText _idNumEditText;
    /*!
    @brief 프로필 이미지 버튼
     */
    ImageButton _profileImageButton;
    /*!
    @brief 학과
     */
    Spinner _departmentsSpinner;
    /*!
    @brief 회원가입 버튼
     */
    Button _registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initViewElements();
    }

    private void initViewElements() {
        _lastnameEditText = (EditText) findViewById(R.id.lastname_editText);
        _firstnameEditText = (EditText) findViewById(R.id.firstname_editText);
        _usernameEditText = (EditText) findViewById(R.id.lastname_editText);
        _passwordEditText = (EditText) findViewById(R.id.password_editText);
        _passwordConfirmEditText = (EditText) findViewById(R.id.password_confirm_editText);
        _idNumEditText = (EditText) findViewById(R.id.idNum_editText);
        _profileImageButton = (ImageButton) findViewById(R.id.profile_imageButton);
        _departmentsSpinner = (Spinner) findViewById(R.id.departments_spinner);
        _registerButton = (Button) findViewById(R.id.register_button);
    }
}
