package tfsapps.securityalarmplus;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.media.MediaPlayer;
import android.os.Bundle;

import android.util.TypedValue;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
//  効果音関連
import android.content.Context;
import android.media.AudioManager;
//  メール関連
import android.content.Intent;
import android.net.Uri;
//  GPS関連
import android.location.*;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.graphics.Color;
//  設定関連
import android.preference.PreferenceManager;
//  ダイアログ
import android.app.AlertDialog;
//  戻るボタン
import android.view.KeyEvent;
//  国設定
import java.util.Locale;
import java.util.logging.Handler;
//　広告

// ライト
//import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
//タイマースレッド
import java.io.IOException;
import java.security.Policy;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;


public class MainActivity extends AppCompatActivity implements LocationListener {
    private static final int REQUEST_LOCATION = 1;
    private int startflag = 0;
    private int stopcount = -1;
    private int stopmax = 1;
    private MediaPlayer bgm;
    private int now_volume;
    private String bgm_name;
    public String mess_disp = ("");
    public String mess_mail = ("");
    // GPS用
    private boolean gpsflag;
    private LocationManager mLocationManager;
    private static int PERMISSION_REQUEST_CODE = 1;
    private static final String[] INITIAL_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private Intent intent;
    //  設定
    private boolean auto_alarm_flag;
    private boolean auto_light_flag;
    private boolean alarm_stop_flag;
    private int alarm_volume;
    private String mailaddr1;
    private String mailaddr2;
    private String mailaddr3;
    private String mailaddr4;
    private String mailaddr5;
    private String mailtitle;
    private String mailtext;
    //  国設定
    private Locale _local;
    private String _language;
    private String _country;

    private AdView mAdview;

    //ライト関連
//    private Camera camera = null;
    private CameraManager mCameraManager;
    private String mCameraId = null;
    private boolean isOn = false;
    static boolean on_off_light = false;

    //  アプリ生成時の処理
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        //  国設定
        _local = Locale.getDefault();
        _language = _local.getLanguage();
        _country = _local.getCountry();

//test_make
        //広告
        MobileAds.initialize(this, initializationStatus -> {
            mAdview = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdview.loadAd(adRequest);
        });

        //　効果音
        bgm = MediaPlayer.create(this, R.raw.alarm);

        //カメラ初期化
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        mCameraManager.registerTorchCallback(new CameraManager.TorchCallback() {
            @Override
            public void onTorchModeChanged(String cameraId, boolean enabled) {
                super.onTorchModeChanged(cameraId, enabled);
                mCameraId = cameraId;
                if (isOn == false && auto_light_flag == true && on_off_light == false)
                {
                    light_start_stop();
                }
                isOn = enabled;
            }
        }, new android.os.Handler());


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.v("LifeCycle", "------------------------------>PERMISSION 1");

            // Check Permissions Now
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            //            gpsflag = true;
            //            gpsflag = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            gpsflag = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, (LocationListener) this);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,3000, 3, (LocationListener) this);
            mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 3000, 3, (LocationListener) this);

        }
        else {
            Log.v("LifeCycle", "------------------------------>PERMISSION 0");

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);

            AlertDialog.Builder ad = new AlertDialog.Builder(this);
            if (_language.equals("ja")) {
                ad.setTitle(" 現在位置の取得について");
                ad.setMessage("\n\n\n 位置情報の取得を許可した場合、次回起動時より現在位置の取得が可能です\n\n\n\n");
                ad.setPositiveButton("ＯＫ", null);
            } else if (_language.equals("zh")) {
                ad.setTitle("对于收购的当前位置");
                ad.setMessage("\n\n\n如果允许获取的位置信息，它是可得到比下次启动时的当前位置\n\n\n\n");
                ad.setPositiveButton("确认", null);
            } else if (_language.equals("es")) {
                ad.setTitle("Para la adquisición de la posición actual");
                ad.setMessage("\n\n\nSi permite que la adquisición de información de posición, está disponible conseguir la posición actual de la próxima puesta en marcha\n\n");
                ad.setPositiveButton("cancelar", null);
            } else if (_language.equals("pt")) {
                ad.setTitle("Para a aquisição da posição actual");
                ad.setMessage("\n\n\nSe você permitir que a aquisição de informações de posição, ele está disponível obter a posição atual do que a próxima inicialização\n\n");
                ad.setPositiveButton("cancelar", null);
            } else {
                ad.setTitle("About acquisition of current position");
                ad.setMessage("\n\n\nWhen acquiring position information is permitted, the current position can be acquired from the next startup\n\n\n");
                ad.setPositiveButton("cancel", null);
            }
            ad.show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        /* テスト用 */
//test_make
//                    location.setLatitude(35.71023);
//                    location.setLongitude(139.797603);

        if (_language.equals("ja")) {
            mess_disp = " 緯度：" + location.getLatitude() + "\n 経度：" + location.getLongitude() + "\n\n";
            mess_mail = "現在の緯度,経度\n" + "http://maps.apple.com/?q=" + location.getLatitude() + "," + location.getLongitude();
        } else if (_language.equals("zh")) {
            mess_disp = " 纬度:" + location.getLatitude() + "\n 经度　:" + location.getLongitude() + "\n\n";
            mess_mail = "当前的纬度，经度\n" + "http://maps.apple.com/?q=" + location.getLatitude() + "," + location.getLongitude();
        } else if (_language.equals("es")) {
            mess_disp = " latitud:" + location.getLatitude() + "\n longitud:" + location.getLongitude() + "\n\n";
            mess_mail = "latitud,longitud\n" + "http://maps.apple.com/?q=" + location.getLatitude() + "," + location.getLongitude();
        } else if (_language.equals("pt")) {
            mess_disp = " latitude:" + location.getLatitude() + "\n longitude:" + location.getLongitude() + "\n\n";
            mess_mail = "latitude,longitude\n" + "http://maps.apple.com/?q=" + location.getLatitude() + "," + location.getLongitude();
        } else {
            mess_disp = " latitude:" + location.getLatitude() + "\n longitude:" + location.getLongitude() + "\n\n";
            mess_mail = "Current latitude,longitude\n" + "http://maps.apple.com/?q=" + location.getLatitude() + "," + location.getLongitude();
        }
        Log.d("GPS", mess_disp);
        mLocationManager.removeUpdates(this);

        TextView v = (TextView) findViewById(R.id.textView);
        v.setText(mess_disp);
        v.setTextColor(Color.WHITE);
        v.setBackgroundTintList(null);
        v.setBackgroundResource(R.drawable.bak_flat);

        // 現在位置アイコン
        ImageView img = (ImageView) findViewById(R.id.img_pos);
        img.setImageResource(R.drawable.pos_1);
    }



    @Override
    public void onStart() {
        super.onStart();// ATTENTION: This was auto-generated to implement the App Indexing API.
        Log.v("LifeCycle", "------------------------------>onStart");

        //  国設定
//        _local = Resources().getSystem().getConfiguration().locale;

        //  設定関連
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        auto_alarm_flag = sharedPreferences.getBoolean("auto_alarm", false);
        auto_light_flag = sharedPreferences.getBoolean("auto_light", false);
        alarm_stop_flag = sharedPreferences.getBoolean("alarm_stop", false);
        mailaddr1 = sharedPreferences.getString("mail_addr1", "");
        mailaddr2 = sharedPreferences.getString("mail_addr2", "");
        mailaddr3 = sharedPreferences.getString("mail_addr3", "");
        mailaddr4 = sharedPreferences.getString("mail_addr4", "");
        mailaddr5 = sharedPreferences.getString("mail_addr5", "");
        mailtitle = sharedPreferences.getString("mail_title", "");
        mailtext = sharedPreferences.getString("mail_text", "");
        String bgm_str = sharedPreferences.getString("alarm_kind", "default");
        String str = sharedPreferences.getString("alarm_value", "0");
        alarm_volume = Integer.parseInt(str);
        //音量調整
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        now_volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        // 現在の音量を取得する
        int ringVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        int ringMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 音量を設定する
        am.setStreamVolume(AudioManager.STREAM_MUSIC, alarm_volume, 0);
//        Toast.makeText(this, str+"現在"+ringVolume+"最大"+ringMaxVolume, Toast.LENGTH_SHORT).show();

        //  アラーム種類が変更？
        if (bgm_name != bgm_str) {
            if (bgm.isPlaying() == true) {
                bgm.stop();
                bgm = null;
                startflag = 0;
            }
            bgm_name = bgm_str;
            if (bgm_str.equals("kind_2") == true) bgm = MediaPlayer.create(this, R.raw.alarm2);
            else if (bgm_str.equals("kind_3") == true) bgm = MediaPlayer.create(this, R.raw.alarm3);
            else if (bgm_str.equals("kind_4") == true) bgm = MediaPlayer.create(this, R.raw.alarm4);
            else if (bgm_str.equals("kind_5") == true) bgm = MediaPlayer.create(this, R.raw.alarm5);
            else if (bgm_str.equals("kind_6") == true) bgm = MediaPlayer.create(this, R.raw.alarm6);
            else if (bgm_str.equals("kind_7") == true) bgm = MediaPlayer.create(this, R.raw.alarm7);
            else bgm = MediaPlayer.create(this, R.raw.alarm);
        }

        //  アラーム自動スタート制御
        if (auto_alarm_flag == false) {
            if (bgm.isPlaying() == false) {
                btnStartDisp();
                startflag = 0;
            }
        } else {
            if (bgm.isPlaying() == false) {
                bgm.setLooping(true);
                bgm.start();
            }
            startflag = 1;
            stopcount = 0;

            btnStopDisp();
        }
        //  アラーム停止制御
        if (alarm_stop_flag == false) stopmax = 1;
        else stopmax = 10;

        //  状態テスト表示
        if (mess_disp.isEmpty() == true) {

            //           gpsflag = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (gpsflag == false) {
                TextView v = (TextView) findViewById(R.id.textView);
                if (_language.equals("ja")) {
                    v.setText("現在地の取得ができません\n位置情報の設定を確認して下さい");
                    v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                } else if (_language.equals("zh")) {
                    v.setText("你不能得到您的位置\n检查的位置信息的设定");
                    v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                } else if (_language.equals("es")) {
                    v.setText("No se puede obtener de su ubicación.\nCompruebe el ajuste de la ubicación.");
                    v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                } else if (_language.equals("pt")) {
                    v.setText("Você não pode obter a sua localização.\nVerifique a definição da localização");
                    v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                } else {
                    v.setText("You can not get your location.\nCheck the setting of the location");
                    v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                }
                v.setTextColor(Color.RED);
            } else {
                TextView v = (TextView) findViewById(R.id.textView);
                if (_language.equals("ja")) {
                    v.setText("現在地を取得しています\nしばらくお待ち下さい．．．");
                    v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                } else if (_language.equals("zh")) {
                    v.setText("你必须得到当前位置\n请稍等片刻 ...");
                    v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                } else if (_language.equals("es")) {
                    v.setText("Usted tiene que obtener la ubicación actual. por favor espera ...");
                    v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                } else if (_language.equals("pt")) {
                    v.setText("Você tem que obter a localização atual.\nPor favor, espere ...");
                    v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                } else {
                    v.setText("You have to get the current location.\nPlease wait ...");
                    v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                }
                v.setTextColor(Color.BLACK);
            }
            // 現在位置アイコン
            ImageView img = (ImageView) findViewById(R.id.img_pos);
            img.setImageResource(R.drawable.pos_0);
        }


        //  自動ライト点灯
        if (auto_light_flag == true && on_off_light == false)
        {
            light_start_stop();
        }
        else {
            if (on_off_light == true) {
                this.btnStopDispL();
            } else {
                this.btnStartDispL();
            }
        }
    }

    //  アラーム停止処理（ボタン押下処理）.
    public void alarm_stop() {
        bgm.pause();
        startflag = 0;
        if (startflag == 0) {
            btnStartDisp();
        }
    }

    //  アラーム開始と停止処理（ボタン押下処理）
    public void alarm_start_stop() {
        /* 効果音スタートの操作 */
        if (startflag == 0) {
            startflag = 1;
            stopcount = 0;
            //  効果音
            bgm.setLooping(true);
            bgm.start();

            if (startflag == 1) {
                btnStopDisp();
            }
        }
        /* 効果音ストップの操作 */
        else {
            stopcount++;
            if (stopcount >= stopmax) {
                if (alarm_stop_flag == true) {
                    AlertDialog.Builder ad = new AlertDialog.Builder(this);
                    if (_language.equals("ja")) {
                        ad.setTitle("アラーム停止の確認");
                        ad.setMessage("\n\n\nアラームを停止しました\n\n\n\n\n\n");
                        ad.setPositiveButton("ＯＫ", null);
                    } else if (_language.equals("zh")) {
                        ad.setTitle("报警停止的确认");
                        ad.setMessage("\n\n\n它已经停止的报警\n\n\n\n\n\n");
                        ad.setPositiveButton("确认", null);
                    } else if (_language.equals("es")) {
                        ad.setTitle("La confirmación de la parada de alarma");
                        ad.setMessage("\n\n\nSe ha detenido la alarma\n\n\n\n\n\n");
                        ad.setPositiveButton("cancelar", null);
                    } else if (_language.equals("pt")) {
                        ad.setTitle("A confirmação da paragem de alarme");
                        ad.setMessage("\n\n\nSe tiver parado o alarme\n\n\n\n\n\n");
                        ad.setPositiveButton("cancelar", null);
                    } else {
                        ad.setTitle("Confirmation of the alarm stop");
                        ad.setMessage("\n\n\nIt has stopped the alarm\n\n\n\n\n\n");
                        ad.setPositiveButton("cancel", null);
                    }
                    ad.show();
                }
                this.alarm_stop();
            }
        }
    }

    //  ボタン：効果音スタート＆ストップ
    public void onStartStop(View view) {
        this.alarm_start_stop();
    }

    //  ボタン：スピーカー
    public void onSpeaker(View view) {
        this.alarm_start_stop();
    }

    private void btnStartDisp() {
        Button btn1 = (Button) findViewById(R.id.btn_startstop);
        btn1.setBackgroundResource(R.drawable.act_btn);
        btn1.setTextColor(Color.parseColor("white"));
        if (_language.equals("es")) {
            btn1.setText("COMIENZO");
            btn1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 46);
        } else if (_language.equals("pt")) {
            btn1.setText("COMEÇAR");
            btn1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 46);
        } else {
            btn1.setText("START");
            btn1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 46);
        }

        ImageButton imgbtn1 = (ImageButton) findViewById(R.id.btn_img_speaker);
        imgbtn1.setImageResource(R.drawable.speaker_0);
//        imgbtn1.setBackgroundColor(Color.rgb(200, 200, 200));
    }

    private void btnStopDisp() {
        Button btn1 = (Button) findViewById(R.id.btn_startstop);
        btn1.setBackgroundResource(R.drawable.noact_btn);
        btn1.setTextColor(Color.parseColor("red"));
        if (_language.equals("es")) {
            btn1.setText("DETENER");
            btn1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 46);
        } else if (_language.equals("pt")) {
            btn1.setText("PARE");
            btn1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 46);
        } else {
            btn1.setText("STOP");
            btn1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 46);
        }

        ImageButton imgbtn1 = (ImageButton) findViewById(R.id.btn_img_speaker);
        imgbtn1.setImageResource(R.drawable.speaker_1);
//        imgbtn1.setBackgroundColor(Color.rgb(255, 0, 0));
    }

    //  ライト開始と停止処理（ボタン押下処理）
    public void light_start_stop() {

        if (on_off_light == false)
        {
            if(mCameraId == null){
                return;
            }
            try {
                mCameraManager.setTorchMode(mCameraId, true);
            } catch (CameraAccessException e) {
                //エラー処理
                e.printStackTrace();
            }
            on_off_light = true;
            this.btnStopDispL();
        }
        else
        {
            if(mCameraId == null){
                return;
            }
            try {
                mCameraManager.setTorchMode(mCameraId, false);
            } catch (CameraAccessException e) {
                //エラー処理
                e.printStackTrace();
            }
            on_off_light = false;
            this.btnStartDispL();
        }
    }

    //  ボタン：ライト点灯スタート＆ストップ
    public void onStartStopL(View view) {
        this.light_start_stop();
    }

    //  ボタン：ライト
    public void onLight(View view) {
        this.light_start_stop();
    }

    private void btnStartDispL() {
        Button btn1 = (Button) findViewById(R.id.btn_light);
        btn1.setBackgroundResource(R.drawable.act_btn);
        btn1.setTextColor(Color.parseColor("white"));
        if (_language.equals("es")) {
            btn1.setText("COMIENZO");
            btn1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 46);
        } else if (_language.equals("pt")) {
            btn1.setText("COMEÇAR");
            btn1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 46);
        } else {
            btn1.setText("START");
            btn1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 46);
        }

        ImageButton imgbtn1 = (ImageButton) findViewById(R.id.btn_img_light);
        imgbtn1.setImageResource(R.drawable.light_0);
//        imgbtn1.setBackgroundColor(Color.rgb(200, 200, 200));
    }

    private void btnStopDispL() {
        Button btn1 = (Button) findViewById(R.id.btn_light);
        btn1.setBackgroundResource(R.drawable.noact_btn);
        btn1.setTextColor(Color.parseColor("red"));
        if (_language.equals("es")) {
            btn1.setText("DETENER");
            btn1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 46);
        } else if (_language.equals("pt")) {
            btn1.setText("PARE");
            btn1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 46);
        } else {
            btn1.setText("STOP");
            btn1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 46);
        }

        ImageButton imgbtn1 = (ImageButton) findViewById(R.id.btn_img_light);
        imgbtn1.setImageResource(R.drawable.light_1);
//        imgbtn1.setBackgroundColor(Color.rgb(255, 0, 0));
    }



    //  ボタン：メール送信
    public void onImgMail(View view) {

/*        if (gpsflag == false) {
            AlertDialog.Builder ad = new AlertDialog.Builder(this);
            ad.setTitle("位置情報が取得できません");
            ad.setMessage("\n\n位置情報の設定が無効です\n\n位置情報の設定を確認して下さい\n\n");
            ad.setPositiveButton("ＯＫ", null);
            ad.show();
            return;
        }

        if (mess_disp.isEmpty() == true) {
            AlertDialog.Builder ad = new AlertDialog.Builder(this);
            ad.setTitle("位置情報を取得しています");
            ad.setMessage("\n\n位置情報を取得した後\n\n再度ボタンを操作して下さい\n\n");
            ad.setPositiveButton("キャンセル", null);
            ad.show();
            return;
        }
        */
        this.mailSend();
    }

    public void composeEmail(String[] addresses, String subject, String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
    public void mailSend() {
        /* 送信 */
        String all_mailaddr[] = new String[5];
        String message = "";
        if (mailaddr1.isEmpty() == false) all_mailaddr[0] = "" + mailaddr1;
        if (mailaddr2.isEmpty() == false) all_mailaddr[1] = "" + mailaddr2;
        if (mailaddr3.isEmpty() == false) all_mailaddr[2] = "" + mailaddr3;
        if (mailaddr4.isEmpty() == false) all_mailaddr[3] = "" + mailaddr4;
        if (mailaddr5.isEmpty() == false) all_mailaddr[4] = "" + mailaddr5;
        message = "" + mess_mail + "\n" + mailtext;
        composeEmail(all_mailaddr, mailtitle, message);
    }

    /*
    public void mailSend() {
        // 送信
        intent = new Intent();
        intent.setAction(Intent.ACTION_SENDTO);
        //宛先をセット
//        intent.setData(Uri.parse("mailto:takasif2924@gmail.com"));
        String all_mailaddr = "";
        if (mailaddr1.isEmpty() == false) all_mailaddr = mailaddr1;
        if (mailaddr2.isEmpty() == false) all_mailaddr = all_mailaddr + "," + mailaddr2;
        if (mailaddr3.isEmpty() == false) all_mailaddr = all_mailaddr + "," + mailaddr3;
        if (mailaddr4.isEmpty() == false) all_mailaddr = all_mailaddr + "," + mailaddr4;
        if (mailaddr5.isEmpty() == false) all_mailaddr = all_mailaddr + "," + mailaddr5;
        if (all_mailaddr.isEmpty() == true) all_mailaddr = "@gmail.com";
        intent.setData(Uri.parse("mailto:" + all_mailaddr));
        //標題をセット
        intent.putExtra(Intent.EXTRA_SUBJECT, mailtitle);
        //本文をセット
        intent.putExtra(Intent.EXTRA_TEXT, mess_mail + "\n" + mailtext);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // メール起動
        startActivity(intent);
    }
    */

    //  戻るボタン
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    // ダイアログ表示など特定の処理を行いたい場合はここに記述
                    // 親クラスのdispatchKeyEvent()を呼び出さずにtrueを返す

                    /* アラームが鳴っている場合は警告表示 */
                    if (bgm.isPlaying() == false && on_off_light == false)
                    {
                        break;
                    }

                    AlertDialog.Builder ad = new AlertDialog.Builder(this);
                    if (_language.equals("ja")) {
                        ad.setTitle("[戻る]は操作無効です");
                        ad.setMessage("\n\nアラーム又はライトを停止して下さい\n\nアラーム又はライトを停止すると操作可能です\n\n\n\n");
//                        ad.setMessage("\n\n[ホーム]ボタンを押して下さい\n\n\n\n\n");
                    }
                    else if (_language.equals("zh")) {
                        ad.setTitle("[返回]按钮是无效操作");
                        ad.setMessage("\n\n请停止报警或点亮\n\n您可以通过停止警报或指示灯来操作\n\n\n\n");
                    }
                    else if (_language.equals("es")) {
                        ad.setTitle("[Volver] es operación no válida");
                        ad.setMessage("\n\nPor favor, detenga la alarma o la luz.\n\nPuede operar deteniendo una alarma o luz.\n\n\n");
                    }
                    else if (_language.equals("pt")) {
                        ad.setTitle("[Voltar] é uma operação inválida");
                        ad.setMessage("\n\nPor favor, pare de alarme ou luz.\n\nVocê pode operá-lo parando um alarme ou luz.\n\n\n");
                    }
                    else {
                        ad.setTitle("Invalid operation");
                        ad.setMessage("\n\n\nPlease stop alarm or light.\n\nYou can operate it by stopping an alarm or light.\n\n\n");
                    }
                    ad.setPositiveButton("ＯＫ", null);
                    ad.show();
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    //  メニュー
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //  メニュー選択時の処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent1 = new Intent(this, CrSetActivity.class);
            startActivity(intent1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v("LifeCycle", "------------------------------>onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v("LifeCycle", "------------------------------>onPause");
    }

    @Override
    public void onRestart() {
        super.onRestart();
        Log.v("LifeCycle", "------------------------------>onRestart");
    }

    @Override
    public void onStop() {
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
        Log.v("LifeCycle", "------------------------------>onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("LifeCycle", "------------------------------>onDestroy");

        // 音量
        if (bgm.isPlaying() == true)
        {
            bgm.stop();
            bgm = null;
        }
        // 音量戻し
        AudioManager backbgm = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        backbgm.setStreamVolume(AudioManager.STREAM_MUSIC, now_volume, 0);
        backbgm = null;

        //カメラ
        if (mCameraManager != null)
        {
            mCameraManager = null;
        }
    }
}