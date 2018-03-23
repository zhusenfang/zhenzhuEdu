package com.xieruinet.zhu.xieruiedu;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.smdt.SmdtManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.xboot.stdcall.DataforHandle;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.vov.vitamio.utils.Base64;


public class MainActivity extends SerialPortActivity {


    private SurfaceView mySurfaceView;
    private SurfaceHolder myHolder;
    private Camera myCamera;



    public int count = 11;//d定时器
    public TimerTask task;
    public Timer t = new Timer();

    public boolean timerflag = false;

    public String serverip = null;
    public String schoolid = null;

    public UpdateInfo info = null;
    public String versionname = null;
    public String token = null;
    //public String bj = null;
   // public String js = null;

    public String bplx = null;//屏幕类型
    public String xlh = null;//屏幕类型
    public String njid = null;//年级
    public String bjid = null;//班级
    public String jsid = null;//教室

    public String fullscreen = "";

    public int CurPage = 0;
    public int CurPageType = 0;

    public boolean UpVessionFlag = false;

    String[] sysUrlList={
            "html/common/class_index.html",
            "html/common/notice.html",
            "html/common/vedio.html",
            "html/common/zb_vedio.html",
    };

    String[] BJList={
            "html/classfile/red_index.html",//班级主页
            "html/classfile/red_index.html",//班级主页
            "html/classfile/blue_xc.html",//班级相册
            "html/classfile/blue_vedio.html",//视频
            "html/classfile/blue_lesson.html",//上课考勤
            "html/classfile/blue_exam.html",//考试考勤
    };

    String[] NJList={
            "html/gradefile/index.html",
    };

    String[] XJList={
            "html/schoolfile/index.html",
    };

    String asesetspath = "file:///android_asset/wwwroot/";
    String webpath = "file:///android_asset/wwwroot/";
    //String webpath = "http://192.168.1.100/APPWeb/";


    public ArrayList<ModleHelp> lsjm = new ArrayList<ModleHelp>();//临时节目

    public ArrayList<ModleHelp> dsjm = new ArrayList<ModleHelp>();//强行插播
    public ArrayList<ModleHelp> bjjm = new ArrayList<ModleHelp>();//班级节目
    public ArrayList<ModleHelp> njjm = new ArrayList<ModleHelp>();//年级节目
    public ArrayList<ModleHelp> xjjm = new ArrayList<ModleHelp>();//校级节目

    private static final String LOG_TAG = "WebViewDemo";
    public WebView mWebView;
    private Handler mHandler = new Handler();
    private MediaPlayer mediaPlayer = new MediaPlayer();



    private String getVersionName() throws Exception{
        //获取packagemanager的实例
        PackageManager packageManager = getPackageManager();
        //getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
        return packInfo.versionName;
    }

    public UpdateInfo getUpdataInfo(InputStream is) throws Exception{
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(is, "utf-8");//设置解析的数据源
        int type = parser.getEventType();
        UpdateInfo info = new UpdateInfo();//实体
        while(type != XmlPullParser.END_DOCUMENT ){
            switch (type) {
                case XmlPullParser.START_TAG:
                    if("version".equals(parser.getName())){
                        info.setVersion(parser.nextText()); //获取版本号
                    }else if ("url".equals(parser.getName())){
                        info.setUrl(parser.nextText()); //获取要升级的APK文件
                    }else if ("description".equals(parser.getName())){
                        info.setDescription(parser.nextText()); //获取该文件的信息
                    }
                    break;
            }
            type = parser.next();
        }
        return info;
    }

    public class CheckVersionTask implements Runnable{
        public void run() {
            try {
                //从资源文件获取服务器 地址
                //包装成url的对象
                URL url = new URL(serverip+"/vessioninfo/update.xml");

                HttpURLConnection conn =  (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                InputStream is =conn.getInputStream();
                info =  getUpdataInfo(is);
                String serversoin =info.getVersion();

                 if(serversoin.equals(versionname)){
                   //Log.i(TAG,"版本号相同无需升级");
                   //LoginMain();
                }else{
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                }
            } catch (Exception e) {
                // 待处理
                Message msg = new Message();
                msg.what = 2;
                handler.sendMessage(msg);
                e.printStackTrace();
            }
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    //对话框通知用户升级程序
                    showUpdataDialog();
                    break;
                case 2:
                    //服务器超时
                    Toast.makeText(getApplicationContext(), "获取服务器更新信息失败，地址：" + serverip, Toast.LENGTH_SHORT).show();
                    //LoginMain();
                    break;
                case 3:
                    //下载apk失败
                    Toast.makeText(getApplicationContext(), "下载新版本失败，地址：" + serverip, Toast.LENGTH_SHORT).show();
                    //LoginMain();
                    break;
            }
        }
    };





    /*
     *
     * 弹出对话框通知用户更新程序
     *
     * 弹出对话框的步骤：
     *  1.创建alertDialog的builder.
     *  2.要给builder设置属性, 对话框的内容,样式,按钮
     *  3.通过builder 创建一个对话框
     *  4.对话框show()出来
     */
    protected void showUpdataDialog() {
        AlertDialog.Builder builer = new AlertDialog.Builder(this) ;
        builer.setTitle("版本升级");
        builer.setMessage(info.getDescription());
        //当点确定按钮时从服务器上下载 新的apk 然后安装
        builer.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Log.i(TAG,"下载apk,更新");
                UpVessionFlag = false;
                downLoadApk();
                Toast.makeText(getApplicationContext(), "下载apk,更新", Toast.LENGTH_SHORT).show();
            }
        });
        //当点取消按钮时进行登录
        builer.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                //LoginMain();
                UpVessionFlag = false;
                Toast.makeText(getApplicationContext(), "取消,更新", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog dialog = builer.create();
        dialog.show();
    }


    /*
     * 从服务器中下载APK
     */
    protected void downLoadApk() {
        final ProgressDialog pd;    //进度条对话框
        pd = new  ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage("正在下载更新");
        pd.show();
        new Thread(){
            @Override
            public void run() {
                try {
                    File file = DownLoadManager.getFileFromServer(info.getUrl(), pd);
                    sleep(3000);
                    installApk(file);
                    pd.dismiss(); //结束掉进度条对话框
                } catch (Exception e) {
                    Message msg = new Message();
                    msg.what = 3;
                    handler.sendMessage(msg);
                    e.printStackTrace();
                }
            }}.start();
    }

    //安装apk
    protected void installApk(File file) {
        Intent intent = new Intent();
        //执行动作
        intent.setAction(Intent.ACTION_VIEW);
        //执行的数据类型
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        startActivity(intent);
    }



    public class ZhengZhuoTask implements Runnable{
        public void run() {
            try {
                while (true) {
                    try {
                       // Thread.sleep(400);
                        String card = scanCardID();
                        if(card.length() > 0)
                        {
                            Message msg = new Message();
                            msg.what = 1;
                            msg.obj = card;
                            handlerzz.sendMessage(msg);
                        }
                       // Thread.sleep(400);
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }
            } catch (Exception e) {
                // 待处理
                e.printStackTrace();
            }
        }
    }

    Handler handlerzz = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String card = msg.obj.toString();
            mWebView.loadUrl("javascript:ReadCard('" + card + "')");
            Toast.makeText(getApplicationContext(), card, Toast.LENGTH_SHORT).show();
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//       requestWindowFeature(Window.FEATURE_NO_TITLE);
//        //去掉Activity上面的状态栏
       getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                      WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        LinearLayout linear=(LinearLayout) findViewById(R.id.LinearLayoutbox);

        SmdtManager smdt= SmdtManager.create(getApplicationContext());
        smdt.smdtSetStatusBar(getApplicationContext(),false);

        xlh = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        serverip = getServerValue("serverip");
        if(serverip.length()> 0)  {  new UpDatePicThread().start(); }

        UpdateVession();//更新版本

        mWebView = new WebView(this);// (WebView) findViewById(R.id.webview);
        linear.addView(mWebView);
        DisplayMetrics dm = new DisplayMetrics();//获取当前显示的界面大小
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width=dm.widthPixels;
        int height=dm.heightPixels;//获取当前界面的高度
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) mWebView.getLayoutParams();
        linearParams.height = height;
        mWebView.setLayoutParams(linearParams);


        InitWebView();

        initSurface();

//        DataforHandle dh =new DataforHandle();
//        dh.setonoff(new String[]{"15:15","15:25","3"});
//        Toast.makeText(getApplicationContext(), "开机时间15:15，关机时间15:25",Toast.LENGTH_SHORT).show();

        //SmdtManager smdt=SmdtManager.create(getApplicationContext());
       // smdt.shutDown();
        //smdt.setPowerOffOnAlarm("18:33","18:38");

        //initCamera();

        ZhengZhuoTask zz = new ZhengZhuoTask();
        Thread testB = new Thread(zz);
        testB.start();


        //LoadUrl("http://192.168.1.100/APPWeb/html/gradefile/index.html");
        InitP();
    }

    public void UpdateVession()
    {
        try {
            versionname = getVersionName();
            serverip = getServerValue("serverip");
            if(serverip.length()> 0)
            {
                CheckVersionTask mya = new CheckVersionTask();
                Thread testA = new Thread(mya);
                testA.start();
               // new UpDatePicThread().start();
            }
            Toast.makeText(getApplicationContext(), "App版本：" + versionname +",服务器IP:" + serverip,Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {

        }
    }

    @Override
    protected void onDataReceived(String card) {

    }

    public class UpDatePicThread extends Thread {

        //继承Thread类，并改写其run方法
        private final static String TAG = "My Thread ===> ";
        public void run(){
            while(true)
            {
                try {
                    Date now = new Date(System.currentTimeMillis());

                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");//设置日期格式
                    String dtstr =   df.format(new Date());
                    int nowtimes = now.getHours()*60+now.getMinutes();
                    if(nowtimes == (11*60 + 30) || nowtimes == (17*60 + 30))
                    {
                        File dir = new File("/mnt/sdcard/xierui/temppic/"+dtstr);
                        if(dir.exists()) {
                            File[] fileArray = dir.listFiles();
                            for (File f : fileArray) {
                                String urlStr = serverip + "/bpxt/bpxt/bpxtapi/UpFiles_img";
                                String st = f.getName();
                                String nm = st.substring(0, st.lastIndexOf("."));
                                Map<String, String> textMap = new HashMap<String, String>();
                                textMap.put("picid", nm);
                                textMap.put("schoolid", schoolid);
                                Map<String, String> fileMap = new HashMap<String, String>();
                                fileMap.put("imgurl", f.getAbsolutePath());
                                String ret = HttpRequest.formUpload(urlStr, textMap, fileMap);
                            }
                            sleep(29 * 1000);
                            deleteDirWihtFile(dir);
                        }
                    }
                    sleep(29*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Log.e(TAG, Thread.currentThread().getName() + "i =  " + i);
            }
        }
    }

    public static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWihtFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }


    //获取文件夹
    private File getDir()
    {
        //得到SD卡根目录
        File dir = Environment.getExternalStorageDirectory();

        if (dir.exists()) {
            return dir;
        }
        else {
            dir.mkdirs();
            return dir;
        }
    }

    public static String getImgStr(String imgFile){

         InputStream in = null;
         byte[] data = null;
         //读取图片字节数组
           try
            {
                   in = new FileInputStream(imgFile);
                    data = new byte[in.available()];
                     in.read(data);
                     in.close();
                 }
            catch (IOException e)
              {
                     e.printStackTrace();
                 }
             return new String(Base64.encodeToString(data, Base64.DEFAULT));
      }





    // 加载web
    @SuppressLint({"SetJavaScriptEnabled", "InlinedApi", "NewApi"})
    private void InitWebView() {




        // TODO Auto-generated method stubs
        // 设置WebView属性，能够执行Javascript脚本
        mWebView.setWebViewClient(new DemoWebViewClient());
        // 添加一个对象, 让JS可以访问该对象的方法, 该对象中可以调用JS中的方法
        mWebView.addJavascriptInterface(new ECPJS(), "ECPJS");

        mWebView.setWebChromeClient(new MyWebChromeClient()); // 播放视频
        WebSettings ws = mWebView.getSettings();

        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);

        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.getTextZoom();
        ws.setJavaScriptCanOpenWindowsAutomatically(true);
        ws.setAllowFileAccess(true);
        ws.setDefaultTextEncodingName("UTF-8");

        ws.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);  //关闭webview中缓存
        ws.setLoadsImagesAutomatically(true);  //支持自动加载图片

        ws.setJavaScriptCanOpenWindowsAutomatically(true);//支持js调用window.open方法
        ws.setSupportMultipleWindows(true);// 设置允许开启多窗口



    }

    private void LoadUrl(String url) {

        mWebView.loadUrl("javascript:try{stopplay();}catch(e){}");//停止播放视频
        //mWebView.loadUrl("file:///android_asset/app_demo/home.html");
        mWebView.loadUrl(url);
    }

    public void InitP()
    {
        SharedPreferences pres = getSharedPreferences("appsyskey", Context.MODE_APPEND);
        //return pres.getString("", "null");
        token = pres.getString("token", null);
        serverip = pres.getString("serverip", null);
        bjid = pres.getString("bjid", null);
        jsid = pres.getString("jsid", null);
        schoolid = pres.getString("schoolid", null);
        bplx = pres.getString("bplx", null);
        njid = pres.getString("njid", null);

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(serverip == null){LoadUrl(webpath + "html/set/bind_serverip.html"); return; }
                if (schoolid == null) {LoadUrl(webpath + "html/set/bind_school.html"); return; }
                if (bplx == null) {LoadUrl(webpath + "html/set/bind_type.html"); return; }
                if(bplx.equals("1"))
                {//班级班牌
                   // if(token == null){ LoadUrl("file:///android_asset/wwwroot/demo4.html"); return; }
                    if(bjid == null){ LoadUrl(webpath + "html/set/bind_class.html"); return; }
                    if(jsid == null){ LoadUrl(webpath + "html/set/bind_class.html"); return; }
                    timerflag = true;
                    starttimer();
                    LoadUrl(webpath + BJList[0]);return;
                    //LoadUrl("http://192.168.1.100/appwebv/html/classfile/red_index.html");return;
                }
                else if(bplx.equals( "2"))
                {//年级班牌
                    if(njid == null){ LoadUrl(webpath + "html/set/bind_grade.html"); return; }
                    timerflag = true;
                    starttimer();
                    LoadUrl(webpath + NJList[0]);return;
                    //LoadUrl("http://192.168.1.100:8787/appweb/html/gradefile/index.html");return;
                }
                else if(bplx.equals( "3"))
                { //学校班牌
                    timerflag = true;
                    starttimer();
                    //LoadUrl(webpath + XJList[0]);return;
                    LoadUrl(webpath + "html/schoolfile/index.html");return;
                }
            }
        });


    }

    public void starttimer()
    {
        if(timerflag) {
            task = new TimerTask() {
                @Override
                public void run() {
                    if (count % 2 == 0) {
                        getlsjm();
                        getJM();
                    }
                    ReadJM();
                    count++;
                    if (count > 10000) count = 0;
                }
            };
            if (task != null && t != null) {
                t.schedule(task, 1000, 5000);
            }
        }

    }


    //插播列表获取
    public void getlsjm()
    {
        String urlstr = "/bpxt/bpxt/bpxtapi/GetSysDayPlay_json?xjid=1";
        if (bplx.equals("1")) urlstr = "/bpxt/bpxt/bpxtapi/GetSysDayPlay_json?bjid=" + bjid;//2、班级节目
        else if (bplx.equals("2")) urlstr = "/bpxt/bpxt/bpxtapi/GetSysDayPlay_json?njid=" + njid;//2、年级节目
        else if (bplx.equals("3")) urlstr = "/bpxt/bpxt/bpxtapi/GetSysDayPlay_json?xjid=1";//2、校级节目
        else{return;}

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(serverip + urlstr, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                JsonParser parse =new JsonParser();  //创建json解析器
                try {
                    if(response.startsWith("callback("))response = "{\"result\":"+response.substring(9);
                    if(response.endsWith(")"))response = response.substring(0,response.length()-1)+"}";

                    JsonObject json=(JsonObject) parse.parse(response);  //创建jsonObject对象
                    //System.out.println("resultcode:"+json.get("ID").getAsInt());  //将json数据转为为int型的数据
                    //System.out.println("reason:"+json.get("BPIDLST").getAsString());     //将json数据转为为String型的数据
                    JsonArray array=json.get("result").getAsJsonArray();    //得到为json的数组
                    lsjm = new ArrayList<ModleHelp>();
                    for(int i=0;i<array.size();i++){
                        JsonObject subObject=array.get(i).getAsJsonObject();
                        ModleHelp m = new ModleHelp();
                        m.ID = subObject.get("ID").getAsInt();
                        m.SchoolID = subObject.get("SchoolID").getAsInt() ;
                        m.MBLX = subObject.get("MBLX").getAsInt();
                        m.Title = subObject.get("Title").getAsString();
                        m.NRMC  = subObject.get("NRMC").getAsString();
                        m.NRLJ  = subObject.get("NRLJ").getAsString();
                        m.AddTime  = StrToDate(subObject.get("AddTime").getAsString());
                        m.FBBM  = subObject.get("FBBM").getAsString();
                        m.JSSJ  = StrToDate(subObject.get("JSSJ").getAsString());
                        m.KSRQ  = StrToDate(subObject.get("KSRQ").getAsString());
                        m.KSXS  = subObject.get("KSXS").getAsInt();
                        m.KSFZ  = subObject.get("KSFZ").getAsInt();
                        m.JSXS  = subObject.get("JSXS").getAsInt();
                        m.JSFZ  = subObject.get("JSFZ").getAsInt();
                        //m.BPIDLST  = subObject.get("BPIDLST").getAsString();
                        m.BFZC  = subObject.get("BFZC").getAsString();
                        m.DQZT   = subObject.get("DQZT").getAsInt();
                        lsjm.add(m);
                    }
                } catch (JsonIOException e) {
                    e.printStackTrace();
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }

                System.out.println("从服务器读取定时节目成功！");
            }


            @Override
            public void onFailure(Throwable var1, String var2) {
                System.out.println(var1);
            }
        });
    }

    //定时芥末列表获取
    public void getJM()
    {
        String urlstr = "/bpxt/bpxt/bpxtapi/GetDayPlay_json?xjid=1";
        if (bplx.equals("1")) urlstr = "/bpxt/bpxt/bpxtapi/GetDayPlay_json?bjid=" + bjid;//2、班级节目
        else if (bplx.equals("2")) urlstr = "/bpxt/bpxt/bpxtapi/GetDayPlay_json?njid=" + njid;//2、年级节目
        else if (bplx.equals("3")) urlstr = "/bpxt/bpxt/bpxtapi/GetDayPlay_json?xjid=1";//2、校级节目
        else{return;}

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(serverip + urlstr, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                JsonParser parse =new JsonParser();  //创建json解析器
                try {
                    if(response.startsWith("callback("))response = "{\"result\":"+response.substring(9);
                    if(response.endsWith(")"))response = response.substring(0,response.length()-1)+"}";

                    JsonObject json=(JsonObject) parse.parse(response);  //创建jsonObject对象
                    //System.out.println("resultcode:"+json.get("ID").getAsInt());  //将json数据转为为int型的数据
                    //System.out.println("reason:"+json.get("BPIDLST").getAsString());     //将json数据转为为String型的数据
                    JsonArray array=json.get("result").getAsJsonArray();    //得到为json的数组
                    bjjm = new ArrayList<ModleHelp>();
                    for(int i=0;i<array.size();i++){
                        JsonObject subObject=array.get(i).getAsJsonObject();
                        ModleHelp m = new ModleHelp();
                        m.ID = subObject.get("ID").getAsInt();
                        m.SchoolID = subObject.get("SchoolID").getAsInt() ;
                        m.MBLX = subObject.get("MBLX").getAsInt();
                        m.Title = subObject.get("Title").getAsString();
                        m.NRMC  = subObject.get("NRMC").getAsString();
                        m.NRLJ  = subObject.get("NRLJ").getAsString();
                        m.AddTime  = StrToDate(subObject.get("AddTime").getAsString());
                        m.FBBM  = subObject.get("FBBM").getAsString();
                        m.JSSJ  = StrToDate(subObject.get("JSSJ").getAsString());
                        m.KSRQ  = StrToDate(subObject.get("KSRQ").getAsString());
                        m.KSXS  = subObject.get("KSXS").getAsInt();
                        m.KSFZ  = subObject.get("KSFZ").getAsInt();
                        m.JSXS  = subObject.get("JSXS").getAsInt();
                        m.JSFZ  = subObject.get("JSFZ").getAsInt();
                        //m.BPIDLST  = subObject.get("BPIDLST").getAsString();
                        m.BFZC  = subObject.get("BFZC").getAsString();
                        m.DQZT   = subObject.get("DQZT").getAsInt();
                        bjjm.add(m);
                    }
                } catch (JsonIOException e) {
                    e.printStackTrace();
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }

                System.out.println("从服务器读取定时节目成功！");
            }

            @Override
            public void onFailure(Throwable var1, String var2) {
                System.out.println(var1);
            }
        });
    }

    //读取节目
    public void ReadJM()
    {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
               // System.out.println("轮询当前插播节目：共" + lsjm.size() + "个,点播节目：共" + dsjm.size() + "个,");
                //1、强行插播
                if (lsjm.size() > 0) {
                    Date now = new Date(System.currentTimeMillis());
                    for (ModleHelp jm : lsjm) {
                        if (now.getTime() <= jm.JSSJ.getTime() && now.getTime() >= jm.KSRQ.getTime()) {
                            if (CurPageType == 4 && CurPage == jm.MBLX) {
                                return;
                            } else {

                                ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
                                ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                                String atname = cn.getClassName();
                                //DebugLog.d(TAG, "isTopActivity = " + cn.getClassName());
                                if (!atname.contains("MainActivity"))
                                {
                                    Intent myIntent = new Intent();
                                    myIntent = new Intent(MainActivity.this, MainActivity.class);
                                    startActivity(myIntent);
                                }

                                CurPage = jm.MBLX;
                                CurPageType = 4;
                                LoadUrl(webpath + sysUrlList[jm.MBLX] + "?id=" + jm.ID + "&tid=" + CurPageType);

                            }
                            return;
                        }
                    }
                }



                Date now = new Date(System.currentTimeMillis());
                ArrayList<ModleHelp> tempjm = new ArrayList<ModleHelp>();
                int temp_pagetype = 0;
                String[] TempList = null;
                //2、班级节目
                if (bplx.equals("1")) {
                    tempjm = bjjm;
                    temp_pagetype = 1;
                    TempList = BJList;
                }
                //3、年级节目
                if (bplx.equals("2")){
                    tempjm = njjm;
                    temp_pagetype = 2;
                    TempList = NJList;
                }
                //4、校级节目
                if (bplx.equals("3")) {
                    tempjm = xjjm;
                    temp_pagetype = 3;
                    TempList = XJList;
                }

                for (ModleHelp jm : tempjm) {
                    if (now.getTime() <= jm.JSSJ.getTime() && now.getTime() >= jm.KSRQ.getTime()) {
                        if (CurPageType == temp_pagetype && CurPage == jm.MBLX) {
                            return;
                        }
                        else {
                            ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
                            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                            String atname = cn.getClassName();
                            //DebugLog.d(TAG, "isTopActivity = " + cn.getClassName());
                            if (!atname.contains("MainActivity"))
                            {
                                Intent myIntent = new Intent();
                                myIntent = new Intent(MainActivity.this, MainActivity.class);
                                startActivity(myIntent);
                            }

                            CurPage = jm.MBLX;
                            CurPageType = temp_pagetype;

                            LoadUrl(webpath + TempList[jm.MBLX] + "?id=" + jm.ID + "&tid=" + CurPageType);

                        }
                        return;
                    }
                }

                if (CurPage > 0 && CurPageType > 0) {
                    ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
                    ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                    String atname = cn.getClassName();
                    //DebugLog.d(TAG, "isTopActivity = " + cn.getClassName());
                    if (!atname.contains("MainActivity"))
                    {
                        Intent myIntent = new Intent();
                        myIntent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(myIntent);
                    }

                    CurPage = 0;
                    CurPageType = 0;
                    LoadUrl(webpath + TempList[0]);
                }

            }
        });
    }


    @Override
    public void onBackPressed() {
        mWebView.loadUrl("javascript:try{stopplay();}catch(e){}");//停止播放视频
        super.onBackPressed();
    }


    public String getServerValue(String key) {

        if (key.length() > 0) {
            SharedPreferences pres = getSharedPreferences("appsyskey", Context.MODE_APPEND);
            return pres.getString(key, "");
        } else {
            return "";
        }
    }

    private final class ECPJS {

        //开启软件盘
        @JavascriptInterface
        public void checkvesion() {
            if(!UpVessionFlag)
            {
                UpdateVession();
                UpVessionFlag = true;
            }
        }



        @JavascriptInterface
        public void showtoast(String key,String type) {
            try {
                Toast  toast =  Toast.makeText(getApplicationContext(), key, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                LinearLayout toastView = (LinearLayout) toast.getView();
                ImageView imageCodeProject = new ImageView(getApplicationContext());
                if(type.equals("gou"))
                { imageCodeProject.setImageResource(R.drawable.gou); }
                else{  imageCodeProject.setImageResource(R.drawable.cha);}
                toastView.addView(imageCodeProject, 0);
                toast.show();

            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }




        @JavascriptInterface
        public void takephoto(String pcid) {
            try {
                Toast.makeText(getApplicationContext(), "开始拍照!",Toast.LENGTH_SHORT).show();
                 initCamera(pcid);
                //Toast.makeText(getApplicationContext(), "结束拍照!",Toast.LENGTH_SHORT).show();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


        @JavascriptInterface
        public String CheckLocalFile(String url) {
            try {
               return downloadFile(url);
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return url;
        }


        //开启软件盘
        @JavascriptInterface
        public void openkeybord() {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (!imm.isActive()) {
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.RESULT_SHOWN);
            }
        }



        //关闭软键盘
        @JavascriptInterface
        public void closekeybord() throws IOException {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(), 0);
               // Runtime runtime = Runtime.getRuntime();
                //runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
            }

            //InputMethodManager imm = (InputMethodManager)getSystemService(
            //        Context.INPUT_METHOD_SERVICE);
            //imm.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(), 0);
           // ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(),
           //         InputMethodManager.HIDE_NOT_ALWAYS);
           //Toast.makeText(getApplicationContext(), "软件盘已隐藏",Toast.LENGTH_SHORT).show();
        }


        @JavascriptInterface
        public void playmp(String id) {
            try {
                if(id.equals("kcwzd")){mediaPlayer=MediaPlayer.create(MainActivity.this, R.raw.kcwzd);}
                else if(id.equals("bsxs")){mediaPlayer=MediaPlayer.create(MainActivity.this, R.raw.bsxs);}
                else if(id.equals("bdxsk")){mediaPlayer=MediaPlayer.create(MainActivity.this, R.raw.bdxsk);}
                else if(id.equals("kqok")){mediaPlayer=MediaPlayer.create(MainActivity.this, R.raw.kqok);}
                else if(id.equals("bdjsk")){mediaPlayer=MediaPlayer.create(MainActivity.this, R.raw.bdjsk);}
                else if(id.equals("wskxm")){mediaPlayer=MediaPlayer.create(MainActivity.this, R.raw.wskxm);}
                else if(id.equals("bsksj")){mediaPlayer=MediaPlayer.create(MainActivity.this, R.raw.bsksj);}
                else if(id.equals("bdzh")){mediaPlayer=MediaPlayer.create(MainActivity.this, R.raw.bdzh);}
               // mediaPlayer=MediaPlayer.create(MainActivity.this, R.raw.kcwzd);
                mediaPlayer.setLooping(false);//设置循环播放
                mediaPlayer.start();//播放声音
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void playurl(String url) {
            try {

                AssetManager assetManager = getAssets();
                AssetFileDescriptor fileDescriptor = assetManager.openFd("wwwroot/" + url);
                mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),fileDescriptor.getStartOffset(),
                        fileDescriptor.getStartOffset());
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @JavascriptInterface
        public void stopplay() {
            try {
                mediaPlayer.stop();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void playaudio(String url) {
            Intent _intent = new Intent(MainActivity.this,HomeActivity.class);
            //在Intent对象当中添加一个键值对
            _intent.putExtra("url",url);
            startActivity(_intent);

           // Uri uri = Uri.parse(url);
           // //调用系统自带的播放器
           // Intent intent = new Intent(Intent.ACTION_VIEW);
           // intent.setDataAndType(uri, "video/*");
           // startActivity(intent);
        }

        @JavascriptInterface
        public String getxlh() {
                return xlh;
        }

        @JavascriptInterface
        public void setscreen(String key) {
            setajax("fullscreen",key);
            if (key.equals("1")) {
                SmdtManager smdt= SmdtManager.create(getApplicationContext());
                smdt.smdtSetStatusBar(getApplicationContext(),false);
                Toast.makeText(getApplicationContext(), "设置全屏成功!",Toast.LENGTH_SHORT).show();
            } else if(key.equals("0")){
                SmdtManager smdt= SmdtManager.create(getApplicationContext());
                smdt.smdtSetStatusBar(getApplicationContext(),true);
                Toast.makeText(getApplicationContext(), "退出全屏成功!",Toast.LENGTH_SHORT).show();
            }
        }

        @JavascriptInterface
        public void ReStart() {
            Intent intent = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        //设置平板序列号
        @JavascriptInterface
        public void initsystem() {
            InitP();
        }


        @JavascriptInterface
        public String getvalue(String key) {

            if (key.length() > 0) {
                SharedPreferences pres = getSharedPreferences("appsyskey", Context.MODE_APPEND);
                return pres.getString(key, "null");
            } else {
                return "null";
            }
        }

        @JavascriptInterface
        public String setajax(String key, String value) {
            if (key.length() > 0 && value.length() > 0) {
                SharedPreferences pres = getSharedPreferences("appsyskey", Context.MODE_APPEND);
                SharedPreferences.Editor editor = pres.edit();
                editor.putString(key, value);
                editor.commit();
                Toast.makeText(getApplicationContext(), "设置成功，谢谢!",Toast.LENGTH_SHORT).show();
                return pres.getString(key, "null");
            } else {
                Toast.makeText(getApplicationContext(), "请输入有效值，谢谢!",Toast.LENGTH_SHORT).show();
                return "null";
            }

        }



        //设置平板序列号
        @JavascriptInterface
        public String setvalue(String key, String value) {

            if (key.length() > 0 && value.length() > 0) {
                SharedPreferences pres = getSharedPreferences("appsyskey", Context.MODE_APPEND);
                SharedPreferences.Editor editor = pres.edit();
                editor.putString(key, value);
                editor.commit();
                Toast.makeText(getApplicationContext(), "设置成功，谢谢!",Toast.LENGTH_SHORT).show();
                InitP();
                return pres.getString(key, "null");
            } else {
                Toast.makeText(getApplicationContext(), "请输入有效值，谢谢!",Toast.LENGTH_SHORT).show();
                return "null";
            }
        }



        @JavascriptInterface
        public String settoken(String bjid,String jsid) {
            if (bjid.length() > 0) {
                SharedPreferences pres = getSharedPreferences("appsyskey", Context.MODE_APPEND);
                SharedPreferences.Editor editor = pres.edit();
                editor.putString("bjid", bjid);
                editor.putString("jsid", jsid);
                editor.commit();
                Toast.makeText(getApplicationContext(), "设置成功，谢谢!",Toast.LENGTH_SHORT).show();
                InitP();
                return bjid;
            } else {
                Toast.makeText(getApplicationContext(), "请输入有效值，谢谢!",Toast.LENGTH_SHORT).show();
                return "null";
            }
        }


        //获取平板序列号
        @JavascriptInterface
        public String gettoken() {
            SharedPreferences pres = getSharedPreferences("appsyskey", Context.MODE_APPEND);
            return pres.getString("token", "null");
        }

        //设置平板序列号
        @JavascriptInterface
        public String settoken(String token) {
            SharedPreferences pres = getSharedPreferences("appsyskey", Context.MODE_APPEND);
            SharedPreferences.Editor editor = pres.edit();
            editor.putString("token", token);
            editor.commit();
            return token;
        }

        //获取服务器IP
        @JavascriptInterface
        public String getserverip() {
            SharedPreferences pres = getSharedPreferences("appsyskey", Context.MODE_APPEND);
            return pres.getString("serverip", "null");
        }

        //设置服务器IP
        @JavascriptInterface
        public String setserverip(String ip) {
            SharedPreferences pres = getSharedPreferences("appsyskey", Context.MODE_APPEND);
            SharedPreferences.Editor editor = pres.edit();
            editor.putString("serverip", ip);
            editor.commit();
            return ip;
        }
    }



    //字符串转时间
    public static Date StrToDate(String str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String getMD5(String info) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes("UTF-8"));
            byte[] encryption = md5.digest();

            StringBuffer strBuf = new StringBuffer();
            for (int i = 0; i < encryption.length; i++) {
                if (Integer.toHexString(0xff & encryption[i]).length() == 1) {
                    strBuf.append("0").append(Integer.toHexString(0xff & encryption[i]));
                } else {
                    strBuf.append(Integer.toHexString(0xff & encryption[i]));
                }
            }

            return strBuf.toString();
        } catch (Exception e) {
            return "";
        }
    }


    class DemoWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        /**
         * 页面载入完成回调
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            view.loadUrl("javascript:try{autoplay();}catch(e){}");//播放视频
        }
    }

    /**
     * 继承WebChromeClient类
     * 在这个类的3个方法中，分别使用Android的内置控件重写了Js中对应的对话框，就是说对js中的对话框做处理了，就是重写了。
     */
    final class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }





    }



    public boolean fileIsExists(String strFile)
    {
        try
        {
            File f=new File(strFile);
            if(!f.exists())
            {
                return false;
            }

        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }


    public String downloadFile(String downloadUrl) {
        //创建下载任务,downloadUrl就是下载链接
        try {
            File tempFile = new File(downloadUrl.trim());
            String fName = tempFile.getName();

            String SDPATH = getCommonPath();
            File path1 = new File(Environment.getExternalStorageDirectory(),  "mydownload/"+ fName);
            Toast.makeText(getApplicationContext(), "path:"+path1.getAbsolutePath(),Toast.LENGTH_SHORT).show();

            if(path1.exists()) { return "file://" + path1.getAbsolutePath(); }
            else {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
                //指定下载路径和下载文件名

                request.setDestinationInExternalPublicDir("mydownload", fName) ;
                //获取下载管理器
                DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                //将下载任务加入下载队列，否则不会进行下载
                downloadManager.enqueue(request);
            }
        }
        catch (Exception e){}
        return  downloadUrl;
    }

    /**
     * 取得程式指定SDCard文件下载目录
     * 内置sdCard
     * APP公用目录
     */
    public String getCommonPath() {
        //有sd卡
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            // 创建一个文件夹对象，赋值为外部存储器的目录
            File sdcardDir = Environment.getExternalStorageDirectory();
            // 得到一个路径，内容是sdcard的文件夹路径和名字
            String path = sdcardDir.getPath() + "/" + "mydownload";
            File path1 = new File(path);
            if (!path1.exists())
                // 若不存在，创建目录，可以在应用启动的时候创建
                path1.mkdirs();

            return path;
        } else{
            //无SD卡
            return "";
        }

    }















    public  String picid = "";
    public static Lock lockpic = new ReentrantLock();
    //开相机
    private Camera openFacingBackCamera() {
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        ;
        for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }

        return cam;
    }

    private void initSurface()
    {
        if(myCamera == null) {myCamera = openFacingBackCamera();}
    }

    //初始化摄像头
    private void initCamera(String pid) {

        lockpic.lock();
        picid = pid;
        try {
            Date now = new Date(System.currentTimeMillis());
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");//设置日期格式
            String dtstr =   df.format(new Date());
            File dir = new File("/mnt/sdcard/xierui/temppic/" + dtstr );
            if (!dir.exists()) {
                //通过file的mkdirs()方法创建<span style="color:#FF0000;">目录中包含却不存在</span>的文件夹
                boolean xf = dir.mkdirs();
            }
            File file = new File(dir, picid + ".jpg");
            //判断文件夹是否存在，如果不存在就创建，否则不创建
            if (!file.exists()) {
                //如果存在摄像头
                if(myCamera == null)
                {
                    myCamera = openFacingBackCamera();
                }
                if (myCamera != null) {
                    SurfaceView dummy = new SurfaceView(getBaseContext());
                    try {
                        myCamera.setPreviewDisplay(dummy.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    myCamera.startPreview();
                    myCamera.takePicture(null, null,myPicCallback);
                }
            }
        }
        catch (Exception e) { }
        finally {
            lockpic.unlock();
        }
    }

    //拍照成功回调函数
    private Camera.PictureCallback myPicCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            //将得到的照片进行270°旋转，使其竖直
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Matrix matrix = new Matrix();
                matrix.preRotate(0);
                bitmap = Bitmap.createBitmap(bitmap ,0,0, bitmap .getWidth(), bitmap .getHeight(),matrix,true);


                Date now = new Date(System.currentTimeMillis());
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");//设置日期格式
                String dtstr =   df.format(new Date());
                File dir = new File("/mnt/sdcard/xierui/temppic/"+dtstr);
                //判断文件夹是否存在，如果不存在就创建，否则不创建
                if (!dir.exists()) {
                    //Toast.makeText(getApplicationContext(), "文件夹不存在，创建之" ,Toast.LENGTH_SHORT).show();
                    //通过file的mkdirs()方法创建<span style="color:#FF0000;">目录中包含却不存在</span>的文件夹
                    dir.mkdirs();
                }


                String fnamex =   picid + ".jpg";
                File pictureFile = new File(dir, fnamex);

                FileOutputStream fos = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                String pt = pictureFile.getPath();

                //UploadUtil.uploadFile(pictureFile,"http://192.168.1.100/bpxt/bpxt/bpxtapi/UpFiles_img");
                //String imgbase = getImgStr(pictureFile.getPath());
                //String filepath="E:/ziliao/0.jpg";

                //                String urlStr = serverip + "/bpxt/bpxt/bpxtapi/UpFiles_img";
                //                Map<String, String> textMap = new HashMap<String, String>();
                //                textMap.put("picid", picid);
                //                textMap.put("schoolid", schoolid);
                //                Map<String, String> fileMap = new HashMap<String, String>();
                //                fileMap.put("imgurl", pictureFile.getAbsolutePath());
                //                String ret = HttpRequest.formUpload(urlStr, textMap, fileMap);

                // HttpRequest.sendPost("http://192.168.1.100/bpxt/bpxt/bpxtapi/UpFiles_img","userId=" + fnamexb);
                //Toast.makeText(getApplicationContext(), "拍照成功！",Toast.LENGTH_SHORT).show();
                // mWebView.loadUrl("javascript:ReadImage('" + pt + "')");
            } catch (Exception error) {
                Toast.makeText(MainActivity.this, "拍照失败", Toast.LENGTH_SHORT).show();;
                error.printStackTrace();
            }
            finally {
                myCamera.stopPreview();
//                myCamera.release();
//                myCamera = null;

            }

        }
    };


//    //初始化surface
//    @SuppressWarnings("deprecation")
//    private void initSurface()
//    {
//        //初始化surfaceview
//        mySurfaceView = (SurfaceView) findViewById(R.id.camera_surfaceview);
//
//        //初始化surfaceholder
//        myHolder = mySurfaceView.getHolder();
//        myHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//
//    }
//
//
//    public  String picid = "";
//    public static Lock lockpic = new ReentrantLock();
//    //初始化摄像头
//    private void initCamera(String pid) {
//        lockpic.lock();
//        try {
//            picid = pid;
//            //如果存在摄像头
//            if (checkCameraHardware(getApplicationContext())) {
//                //获取摄像头（首选前置，无前置选后置）
//                if (openFacingFrontCamera()) {
//                    //进行对焦
//                    autoFocus();
//                }
//            }
//        }catch (Exception e) { }
//        finally {
//            lockpic.unlock();
//        }
//
//    }
//
//    //对焦并拍照
//    private void autoFocus() {
//
//        try {
//            //因为开启摄像头需要时间，这里让线程睡两秒
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        //自动对焦
//        myCamera.autoFocus(myAutoFocus);
//
//        //对焦后拍照
//        myCamera.takePicture(null, null, myPicCallback);
//    }
//
//
//
//    //判断是否存在摄像头
//    private boolean checkCameraHardware(Context context) {
//
//        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
//            // 设备存在摄像头
//            return true;
//        } else {
//            // 设备不存在摄像头
//            return false;
//        }
//
//    }
//
//    //得到后置摄像头
//    private boolean openFacingFrontCamera() {
//
//        //尝试开启前置摄像头
//        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
//        for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
//            Camera.getCameraInfo(camIdx, cameraInfo);
//            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//                try {
//                    Log.d("Demo", "tryToOpenCamera");
//                    myCamera = Camera.open(camIdx);
//                } catch (RuntimeException e) {
//                    e.printStackTrace();
//                    return false;
//                }
//            }
//        }
//
//        //如果开启前置失败（无前置）则开启后置
//        if (myCamera == null) {
//            for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
//                Camera.getCameraInfo(camIdx, cameraInfo);
//                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
//                    try {
//                        myCamera = Camera.open(camIdx);
//                    } catch (RuntimeException e) {
//                        return false;
//                    }
//                }
//            }
//        }
//
//        try {
//            //这里的myCamera为已经初始化的Camera对象
//            myCamera.setPreviewDisplay(myHolder);
//        } catch (IOException e) {
//            e.printStackTrace();
//            myCamera.stopPreview();
//            myCamera.release();
//            myCamera = null;
//        }
//
//        myCamera.startPreview();
//
//        return true;
//    }
//
//    //自动对焦回调函数(空实现)
//    private Camera.AutoFocusCallback myAutoFocus = new Camera.AutoFocusCallback() {
//        @Override
//        public void onAutoFocus(boolean success, Camera camera) {
//        }
//    };
//
//    //拍照成功回调函数
//    private Camera.PictureCallback myPicCallback = new Camera.PictureCallback() {
//
//        @Override
//        public void onPictureTaken(byte[] data, Camera camera) {
//            //完成拍照后关闭Activity
//            //CameraActivity.this.finish();
//
//            //将得到的照片进行270°旋转，使其竖直
//            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//            Matrix matrix = new Matrix();
//            matrix.preRotate(0);
//            bitmap = Bitmap.createBitmap(bitmap ,0,0, bitmap .getWidth(), bitmap .getHeight(),matrix,true);
//
//
//            Date now = new Date(System.currentTimeMillis());
//            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");//设置日期格式
//            String dtstr =   df.format(new Date());
//            File dir = new File("/mnt/sdcard/xierui/temppic/"+dtstr);
//            //判断文件夹是否存在，如果不存在就创建，否则不创建
//            if (!dir.exists()) {
//                //通过file的mkdirs()方法创建<span style="color:#FF0000;">目录中包含却不存在</span>的文件夹
//                dir.mkdirs();
//            }
//
//            //创建并保存图片文件
//            String fnamex =   picid;
//            File pictureFile = new File(dir, fnamex);
//            try {
//                FileOutputStream fos = new FileOutputStream(pictureFile);
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//                fos.close();
//                String pt = pictureFile.getPath();
//
//                //UploadUtil.uploadFile(pictureFile,"http://192.168.1.100/bpxt/bpxt/bpxtapi/UpFiles_img");
//                //String imgbase = getImgStr(pictureFile.getPath());
//                //String filepath="E:/ziliao/0.jpg";
//
////                String urlStr = serverip + "/bpxt/bpxt/bpxtapi/UpFiles_img";
////                Map<String, String> textMap = new HashMap<String, String>();
////                textMap.put("picid", picid);
////                textMap.put("schoolid", schoolid);
////                Map<String, String> fileMap = new HashMap<String, String>();
////                fileMap.put("imgurl", pictureFile.getAbsolutePath());
////                String ret = HttpRequest.formUpload(urlStr, textMap, fileMap);
//
//
//
//               // HttpRequest.sendPost("http://192.168.1.100/bpxt/bpxt/bpxtapi/UpFiles_img","userId=" + fnamexb);
//                //Toast.makeText(getApplicationContext(), "post上传文件",Toast.LENGTH_SHORT).show();
//                // mWebView.loadUrl("javascript:ReadImage('" + pt + "')");
//            } catch (Exception error) {
//             //   Toast.makeText(MainActivity.this, "拍照失败", Toast.LENGTH_SHORT).show();;
//                error.printStackTrace();
//                myCamera.stopPreview();
//                myCamera.release();
//                myCamera = null;
//            }
//
//           // Toast.makeText(MainActivity.this, "获取照片成功", Toast.LENGTH_SHORT).show();;
//            myCamera.stopPreview();
//            myCamera.release();
//            myCamera = null;
//        }
//    };



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                AlertDialog.Builder build = new AlertDialog.Builder(this);
                build.setTitle("注意")
                        .setMessage("确定要退出吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                finish();
                                System.exit(0);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub

                            }
                        })
                        .show();
                break;

            default:
                break;
        }
        return false;
    }





}