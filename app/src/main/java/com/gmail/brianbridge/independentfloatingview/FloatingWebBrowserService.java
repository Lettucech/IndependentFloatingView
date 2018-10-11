package com.gmail.brianbridge.independentfloatingview;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * Created by Brian Ho on 2018/10/10.
 */

public class FloatingWebBrowserService extends Service {
	private WindowManager mWindowManager;
	private FloatingWebBrowserView mFloatingWebBrowserView;
	private final IBinder mBinder = new LocalBinder();

	public FloatingWebBrowserService() {
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		if (mFloatingWebBrowserView != null) {
			mFloatingWebBrowserView.setBackPressDetectedListener(null);
		}
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		if (mWindowManager != null) {
			mFloatingWebBrowserView = new FloatingWebBrowserView(this);
			final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT,
					WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
					WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
					PixelFormat.TRANSLUCENT);

			DisplayMetrics dm = new DisplayMetrics();
			mWindowManager.getDefaultDisplay().getMetrics(dm);

			params.gravity = Gravity.TOP | Gravity.LEFT;
			params.x = 0;
			params.y = 100;

			mFloatingWebBrowserView.getImageView().setOnTouchListener(new View.OnTouchListener() {
				private int initialX;
				private int initialY;
				private float initialTouchX;
				private float initialTouchY;

				@Override
				public boolean onTouch(View view, MotionEvent motionEvent) {
					switch (motionEvent.getAction()) {
						case MotionEvent.ACTION_DOWN:

							//remember the initial position.
							initialX = params.x;
							initialY = params.y;

							//get the touch location
							initialTouchX = motionEvent.getRawX();
							initialTouchY = motionEvent.getRawY();
							return true;
						case MotionEvent.ACTION_UP:
							int Xdiff = (int) Math.abs(motionEvent.getRawX() - initialTouchX);
							int Ydiff = (int) Math.abs(motionEvent.getRawY() - initialTouchY);


							//The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
							//So that is click event.
							if (Xdiff < 10 && Ydiff < 10) {
								if (mFloatingWebBrowserView.getWebViewContainer().getVisibility() == View.GONE) {
									//When user clicks on the image view of the collapsed layout,
									//visibility of the collapsed layout will be changed to "View.GONE"
									//and expanded view will become visible.
									DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
									FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFloatingWebBrowserView.getWebViewContainer().getLayoutParams();
									lp.width = displayMetrics.widthPixels;
									lp.height = displayMetrics.heightPixels;
									mFloatingWebBrowserView.getWebViewContainer().setLayoutParams(lp);

									mFloatingWebBrowserView.getWebViewContainer().setVisibility(View.VISIBLE);
									mFloatingWebBrowserView.getImageView().setVisibility(View.GONE);
								}
							}
							return true;
						case MotionEvent.ACTION_MOVE:
							//Calculate the X and Y coordinates of the view.
							params.x = initialX + (int) (motionEvent.getRawX() - initialTouchX);
							params.y = initialY + (int) (motionEvent.getRawY() - initialTouchY);


							//Update the layout with new X & Y coordinate
							mWindowManager.updateViewLayout(mFloatingWebBrowserView, params);
							return true;
					}
					return false;
				}
			});

			mWindowManager.addView(mFloatingWebBrowserView, params);
			Toast.makeText(this, "Floating view service started, view added to WindowManager", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "Floating view service started, but view failed to add into WindowManager", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mFloatingWebBrowserView != null) {
			mWindowManager.removeView(mFloatingWebBrowserView);
			Toast.makeText(this, "Floating view service stopped, view removed from WindowManager", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "Floating view service stopped", Toast.LENGTH_SHORT).show();
		}
	}

	public void hideView() {
		if (mFloatingWebBrowserView != null) {
			mFloatingWebBrowserView.setVisibility(View.GONE);
		}
	}

	public void showView() {
		if (mFloatingWebBrowserView != null) {
			mFloatingWebBrowserView.setVisibility(View.VISIBLE);
		}
	}

	public void setBackPressDetectedListener(FloatingWebBrowserView.OnBackPressDetectedListener listener) {
		if (mFloatingWebBrowserView != null) {
			mFloatingWebBrowserView.setBackPressDetectedListener(listener);
		}
	}

	public class LocalBinder extends Binder {
		FloatingWebBrowserService getInstance() {
			return FloatingWebBrowserService.this;
		}
	}
}
