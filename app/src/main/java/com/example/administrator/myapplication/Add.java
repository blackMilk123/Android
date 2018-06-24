package com.example.administrator.myapplication;

import android.app.Activity;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;


public class Add extends ListActivity{
    private SimpleCursorAdapter adapter = null;
    private NotesDB db;
    private SQLiteDatabase dbRead;

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_EDIT_NOTE = 2;
    public static final int REQUEST_CODE_DELETE=3;
    private View.OnClickListener btnAddNote_clickHandler = new View.OnClickListener() {

        @Override
         public void onClick(View v) {
            // 有返回结果的开启编辑日志的Activity，
            System.out.println("这么叼");
            startActivityForResult(new Intent(Add.this,
                    AtyEditNote.class), REQUEST_CODE_ADD_NOTE);
        }
    };
    private View.OnClickListener btnfindNote_clickHandler = new View.OnClickListener() {

        @Override
        public void onClick(View v) {


            EditText text = (EditText) findViewById(R.id.et_find);
            String find_word = text.getText().toString();
            System.out.println(find_word);
            //查询并刷新
            findRefreshNotesListView(find_word);
            System.out.println("6666666666");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.one);

         /*定义事件*/
        findViewById(R.id.btnAddNote).setOnClickListener(
                btnAddNote_clickHandler);
        findViewById(R.id.find).setOnClickListener(
                btnfindNote_clickHandler);
        /*findViewById(R.id.delete).setOnClickListener(btnDeleteNote_clickHandler);*/

        getListView();

       ListView listView = getListView();
       listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
           @Override
           public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
               System.out.println("长按了:"+i+"和"+l);
               AlertDialog.Builder dialog = new AlertDialog.Builder(Add.this);

               dialog.setTitle("删除提示框");
                       dialog.setMessage("确认删除？");
                       dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialogInterface, int i) {
                               //获取当前笔记条目的Cursor对象
                               Cursor c = adapter.getCursor();
                               //获取id
                              String id =  c.getString(c.getColumnIndex(NotesDB.COLUMN_NAME_ID));
                              //删除并刷新
                             deleteRefresh(id);
                               //
                           }
                       });
                      dialog .setNegativeButton("取消",null);
                      dialog.create();
                      dialog.show();

               return false;
           }
       });
        // 操作数据库
        db = new NotesDB(this);
        dbRead = db.getReadableDatabase();


        // 查询数据库并将数据显示在ListView上。
        // 建议使用CursorLoader，这个操作因为在UI线程，容易引起无响应错误
        adapter = new SimpleCursorAdapter(this, R.layout.nodes_list, null,
                new String[] { NotesDB.COLUMN_NAME_NOTE_NAME,
                        NotesDB.COLUMN_NAME_NOTE_DATE }, new int[] {
                R.id.tvName, R.id.tvDate });
        setListAdapter(adapter);

        refreshNotesListView();


    }


    /**
     * 复写方法，笔记列表中的笔记条目被点击时被调用，打开编辑笔记页面，同时传入当前笔记的信息
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        // 获取当前笔记条目的Cursor对象
        Cursor c = adapter.getCursor();

        //移动到指定行
        c.moveToPosition(position);


        // 显式Intent开启编辑笔记页面
        Intent i = new Intent(Add.this, ReAtyEditNote.class);

        // 传入笔记id，name，content
        i.putExtra(AtyEditNote.EXTRA_NOTE_ID,
                c.getInt(c.getColumnIndex(NotesDB.COLUMN_NAME_ID)));
        i.putExtra(AtyEditNote.EXTRA_NOTE_NAME,
                c.getString(c.getColumnIndex(NotesDB.COLUMN_NAME_NOTE_NAME)));
        i.putExtra(AtyEditNote.EXTRA_NOTE_CONTENT,
                c.getString(c.getColumnIndex(NotesDB.COLUMN_NAME_NOTE_CONTENT)));

        // 有返回的开启Activity
        startActivityForResult(i, REQUEST_CODE_EDIT_NOTE);

        super.onListItemClick(l, v, position, id);
    }

    /**
     *
     * 当被开启的Activity存在并返回结果时调用的方法
     *
     * 当从编辑笔记页面返回时调用，刷新笔记列表
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CODE_ADD_NOTE:
            case REQUEST_CODE_EDIT_NOTE:
                if (resultCode == Activity.RESULT_OK) {
                    //调用下面的刷新方法,从数据库中查询并刷新页面
                    refreshNotesListView();
                }
                break;

            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }/*删除数据库并刷新
    */
public void deleteRefresh(String id){

            db = new NotesDB(this);
    dbRead = db.getReadableDatabase();
    adapter =  new SimpleCursorAdapter(this, R.layout.nodes_list, null,
            new String[] { NotesDB.COLUMN_NAME_NOTE_NAME,
                    NotesDB.COLUMN_NAME_NOTE_DATE }, new int[] {
            R.id.tvName, R.id.tvDate });
    setListAdapter(adapter);
    String[] noteId = {id};
    //删除并再查询刷新
    dbRead.delete("notes","_id=?",noteId);
    adapter.changeCursor(dbRead.query(NotesDB.TABLE_NAME_NOTES, null, null,
            null, null, null,"date desc"));
}
    /**
     * 刷新笔记列表，内容从数据库中查询
     */
    public void refreshNotesListView() {

        adapter.changeCursor(dbRead.query(NotesDB.TABLE_NAME_NOTES, null, null,
                null, null, null,"date desc"));

    }
    /*查询并刷新*/
    public void findRefreshNotesListView(String name) {
        // 操作数据库
        db = new NotesDB(this);
        dbRead = db.getReadableDatabase();


        // 查询数据库并将数据显示在ListView上。
        // 建议使用CursorLoader，这个操作因为在UI线程，容易引起无响应错误
        adapter = new SimpleCursorAdapter(this, R.layout.nodes_list, null,
                new String[] { NotesDB.COLUMN_NAME_NOTE_NAME,
                        NotesDB.COLUMN_NAME_NOTE_DATE }, new int[] {
                R.id.tvName, R.id.tvDate });
        setListAdapter(adapter);

        String[] text = {"%"+name+"%"};
        adapter.changeCursor(dbRead.query(NotesDB.TABLE_NAME_NOTES, null, "name like ?",
                text, null, null, "date desc"));

    }



}
