package android.code.test;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

public class AlarmActivity extends Service {
	private View mView;
	private WindowManager mManager;
	private WindowManager.LayoutParams mParams;

	private float mTouchX, mTouchY;
	private int mViewX, mViewY;

	private boolean isMove = false;

	@Override
	public void onCreate() {
		super.onCreate();

		LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = mInflater.inflate(R.layout.layout_alarm, null);
		mView.setOnTouchListener(mViewTouchListener);
		mParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
		mParams.gravity = Gravity.CENTER;

		mManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		mManager.addView(mView, mParams);

	}

	private OnTouchListener mViewTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:// 알람창을 눌렀을때
				isMove = false;

				mTouchX = event.getRawX();
				mTouchY = event.getRawY();
				mViewX = mParams.x;
				mViewY = mParams.y;

				break;

			case MotionEvent.ACTION_UP:// 알람창을 클릭했을때
				if (!isMove) {

					stopService(new Intent(getApplicationContext(), AlarmActivity.class));

					Intent intent = new Intent(getApplicationContext(), MainActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);

					MainActivity.alarm_off();
					MainActivity.vibe.cancel();
				}

				break;

			case MotionEvent.ACTION_MOVE:// 알람창을 움직일때
				isMove = true;

				int x = (int) (event.getRawX() - mTouchX);
				int y = (int) (event.getRawY() - mTouchY);

				final int num = 5;
				if ((x > -num && x < num) && (y > -num && y < num)) {
					isMove = false;
					break;
				}

				mParams.x = mViewX + x;
				mParams.y = mViewY + y;

				mManager.updateViewLayout(mView, mParams);

				break;
			}

			return true;
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mView != null) {
			mManager.removeView(mView);
			mView = null;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
