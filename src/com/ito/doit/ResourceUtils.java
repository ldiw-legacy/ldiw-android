package com.ito.doit;

import android.app.*;
import android.content.*;
import java.io.*;

public class ResourceUtils {

public static String loadResToString(int resId, Activity ctx) {

  try {
    InputStream is = ctx.getResources().openRawResource(resId);

    byte[] buffer = new byte[4096];
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    while (true) {
      int read = is.read(buffer);

      if (read == -1) {
        break;
      }

      baos.write(buffer, 0, read);
    }

    baos.close();
    is.close();

    String data = baos.toString();


    return data;
  }
  catch (Exception e) {
        return null;
  }

}

public static int getResourceIdForDrawable(Context _context, String resPackage, String resName) {
  return _context.getResources().getIdentifier(
      resName,
      "drawable",
      resPackage
  );
}

}//end class ResourceUtils
