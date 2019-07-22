package com.example.mediaplayer.download.demo.ui.main;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Trace;
import android.view.Surface;

import androidx.annotation.RequiresApi;

import com.example.mediaplayer.TestUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author xiezeqing
 * @date 7/17/2019
 * @email xiezeqing@hikcreate.com
 */
public class MediaCodecUtils {
    private static final String TAG = "MediaCodecUtils";
    final int TIMEOUT_USER = 1000*10;
    private boolean isPlaying = false;
    private Surface surface;

    private String mediaPath;
    private MediaCallBack callBack;

    //处理音频通道
    private class VideoThread extends Thread{
        private boolean isVideoOver = false;
        int frameIndex = 0;

        @Override
        public void run() {
            try{
                MediaExtractor videoExtractor = new MediaExtractor();
                MediaCodec mediaCodec = null;
                videoExtractor.setDataSource(mediaPath);

                //获得视频所在的轨道
                int trackIndex = getMediaTrackIndex(videoExtractor,"video/");
                if(trackIndex > 0){
                    MediaFormat format = videoExtractor.getTrackFormat(trackIndex);
                    //指定解码后的帧格式
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatRGBFlexible);
                    }

                    String mimeType = format.getString(MediaFormat.KEY_MIME);
                    int width = format.getInteger(MediaFormat.KEY_WIDTH);
                    int height = format.getInteger(MediaFormat.KEY_HEIGHT);

                    long duration = format.getLong(MediaFormat.KEY_DURATION);
                    if(callBack != null){
                        callBack.getMediaBaseMsg(width,height,duration);
                    }

                    //切换到视频信道
                    videoExtractor.selectTrack(trackIndex);
                    //创建解码视频的MediaCodec,解码器
                    mediaCodec = MediaCodec.createDecoderByType(mimeType);
                    //配置绑定surface
                    mediaCodec.configure(format,surface,null,0);
                }

                if(mediaCodec == null){
                    return;
                }
                mediaCodec.start();

                //开始循环，一直到视频资源结束
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                ByteBuffer[] inBuf = mediaCodec.getInputBuffers();//用来存放媒体文件的数据
                ByteBuffer[] outBuf = mediaCodec.getOutputBuffers();//解码后的数据
                long startTime = System.currentTimeMillis();
                //当前Thread没有被中断
                while(!Thread.interrupted()){
                    if(!isPlaying)continue;

                    if(!isVideoOver){
                        //视频没有结束，提取一个单位的视频资源放到解码器缓冲区
                        isVideoOver =putBufferToMediaCodec(videoExtractor,mediaCodec,inBuf);
                    }

                    //返回一个被成功解码的buffer的index或是一个信息 同时更新videoBufferInfo的数据
                    int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,TIMEOUT_USER);
                    switch (outputBufferIndex){
                        case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                            TestUtil.logd("format changed");
                            break;
                        case MediaCodec.INFO_TRY_AGAIN_LATER:
                            TestUtil.logd("超时");
                            break;
                        case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                            TestUtil.logd("output buffers changed");
                            break;
                        default:
                            //直接渲染到Surface时使用不到OutputBuffer
                            //ByteBuffer outputBuffer = outBuf[outputBufferIndex];
                            //延时操作
                            //如果缓冲区里的可展示时间 > 当前视频播放的总时间，就休眠一下，展示当前的帧
                            sleepRender(bufferInfo,startTime);

                            //渲染为true时就会渲染到surface
                            mediaCodec.releaseOutputBuffer(outputBufferIndex,true);
                            frameIndex ++;
                            break;
                    }

                    if((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0){
                        TestUtil.logd("buffer stream end");
                        break;
                    }
                }

                mediaCodec.stop();
                mediaCodec.release();
                videoExtractor.release();

            }catch (IOException e){
                TestUtil.loge("视频源出现问题",e);
            }
        }
    }

    private interface MediaCallBack{
        void getMediaBaseMsg(int width,int height, long duration);
    }



    //获取指定类型媒体文件所在轨道
    private int getMediaTrackIndex(MediaExtractor videoExtractor, String MEDIA_TYPE){
        int trackIndex = -1;
        //获得轨道数量
        int trackNum = videoExtractor.getTrackCount();

        for(int i = 0; i < trackNum ;i++){
            MediaFormat mediaFormat = videoExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if(mime.startsWith(MEDIA_TYPE)){
                trackIndex = 1;
                break;
            }
        }
        return trackIndex;
    }

    //将缓冲区传递至解码器，如果到了文件末尾返回true，否则返回false
    private boolean putBufferToMediaCodec(MediaExtractor extractor, MediaCodec decoder,
                                          ByteBuffer[] inBufs){
        boolean isMediaEOS = false;
        //解码器，要填充有效数据的输入缓冲区的索引 ------ 此id的缓冲区可以被使用
        int inputBufferIndex = decoder.dequeueInputBuffer(TIMEOUT_USER);
        if(inputBufferIndex >= 0){
            ByteBuffer inputBuffer = inBufs[inputBufferIndex];
            //MediaExtractor读取媒体文件的数据，存储到缓冲区中。并返回大小。结束为1
            int sampleSize = extractor.readSampleData(inputBuffer,0);
            if(sampleSize < 0){
                decoder.queueInputBuffer(inputBufferIndex,
                        0,0,0,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                isMediaEOS = true;
                TestUtil.logd("media eos");
            }else {
                //在输入缓冲区添加数据之后，把它告诉MediaCodec（解码）
                decoder.queueInputBuffer(inputBufferIndex,0,sampleSize,
                        extractor.getSampleTime(),0);
                //MediaExtractor 准备下一个单位的数据
                boolean ad = extractor.advance();
                if(!ad){
                    isMediaEOS = false;
                }
            }
        }else{
            //缓冲区不可用
        }
        return isMediaEOS;
    }

    private void sleepRender(MediaCodec.BufferInfo audioUfferInfo,long startMs){
        //这里的时间是毫秒 presentationTimeUs 的时间是累加的 以微秒进行一帧一帧的累加
        //audioBufferInfo 是改变的
        while (audioUfferInfo.presentationTimeUs / 1000 > System.currentTimeMillis()){
            try{
                //10毫秒
                Thread.sleep(10);
            }catch (InterruptedException e){
                TestUtil.loge("超时",e);
                break;
            }
        }
    }
}
