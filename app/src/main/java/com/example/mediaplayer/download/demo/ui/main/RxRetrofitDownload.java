package com.example.mediaplayer.download.demo.ui.main;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * @author xiezeqing
 * @date 7/17/2019
 * @email xiezeqing@hikcreate.com
 */
public interface RxRetrofitDownload {
    @Streaming
    @GET()
    Observable<ResponseBody> downloadFileUrl(@Url String fileUrl);
}
