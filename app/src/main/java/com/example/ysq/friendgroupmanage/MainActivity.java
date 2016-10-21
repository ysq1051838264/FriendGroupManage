package com.example.ysq.friendgroupmanage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button jumpBtn = (Button) findViewById(R.id.jumpBtn);
        jumpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initData();
            }
        });
    }

    private void initData() {
        ArrayList<ManageGroupModel> models = new ArrayList<ManageGroupModel>();
        for (int i = 0; i < 5; i++) {
            ManageGroupModel model = new ManageGroupModel();
            model.groupName = "第" + i + "分组";
            model.memberNum = i;
            model.serverId = i;
            model.groupOrder = i;
            model.defaultFlag = i;
            models.add(i, model);
        }

        startActivity(ManageGroupActivity.getCallIntent(this, models));
    }
}
