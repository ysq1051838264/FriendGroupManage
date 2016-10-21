package com.example.ysq.friendgroupmanage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by ysq on 16/10/8.
 */

public class ManageGroupActivity extends AppCompatActivity {
    RelativeLayout manageGroupRl;
    DragListView dragList;

    ArrayList<ManageGroupModel> models;
    DragListAdapter mAdapter = null;

    public static final String KEY_LIST_MODEL = "key_list_model";

    public static Intent getCallIntent(Context context, List<ManageGroupModel> models) {
        return new Intent(context, ManageGroupActivity.class)
                .putParcelableArrayListExtra(KEY_LIST_MODEL, (ArrayList<? extends Parcelable>) models);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_group_activity);

        initView();

        initData();
    }

    private void initView() {
        manageGroupRl = (RelativeLayout) findViewById(R.id.manageGroupRl);
        dragList = (DragListView) findViewById(R.id.dragList);

        manageGroupRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onManageClick();
            }
        });
    }

    protected void initData() {
        models = getIntent().getParcelableArrayListExtra(KEY_LIST_MODEL);

        mAdapter = new DragListAdapter(this, models);
        mAdapter.listener(new DragListAdapter.ManageGroupLister() {
            @Override
            public void onItemClick(ManageGroupModel manageGroupModel) {
                Toast.makeText(ManageGroupActivity.this, "点击修改组名字", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemDelete(ManageGroupModel manageGroupModel) {
                Toast.makeText(ManageGroupActivity.this, "点击删除了", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void updateView(List<ManageGroupModel> lists) {
                models = (ArrayList<ManageGroupModel>) lists;
            }
        });
        dragList.setAdapter(mAdapter);
    }

    void onManageClick() {
        //添加分组
        final View dialogView = LayoutInflater.from(this).inflate(R.layout.edit_text, null);

        AlertDialog.Builder customizeDialog = new AlertDialog.Builder(this)
                .setTitle("添加分组")
                .setView(dialogView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 获取EditView中的输入内容
                        EditText edit_text = (EditText) dialogView.findViewById(R.id.edit_text);
                        Toast.makeText(dialogView.getContext(), edit_text.getText().toString(), Toast.LENGTH_SHORT).show();
                    }
                });
        customizeDialog.show();
    }

}
