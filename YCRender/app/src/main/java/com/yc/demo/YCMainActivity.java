package com.yc.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by lidm on 18/3/12.
 * 主菜单界面
 */
public class YCMainActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setListener();
    }

    private void setListener() {
        findViewById(R.id.ycRecordBtn).setOnClickListener(this);
        findViewById(R.id.ycTestMeshBtn).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ycRecordBtn:
                startActivity(new Intent(this, YCCameraActivity.class));
                finish();
                break;
            case R.id.ycTestMeshBtn:
                startActivity(new Intent(this, YCTestMeshActivity.class));
                finish();
                break;
            default:
                break;
        }
    }
}
