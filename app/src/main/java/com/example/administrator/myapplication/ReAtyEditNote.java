package com.example.administrator.myapplication;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class ReAtyEditNote extends ListActivity {

    private int noteId = -1;
    private EditText etName, etContent;
    private MediaAdapter adapter;
    private SimpleCursorAdapter simpleCursorAdapter = null;
    private NotesDB db;
    private SQLiteDatabase dbRead, dbWrite;
    private String currentPath = null;

    public static final int REQUEST_CODE_GET_PHOTO = 1;
    public static final int REQUEST_CODE_GET_VIDEO = 2;

    public static final String EXTRA_NOTE_ID = "noteId";
    public static final String EXTRA_NOTE_NAME = "noteName";
    public static final String EXTRA_NOTE_CONTENT = "noteContent";

    /**
     * 按钮点击的监听器，实现OnClickListener接口
     */
    private OnClickListener btnClickHandler = new OnClickListener() {

        Intent i;
        File f;
        Button btn ;
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnEdit:
                    System.out.println("点击了按钮");
                    //对编辑和删除按钮进行隐藏
                    btn = (Button) findViewById(R.id.btnEdit);
                    Button cancel = (Button) findViewById(R.id.btnCancel);
                    btn.setVisibility(View.GONE);
                    cancel.setVisibility(View.GONE);


                    //对其他四个按钮进行展开
                    Button save = (Button) findViewById(R.id.btnSave);
                    Button photo = (Button) findViewById(R.id.btnAddPhoto);
                    Button video = (Button) findViewById(R.id.btnAddVideo);
                    Button delete = (Button)findViewById(R.id.delete);


                    save.setVisibility(View.VISIBLE);
                    photo.setVisibility(View.VISIBLE);
                    video.setVisibility(View.VISIBLE);
                    delete.setVisibility(View.VISIBLE);
                    break;
                case R.id.btnAddPhoto:// 添加照片按钮
                    // 使用Intent调用系统照相机，传入图像保存路径和名称
                    i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    f = new File(getMediaDir(), System.currentTimeMillis() + ".jpg");
                    if (!f.exists()) {
                        try {
                            f.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    currentPath = f.getAbsolutePath();
                    i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(i, REQUEST_CODE_GET_PHOTO);
                    break;
                case R.id.btnAddVideo:// 添加视频按钮
                    // 使用Intent调用系统录像器，传入视频保存路径和名称
                    i = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    f = new File(getMediaDir(), System.currentTimeMillis() + ".mp4");
                    if (!f.exists()) {
                        try {
                            f.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    currentPath = f.getAbsolutePath();
                    i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));

                    startActivityForResult(i, REQUEST_CODE_GET_VIDEO);
                    break;
                case R.id.btnSave:// 保存按钮
                    // 保存多媒体信息和笔记信息到数据库，然后关闭当前页面，返回到笔记列表页面/主页面
                    saveMedia(saveNote());
                    setResult(RESULT_OK);
                    finish();
                    break;
                case R.id.delete:// 删除按钮
                    noteId = getIntent().getIntExtra(EXTRA_NOTE_ID, -1);
                    String id = noteId+"";
                    // 关闭当前页面，返回到笔记列表页面/主页面
                    deleteItem(id);
                    System.out.println("外面删除 ");
                    setResult(RESULT_OK);
                    finish();
                    break;
                case R.id.btnCancel:// 取消按钮
                    // 关闭当前页面，返回到笔记列表页面/主页面
                    setResult(RESULT_CANCELED);
                    finish();
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reedit);

        //定义点击事件
        findViewById(R.id.btnSave).setOnClickListener(btnClickHandler);
        findViewById(R.id.delete).setOnClickListener(btnClickHandler);
        findViewById(R.id.btnAddPhoto).setOnClickListener(btnClickHandler);
        findViewById(R.id.btnAddVideo).setOnClickListener(btnClickHandler);
        findViewById(R.id.btnEdit).setOnClickListener(btnClickHandler);
        findViewById(R.id.btnCancel).setOnClickListener(btnClickHandler);

        db = new NotesDB(this);
        dbRead = db.getReadableDatabase();
        dbWrite = db.getWritableDatabase();

        // 显示多媒体列表
        adapter = new MediaAdapter(this);
        setListAdapter(adapter);

        etName = (EditText) findViewById(R.id.etName);
        etContent = (EditText) findViewById(R.id.etContent);

        // 获取Activity传递过来的noteId
        noteId = getIntent().getIntExtra(EXTRA_NOTE_ID, -1);

        if (noteId > -1) {
            etName.setText(getIntent().getStringExtra(EXTRA_NOTE_NAME));
            etContent.setText(getIntent().getStringExtra(EXTRA_NOTE_CONTENT));

            // 查询本笔记的noteId并且检查是否有对应的多媒体，有则遍历显示在MediaList中
            Cursor c = dbRead.query(NotesDB.TABLE_NAME_MEDIA, null,
                    NotesDB.COLUMN_NAME_MEDIA_OWNER_NOTE_ID + "=?",
                    new String[] { noteId + "" }, null, null, null);
            while (c.moveToNext()) {
                adapter.add(new AtyEditNote.MediaListCellData(c.getString(c
                        .getColumnIndex(NotesDB.COLUMN_NAME_MEDIA_PATH)), c
                        .getInt(c.getColumnIndex(NotesDB.COLUMN_NAME_ID))));
            }


            adapter.notifyDataSetChanged();
        }


    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        AtyEditNote.MediaListCellData data = adapter.getItem(position);
        Intent i;

        switch (data.type) {
            case MediaType.PHOTO:
                i = new Intent(this, AtyPhotoViewer.class);
                i.putExtra(AtyPhotoViewer.EXTRA_PATH, data.path);
                startActivity(i);
                break;
            case MediaType.VIDEO:
                i = new Intent(this, AtyVideoViewer.class);
                i.putExtra(AtyVideoViewer.EXTRA_PATH, data.path);
                startActivity(i);
                break;
        }

        super.onListItemClick(l, v, position, id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        System.out.println(data);

        switch (requestCode) {
            case REQUEST_CODE_GET_PHOTO:
            case REQUEST_CODE_GET_VIDEO:
                if (resultCode == RESULT_OK) {
                    adapter.add(new AtyEditNote.MediaListCellData(currentPath));
                    adapter.notifyDataSetChanged();
                }
                break;
            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 获取存储Media的目录路径
     *
     * @return File类型的目录路径
     */
    public File getMediaDir() {
        File dir = new File(Environment.getExternalStorageDirectory(),
                "360");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * 保存Media信息到数据库
     *
     * @param noteId
     */
    public void saveMedia(int noteId) {

        AtyEditNote.MediaListCellData data;
        ContentValues cv;

        for (int i = 0; i < adapter.getCount(); i++) {
            data = adapter.getItem(i);

            if (data.id <= -1) {
                cv = new ContentValues();
                cv.put(NotesDB.COLUMN_NAME_MEDIA_PATH, data.path);
                cv.put(NotesDB.COLUMN_NAME_MEDIA_OWNER_NOTE_ID, noteId);
                dbWrite.insert(NotesDB.TABLE_NAME_MEDIA, null, cv);
            }
        }

    }
/*
* 删除数据
* */
public void deleteItem(String noteId){
    // 操作数据库
    db = new NotesDB(this);
    dbRead = db.getReadableDatabase();
   simpleCursorAdapter =  new SimpleCursorAdapter(this, R.layout.nodes_list, null,
            new String[] { NotesDB.COLUMN_NAME_NOTE_NAME,
                    NotesDB.COLUMN_NAME_NOTE_DATE }, new int[] {
            R.id.tvName, R.id.tvDate });
    setListAdapter(simpleCursorAdapter);
    String[] id = {noteId};
    dbRead.delete("notes","_id=?",id);
    simpleCursorAdapter.changeCursor(dbRead.query(NotesDB.TABLE_NAME_NOTES, null, null,
            null, null, null,"date desc"));
    System.out.println("删除里面");

}
    /**
     * 保存日志到数据库

     */
    public int saveNote() {

        ContentValues cv = new ContentValues();
        cv.put(NotesDB.COLUMN_NAME_NOTE_NAME, etName.getText().toString());
        cv.put(NotesDB.COLUMN_NAME_NOTE_CONTENT, etContent.getText().toString());
        cv.put(NotesDB.COLUMN_NAME_NOTE_DATE, new SimpleDateFormat(
                "yyyy-MM-dd hh:mm:ss").format(new Date()));

        if (noteId > -1) {
            dbWrite.update(NotesDB.TABLE_NAME_NOTES, cv, NotesDB.COLUMN_NAME_ID
                    + "=?", new String[] { noteId + "" });
            return noteId;
        } else {
            return (int) dbWrite.insert(NotesDB.TABLE_NAME_NOTES, null, cv);
        }
    }

    /**
     * 复写Activity的生命周期方法，用于关闭读写数据库的操作
     */
    @Override
    protected void onDestroy() {
        dbRead.close();
        dbWrite.close();
        super.onDestroy();
    }

    /**
     * 多媒体的种类

     */
    static class MediaType {
        static final int PHOTO = 1;
        static final int VIDEO = 2;
    }
}