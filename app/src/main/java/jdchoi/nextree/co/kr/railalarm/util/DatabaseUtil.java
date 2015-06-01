package jdchoi.nextree.co.kr.railalarm.util;

import android.content.res.AssetManager;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2015-05-29.
 */
public final class DatabaseUtil {

    /*
     *  앱의 DB정보를 외부저장소로 복사하여 외부저장소를 사용하기 위한 함수.
     */
    public static final String doCopy(AssetManager am) {

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
