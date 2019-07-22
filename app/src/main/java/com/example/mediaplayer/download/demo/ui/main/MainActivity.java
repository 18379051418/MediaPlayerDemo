package com.example.mediaplayer.download.demo.ui.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.example.mediaplayer.R;
import com.example.mediaplayer.TestUtil;
import com.example.mediaplayer.Util.RequestPermission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity{

    private RxRetrofitDownload service;
    private static final String URL = "big_buck_bunny.mp4";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://clips.vorwaerts-gmbh.de/")
                .build();

        service = retrofit.create(RxRetrofitDownload.class);

        if(RequestPermission.checkPermission(this)) {
            beginDownload(URL);
        }else{
            RequestPermission.requestPermission(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 0){
            for(int result : grantResults){
                if(result == PackageManager.PERMISSION_DENIED)return;
            }
            beginDownload(URL);
        }
    }

    private void beginDownload(String url){
        try{
            Disposable d = service.downloadFileUrl(url)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(n-> new Thread(new FileDownloadRun(n)).start(), e->Log.e("测试", "", e));
        }catch (Exception e){
            Log.e("测试", "", e);
        }
    }

    private class FileDownloadRun implements Runnable{
        ResponseBody responseBody;
        public FileDownloadRun(ResponseBody responseBody){
            this.responseBody = responseBody;
        }


        @Override
        public void run() {
            writeResponseBodyToDisk(responseBody, new DownloadListener() {
                @Override
                public void onStart() {
                    TestUtil.logd("start");
                }

                @Override
                public void onProgress(int p) {
                    TestUtil.logd("progress" + p);
                }

                @Override
                public void onFinish(String path) {
                    TestUtil.logd("finish");
                }

                @Override
                public void onError(String msg) {
                    Log.e("测试", msg, null);
                }
            });
        }
    }

    private void writeResponseBodyToDisk(ResponseBody responseBody, DownloadListener downloadListener){
        downloadListener.onStart();
        try{
            File file = new File(Environment.getExternalStorageDirectory(),"test");
            if(file.exists())
                file.delete();
            InputStream in = null;
            OutputStream out= null;

            try{
                byte[] fileReader = new byte[4096];

                long fileSize = responseBody.contentLength();
                long fileSizeDownloaded = 0;

                in = responseBody.byteStream();
                out = new FileOutputStream(file);

                while (true){
                    int read = in.read(fileReader);

                    if(read == -1)break;

                    out.write(fileReader,0,read);

                    fileSizeDownloaded += read;

                    downloadListener.onProgress((int) (100*fileSizeDownloaded / fileSize));
                }

                downloadListener.onFinish(file.getPath());
                TestUtil.logd("file:" + file.getPath());
                out.flush();
            }catch (Exception e){
                downloadListener.onError("测试\n" + e.getMessage());
            }finally {
                if(in != null) in.close();
                if(out != null) out.close();
            }
        }catch (Exception e){
            downloadListener.onError("测试\n" + e.toString());
        }
    }
}
