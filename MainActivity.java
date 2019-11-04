package android.code.test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MainActivity extends Activity implements View.OnClickListener {

	private InputMethodManager imm;
	private String server = "192.168.0.100";
	private int port = 80;
	private Socket socket;
	private OutputStream outs;
	private Thread rcvThread;
	public logger logger;
	int server_state = 0;

	static String data = "";

	float width, height;
	static RelativeLayout layout;
	static Button set_bt1, set_bt2, set_bt3, main_bt1, menu2_bt1, menu2_bt2, menu2_bt3, menu2_bt4, menu2_bt5, menu2_bt6;
	static TextView set_tv1, main_tv1, main_tv2, main_tv3, main_tv4, main_tv5, main_tv6, menu1_tv1, loading_tv_1;
	static EditText set_et1, set_et2, menu2_et1, menu2_et2, menu2_et3,menu2_et4;

	static Vibrator vibe;
	Handler myHandler;

	int temp = 0;
	int dust = 0;
	int water = 0;
	int gas = 0;

	int window_state = 0;
	int manual_automatic = 0;

	int send_count = 0;
	int button_delay = 0;

	static int alarm_state = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		// 스마트폰 화면 가로 전체 길이
		width = getWindow().getWindowManager().getDefaultDisplay().getWidth();
		// 스마트폰 화면 세로 전체 길이
		height = getWindow().getWindowManager().getDefaultDisplay().getHeight();

		width = width / 100;
		height = height / 100;

		// 레이아웃 선언
		layout = new RelativeLayout(this);
		layout.setBackgroundResource(R.drawable.back0);
		setContentView(layout);

		// 어플에 들어가는 view 코딩으로 제작함

		// 상단 시간출력
		main_tv1 = new TextView(this);
		main_tv1.setId(11);
		RelativeLayout.LayoutParams param11 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		param11.addRule(RelativeLayout.ALIGN_PARENT_TOP, 11);// view 위치는 맨위에
		param11.addRule(RelativeLayout.CENTER_HORIZONTAL, 11);// 그리고 가운데
		param11.height = (int) height * 10;// view 높이
		param11.width = (int) width * 100;// view 너비
		param11.setMargins(0, (int) height * 17, 0, 0);// 상단에 마진
		main_tv1.setGravity(Gravity.CENTER);// 텍스트 위치 중앙
		main_tv1.setText(getNowTime());// 텍스트 내용
		main_tv1.setTextSize(19);// 텍스트 사이즈
		main_tv1.setTextColor(Color.parseColor("#000000"));// 텍스트 색상
		main_tv1.setPaintFlags(main_tv1.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
		main_tv1.setLayoutParams(param11);
		layout.addView(main_tv1);

		// 실내온도 출력
		main_tv2 = new TextView(this);
		main_tv2.setId(12);
		RelativeLayout.LayoutParams param12 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		param12.addRule(RelativeLayout.BELOW, 11);
		param12.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 12);
		param12.height = (int) height * 16;
		param12.width = (int) width * 32;
		param12.setMargins((int) width * 21, (int) height * 2, 0, 0);
		main_tv2.setGravity(Gravity.CENTER);
		main_tv2.setText("");
		main_tv2.setTextSize(28);
		main_tv2.setTextColor(Color.parseColor("#000000"));
		main_tv2.setPaintFlags(main_tv2.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
		main_tv2.setLayoutParams(param12);
		layout.addView(main_tv2);

		// 미세먼지 출력
		main_tv3 = new TextView(this);
		main_tv3.setId(13);
		RelativeLayout.LayoutParams param13 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		param13.addRule(RelativeLayout.BELOW, 11);
		param13.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 13);
		param13.height = (int) height * 16;
		param13.width = (int) width * 32;
		param13.setMargins((int) width * 72, (int) height * 2, 0, 0);
		main_tv3.setGravity(Gravity.CENTER);
		main_tv3.setText("");
		main_tv3.setTextSize(25);
		main_tv3.setTextColor(Color.parseColor("#000000"));
		main_tv3.setPaintFlags(main_tv3.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
		main_tv3.setLayoutParams(param13);
		layout.addView(main_tv3);

		// 빗물 출력
		main_tv4 = new TextView(this);
		main_tv4.setId(14);
		main_tv4.setBackgroundResource(R.drawable.weather_1);
		RelativeLayout.LayoutParams param14 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		param14.addRule(RelativeLayout.BELOW, 13);
		param14.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 14);
		param14.height = (int) height * 16;
		param14.width = (int) width * 32;
		param14.setMargins((int) width * 21, (int) height * 6, 0, 0);
		main_tv4.setGravity(Gravity.CENTER);
		main_tv4.setText("");
		main_tv4.setTextSize(20);
		main_tv4.setTextColor(Color.parseColor("#000000"));
		main_tv4.setLayoutParams(param14);
		layout.addView(main_tv4);

		// 화재 출력
		main_tv5 = new TextView(this);
		main_tv5.setId(15);
		main_tv5.setBackgroundResource(R.drawable.face_1);
		RelativeLayout.LayoutParams param15 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		param15.addRule(RelativeLayout.BELOW, 13);
		param15.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 14);
		param15.height = (int) height * 16;
		param15.width = (int) width * 32;
		param15.setMargins((int) width * 72, (int) height * 6, 0, 0);
		main_tv5.setGravity(Gravity.CENTER);
		main_tv5.setText("");
		main_tv5.setTextSize(20);
		main_tv5.setTextColor(Color.parseColor("#000000"));
		main_tv5.setLayoutParams(param15);
		main_tv5.setOnClickListener(this);
		layout.addView(main_tv5);

		// 현재창문상태 출력
		main_tv6 = new TextView(this);
		main_tv6.setId(16);
		RelativeLayout.LayoutParams param16 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		param16.addRule(RelativeLayout.BELOW, 15);
		param16.addRule(RelativeLayout.CENTER_HORIZONTAL, 16);
		param16.height = (int) height * 14;
		param16.width = (int) width * 70;
		param16.setMargins(0, (int) height * 13, 0, 0);
		main_tv6.setGravity(Gravity.CENTER);
		main_tv6.setText("");
		main_tv6.setTextSize(35);
		main_tv6.setTextColor(Color.parseColor("#000000"));
		main_tv6.setPaintFlags(main_tv6.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
		main_tv6.setLayoutParams(param16);
		layout.addView(main_tv6);

		// 온도 출력 텍스트뷰
		menu1_tv1 = new TextView(this);
		menu1_tv1.setId(17);
		menu1_tv1.setBackgroundResource(R.drawable.button_3);
		RelativeLayout.LayoutParams param17 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		param17.addRule(RelativeLayout.ALIGN_PARENT_TOP, 17);
		param17.addRule(RelativeLayout.CENTER_HORIZONTAL, 17);
		param17.height = (int) height * 72;
		param17.width = (int) width * 92;
		param17.setMargins(0, (int) height * 25, 0, 0);
		menu1_tv1.setGravity(Gravity.START);
		menu1_tv1.setText("안녕하세요\n\nHELLO");
		menu1_tv1.setTextSize(18);
		menu1_tv1.setTextColor(Color.parseColor("#000000"));
		menu1_tv1.setLayoutParams(param17);
		layout.addView(menu1_tv1);

		// 창문 열림 버튼
		menu2_bt1 = new Button(this);
		menu2_bt1.setId(2);
		menu2_bt1.setBackgroundResource(R.drawable.button_6);
		RelativeLayout.LayoutParams param2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		param2.addRule(RelativeLayout.ALIGN_PARENT_TOP, 2);
		param2.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 2);
		param2.height = (int) height * 8;
		param2.width = (int) width * 33;
		param2.setMargins((int) width * 21, (int) height * 24, 0, 0);
		menu2_bt1.setGravity(Gravity.CENTER);
		menu2_bt1.setText("열림");
		menu2_bt1.setTextSize(22);
		menu2_bt1.setTextColor(Color.parseColor("#FFFFFF"));
		menu2_bt1.setLayoutParams(param2);
		menu2_bt1.setOnClickListener(this);
		layout.addView(menu2_bt1);

		// 창문 닫힘 버튼
		menu2_bt2 = new Button(this);
		menu2_bt2.setId(3);
		menu2_bt2.setBackgroundResource(R.drawable.button_5);
		RelativeLayout.LayoutParams param3 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		param3.addRule(RelativeLayout.ALIGN_PARENT_TOP, 3);
		param3.addRule(RelativeLayout.RIGHT_OF, 2);
		param3.height = (int) height * 8;
		param3.width = (int) width * 33;
		param3.setMargins(0, (int) height * 24, 0, 0);
		menu2_bt2.setGravity(Gravity.CENTER);
		menu2_bt2.setText("닫힘");
		menu2_bt2.setTextSize(22);
		menu2_bt2.setTextColor(Color.parseColor("#FFFFFF"));
		menu2_bt2.setLayoutParams(param3);
		menu2_bt2.setOnClickListener(this);
		layout.addView(menu2_bt2);

		// 창문 자동 버튼
		menu2_bt3 = new Button(this);
		menu2_bt3.setId(4);
		menu2_bt3.setBackgroundResource(R.drawable.button_6);
		RelativeLayout.LayoutParams param4 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		param4.addRule(RelativeLayout.BELOW, 3);
		param4.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 4);
		param4.height = (int) height * 8;
		param4.width = (int) width * 33;
		param4.setMargins((int) width * 21, (int) height * 12, 0, 0);
		menu2_bt3.setGravity(Gravity.CENTER);
		menu2_bt3.setText("자동");
		menu2_bt3.setTextSize(22);
		menu2_bt3.setTextColor(Color.parseColor("#FFFFFF"));
		menu2_bt3.setLayoutParams(param4);
		menu2_bt3.setOnClickListener(this);
		layout.addView(menu2_bt3);

		// 창문 수동 버튼
		menu2_bt4 = new Button(this);
		menu2_bt4.setId(5);
		menu2_bt4.setBackgroundResource(R.drawable.button_5);
		RelativeLayout.LayoutParams param5 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		param5.addRule(RelativeLayout.BELOW, 3);
		param5.addRule(RelativeLayout.RIGHT_OF, 4);
		param5.height = (int) height * 8;
		param5.width = (int) width * 33;
		param5.setMargins(0, (int) height * 12, 0, 0);
		menu2_bt4.setGravity(Gravity.CENTER);
		menu2_bt4.setText("수동");
		menu2_bt4.setTextSize(22);
		menu2_bt4.setTextColor(Color.parseColor("#FFFFFF"));
		menu2_bt4.setLayoutParams(param5);
		menu2_bt4.setOnClickListener(this);
		layout.addView(menu2_bt4);

		// 예약열림 [시] 입력
		menu2_et1 = new EditText(this);
		menu2_et1.setId(1001);
		menu2_et1.setBackgroundResource(R.drawable.button_3);
		RelativeLayout.LayoutParams param1001 = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		param1001.addRule(RelativeLayout.BELOW, 5);
		param1001.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1001);
		param1001.height = (int) height * 7;
		param1001.width = (int) width * 22;
		param1001.setMargins((int) width * 5, (int) height * 19, 0, 0);
		menu2_et1.setInputType(InputType.TYPE_CLASS_NUMBER);
		menu2_et1.setGravity(Gravity.CENTER);
		menu2_et1.setHintTextColor(Color.parseColor("#000000"));
		menu2_et1.setTextColor(Color.parseColor("#000000"));
		menu2_et1.setHint("");
		menu2_et1.setTextSize(18);
		menu2_et1.setLayoutParams(param1001);
		layout.addView(menu2_et1);
		InputFilter[] fArray = new InputFilter[1];
		fArray[0] = new InputFilter.LengthFilter(2);
		menu2_et1.setFilters(fArray);

		// 예약열림 [분] 입력
		menu2_et2 = new EditText(this);
		menu2_et2.setId(1002);
		menu2_et2.setBackgroundResource(R.drawable.button_3);
		RelativeLayout.LayoutParams param1002 = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		param1002.addRule(RelativeLayout.BELOW, 5);
		param1002.addRule(RelativeLayout.RIGHT_OF, 1001);
		param1002.height = (int) height * 7;
		param1002.width = (int) width * 22;
		param1002.setMargins((int) width * 8, (int) height * 19, 0, 0);
		menu2_et2.setInputType(InputType.TYPE_CLASS_NUMBER);
		menu2_et2.setGravity(Gravity.CENTER);
		menu2_et2.setHintTextColor(Color.parseColor("#000000"));
		menu2_et2.setTextColor(Color.parseColor("#000000"));
		menu2_et2.setHint("");
		menu2_et2.setTextSize(18);
		menu2_et2.setLayoutParams(param1002);
		layout.addView(menu2_et2);
		menu2_et2.setFilters(fArray);

		// 예약열림설정
		menu2_bt5 = new Button(this);
		menu2_bt5.setId(6);
		menu2_bt5.setBackgroundResource(R.drawable.button_4);
		RelativeLayout.LayoutParams param6 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		param6.addRule(RelativeLayout.BELOW, 5);
		param6.addRule(RelativeLayout.RIGHT_OF, 1002);
		param6.height = (int) height * 7;
		param6.width = (int) width * 22;
		param6.setMargins((int) width * 15, (int) height * 19, 0, 0);
		menu2_bt5.setGravity(Gravity.CENTER);
		menu2_bt5.setText("설정");
		menu2_bt5.setTextSize(22);
		menu2_bt5.setTextColor(Color.parseColor("#FFFFFF"));
		menu2_bt5.setLayoutParams(param6);
		menu2_bt5.setOnClickListener(this);
		layout.addView(menu2_bt5);

		// 예약닫힘 [시] 입력
		menu2_et3 = new EditText(this);
		menu2_et3.setId(1003);
		menu2_et3.setBackgroundResource(R.drawable.button_3);
		RelativeLayout.LayoutParams param1003 = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		param1003.addRule(RelativeLayout.BELOW, 1002);
		param1003.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1003);
		param1003.height = (int) height * 7;
		param1003.width = (int) width * 22;
		param1003.setMargins((int) width * 5, (int) height * 9, 0, 0);
		menu2_et3.setInputType(InputType.TYPE_CLASS_NUMBER);
		menu2_et3.setGravity(Gravity.CENTER);
		menu2_et3.setHintTextColor(Color.parseColor("#000000"));
		menu2_et3.setTextColor(Color.parseColor("#000000"));
		menu2_et3.setHint("");
		menu2_et3.setTextSize(18);
		menu2_et3.setLayoutParams(param1003);
		layout.addView(menu2_et3);
		menu2_et3.setFilters(fArray);

		// 예약닫힘 [분] 입력
		menu2_et4 = new EditText(this);
		menu2_et4.setId(1004);
		menu2_et4.setBackgroundResource(R.drawable.button_3);
		RelativeLayout.LayoutParams param1004 = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		param1004.addRule(RelativeLayout.BELOW, 1002);
		param1004.addRule(RelativeLayout.RIGHT_OF, 1003);
		param1004.height = (int) height * 7;
		param1004.width = (int) width * 22;
		param1004.setMargins((int) width * 8, (int) height * 9, 0, 0);
		menu2_et4.setInputType(InputType.TYPE_CLASS_NUMBER);
		menu2_et4.setGravity(Gravity.CENTER);
		menu2_et4.setHintTextColor(Color.parseColor("#000000"));
		menu2_et4.setTextColor(Color.parseColor("#000000"));
		menu2_et4.setHint("");
		menu2_et4.setTextSize(18);
		menu2_et4.setLayoutParams(param1004);
		layout.addView(menu2_et4);
		menu2_et4.setFilters(fArray);

		// 예약닫힘설정
		menu2_bt6 = new Button(this);
		menu2_bt6.setId(7);
		menu2_bt6.setBackgroundResource(R.drawable.button_4);
		RelativeLayout.LayoutParams param7 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		param7.addRule(RelativeLayout.BELOW, 1002);
		param7.addRule(RelativeLayout.RIGHT_OF, 1004);
		param7.height = (int) height * 7;
		param7.width = (int) width * 22;
		param7.setMargins((int) width * 15, (int) height * 9, 0, 0);
		menu2_bt6.setGravity(Gravity.CENTER);
		menu2_bt6.setText("설정");
		menu2_bt6.setTextSize(22);
		menu2_bt6.setTextColor(Color.parseColor("#FFFFFF"));
		menu2_bt6.setLayoutParams(param7);
		menu2_bt6.setOnClickListener(this);
		layout.addView(menu2_bt6);

		// 메뉴 리스트 버튼
		main_bt1 = new Button(this);
		main_bt1.setId(1);
		main_bt1.setBackgroundResource(R.drawable.list);
		RelativeLayout.LayoutParams param1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		param1.addRule(RelativeLayout.ALIGN_PARENT_TOP, 1);
		param1.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
		param1.setMargins((int) height * 4, (int) height * 4, 0, 0);
		param1.height = (int) height * 10;
		param1.width = (int) width * 16;
		main_bt1.setLayoutParams(param1);
		main_bt1.setOnClickListener(this);
		layout.addView(main_bt1);

		// 서버 접속 상태 텍스트뷰
		set_tv1 = new TextView(this);
		set_tv1.setId(500);
		set_tv1.setBackgroundResource(R.drawable.button_4);
		RelativeLayout.LayoutParams param500 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		param500.addRule(RelativeLayout.ALIGN_PARENT_TOP, 500);
		param500.addRule(RelativeLayout.CENTER_HORIZONTAL, 500);
		param500.height = (int) height * 10;
		param500.width = (int) width * 55;
		param500.setMargins(0, (int) height * 31, 0, 0);
		set_tv1.setGravity(Gravity.CENTER);
		set_tv1.setText("서버 : 미접속");
		set_tv1.setTextSize(25);
		set_tv1.setTextColor(Color.parseColor("#FFFFFF"));
		set_tv1.setLayoutParams(param500);
		layout.addView(set_tv1);
		logger = new logger(set_tv1);

		// 서버 IP 입력창
		set_et1 = new EditText(this);
		set_et1.setId(501);
		set_et1.setBackgroundResource(R.drawable.button_3);
		RelativeLayout.LayoutParams param501 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		param501.addRule(RelativeLayout.BELOW, 500);
		param501.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 501);
		param501.height = (int) height * 11;
		param501.width = (int) width * 60;
		param501.setMargins((int) width * 10, (int) height * 7, 0, 0);
		set_et1.setInputType(InputType.TYPE_CLASS_TEXT);
		set_et1.setGravity(Gravity.CENTER);
		set_et1.setText(server);
		set_et1.setHintTextColor(Color.parseColor("#000000"));
		set_et1.setTextColor(Color.parseColor("#000000"));
		set_et1.setTextSize(26);
		set_et1.setLayoutParams(param501);
		layout.addView(set_et1);
		InputFilter[] fArray1 = new InputFilter[1];
		fArray1[0] = new InputFilter.LengthFilter(16);
		set_et1.setFilters(fArray1);

		// 서버 포트 입력창
		set_et2 = new EditText(this);
		set_et2.setId(502);
		set_et2.setBackgroundResource(R.drawable.button_3);
		RelativeLayout.LayoutParams param502 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		param502.addRule(RelativeLayout.BELOW, 501);
		param502.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 502);
		param502.height = (int) height * 11;
		param502.width = (int) width * 60;
		param502.setMargins((int) width * 10, (int) height * 4, 0, 0);
		set_et2.setInputType(InputType.TYPE_CLASS_NUMBER);
		set_et2.setGravity(Gravity.CENTER);
		set_et2.setText(port + "");
		set_et2.setHintTextColor(Color.parseColor("#000000"));
		set_et2.setTextColor(Color.parseColor("#000000"));
		set_et2.setTextSize(30);
		set_et2.setLayoutParams(param502);
		layout.addView(set_et2);
		InputFilter[] fArray2 = new InputFilter[1];
		fArray2[0] = new InputFilter.LengthFilter(4);
		set_et2.setFilters(fArray2);

		// 서버 연결 버튼
		set_bt1 = new Button(this);
		set_bt1.setId(503);
		set_bt1.setBackgroundResource(R.drawable.button_4);
		RelativeLayout.LayoutParams param503 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		param503.addRule(RelativeLayout.BELOW, 500);
		param503.addRule(RelativeLayout.RIGHT_OF, 501);
		param503.height = (int) height * 11;
		param503.width = (int) width * 25;
		param503.setMargins((int) width * 5, (int) height * 7, 0, 0);
		set_bt1.setGravity(Gravity.CENTER);
		set_bt1.setText("연결");
		set_bt1.setTextSize(22);
		set_bt1.setTextColor(Color.parseColor("#FFFFFF"));
		set_bt1.setLayoutParams(param503);
		set_bt1.setOnClickListener(this);
		layout.addView(set_bt1);

		// 서버 연결 취소 버튼
		set_bt2 = new Button(this);
		set_bt2.setId(504);
		set_bt2.setBackgroundResource(R.drawable.button_4);
		RelativeLayout.LayoutParams param504 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		param504.addRule(RelativeLayout.BELOW, 503);
		param504.addRule(RelativeLayout.RIGHT_OF, 502);
		param504.height = (int) height * 11;
		param504.width = (int) width * 25;
		param504.setMargins((int) width * 5, (int) height * 4, 0, 0);
		set_bt2.setGravity(Gravity.CENTER);
		set_bt2.setText("취소");
		set_bt2.setTextSize(22);
		set_bt2.setTextColor(Color.parseColor("#FFFFFF"));
		set_bt2.setLayoutParams(param504);
		set_bt2.setOnClickListener(this);
		layout.addView(set_bt2);

		// 서버 설정 완료 버튼
		set_bt3 = new Button(this);
		set_bt3.setId(505);
		set_bt3.setBackgroundResource(R.drawable.button_4);
		RelativeLayout.LayoutParams param505 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		param505.addRule(RelativeLayout.BELOW, 504);
		param505.addRule(RelativeLayout.CENTER_HORIZONTAL, 505);
		param505.height = (int) height * 10;
		param505.width = (int) width * 55;
		param505.setMargins(0, (int) height * 7, 0, 0);
		set_bt3.setGravity(Gravity.CENTER);
		set_bt3.setText("설정 완료");
		set_bt3.setTextSize(25);
		set_bt3.setTextColor(Color.parseColor("#FFFFFF"));
		set_bt3.setLayoutParams(param505);
		set_bt3.setOnClickListener(this);
		layout.addView(set_bt3);

		// 로딩 화면
		loading_tv_1 = new TextView(this);
		loading_tv_1.setId(18);
		loading_tv_1.setBackgroundResource(R.drawable.loading);
		RelativeLayout.LayoutParams param18 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		param18.addRule(RelativeLayout.CENTER_VERTICAL, 18);
		param18.addRule(RelativeLayout.CENTER_HORIZONTAL, 18);
		param18.height = (int) height * 100;
		param18.width = (int) width * 105;
		loading_tv_1.setLayoutParams(param18);
		layout.addView(loading_tv_1);

		Handler hd = new Handler();
		hd.postDelayed(new Runnable() {
			@Override
			public void run() {
				loading_tv_1.setVisibility(View.GONE);
			}
		}, 2000);

		// 메인 화면 제외한 view들은 가린채로 시작
		menu1_tv1.setVisibility(View.GONE);

		menu2_bt1.setVisibility(View.GONE);
		menu2_bt2.setVisibility(View.GONE);
		menu2_bt3.setVisibility(View.GONE);
		menu2_bt4.setVisibility(View.GONE);
		menu2_bt5.setVisibility(View.GONE);
		menu2_bt6.setVisibility(View.GONE);
		menu2_et1.setVisibility(View.GONE);
		menu2_et2.setVisibility(View.GONE);
		menu2_et3.setVisibility(View.GONE);
		menu2_et4.setVisibility(View.GONE);

		set_bt1.setVisibility(View.GONE);
		set_bt2.setVisibility(View.GONE);
		set_tv1.setVisibility(View.GONE);
		set_bt3.setVisibility(View.GONE);
		set_et1.setVisibility(View.GONE);
		set_et2.setVisibility(View.GONE);

		myHandler = new Handler();
		TimeThread timerThread = new TimeThread();
		timerThread.start();// 쓰레드 실행

	}

	// 현재시간 출력 함수
	public String getNowTime() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy" + "년 " + "MM" + "월 " + "dd" + "일 " + "E" + "요일     " + "HH" + ":" + "mm" + ":" + "ss" + "",
				new Locale("ko", "kr"));
		String time = sdf.format(calendar.getTime());
		return time;
	}

	// TODO
	class TimeThread extends Thread {
		@Override
		public void run() {
			super.run();
			while (true) {
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				myHandler.post(new Runnable() {// 1초마다 실행되는 쓰레드
					@Override
					public void run() {

						main_tv1.setText(getNowTime());// main_tv1에 현재시간 출력

						try {

							if (server_state == 1) {// 만약 서버에 연결되어 있다면

								if (send_count > 2) {// 만약 send_count값이 2보다 크면
									send_count = 0;// 0으로 초기화하고

									send_data("getdata");// getdata 데이터 전송
									// 아두이노는 getdata 수신하면 센서정보등의 데이터를 회신함
								}
								send_count++;// send_count값을 1씩 증가
							}

						} catch (Exception e) {
							Toast.makeText(MainActivity.this, "error1 : " + e, Toast.LENGTH_SHORT).show();
						}

						// data 변수에 rcvthread 클래스에 있는
						// get_data 함수를 실행해서 수신받은 데이터를 가져온다
						data = rcvthread.get_data() + "";

						// main_tv1.setText(data + " " + data.length());

						// 수신받은 데이터의 첫번째 데이터가 @이고 마지막 데이터가 #이라면
						// (수신받은 데이터가 정상이라면)
						if (data.charAt(0) == '@' && data.charAt(data.length() - 1) == '#') {

							try {

								String[] data_arry;
								data_arry = data.split("\\/");
								// data_arry 배열에 수신받은 데이터를 /부호 기준으로 파싱하여 담는다

								// 파싱된 데이터를 각각 변수에 int형으로 변환하여 저장한다
								temp = Integer.parseInt(data_arry[1]);
								dust = Integer.parseInt(data_arry[2]);
								water = Integer.parseInt(data_arry[3]);
								gas = Integer.parseInt(data_arry[4]);
								window_state = Integer.parseInt(data_arry[5]);
								manual_automatic = Integer.parseInt(data_arry[6]);

								// 온도데이터 출력
								main_tv2.setText(temp + " ℃");
								// 먼지데이터 출력
								main_tv3.setText(dust + "\n㎍/m³");
								// main_tv4.setText(water + "");
								// main_tv5.setText(gas + "");

								// 먼지값이 300이거나 300보다 크면
								if (dust >= 300) {
									// 글자색을 빨간색으로
									main_tv3.setTextColor(Color.parseColor("#FF0000"));
								} else {
									// 아니라면 글자색을 검정색으로
									main_tv3.setTextColor(Color.parseColor("#000000"));
								}

								// 수분데이터값이 500이거나 500보다 크면
								if (water >= 500) {
									// 해당뷰에 이미지를 weather_2로 한다
									main_tv4.setBackgroundResource(R.drawable.weather_2);
								} else {
									// 아니라면 해당뷰에 이미지를 weather_1로 한다
									main_tv4.setBackgroundResource(R.drawable.weather_1);
								}

								// 가스데이터값이 300이거나 300보다 크면
								if (gas >= 300) {
									// 해당뷰에 이미지를 face_2로 한다
									main_tv5.setBackgroundResource(R.drawable.face_2);

									// 만약 alarm_state 변수값이 0이면 (알람 중복 방지를 위해서)
									if (alarm_state == 0) {
										// alarm_state 변수값 1 변경
										alarm_state = 1;

										// 해당 패턴으로 진동을 무한으로 울림
										long[] pattern = { 300, 400, 300, 400 };
										vibe.vibrate(pattern, 0);

										// 알람창을 띄운다.
										startService(new Intent(getApplicationContext(), AlarmActivity.class));
									}

								} else {
									// 아니라면 해당뷰에 이미지를 face_1로 한다
									main_tv5.setBackgroundResource(R.drawable.face_1);
								}

								// 만약 window_state 값이 1이라면 (수신받은 창문상태값이 열림이면)
								if (window_state == 1) {
									main_tv6.setText("OPEN");
									main_tv6.setTextColor(Color.parseColor("#0000FF"));
									menu2_bt1.setBackgroundResource(R.drawable.button_5);
									menu2_bt2.setBackgroundResource(R.drawable.button_6);
								} else {// 아니라면
									main_tv6.setText("CLOSE");
									main_tv6.setTextColor(Color.parseColor("#FF0000"));
									menu2_bt1.setBackgroundResource(R.drawable.button_6);
									menu2_bt2.setBackgroundResource(R.drawable.button_5);
								}

								// 만약 manual_automatic 값이 1이라면 (수신받은 창문모드값이자동이면)
								if (manual_automatic == 1) {
									menu2_bt3.setBackgroundResource(R.drawable.button_5);
									menu2_bt4.setBackgroundResource(R.drawable.button_6);
								} else {// 아니라면
									menu2_bt3.setBackgroundResource(R.drawable.button_6);
									menu2_bt4.setBackgroundResource(R.drawable.button_5);
								}

							} catch (Exception e) {
								Toast.makeText(MainActivity.this, "error2 : " + e, Toast.LENGTH_SHORT).show();
							}
						}

					}
				});
			}
		}

	}

	// 데이터를 보내는 함수
	public void send_data(final String s) {

		if (server_state == 1) {// 만약 서버에 연결되어 있다면
			try {

				if (s.equals("getdata")) {// 만약 전송할 데이터가 getdata라면
					outs.write(s.getBytes("UTF-8"));// 입력한 데이터 전송
				} else {// 만약 전송할 데이터가 getdata가 아니면

					send_count = 0;// send_count 값 초기화

					outs.write(s.getBytes("UTF-8"));// 입력한 데이터 전송
				}

				outs.flush();
			} catch (IOException e) {
				Toast.makeText(this, "전송에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		} else {
			Toast.makeText(this, "서버가 연결되어있지 않습니다.", Toast.LENGTH_SHORT).show();
		}

	}

	// onPause,onStart등... 액티비티 생명주기 참조
	protected void onPause() {
		super.onPause();
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onStart() {
		super.onStart();

	}

	@Override
	public synchronized void onResume() {
		super.onResume();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		finish();
	}

	// 종료 버튼을 눌렀을때
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:

			TextView tv = new TextView(MainActivity.this);
			tv.setGravity(Gravity.CENTER);
			tv.setText("\n[페어피플]앱을 종료 하시겠습니까?\n");
			tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
			tv.setTextColor(Color.parseColor("#FFFFFF"));

			new AlertDialog.Builder(this).setCustomTitle(tv).setCancelable(false)
					.setPositiveButton("예", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							android.os.Process.killProcess(android.os.Process.myPid());
						}
					}).setNegativeButton("아니오", null).show();

			break;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onClick(View v) {

		vibe.vibrate(100);

		if (v == set_bt1) {// 서버 연결 버튼
			if (server_state == 0) {// 서버가 연결되어있지 않다면
				imm.hideSoftInputFromWindow(set_et1.getWindowToken(), 0);
				try {
					if (socket != null) {// 만약 서버가 연결되어 있다면
						socket.close();
						socket = null;// 소켓을 초기화 시킨다
					}
					// 입력받은 IP를 server라는 변수에 저장한다
					server = set_et1.getText().toString();
					// 입력받은 포트번호를 port 변수에 저장한다
					port = Integer.parseInt(set_et2.getText().toString());
					// 입력받은 IP와 PORT로 소켓을 생성한다
					socket = new Socket(server, port);
					outs = socket.getOutputStream();
					rcvThread = new Thread(new rcvthread(logger, socket));
					rcvThread.start();
					logger.log("서버 : 접속 완료");
					server_state = 1;// 접속에 성공하면 해당 변수를 1으로 한다

					// 접속에 성공하면 현재스마트폰 시간을 아두이노로 보낸다 (아두이노에 현재시간을 set하기 위해)
					String nowtime = getNowTime().substring(22, 24) + getNowTime().substring(25, 27)
							+ getNowTime().substring(28, 30) + "$";
					send_data(nowtime);

				} catch (IOException e) {
					logger.log("Fail to connect");
					e.printStackTrace();
					server_state = 0;// 접속에 실패하면 해당 변수를 0으로 한다
				}
			} else {// 만약 서버에 연결되어 있다면
				Toast.makeText(this, "서버가 연결되어 있습니다.", Toast.LENGTH_SHORT).show();
			}
		}

		if (v == set_bt2) {// 서버 연결 취소 버튼
			imm.hideSoftInputFromWindow(set_et1.getWindowToken(), 0);
			if (socket != null) {
				server_state = 0;
				try {
					socket.close();
					socket = null;
					logger.log("서버 : 미접속");
					rcvThread = null;
					server_state = 0;
				} catch (IOException e) {
					logger.log("Fail to close");
					e.printStackTrace();
					server_state = 0;
				}
			}
		}

		if (v == set_bt3) {// 서버 설정 완료 버튼

			layout.setBackgroundResource(R.drawable.back0);

			set_bt1.setVisibility(View.GONE);
			set_bt2.setVisibility(View.GONE);
			set_tv1.setVisibility(View.GONE);
			set_bt3.setVisibility(View.GONE);
			set_et1.setVisibility(View.GONE);
			set_et2.setVisibility(View.GONE);

			main_tv1.setVisibility(View.VISIBLE);
			main_tv2.setVisibility(View.VISIBLE);
			main_tv3.setVisibility(View.VISIBLE);
			main_tv4.setVisibility(View.VISIBLE);
			main_tv5.setVisibility(View.VISIBLE);
			main_tv6.setVisibility(View.VISIBLE);

			menu1_tv1.setVisibility(View.GONE);

			menu2_bt1.setVisibility(View.GONE);
			menu2_bt2.setVisibility(View.GONE);
			menu2_bt3.setVisibility(View.GONE);
			menu2_bt4.setVisibility(View.GONE);
			menu2_bt5.setVisibility(View.GONE);
			menu2_bt6.setVisibility(View.GONE);
			menu2_et1.setVisibility(View.GONE);
			menu2_et2.setVisibility(View.GONE);
			menu2_et3.setVisibility(View.GONE);
			menu2_et4.setVisibility(View.GONE);

			main_bt1.setVisibility(View.VISIBLE);
		}

		if (v == main_bt1) {// 메뉴 리스트 버튼

			PopupMenu popup = new PopupMenu(this, main_bt1);
			popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());
			// 메뉴 클릭시 이벤트
			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					switch (item.getItemId()) {

					case R.id.menu_0:// 메인 화면

						layout.setBackgroundResource(R.drawable.back0);

						set_bt1.setVisibility(View.GONE);
						set_bt2.setVisibility(View.GONE);
						set_tv1.setVisibility(View.GONE);
						set_bt3.setVisibility(View.GONE);
						set_et1.setVisibility(View.GONE);
						set_et2.setVisibility(View.GONE);

						main_tv1.setVisibility(View.VISIBLE);
						main_tv2.setVisibility(View.VISIBLE);
						main_tv3.setVisibility(View.VISIBLE);
						main_tv4.setVisibility(View.VISIBLE);
						main_tv5.setVisibility(View.VISIBLE);
						main_tv6.setVisibility(View.VISIBLE);

						menu1_tv1.setVisibility(View.GONE);

						menu2_bt1.setVisibility(View.GONE);
						menu2_bt2.setVisibility(View.GONE);
						menu2_bt3.setVisibility(View.GONE);
						menu2_bt4.setVisibility(View.GONE);
						menu2_bt5.setVisibility(View.GONE);
						menu2_bt6.setVisibility(View.GONE);
						menu2_et1.setVisibility(View.GONE);
						menu2_et2.setVisibility(View.GONE);
						menu2_et3.setVisibility(View.GONE);
						menu2_et4.setVisibility(View.GONE);

						main_bt1.setVisibility(View.VISIBLE);
						break;

					case R.id.menu_1:// 어플소개 및 사용법

						layout.setBackgroundResource(R.drawable.back1);

						set_bt1.setVisibility(View.GONE);
						set_bt2.setVisibility(View.GONE);
						set_tv1.setVisibility(View.GONE);
						set_bt3.setVisibility(View.GONE);
						set_et1.setVisibility(View.GONE);
						set_et2.setVisibility(View.GONE);

						main_tv1.setVisibility(View.GONE);
						main_tv2.setVisibility(View.GONE);
						main_tv3.setVisibility(View.GONE);
						main_tv4.setVisibility(View.GONE);
						main_tv5.setVisibility(View.GONE);
						main_tv6.setVisibility(View.GONE);

						menu1_tv1.setVisibility(View.VISIBLE);

						menu2_bt1.setVisibility(View.GONE);
						menu2_bt2.setVisibility(View.GONE);
						menu2_bt3.setVisibility(View.GONE);
						menu2_bt4.setVisibility(View.GONE);
						menu2_bt5.setVisibility(View.GONE);
						menu2_bt6.setVisibility(View.GONE);
						menu2_et1.setVisibility(View.GONE);
						menu2_et2.setVisibility(View.GONE);
						menu2_et3.setVisibility(View.GONE);
						menu2_et4.setVisibility(View.GONE);

						main_bt1.setVisibility(View.VISIBLE);
						break;

					case R.id.menu_2:// 창문 제어

						layout.setBackgroundResource(R.drawable.back2);

						set_bt1.setVisibility(View.GONE);
						set_bt2.setVisibility(View.GONE);
						set_tv1.setVisibility(View.GONE);
						set_bt3.setVisibility(View.GONE);
						set_et1.setVisibility(View.GONE);
						set_et2.setVisibility(View.GONE);

						main_tv1.setVisibility(View.GONE);
						main_tv2.setVisibility(View.GONE);
						main_tv3.setVisibility(View.GONE);
						main_tv4.setVisibility(View.GONE);
						main_tv5.setVisibility(View.GONE);
						main_tv6.setVisibility(View.GONE);

						menu1_tv1.setVisibility(View.GONE);

						menu2_bt1.setVisibility(View.VISIBLE);
						menu2_bt2.setVisibility(View.VISIBLE);
						menu2_bt3.setVisibility(View.VISIBLE);
						menu2_bt4.setVisibility(View.VISIBLE);
						menu2_bt5.setVisibility(View.VISIBLE);
						menu2_bt6.setVisibility(View.VISIBLE);
						menu2_et1.setVisibility(View.VISIBLE);
						menu2_et2.setVisibility(View.VISIBLE);
						menu2_et3.setVisibility(View.VISIBLE);
						menu2_et4.setVisibility(View.VISIBLE);

						main_bt1.setVisibility(View.VISIBLE);
						break;

					case R.id.menu_3:// 서버 접속

						layout.setBackgroundResource(R.drawable.back3);

						set_bt1.setVisibility(View.VISIBLE);
						set_bt2.setVisibility(View.VISIBLE);
						set_tv1.setVisibility(View.VISIBLE);
						set_bt3.setVisibility(View.VISIBLE);
						set_et1.setVisibility(View.VISIBLE);
						set_et2.setVisibility(View.VISIBLE);

						main_tv1.setVisibility(View.GONE);
						main_tv2.setVisibility(View.GONE);
						main_tv3.setVisibility(View.GONE);
						main_tv4.setVisibility(View.GONE);
						main_tv5.setVisibility(View.GONE);
						main_tv6.setVisibility(View.GONE);

						menu1_tv1.setVisibility(View.GONE);

						menu2_bt1.setVisibility(View.GONE);
						menu2_bt2.setVisibility(View.GONE);
						menu2_bt3.setVisibility(View.GONE);
						menu2_bt4.setVisibility(View.GONE);
						menu2_bt5.setVisibility(View.GONE);
						menu2_bt6.setVisibility(View.GONE);
						menu2_et1.setVisibility(View.GONE);
						menu2_et2.setVisibility(View.GONE);
						menu2_et3.setVisibility(View.GONE);
						menu2_et4.setVisibility(View.GONE);

						main_bt1.setVisibility(View.GONE);
						break;

					}
					return true;
				}
			});
			popup.show();

		}

		// 입력한 버튼이 main_bt2 ~ main_bt7 라면
		if (v == menu2_bt1 || v == menu2_bt2 || v == menu2_bt3 || v == menu2_bt4 || v == menu2_bt5 || v == menu2_bt6)

		{

			// TODO
			if (server_state == 1) {// 만약 서버에 접속이 되어있다면

				rcvthread.delete_data();

				if (button_delay == 0) {// 만약 button_delay값이 0이라면
					button_delay = 1;
					// button_delay값을 1로 변경 (빠른속도로 여러번 클릭되는걸 막기 위해서)

					if (v == menu2_bt1) {// 창문 열림 버튼

						// set_button_delay 함수실행 (3초동안 다른버튼 안눌림)
						set_button_delay(3);
						menu2_bt1.setBackgroundResource(R.drawable.button_5);
						menu2_bt2.setBackgroundResource(R.drawable.button_6);
						// window1 데이터 전송
						// 아두이노는 window1 수신하면 창문ON
						send_data("window1");

						menu2_bt3.setBackgroundResource(R.drawable.button_6);
						menu2_bt4.setBackgroundResource(R.drawable.button_5);

						// 수동으로 창문제어 버튼을 눌렀으니 혹시 예약시간설정이 되어있다면 지워주는 함수
						delete_set_time();
					}
					if (v == menu2_bt2) {// 창문 닫힘 버튼

						set_button_delay(3);
						menu2_bt1.setBackgroundResource(R.drawable.button_6);
						menu2_bt2.setBackgroundResource(R.drawable.button_5);
						send_data("window0");

						menu2_bt3.setBackgroundResource(R.drawable.button_6);
						menu2_bt4.setBackgroundResource(R.drawable.button_5);

						delete_set_time();
					}
					if (v == menu2_bt3) {// 창문 자동 버튼

						set_button_delay(1);
						menu2_bt3.setBackgroundResource(R.drawable.button_5);
						menu2_bt4.setBackgroundResource(R.drawable.button_6);
						send_data("mode__1");
						manual_automatic = 1;
					}
					if (v == menu2_bt4) {// 창문 수동 버튼

						set_button_delay(1);
						menu2_bt3.setBackgroundResource(R.drawable.button_6);
						menu2_bt4.setBackgroundResource(R.drawable.button_5);
						send_data("mode__0");
						manual_automatic = 0;
					}
					if (v == menu2_bt5) {// 예약열림설정

						set_button_delay(1);

						if (manual_automatic == 0) {// 창문모드가 수동이라면
							if (menu2_et1.getText().toString().length() == 2
									&& menu2_et2.getText().toString().length() == 2) {

								// 설정 시간값을 전송
								String timest1 = menu2_et1.getText().toString() + menu2_et2.getText().toString()
										+ "00@";
								send_data(timest1);
								Toast.makeText(this, "예약열림설정 설정", Toast.LENGTH_SHORT).show();

							} else {// 입력 누락이 있다면
								Toast.makeText(this, "예약열림설정 시,분 설정값을 입력해주세요\n[주의사항: 분 입력에 5를 넣는다면 05를 입력]",
										Toast.LENGTH_SHORT).show();
							}
						} else {// 창문모드가 자동이라면
							Toast.makeText(this, "창문모드가 수동일때 사용가능합니다", Toast.LENGTH_SHORT).show();
						}

					}
					if (v == menu2_bt6) {// 예약닫힘설정

						set_button_delay(1);

						if (manual_automatic == 0) {
							if (menu2_et3.getText().toString().length() == 2
									&& menu2_et4.getText().toString().length() == 2) {

								String timest2 = menu2_et3.getText().toString() + menu2_et4.getText().toString()
										+ "00#";
								send_data(timest2);
								Toast.makeText(this, "예약닫힘설정 설정", Toast.LENGTH_SHORT).show();

							} else {// 입력 누락이 있다면
								Toast.makeText(this, "예약닫힘설정 시,분 설정값을 입력해주세요\n[주의사항: 분 입력에 5를 넣는다면 05를 입력]",
										Toast.LENGTH_SHORT).show();
							}
						} else {
							Toast.makeText(this, "창문모드가 수동일때 사용가능합니다", Toast.LENGTH_SHORT).show();
						}

					}

				} else {
					Toast.makeText(this, "잠시 후에 눌러 주세요.", Toast.LENGTH_SHORT).show();
				}

			} else {
				Toast.makeText(this, "서버가 연결되어있지 않습니다.", Toast.LENGTH_SHORT).show();
			}

		}

	}

	void set_button_delay(int a) {
		Handler hd = new Handler();
		hd.postDelayed(new Runnable() {
			@Override
			public void run() {
				button_delay = 0;// a 초뒤 해당 변수 초기화
				// 즉, 버튼을 누르면 a 초뒤 버튼입력 활성화
			}
		}, a * 1000);
	}

	// 예약시간을 입력부분을 초기화
	void delete_set_time() {
		menu2_et1.setText("");
		menu2_et2.setText("");
		menu2_et3.setText("");
		menu2_et4.setText("");
	}

	// 알람을 한번끄면 5초뒤에 다시 알람을 울릴수있는 상태로 변경
	static void alarm_off() {

		Handler hd = new Handler();
		hd.postDelayed(new Runnable() {
			@Override
			public void run() {
				alarm_state = 0;
			}
		}, 5000);
	}

}
