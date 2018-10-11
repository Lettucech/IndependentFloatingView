package com.gmail.brianbridge.independentfloatingview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.gmail.brianbridge.independentfloatingview.R;

/**
 * Created by Brian Ho on 2018/10/11.
 */

public class FloatingWebBrowserView extends FrameLayout {
	public interface OnBackPressDetectedListener {
		void backPressDetected();
	}

	private ImageView mImageView;
	private LinearLayout mWebViewContainer;
	private Button mCloseButton;
	private WebView mWebView;

	private OnBackPressDetectedListener mBackPressDetectedListener;

	public FloatingWebBrowserView(@NonNull Context context) {
		super(context);
		init();
	}

	public FloatingWebBrowserView(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FloatingWebBrowserView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (mBackPressDetectedListener != null && event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
			mBackPressDetectedListener.backPressDetected();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	private void init() {
		View view = LayoutInflater.from(getContext()).inflate(R.layout.view_floating_web_browser, this, true);
		mImageView = view.findViewById(R.id.imageView);
		mWebViewContainer = view.findViewById(R.id.linearLayout_webView_container);
		mCloseButton = view.findViewById(R.id.btn_close);
		mWebView = view.findViewById(R.id.webView);

		mCloseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				mWebViewContainer.setVisibility(GONE);
				mImageView.setVisibility(VISIBLE);
			}
		});

		mWebView.setWebViewClient(new WebViewClient());
		mWebView.loadUrl("https://www.google.com");
	}

	public ImageView getImageView() {
		return mImageView;
	}

	public WebView getWebView() {
		return mWebView;
	}

	public LinearLayout getWebViewContainer() {
		return mWebViewContainer;
	}

	public void setBackPressDetectedListener(OnBackPressDetectedListener listener) {
		this.mBackPressDetectedListener = listener;
	}
}
