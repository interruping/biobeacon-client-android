package kr.ac.dju.biobeacon;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class ProfileFragment extends Fragment {

    //뷰 선언
    /*!
    @brief 아이디
     */
    TextView _usernameTextView;
    /*!
    @brief 학번
     */
    TextView _idNumTextView;
    /*!
    @brief 이름
     */
    TextView _nameTextView;
    /*!
    @brief 이메일
     */
    TextView _emailTextView;
    /*!
    @brief 학과
     */
    TextView _departmentTextView;
    /*!
    @brief 프로필 사진
     */
    ImageView _profileImageView;
    /*
   @brief 이름
    */
    String first_name;
    /*!
   @brief 성
    */
    String last_name;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        initViewElements(rootView);
        loadProfileFromServer();
        return rootView;
    }

    /*!
    @brief
     */
    private void initViewElements(View rootView) {
        _usernameTextView = (TextView)rootView.findViewById(R.id.username_textView);
        _idNumTextView = (TextView)rootView.findViewById(R.id.idNum_textView);
        _nameTextView = (TextView)rootView.findViewById(R.id.name_textView);
        _emailTextView = (TextView)rootView.findViewById(R.id.email_textView);
        _departmentTextView = (TextView)rootView.findViewById(R.id.department_textView);
        _profileImageView = (ImageView)rootView.findViewById(R.id.profile_imageView);

    }

    /*!
    @brief 프로필 정보 불러오기
     */
    private void loadProfileFromServer() {

        AsyncHttpClient profileLoadClient = new AsyncHttpClient();

        profileLoadClient.addHeader(getString(R.string.auth_key), CookieManager.getInstance().getCookie(getString(R.string.token_key)));

        String profileURL = getString(R.string.server_url) + getString(R.string.profile_url);


        profileLoadClient.get(getActivity(), profileURL, new JsonHttpResponseHandler(){
            //불러오기 성공
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    // 성, 이름 별개로 미리 출력
                    last_name = response.getString("last_name");
                    first_name = response.getString("first_name");
                    // 프로필 값 받아오기
                    _usernameTextView.setText(response.getString("username"));
                    _idNumTextView.setText(response.getString("id"));
                    _nameTextView.setText(last_name+first_name);
                    _emailTextView.setText(response.getString("email"));
                    _departmentTextView.setText(response.getString("department"));
                    Picasso.with(getActivity())
                            .load(getString(R.string.server_url) + response.getString("profile_image"))
                            .into(_profileImageView);

                } catch (Exception e) {
                    //응답은 성공하였으나 값이 올바르지 않음
                    e.printStackTrace();

                }
            }
            //불러오기 실패
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

}
