package kr.ac.dju.biobeacon;

/**
 * Created by geonyounglim on 2017. 12. 10..
 */

public class Base64EncodedImagePassHelper {
    private static String _data = null;

    static public void save(String image) {
        _data = image;
    }

    static public boolean isSaved() {
        if ( _data != null )
            return true;
        else
            return false;

    }

    static public String get() {
        String ret = _data;
        _data = null;
        return ret;
    }
}
