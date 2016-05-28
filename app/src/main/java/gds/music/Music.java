package gds.music;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Random;
import java.util.TreeMap;

public class Music extends AppCompatActivity implements View.OnClickListener  {

    boolean isFirstPlay = true;
    boolean isOrderPlay = true;//顺序模式
    boolean isRandPlay = false;//随机模式,默认是顺序模式
    boolean isOnlyPlay = false;//单曲模式,默认是顺序模式
    boolean isThread = true;
    private StringBuilder sb = new StringBuilder();

    private Button  btnPlay;
    private Button  btnBefore;
    private Button  btnNext;
    private TextView tvtDuration;
    private SeekBar proBar;
    private ListView lv;

    private Thread th;
    ArrayList<MusicInfo> list = new ArrayList<MusicInfo>();
    private MusicAdapter musicAdapter= null;

    private MediaPlayer music = new MediaPlayer();
    private int currentPlayingIndex = -1;
    private String strRollWord;
    private int strN=0;


    static final int TIME = 0;
    static final int ROLL = 1;

    Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case TIME:
                    tvtDuration.setText((String) msg.obj);
                    break;
                case ROLL:
                    if(strN<=0)
                        strN=strRollWord.length();
                    setTitle(strRollWord.substring(strRollWord.length()-strN--,strRollWord.length()));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Happy Ending Player");
        setContentView(R.layout.activity_music);
        lv = (ListView) findViewById(R.id.lv_music_name);
        btnPlay = (Button) findViewById(R.id.btn_play);
        btnBefore = (Button) findViewById(R.id.btn_befor);
        btnNext = (Button) findViewById(R.id.btn_next);
        proBar = (SeekBar) findViewById(R.id.pro);
        proBar.setProgress(0);
        btnPlay.setEnabled(false);
        btnNext.setEnabled(false);
        btnBefore.setEnabled(false);
        proBar.setEnabled(false);
        tvtDuration = (TextView) findViewById(R.id.tvt_duration);


        btnPlay.setOnClickListener(this);
        btnBefore.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        proBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
             * @param seekBar
             * @param progress  改变后的新值
             * @param fromUser 本次改变是不是“用户发起”的
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String time = getMusicTime(music.getCurrentPosition());
                Message msg =  new Message();
                msg.obj = time;
                msg.what = TIME;
                handler.sendMessage(msg);
                if(fromUser){
                    music.seekTo(progress);
                }
            }
            /**
             * 只有的用户开始“touch屏幕”时才会触发
             * @param seekBar
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                    music.pause();
            }
            /**
             * 只有的用户“touch屏幕”结束时才会触发
             * @param seekBar
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                    music.start();
            }
        });

      lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              try {
                  myDebug("onItemClick...");
                  currentPlayingIndex = position;

                  playMusic(list.get(position).getAbsPath());


              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
      });

      lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
          @Override
          public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
              MusicInfo musicInfo = list.get(position);
              DecimalFormat df = new DecimalFormat("0.00");

              String size =  df.format(musicInfo.getSize()/1024/1024);
              String[] info = { "曲名:"+musicInfo.getTitle(), "歌手:"+musicInfo.getSinger(), "专辑:"+musicInfo.getAlbum(),
                       "时长:"+String.valueOf(getMusicTime(musicInfo.getDuration())),
                      "大小:"+size+"M" };
              AlertDialog.Builder ab =new AlertDialog.Builder(Music.this);
              ab.setItems(info, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {

                  }
              });
              ab.create();
              ab.show();
              return true;
          }
      });

        music.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    currentPlayingIndex =  createCurrentPlayingIndexByMode();
                    playMusic(list.get(currentPlayingIndex).getAbsPath());
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }//onCreate

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isThread =false;
        music.stop();
        music.release();
        myDebug("资源被释放了...");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_play:
                if(btnPlay.getText().toString()=="暂停"){
                    music.pause();
                    btnPlay.setText("播放");
                }
                else{
                    music.start();
                    btnPlay.setText("暂停");
                }
                break;

            case R.id.btn_befor:
                try {
                    currentPlayingIndex--;
                    if(currentPlayingIndex <0){
                        currentPlayingIndex = list.size()-1;
                    }
                    playMusic(list.get(currentPlayingIndex).getAbsPath());
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.btn_next:
                try {
                    currentPlayingIndex++;
                    if(currentPlayingIndex >= list.size()){
                        currentPlayingIndex = 0;
                    }
                    playMusic(list.get(currentPlayingIndex).getAbsPath());
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.title_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.orderPlay:
                isOrderPlay =true;
                isRandPlay =false;
                isOnlyPlay =false;
                Toast.makeText(Music.this,"顺序模式",Toast.LENGTH_SHORT).show();
                break;

            case R.id.randPlay:
                isRandPlay = true;
                isOrderPlay = false;
                isOnlyPlay =false;
                Toast.makeText(Music.this,"随机模式",Toast.LENGTH_SHORT).show();
                break;

            case R.id.onlyPlay:
                /*开启“文件浏览器”
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("audio/x-mpeg");
                startActivityForResult(i,100);*/
                isOnlyPlay = true;
                isRandPlay =false;
                isOrderPlay =false;
                Toast.makeText(Music.this,"单曲模式",Toast.LENGTH_SHORT).show();
                break;

            case R.id.scanner:
                scannerMusic();
                musicAdapter = new MusicAdapter(Music.this,R.layout.listview_item,list);
                lv.setAdapter(musicAdapter);
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Toast.makeText(Music.this,data.getData().toString(),Toast.LENGTH_LONG).show();

    }

    private int createCurrentPlayingIndexByMode(){
        //如果是单曲循环模式，返回的仍然是当前索引
        if(isOnlyPlay){
            return currentPlayingIndex;
        }
        //如果是随机模式，返回随机索引
        else if(isRandPlay){
            return getRand();
        }
        //如果是顺序模式，返回下一首
        else{
            currentPlayingIndex++;
            if(currentPlayingIndex >= list.size()){
                currentPlayingIndex = 0;
            }
            return currentPlayingIndex;
        }
    }

    private void playMusic(String pmMusicAbsPath) throws IOException {

        if(!isFirstPlay){
            music.reset();
        }
        music.setDataSource(pmMusicAbsPath);
        music.prepare();
        music.start();
        btnPlay.setText("暂停");
        tvtDuration.setText("00:00");
        proBar.setProgress(0);
        proBar.setMax(music.getDuration());
        updateProgress();
        isFirstPlay = false;
        strRollWord = getRollWord();
        strN = strRollWord.length();
    }

    private void updateProgress(){
       th =  new Thread(new Runnable() {
            @Override
            public void run() {
                while (isThread){
                    if(music.isPlaying()){
                        proBar.setProgress(music.getCurrentPosition());
                        Message msg =new Message();
                        msg.what = ROLL;
                        handler.sendMessage(msg);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        th.start();
    }

    private ArrayList<String> getMusicNameList(String gmnlDirPath){
        /*ArrayList<String> gmnlListMusicName =new ArrayList<String>();
        File dir = new File(gmnlDirPath);
        if(!dir.isDirectory()){
            Toast.makeText(Music.this,"请选择一个目录",Toast.LENGTH_SHORT).show();
            return null;
        }
        File[] files =dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if(filename.endsWith(".mp3") || filename.endsWith(".wmv") || filename.endsWith(".Ogg"))
                    return true;
                else
                    return false;
            }
        });
        for(File f : files ){
            gmnlListMusicName.add(f.getName());
            htMusicPath.put(f.getName(),f.getAbsolutePath());
        }
        return gmnlListMusicName;*/
        return null;
    }

    private void scannerMusic(){
        final Cursor cursor =getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,null,
                null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if(cursor == null){
            Toast.makeText(Music.this,"没有可用资源",Toast.LENGTH_SHORT).show();
            return;
        }
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
            String singer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
            long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
            MusicInfo mi = new MusicInfo(title, album, singer, path, duration, size);
            list.add(mi);
        }
        if(!list.isEmpty()){
            btnPlay.setEnabled(true);
            btnNext.setEnabled(true);
            btnBefore.setEnabled(true);
            proBar.setEnabled(true);
        }
    }

    private int getRand(){
        Random random =new Random();
        return random.nextInt(list.size());
    }

    private String getRollWord(){
        sb.replace(0,sb.length(),"");
        sb.append("                           ");
        sb.append(list.get(currentPlayingIndex).getTitle());
        sb.append(" - ");
        sb.append(list.get(currentPlayingIndex).getSinger());
        return sb.toString();
    }

    private String getMusicTime(long i){
        SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
        return dateFormat.format(new Date(i));
    }

    static public void  myDebug(String msg){
        Log.d("gds",msg);
    }
}
