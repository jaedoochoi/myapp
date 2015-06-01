package jdchoi.nextree.co.kr.railalarm.util;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import jdchoi.nextree.co.kr.railalarm.domain.AlarmStations;

/**
 * Created by Administrator on 2015-06-01.
 */
public final class AlarmStationsMaker {
    private static AlarmStations alarmStations = null;
    private static List<String> st_id = null;
    private static List<String> st_name = null;
    private static List<Integer> st_etime = null;
    private static List<Integer> st_extime = null;

    public static AlarmStations make(Cursor cursor){
        alarmStations = new AlarmStations();
        st_id = new ArrayList<String>();
        st_name = new ArrayList<String>();
        st_etime = new ArrayList<Integer>();
        st_extime = new ArrayList<Integer>();

        while(cursor.moveToNext()){

            int st_id_idx = cursor.getColumnIndex("ST_ID");
            int st_nm_idx = cursor.getColumnIndex("ST_NAME");
            int st_etime_idx = cursor.getColumnIndex("ST_ETIME");
            int st_extime_idx = cursor.getColumnIndex("ST_EXTIME");

            st_id.add(cursor.getString(st_id_idx));
            st_name.add(cursor.getString(st_nm_idx));
            st_etime.add(cursor.getInt(st_etime_idx));
            st_extime.add(cursor.getInt(st_extime_idx));
        }

        alarmStations.setStId(st_id);
        alarmStations.setStName(st_name);
        alarmStations.setEtime(st_etime);
        alarmStations.setExtime(st_extime);

        return alarmStations;
    }
}
