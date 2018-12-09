package kr.ac.dju.biobeacon;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;

import android.os.Handler;
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
import java.util.List;

import cz.msebera.android.httpclient.Header;


public class AvailableLectureListFragment extends Fragment {
    ArrayList<LectureData> list = new ArrayList<>();

    LectureData temp;
    int _position;
    private ArrayList<String> mList;
    private ListView mListView;
    private ArrayAdapter mAdapter;
    public AvailableLectureListFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_available_lecture_list, container, false);
        ListView listview;

        mList = new ArrayList<String>();
        mListView= (ListView) rootView.findViewById(R.id.lecture_list);
        mAdapter =  new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, mList);
        mListView.setAdapter(mAdapter);
        loadLecturList();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View view, int position, long l) {
               _position = position;
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setTitle("수강신청안내");
                alertDialogBuilder.setMessage("수강신청하시겠습니까?");
                alertDialogBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //확인을 눌렀을 때
                        lecturereqest(list,_position);
                    }
                });
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.show();
            }
        });

        return rootView;
    }

    private void loadLecturList() {
        AsyncHttpClient profileLoadClient = new AsyncHttpClient();

        profileLoadClient.addHeader(getString(R.string.auth_key), CookieManager.getInstance().getCookie(getString(R.string.token_key)));


        String profileURL = getString(R.string.server_url) + getString(R.string.lecture_list_url);


        profileLoadClient.get(getActivity(), profileURL, new JsonHttpResponseHandler() {
            //불러오기 성공
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    int count;
                      for(count = 0; count< response.length(); count++) {
                          JSONArray ja = response;
                          JSONObject order = ja.optJSONObject(count);
                          String lecture_title = order.getString("lectureList");
                          String lecture_id = order.getString("id");
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

    private void lecturereqest(ArrayList<LectureData> data, int position) {


        AsyncHttpClient uploadClient = new AsyncHttpClient();

        uploadClient.addHeader(getString(R.string.auth_key), CookieManager.getInstance().getCookie(getString(R.string.token_key)));
        RequestParams params = new RequestParams();

        try {
            params.put("id", data.get(position).get_id());


        } catch (Exception e) {
            e.printStackTrace();
        }

        String uploadURL = getString(R.string.server_url) + getString(R.string.lecture_list_reqest);

        uploadClient.post(getActivity(), uploadURL, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    final Handler mainHandler = new Handler(getActivity().getMainLooper());
                        mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(getActivity(), "수강신청되었습니다.", Toast.LENGTH_SHORT);
                            toast.show();
//                             mList.remove(position);
                            mAdapter.notifyDataSetChanged();
                            }

                        }
                    );

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

}
