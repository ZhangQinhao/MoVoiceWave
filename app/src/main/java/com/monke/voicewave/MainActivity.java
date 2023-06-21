package com.monke.voicewave;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.monke.mopermission.MoPermission;
import com.monke.mopermission.OnRequestNecessaryPermissionListener;
import com.monke.movoicewavelib.MoVisualizerView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWave();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private MediaPlayer mPlayer;
    private Visualizer mVisualizer;
    private MoVisualizerView moVisualizerView;

    private void initWave() {
        moVisualizerView = findViewById(R.id.movv);
        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoPermission.Companion.requestNecessaryPermission(MainActivity.this, "获取Record_audio权限", new OnRequestNecessaryPermissionListener() {
                    @Override
                    public void success(List<String> permissions) {
                        initPlayer();
                        if (mPlayer != null) {
                            mPlayer.start();
                            mVisualizer.setEnabled(true);
                        }
                    }

                    @Override
                    public void fail(List<String> permissions) {
                        showToast("请获取Record_audio权限");
                    }
                }, Manifest.permission.RECORD_AUDIO);

            }
        });
        findViewById(R.id.btn_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer != null) {
                    mPlayer.pause();
                    mVisualizer.setEnabled(false);
                }
            }
        });
        findViewById(R.id.btn_release).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVisualizer != null) {
                    mVisualizer.release();
                    mVisualizer = null;
                }
                if (mPlayer != null) {
                    mPlayer.pause();
                }
                moVisualizerView.release();
            }
        });
        findViewById(R.id.btn_type_0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moVisualizerView.setUiType(0);
            }
        });
        findViewById(R.id.btn_type_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moVisualizerView.setUiType(1);
            }
        });
        findViewById(R.id.btn_type_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moVisualizerView.setUiType(2);
            }
        });
        findViewById(R.id.btn_type_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moVisualizerView.setUiType(3);
            }
        });
        findViewById(R.id.btn_type_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moVisualizerView.setUiType(4);
            }
        });
        findViewById(R.id.btn_type_5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moVisualizerView.setUiType(5);
            }
        });
        findViewById(R.id.btn_type_6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moVisualizerView.setUiType(6);
            }
        });
        findViewById(R.id.btn_type_7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moVisualizerView.setUiType(7);
            }
        });
        moVisualizerView.setInitData(1, 10);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initPlayer()  {
        if (mPlayer == null) {
            mPlayer = MediaPlayer.create(this,R.raw.mojito);
//            try {
//////                mPlayer.reset();
//////                mPlayer.setDataSource();
//////                mPlayer.prepare();
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
        }
        if (mVisualizer == null) {
            mVisualizer = new Visualizer(mPlayer.getAudioSessionId());
            final int captureSize = Visualizer.getCaptureSizeRange()[1];
            mVisualizer.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mVisualizer.setMeasurementMode(Visualizer.MEASUREMENT_MODE_PEAK_RMS);
            }
            mVisualizer.setCaptureSize(captureSize);
            mVisualizer.setDataCaptureListener(
                    new Visualizer.OnDataCaptureListener() {

                        //这个回调应该采集的是快速傅里叶变换有关的数据
                        @Override
                        public void onFftDataCapture(Visualizer visualizer,
                                                     byte[] fft, int samplingRate) {
                            if (mPlayer != null && mVisualizer != null) {
                                moVisualizerView.updateDataWithAnim(fft, 1);
                            }
                        }

                        //这个回调应该采集的是波形数据
                        @Override
                        public void onWaveFormDataCapture(Visualizer visualizer,
                                                          byte[] waveform, int samplingRate) {
                            // 用waveform波形数据更新mVisualizerView组件
//                            moVisualizerView.updateDataWithAnim(waveform);
                        }
                    }, Visualizer.getMaxCaptureRate() / 4, false, true);
        }
    }

    private void showToast(String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.release();
        }
        if (mVisualizer != null) {
            mVisualizer.release();
        }
    }
}