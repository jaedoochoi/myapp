package jdchoi.nextree.co.kr.railalarm.helper;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import jdchoi.nextree.co.kr.railalarm.domain.AlarmStations;
import jdchoi.nextree.co.kr.railalarm.util.AlarmStationsMaker;
import jdchoi.nextree.co.kr.railalarm.util.DatabaseUtil;

/**
 * Created by Administrator on 2015-05-28.
 */
public class DatabaseHelper extends SQLiteOpenHelper{

    private static final String DATA_BASE_NAME = "RailAlarm.db";
    private static final int DATA_BASE_VERSION = 1;
    private String TABLE_NAME = "STATION_LINE1_TB";
    private String EXTERNAL_DB_PATH;
    private SQLiteDatabase db;
    private String query;
    private AssetManager assetManager;

    public DatabaseHelper(Context context){
        //생성과 동시에 데이터베이스를 준비한다.
        super(context, DATA_BASE_NAME, null, DATA_BASE_VERSION);
        //Assets에 위치한 데이터베이스 파일에 접근하기 위해 AssetManager를 얻는다.
        assetManager = context.getAssets();
        //데이터베이스 파일에 접근하기위해 assets에 위치한 DB파일을 외장메모리에 복사한다.
        EXTERNAL_DB_PATH = DatabaseUtil.doCopy(assetManager);

        db = SQLiteDatabase.openDatabase(EXTERNAL_DB_PATH+"/"+DATA_BASE_NAME, null, SQLiteDatabase.OPEN_READONLY);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /*
     *  역정보 조회 관련 함수
     */
    public AlarmStations selectStationsInfo(int lineNumber){
        setTableName(lineNumber);

        query = "SELECT ST_ID, ST_NAME, ST_ETIME, ST_EXTIME FROM "+TABLE_NAME;

        Cursor cursor = db.rawQuery(query, null);

        return AlarmStationsMaker.make(cursor);
    }

    /*
     *  역의 출발역과 도착역의 ID로 두 역사이의 역을 조회하여 총 소요시간을 계산하는 함수.
     */
    public int selectTotalSpendTime(String from_stId, String to_stId){
        //상행일 경우 출발역의 소요시간을 포함하지 않고
        //하행일 경우 출발역의 소요시간을 포함한다.
        query = "SELECT ST_ID, ST_ETIME, ST_EXTIME FROM "+ TABLE_NAME;
        if(Integer.valueOf(from_stId) - Integer.valueOf(to_stId) > 0){
            query += " WHERE ST_ID <= ? AND ST_ID > ?";
        } else {
            query += " WHERE ST_ID > ? AND ST_ID <= ?";
        }

        Cursor cursor = db.rawQuery(query, new String[] {from_stId, to_stId});
        int tot_etime = 0;
        while(cursor.moveToNext()){
            int st_etime_idx = cursor.getColumnIndex("ST_ETIME");
            //테스트 결과 시간이 예상보다 많이 안맞는 경우가 발생함.
            //조정이 필요하여 2분 이상인 역의 경우 1분씩 줄임. 출퇴근 시간에 열차가 평소보다 빨리 움직이나?
            int modify_tot_etime = cursor.getInt(st_etime_idx);
            if(modify_tot_etime > 2){
                modify_tot_etime -= 1;
            }
            tot_etime += modify_tot_etime;
        }
        return tot_etime;
    }

    private void setTableName(int lineNumber){
        switch (lineNumber){
            case 1:
                this.TABLE_NAME = "STATION_LINE1_TB";
                break;
            case 2:
                this.TABLE_NAME = "STATION_LINE1_ICH_TB";
                break;
            case 3:
                this.TABLE_NAME = "STATION_LINE2_INN_TB";
                break;
            case 4:
                this.TABLE_NAME = "STATION_LINE2_OUT_TB";
                break;
            case 5:
                this.TABLE_NAME = "STATION_LINE3_TB";
                break;
            case 6:
                this.TABLE_NAME = "STATION_LINE4_TB";
                break;
            default:
                this.TABLE_NAME = "STATION_LINE1_TB";
                break;
        }
    }
}
