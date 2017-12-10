package kr.ac.dju.biobeacon;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.EditText;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.client.CookieStore;
import cz.msebera.android.httpclient.entity.StringEntity;

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
                loginStart();
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
    /*!
    @brief 로그인 버튼 눌렀을 경우 호출됨
     */
    private void loginStart() {
        if ( checkblank() ){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("로그인 실패");
            alertDialogBuilder.setMessage("아이디 또는 비밀번호가 누락됐습니다.");
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


        AttemptLogin();
    }

    /*!
    @breif 서버로 로그인 요청 보냄
     */
    private void AttemptLogin() {
        //alias this
        final LoginActivity self = this;

        AsyncHttpClient loginClient = new AsyncHttpClient();

        JSONObject jsonParams = new JSONObject();
        StringEntity entity = null;
        try {
            jsonParams.put("username", _usernameEditText.getText().toString());
            jsonParams.put("password", _passwordEditText.getText().toString());
            entity = new StringEntity(jsonParams.toString());
        } catch (Exception e) {

        }

        String loginURL = getString(R.string.server_url) + getString(R.string.user_auth_url);
        loginClient.post(this, loginURL, entity, "application/json", new JsonHttpResponseHandler(){
            //로그인 성공
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                try {
                    String rawToken = response.getString("token");
                    CookieManager.getInstance().setCookie(getString(R.string.token_key), "JWT" + " " + rawToken);
                    loginSucessHandle();
                } catch (Exception e) {
                    //로그인은 성공하였으나 토큰값을 못 받아올 때.
                    e.printStackTrace();
                }
            }
            //로그인 실패
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(self);
                alertDialogBuilder.setTitle("로그인 실패");
                alertDialogBuilder.setMessage("아이디 또는 비밀번호를 확인하세요.");
                alertDialogBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //확인을 눌렀을 때
                    }
                });
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.show();
            }
        });
    }

    /*!
    @brief 로그인 성공 후 처리
     */
    private void loginSucessHandle() {
        Intent menuIntent = new Intent(this, MenuActivity.class);
        startActivity(menuIntent);
    }

    /*!
    @brief 아이디 비밀번화 비었는지 확인
     */
    private boolean checkblank() {
        if (_usernameEditText.getText().toString().isEmpty()) return true;
        if (_passwordEditText.getText().toString().isEmpty()) return true;

        return false;
    }

}
