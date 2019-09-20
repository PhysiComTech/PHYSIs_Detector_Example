package com.physicomtech.kit.physis_psmdetector_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.physicomtech.kit.physislibrary.PHYSIsMQTTActivity;

public class MonitoringActivity extends PHYSIsMQTTActivity {

    private final String SERIAL_NUMBER = "XXXXXXXXXXXX";        // PHYSIs Maker Kit 시리얼번호
    public static final String SUB_TOPIC = "Detector";          // Subscribe Topic

    Button btnConnect, btnDisconnect, btnStart, btnStop;        // 액티비티 위젯
    TextView tvHallState, tvPIRState, tvSoundState;
    ProgressBar pgbConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);

        initWidget();                   // 위젯 생성 및 초기화 함수 호출
        setEventListener();             // 이벤트 리스너 설정 함수 호출
    }

    /*
        # 위젯 생성 및 초기화
     */
    private void initWidget() {
        tvHallState = findViewById(R.id.tv_hall_state);             // 텍스트뷰 생성
        tvPIRState = findViewById(R.id.tv_pir_state);
        tvSoundState = findViewById(R.id.tv_sound_state);
        btnConnect = findViewById(R.id.btn_connect);                // 버튼 생성
        btnDisconnect = findViewById(R.id.btn_disconnect);
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        pgbConnect = findViewById(R.id.pgb_connect);                // 프로그래스 생성
    }

    /*
        # 뷰 (버튼) 이벤트 리스너 설정
     */
    private void setEventListener() {
        btnConnect.setOnClickListener(new View.OnClickListener() {                  // 연결 버튼
            @Override
            public void onClick(View v) {           // 버튼 클릭 시 호출
                btnConnect.setEnabled(false);               // 연결 버튼 비활성화 설정
                pgbConnect.setVisibility(View.VISIBLE);     // 연결 프로그래스 가시화 설정
                connectMQTT();                              // MQTT 연결 시도
            }
        });

        btnDisconnect.setOnClickListener(new View.OnClickListener() {               // 연결 종료 버튼
            @Override
            public void onClick(View v) {
                disconnectMQTT();                               // MQTT 연결 종료
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {                    // 모니터링 시작 버튼
            @Override
            public void onClick(View v) {
                startSubscribe(SERIAL_NUMBER, SUB_TOPIC);       // Subscribe 시작
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {                     // 모니터링 종료 버튼
            @Override
            public void onClick(View v) {
                stopSubscribe(SERIAL_NUMBER, SUB_TOPIC);        // Subscribe 중지
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
            }
        });
    }

    /*
        # MQTT 연결 결과 수신
        - MQTT Broker 연결에 따른 결과를 전달받을 때 호출
        - 인자로 연결 성공 여부를 전달
     */
    @Override
    protected void onMQTTConnectedStatus(boolean result) {
        super.onMQTTConnectedStatus(result);
        pgbConnect.setVisibility(View.INVISIBLE);               // 연결 프로그래스 비가시화 설정
        String toastMsg;                                        // 연결 결과에 따른 Toast 메시지 출력
        if(result){
            toastMsg = "MQTT Broker와 연결되었습니다.";
        }else{
            toastMsg = "MQTT Broker와 연결에 실패하였습니다.";
        }
        Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();

        btnConnect.setEnabled(!result);                          // 연결 버튼 활성화 상태 설정
        btnDisconnect.setEnabled(result);
        btnStart.setEnabled(result);                             // 모니터링 제어 버튼 상태 설정
        btnStop.setEnabled(false);
    }

    /*
          # MQTT 연결 종료 처리
    */
    @Override
    protected void onMQTTDisconnected() {
        super.onMQTTDisconnected();
        Toast.makeText(getApplicationContext(), "MQTT Broker와 연결이 종료되었습니다.", Toast.LENGTH_SHORT).show();

        btnConnect.setEnabled(true);
        btnDisconnect.setEnabled(false);
        btnStart.setEnabled(false);
        btnStop.setEnabled(false);
    }

    /*
       # MQTT Subscribe 리스너
       - Subscribe한 Topic에 대한 Publish 메시지가 수신되었을 때 호출
    */
    @Override
    protected void onSubscribeListener(String serialNum, String topic, String data) {
        super.onSubscribeListener(serialNum, topic, data);
        if(serialNum.equals(SERIAL_NUMBER) && topic.equals(SUB_TOPIC)){     // 토픽에 따른 데이터 처리
            showDetectedData(data);               // 감지 상태 출력 함수 호출
        }
    }

    /*
        # 감지 상태 출력
        - 전달받은 감지 센서(PIR센서/사운드센서/홀센서)의 상태에 따른 텍스트 출력
        - 수신 메시지 포멧 : PIR센서값 사운드센서값 홀센서값 ( 예 : 001 )
     */
    private void showDetectedData(String data) {
        if (data == null || data.equals(""))
            return;

        if (data.charAt(0) == '1') {
            tvPIRState.setText("인체 감지");
        } else {
            tvPIRState.setText("이상 없음");
        }
        if (data.charAt(1) == '1') {
            tvSoundState.setText("사운드 감지");
        } else {
            tvSoundState.setText("이상 없음");
        }
        if (data.charAt(2) == '1') {
            tvHallState.setText("이상 없음");
        } else {
            tvHallState.setText("문열림 감지");
        }
    }
}
