package jdchoi.nextree.co.kr.railalarm;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import jdchoi.nextree.co.kr.railalarm.domain.AlarmStations;
import jdchoi.nextree.co.kr.railalarm.enm.Line;
import jdchoi.nextree.co.kr.railalarm.helper.DatabaseHelper;
import jdchoi.nextree.co.kr.railalarm.helper.GooglePlaceConnectionHelper;


public class MainActivity extends ActionBarActivity implements TextWatcher {
    //GUI관련 변수
    private AutoCompleteTextView from_text, to_text;
    private int line_number, from_etime, to_etime, back_press_cnt;
    private DatabaseHelper dbHelper;
    private String to_stId = "", from_stId = "";

    private List<String> stIdList = null;
    private List<String> stNameList = null;
    private List<Integer> stEtimeList = null;
    private List<Integer> stExtimeList = null;
    private AlarmStations alarmStations;

    //알람관련 변수
    private HttpsURLConnection client;

    private  LocationManager locationManager;
    private Location location;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            //Network를 통하여 위치정보를 얻는다.
            startLocationService();

            // Gets the URL from the UI's text field.
            String stringUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/output?";
            if (networkInfo != null && networkInfo.isConnected()) {
               GooglePlaceConnectionHelper helper =  new GooglePlaceConnectionHelper(location);
                helper.execute(stringUrl);
            }
        }

        back_press_cnt = 0;
        from_text = (AutoCompleteTextView) findViewById(R.id.edit_from);
        to_text = (AutoCompleteTextView) findViewById(R.id.edit_to);

        //AutoCompleteTextView에 TextWatcher를 셋팅한다.
        from_text.addTextChangedListener(this);
        to_text.addTextChangedListener(this);

        if(dbHelper == null){
            dbHelper = new DatabaseHelper(this);
            alarmStations = dbHelper.selectStationsInfo(Integer.valueOf(Line.ONE.getNo()));
        }

        //호선선택 스피너에서 몇호선인지 선택했을 때의 이벤트 처리
        Spinner line_spiner = (Spinner) findViewById(R.id.line_spinner);
        line_spiner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                //호선이 바뀌었을 경우 역정보를 다시 조회함.
                if (line_number != position + 1) {
                    from_text.setText("");
                    to_text.setText("");
                    line_number = position + 1;

                    alarmStations = dbHelper.selectStationsInfo(line_number);

                    stIdList = alarmStations.getStId();
                    stNameList = alarmStations.getStName();
                    stEtimeList = alarmStations.getEtime();
                    stExtimeList = alarmStations.getExtime();

                    //AutoCompleteTextView에 자동으로 검색되도록 검색할 단어들을 ArrayAdapter로 만들어 인수로 넘긴다.
                    from_text.setAdapter(new ArrayAdapter<String>(
                            parent.getContext(),
                            android.R.layout.simple_spinner_dropdown_item,
                            stNameList
                    ));

                    if (line_number == 1 || line_number == 2) {
                        from_text.setTextColor(Color.rgb(0, 73, 139));
                        to_text.setTextColor(Color.rgb(0, 73, 139));
                    } else if (line_number == 3) {
                        from_text.setTextColor(Color.rgb(0, 146, 70));
                        to_text.setTextColor(Color.rgb(0, 146, 70));
                    } else if (line_number == 4) {
                        from_text.setTextColor(Color.rgb(243, 102, 48));
                        to_text.setTextColor(Color.rgb(243, 102, 48));
                    } else if (line_number == 5) {
                        from_text.setTextColor(Color.rgb(0, 162, 209));
                        to_text.setTextColor(Color.rgb(0, 162, 209));
                    }

                    from_text.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String name_txt = (String) parent.getItemAtPosition(position);
                            for (int i = 0; i < stNameList.size(); i++) {
                                String value = stNameList.get(i);
                                if (value.equals(name_txt)) {
                                    from_stId = stIdList.get(i);
                                    continue;
                                }
                            }
                        }
                    });

                    to_text.setAdapter(new ArrayAdapter<String>(
                            parent.getContext(),
                            android.R.layout.simple_spinner_dropdown_item,
                            stNameList
                    ));

                    to_text.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String name_txt = (String) parent.getItemAtPosition(position);

                            for (int i = 0; i < stNameList.size(); i++) {
                                String value = stNameList.get(i);
                                if (value.equals(name_txt)) {
                                    to_stId = stIdList.get(i);
                                    continue;
                                }
                            }
                        }
                    });
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    /**
     *
     */
    private void startLocationService() {
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        String msg = "Last known Location = > Latitude: "+location.getLatitude()+"  longitute: "+location.getLongitude();
        Log.d("TAG: ", msg);

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                location = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });

        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    /*
     * 설정버튼 클릭시 발생하는 이벤트함수.
     */
    public void onBtnSetClicked(View v){
        if(from_stId.equals("") || to_stId.equals("")){
            Toast.makeText(this, "출발역과 도착역을 입력하세요.", Toast.LENGTH_LONG).show();
            return;
        }

        int tot_spend_time = dbHelper.selectTotalSpendTime(from_stId, to_stId);

        //알람 매니저를 취득
        alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        // 브로드캐스팅을 수행할 PendingIntent를 조회한다.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, tot_spend_time);

        Intent intent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        Toast.makeText(getApplicationContext(), "알람이 설정 되었습니다. 설정된 시간은 "+ tot_spend_time+"분 입니다.", Toast.LENGTH_LONG).show();

    }

    private AlertDialog createDialogBox(String title, String message){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(alarmManager != null) {
                    alarmManager.cancel(pendingIntent);
                    alarmManager = null;
                 }
                finish();
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog =  builder.create();
        return dialog;
    }

    /*
     * 해제 버튼 클릭시 발생하는 이벤트함수.
     */
    public void onBtnCancelClicked(View v){

        if(alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            alarmManager = null;

            from_text.setText("");
            to_text.setText("");

            Toast.makeText(getApplicationContext(), "알람이 해제 되었습니다.", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getApplicationContext(), "설정된 알람이 없습니다.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        back_press_cnt++;
        if(back_press_cnt >= 2){
            String title = "지하철알람";
            String message = "앱을 종료하시겠습니까?";
            AlertDialog dialog = createDialogBox(title, message);
            dialog.show();
        }else{
            Toast.makeText(getApplicationContext(), "한번 더 누르시면 앱이 종료됩니다.", Toast.LENGTH_LONG).show();
            return;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
