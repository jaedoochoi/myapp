package jdchoi.nextree.co.kr.railalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
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

import java.util.ArrayList;
import java.util.List;

import jdchoi.nextree.co.kr.railalarm.helper.DatabaseHelper;


public class MainActivity extends ActionBarActivity implements TextWatcher{
    //GUI관련 변수
    private AutoCompleteTextView from_text, to_text;
    private int line_number, from_etime, to_etime;
    DatabaseHelper dbHelper;
    Cursor cursor;
    private String to_stId = "", from_stId = "";

    //알람관련 변수
    AlarmManager alarmManager;
    PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //호선선택 스피너에서 몇호선인지 선택했을 때의 이벤트 처리
        Spinner line_spiner = (Spinner) findViewById(R.id.line_spinner);
        line_spiner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                line_number = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //sqlite에 연결하여 선택한 호선의 역리스트를 가져온다.
        //역이름과 역의 코드번호를 같이 가져와야함.
        dbHelper = new DatabaseHelper(this, line_number);
        cursor = dbHelper.selectStationsInfo();

        final List<String> st_id = new ArrayList<String>();
        final List<String> st_name = new ArrayList<String>();
        final List<Integer> st_etime = new ArrayList<Integer>();
        List<Integer> st_extime = new ArrayList<Integer>();

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

        from_text = (AutoCompleteTextView) findViewById(R.id.edit_from);
        to_text = (AutoCompleteTextView) findViewById(R.id.edit_to);

        //AutoCompleteTextView에 TextWatcher를 셋팅한다.
        from_text.addTextChangedListener(this);
        to_text.addTextChangedListener(this);

        //AutoCompleteTextView에 자동으로 검색되도록 검색할 단어들을 ArrayAdapter로 만들어 인수로 넘긴다.
        from_text.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                st_name
        ));
        from_text.setTextColor(Color.BLUE);

        from_text.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name_txt = (String) parent.getItemAtPosition(position);

                for (int i = 0; i < st_name.size(); i++) {
                    String value = st_name.get(i);
                    if (value.equals(name_txt)) {
                        from_stId = st_id.get(i);
                        continue;
                    }
                }
            }
        });

        to_text.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                st_name
        ));
        to_text.setTextColor(Color.RED);

        to_text.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name_txt = (String) parent.getItemAtPosition(position);

                for (int i = 0; i < st_name.size(); i++) {
                    String value = st_name.get(i);
                    if (value.equals(name_txt)) {
                        to_stId = st_id.get(i);
                        continue;
                    }
                }
            }
        });
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
        if (id == R.id.action_settings) {
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

        //브로드 케스팅을 활성화 한다.
        ComponentName receiver = new ComponentName(this, AlarmReceiver.class);
        PackageManager pm = this.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        // 브로드캐스팅을 수행할 PendingIntent를 조회한다.
        Intent intent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (tot_spend_time * 60 * 1000), pendingIntent);

        Toast.makeText(getApplicationContext(), "알람이 설정 되었습니다. 설정된 시간은 "+ tot_spend_time+"분 입니다.", Toast.LENGTH_LONG).show();

    }

    /*
     * 해제 버튼 클릭시 발생하는 이벤트함수.
     */
    public void onBtnCancelClicked(View v){
        Log.d("MainActivity", "onBtnCancelClicked() 호출됨.");
        if(alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            alarmManager = null;
        }

        //브로드 케스팅을 비활성화 한다.
        ComponentName receiver = new ComponentName(this, AlarmReceiver.class);
        PackageManager pm = this.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        from_text.setText("");
        to_text.setText("");
        Toast.makeText(getApplicationContext(), "알람이 해제 되었습니다.", Toast.LENGTH_LONG).show();
    }

}
