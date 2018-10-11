package com.gmail.brianbridge.independentfloatingview;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, FloatingWebBrowserView.OnBackPressDetectedListener {
	public static final int REQUEST_CODE_OVERLAY = 1;

	private ServiceConnection mServiceConnection;
	private FloatingWebBrowserService mFloatingWebBrowserService;
	private static boolean mFloatingWebBrowserCreated = false;
	private boolean floating = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		floating = getIntent().getBooleanExtra("floating", true);
		if (floating) {
			findViewById(R.id.btn_show).setOnClickListener(this);
			findViewById(R.id.btn_hide).setOnClickListener(this);
			findViewById(R.id.btn_kill).setOnClickListener(this);
		} else {
			findViewById(R.id.btn_show).setVisibility(View.GONE);
			findViewById(R.id.btn_hide).setVisibility(View.GONE);
			findViewById(R.id.btn_kill).setVisibility(View.GONE);
		}
		findViewById(R.id.btn_new_activity_with_floating).setOnClickListener(this);
		findViewById(R.id.btn_new_activity_without_floating).setOnClickListener(this);

		mServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
				mFloatingWebBrowserService = ((FloatingWebBrowserService.LocalBinder)iBinder).getInstance();
				mFloatingWebBrowserService.setBackPressDetectedListener(MainActivity.this);
			}

			@Override
			public void onServiceDisconnected(ComponentName componentName) {
				mFloatingWebBrowserService = null;
			}
		};
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btn_show:
				if (mFloatingWebBrowserCreated && mFloatingWebBrowserService != null) {
					mFloatingWebBrowserService.showView();
				} else {
					showFloatingView();
				}
				break;
			case R.id.btn_hide:
				if (mFloatingWebBrowserCreated && mFloatingWebBrowserService != null) {
					mFloatingWebBrowserService.hideView();
				}
				break;
			case R.id.btn_kill:
				if (mFloatingWebBrowserCreated) {
					unbindService(mServiceConnection);
					stopService(new Intent(this, FloatingWebBrowserService.class));
					mFloatingWebBrowserCreated = false;
				}
				break;
			case R.id.btn_new_activity_with_floating: {
				Intent intent = new Intent(this, MainActivity.class);
				startActivity(intent);
				break;
			}
			case R.id.btn_new_activity_without_floating: {
				if (mFloatingWebBrowserService != null) {
					mFloatingWebBrowserService.hideView();
				}
				Intent intent = new Intent(this, MainActivity.class);
				intent.putExtra("floating", false);
				startActivity(intent);
				break;
			}
			default:
				// ignored
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mFloatingWebBrowserCreated && floating) {
			if (mFloatingWebBrowserService != null) {
				mFloatingWebBrowserService.showView();
			}
			Intent intent = new Intent(this, FloatingWebBrowserService.class);
			bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (mFloatingWebBrowserCreated && floating) {
			if (mFloatingWebBrowserService != null) {
				mFloatingWebBrowserService.hideView();
			}
			unbindService(mServiceConnection);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_OVERLAY) {
			showFloatingView();
		}
	}

	public void showFloatingView() {
		int systemAlertWindowMode = 2; // disabled
		AppOpsManager appOpsMgr = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
		if (appOpsMgr != null) {
			systemAlertWindowMode = appOpsMgr.checkOpNoThrow("android:system_alert_window", android.os.Process.myUid(), getPackageName());
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
				&& (!Settings.canDrawOverlays(this)) && systemAlertWindowMode == 2) {
			Toast.makeText(this, "canDrawOverlays is false and system alert window mode is 2(disabled)", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
			startActivityForResult(intent, REQUEST_CODE_OVERLAY);
			return;
		}

		Intent intent = new Intent(this, FloatingWebBrowserService.class);
		startService(intent);
		bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
		mFloatingWebBrowserCreated = true;
	}

	@Override
	public void backPressDetected() {
		onBackPressed();
	}
}
