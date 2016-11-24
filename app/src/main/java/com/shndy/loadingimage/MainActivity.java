package com.shndy.loadingimage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText editText;
    private ImageView imageView;
    private static final int SUCCEED = 215;
    private static final int ERROR = 392;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCEED:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    imageView.setImageBitmap(bitmap);
                    break;
                case ERROR:
                    Toast.makeText(MainActivity.this, "获取图片失败", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        editText = (EditText) findViewById(R.id.editText);
        imageView = (ImageView) findViewById(R.id.imageView);
    }

    public void onClick(View view) {

        //切换url 选择任意一行注释掉
//        final String path = editText.getText().toString().trim();
        final String path = "http://img3.duitang.com/uploads/item/201409/15/20140915140123_WEH4N.png";

        final File file = new File(getCacheDir(), GetFileName(path));
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            Message obtain = Message.obtain();
            obtain.obj = bitmap;
            obtain.what = SUCCEED;
            handler.sendMessage(obtain);
        } else {
            if (TextUtils.isEmpty(path)) {
                Toast.makeText(this, "请输入图片的网址", Toast.LENGTH_SHORT).show();
                return;
            }
            new Thread() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(path);
                        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                        //GET请求
                        httpURLConnection.setRequestMethod("GET");
                        //连接超时等待时间
                        httpURLConnection.setConnectTimeout(4000);
                        //网络读取超时时间
                        httpURLConnection.setReadTimeout(4000);

                        String contentType = httpURLConnection.getContentType();
                        Log.i("ContentType:", contentType);
                        int responseCode = httpURLConnection.getResponseCode();
                        if (responseCode == 200) {
                            InputStream inputStream = httpURLConnection.getInputStream();

                            byte[] bytes = new byte[1024];

                            int length;

                            FileOutputStream fileOutputStream = new FileOutputStream(file);

                            while ((length = inputStream.read(bytes)) != -1) {
                                fileOutputStream.write(bytes, 0, length);
                            }

                            fileOutputStream.close();

                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                            //定义一个Message
                            Message message = Message.obtain();
                            //设置响应码
                            message.what = SUCCEED;
                            //设置object
                            message.obj = bitmap;
                            handler.sendMessage(message);
                            inputStream.close();
                        } else {
                            handler.sendEmptyMessage(ERROR);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    /**
     * http://img3.duitang.com/uploads/item/201409/15/20140915140123_WEH4N.png
     *
     * @return
     */
    private String GetFileName(String url) {
        int i = url.lastIndexOf("/");
        String substring = url.substring(i + 1);
        return substring;
    }

}
