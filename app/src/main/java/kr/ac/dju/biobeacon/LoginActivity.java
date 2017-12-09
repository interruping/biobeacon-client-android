package kr.ac.dju.biobeacon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {
    /*!
    @brief 로그인 버튼
     */
    Button _loginButton;
    /*!
    @breif 아이디
     */
    EditText _usernameEditText;
    /*!
    @brief 패스워드
     */
    EditText _passwordEditText;
    /*!
    @breif 회원가입 버튼
     */
    Button _registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViewElements();
    }

    /*!
    @brief 뷰 요소 초기화
     */
    private void initViewElements() {
        _loginButton = (Button)findViewById(R.id.login_button);
        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //로그인 버튼 눌림
            }
        });
        _usernameEditText = (EditText)findViewById(R.id.username_editText);
        _passwordEditText = (EditText)findViewById(R.id.password_editText);
        _registerButton = (Button)findViewById(R.id.register_button);
        _registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //회원가입 버튼 눌림
                registerActivityStart();
            }
        });
    }

    /*!
    @breif 회원가입 액티비티 활성화
     */
    private void registerActivityStart() {
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        startActivity(registerIntent);
    }

}
