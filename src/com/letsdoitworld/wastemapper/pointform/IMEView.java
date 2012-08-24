package com.letsdoitworld.wastemapper.pointform;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class IMEView extends View {

  private int height = -1;
  private IMEListener listener = null;

  public IMEView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public IMEView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setIMEListener(IMEListener listener) {
    this.listener = listener;
  }

  public IMEView(Context context) {
    super(context);
  }

  @Override
  public void draw(Canvas canvas) {
    super.draw(canvas);
    int h = getHeight();
    if (h != height) {
      if (h > height) {
        if (height != -1)
          if (listener != null)
            listener.onChange(false);
      } else if (h + 100 < height) {
        if (listener != null)
          listener.onChange(true);
      }
      height = h;
    }
  }

}
