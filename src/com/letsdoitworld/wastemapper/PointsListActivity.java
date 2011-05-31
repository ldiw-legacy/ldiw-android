package com.letsdoitworld.wastemapper;

import com.letsdoitworld.wastemapper.R;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class PointsListActivity extends ListActivity {

  private SharedPool sharedPool;
  private Context mContext;
  private ProgressDialog progress;
  private boolean done;
  private PointsArrayAdapter pointArrayAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.list);
    sharedPool = SharedPool.getInstance();
    mContext = this;
    pointArrayAdapter = new PointsArrayAdapter(this, R.layout.list, R.id.point_id, doneListener);
    setListAdapter(pointArrayAdapter);
    ListView listView = getListView();

    listView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String[] ids = (String[]) sharedPool.get(AppConstants.SP_POINTS_IDS);
        Intent intent = new Intent(mContext, ShowPoint2Activity.class);
        intent.putExtra(ShowPointActivity.EXTRAS_POINT_ID, ids[position]);
        mContext.startActivity(intent);
      }
    });
    progress = new ProgressDialog(this);
    progress.setIndeterminate(true);
    String wait = getResources().getString(R.string.message_wait);
    progress.setMessage(wait);
    progress.setCancelable(true);
    progress.setOnCancelListener(new OnCancelListener() {
      @Override
      public void onCancel(DialogInterface arg0) {
        finish();
      }
    });

  }

  private DoneListener doneListener = new DoneListener() {
    @Override
    public void onDone() {
      done = true;
      progress.dismiss();
    }
  };

  @Override
  protected void onResume() {
    super.onResume();
    MainService.startService(this);
    MainService.setDataListener(dataListener);
    if (!done) {
      progress.show();
      pointArrayAdapter.refreshAll();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    MainService.gone();
    pointArrayAdapter.stop();
    if (!done)
      progress.hide();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.layout.list_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.map:
      Intent intent = new Intent(this, Map.class);
      this.startActivity(intent);
      return true;
    case R.id.new_point:
      Intent intent2 = new Intent(this, NewPointActivity.class);
      this.startActivity(intent2);
      return true;
    }
    ;
    return super.onOptionsItemSelected(item);
  }

  private OnDataListener dataListener = new OnDataListener() {
    @Override
    public void onData(int objectCode, Object data) {
      if (MainService.DISTANCES == objectCode) {
        pointArrayAdapter.refreshAll();
      }
    }
  };

}
