package com.ito.doit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

public class SmartEditText extends EditText {

  private boolean trackballMove;
  private IMEListener listener;
  private boolean isShown;

  public SmartEditText(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  public SmartEditText(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public SmartEditText(Context context) {
    super(context);
    init();
  }

  public void init() {
    listener = null;
    trackballMove = false;
    isShown = false;
  }

  public void setIMEListener(IMEListener listener) {
    this.listener = listener;
  }

  @Override
  public boolean onKeyPreIme(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && isShown) {
      if (listener != null)
        listener.onChange(false);
      isShown = false;
    }
    return super.onKeyPreIme(keyCode, event);
  }

  @Override
  public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
    if (listener != null && !trackballMove && !isShown) {
      listener.onChange(true);
      isShown = true;
    }
    return super.onCreateInputConnection(outAttrs);
  }

  @Override
  public boolean onTrackballEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_MOVE) {
      trackballMove = true;
    } else if (event.getAction() == MotionEvent.ACTION_DOWN && !isShown) {
      trackballMove = false;
      if (listener != null)
        listener.onChange(true);
      isShown = true;
    }

    return super.onTrackballEvent(event);
  }

}
