package kr.ac.dju.biobeacon;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


public class LectureListFragment extends android.app.Fragment {

    ArrayList<LectureData> list = new ArrayList<>();

    LectureData temp;
    int _position;
    private ArrayList<String> mList;
    private ListView mListView;
    private ArrayAdapter mAdapter;
    public LectureListFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_lecture_lsit, container, false);


        mList = new ArrayList<String>();
        mListView= (ListView) rootView.findViewById(R.id.list_lecture);
        mAdapter =  new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, mList);
        mListView.setAdapter(mAdapter);
        loadLectureList();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View view, int position, long l) {
                _position = position;
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setTitle("출석안내");
                alertDialogBuilder.setMessage("출석하시겠습니까?");
                alertDialogBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //확인을 눌렀을 때
                        lecturecheck(list, _position);
                    }
                });
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.show();
            }
        });

        return rootView;
    }

    private void loadLectureList() {
        AsyncHttpClient profileLoadClient = new AsyncHttpClient();

        profileLoadClient.addHeader(getString(R.string.auth_key), CookieManager.getInstance().getCookie(getString(R.string.token_key)));

        String profileURL = getString(R.string.server_url) + getString(R.string.lecture_list);

        profileLoadClient.get(getActivity(), profileURL, new JsonHttpResponseHandler(){
            //불러오기 성공
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    int count;
                    JSONArray ja = response.getJSONArray("result");
                    for(count = 0; count< ja.length(); count++) {
                        JSONObject order = ja.optJSONObject(count);
                        String lecture_title = order.getString("lecture");
                        String lecture_id = order.getString("pk");
                        temp = new LectureData(lecture_title, lecture_id);
                        list.add(temp);
                    }

                    final Handler mainHandler = new Handler(getActivity().getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            for(int i=0; i<list.size();i++) {

                                mList.add(list.get(i).get_LectureData());

                            }
                            mAdapter.notifyDataSetChanged();
                        }
                    });

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


    private void lecturecheck(ArrayList<LectureData> data, int position) {
        String _lectureTitle =  data.get(position).get_LectureData();
        String _lectureId =  data.get(position).get_id();


        Intent attendanceCheckIntent = new Intent(getActivity(), AttendanceCheckActivity.class);
        attendanceCheckIntent.putExtra("lecture_title", _lectureTitle);
        attendanceCheckIntent.putExtra("lecture_id", _lectureId);
        startActivity(attendanceCheckIntent);
    }



}
