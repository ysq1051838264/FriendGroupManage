# FriendGroupManage
仿qq分组管理，可以删除、增加、以及修改等等 

![image](https://github.com/ysq1051838264/FriendGroupManage/blob/master/1.gif)

###使用方法
    1.在XML中添加
    <com.example.ysq.friendgroupmanage.DragListView
        android:id="@+id/dragList"
        android:layout_marginTop="10dp"
        android:layout_width="fill_parent"
        android:layout_height="fill_parent"
        android:cacheColorHint="#00000000"
        android:divider="#e5e5e5"
        android:dividerHeight="1px"
        android:fadingEdge="none"
        android:listSelector="#00000000" />
   
     2.在代码中添加
             mAdapter.listener(new DragListAdapter.ManageGroupLister() {
                       @Override
                       public void onItemClick(final ManageGroupModel manageGroupModel) {
                           new TextInputDialog.Builder()
                                   .setText(manageGroupModel.groupName)
                                   .textInputConfirmListener(new TextInputDialog.TextInputConfirmListener() {
                                       @Override
                                       public boolean onConfirm(String text) {
                                           //修改了组名,提交服务器
                                           models.get(manageGroupModel.serverId).groupName = text;
                                           mAdapter.notifyDataSetChanged();
                                           
                                           return true;
                                       }
                                   })
                                   .setContext(getContext())
                                   .build().show();
                       }
           
                       @Override
                       public void onItemDelete(ManageGroupModel manageGroupModel) {
                           ToastMaker.show(getContext(), "删除了哈哈哈");
                       }
           
                       @Override
                       public void updateView(List<ManageGroupModel> lists) {
                           //移动分组,更新UI以及保存
                           models = (ArrayList<ManageGroupModel>) lists;
                       }
                   });
        

**[DownLoad Apk](https://github.com/ysq1051838264/FriendGroupManage/blob/master/apk/FriendGroupManage.apk?raw=true)**
