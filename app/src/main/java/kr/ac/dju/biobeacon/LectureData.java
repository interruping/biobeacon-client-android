package kr.ac.dju.biobeacon;

/**
 * Created by dodrn on 2017-12-12.
 */

public class LectureData {
    private  String _lectureValue;
    private  String _id;

    public LectureData(String lecture,String id){
        _lectureValue = lecture;
        _id = id;
    }

    public String get_LectureData(){

        return _lectureValue;
    }

    public String get_id(){



        return _id;
    }
}
