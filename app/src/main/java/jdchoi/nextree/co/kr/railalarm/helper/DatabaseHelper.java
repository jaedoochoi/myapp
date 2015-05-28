package jdchoi.nextree.co.kr.railalarm.helper;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

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

    public DatabaseHelper(Context context, int lineNumber){
        //생성과 동시에 데이터베이스를 준비한다.
        super(context, DATA_BASE_NAME, null, DATA_BASE_VERSION);
        //Assets에 위치한 데이터베이스 파일에 접근하기 위해 AssetManager를 얻는다.
        assetManager = context.getAssets();
        //데이터베이스 파일에 접근하기위해 assets에 위치한 DB파일을 외장메모리에 복사한다.
        EXTERNAL_DB_PATH = doCopy(assetManager);

        db = SQLiteDatabase.openDatabase(EXTERNAL_DB_PATH+"/"+DATA_BASE_NAME, null, SQLiteDatabase.OPEN_READONLY);

        switch (lineNumber){
           case 2: TABLE_NAME = "STATION_LINE2_TB";
               break;
           case 3: TABLE_NAME = "STATION_LINE3_TB";
               break;
           case 4: TABLE_NAME = "STATION_LINE4_TB";
               break;
           default:
               break;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //역정보 조회 관련 함수
    public Cursor selectStationsInfo(){
        query = "SELECT ST_ID, ST_NAME, ST_ETIME, ST_EXTIME FROM "+TABLE_NAME;

        return db.rawQuery(query, null);
    }

    //역의 출발역과 도착역의 ID로 두 역사이의 역을 조회하여 총 소요시간을 계산한다.
    public int selectTotalSpendTime(String from_stId, String to_stId){
        String selectionArgs[] = {from_stId, to_stId};
        query = "SELECT ST_ID, ST_ETIME, ST_EXTIME FROM "+ TABLE_NAME+
                "WHERE ST_ID > ? AND ST_ID <= ?";

        Cursor cursor = db.rawQuery(query, selectionArgs);
        int tot_etime = 0;
        while(cursor.moveToNext()){
            int st_etime_idx = cursor.getColumnIndex("ST_ETIME");
            tot_etime += cursor.getInt(st_etime_idx);
        }

        Log.d("tot_etime=========>", ""+tot_etime);
        return tot_etime;
    }

    //앱의 DB정보를 외부저장소로 복사하여 외부저장소를 사용하기 위함.
    public String doCopy(AssetManager am) {

        File outDir=null, outFile = null;
        String sdCardDir = Environment.getExternalStorageDirectory().getAbsolutePath();//"/storage/extSdCard/Android";

        // 외장메모리가 사용가능 상태인지 확인
        if ( Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ) {

            outDir = new File( sdCardDir, "/data/jdchoi.nextree.co.kr.railalarm/databases");
            sdCardDir += "/data/jdchoi.nextree.co.kr.railalarm/databases";

            outDir.mkdirs();

            outFile = new File(outDir, "RailAlarm.db");

            InputStream is = null;
            OutputStream os = null;

            int size;

            try {
                // AssetsManager를 이용하여 RailAlarm.db파일 읽기
                is = am.open("RailAlarm.db");
                size = is.available();

                outFile.createNewFile();           // 디렉토리 생성

                os = new FileOutputStream(outFile);

                byte[] buffer = new byte[size];

                is.read(buffer);

                os.write(buffer);

                is.close();
                os.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sdCardDir;
    }
}
