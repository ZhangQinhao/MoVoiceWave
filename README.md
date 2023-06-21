# MoSubtitleView

![enter description here][1]  
![enter description here][2]  
![enter description here][3]  
![enter description here][4]  
![enter description here][5]  
![enter description here][6]  
![enter description here][7]  
![enter description here][8]

``` stylus
<com.monke.movoicewavelib.MoVisualizerView
        android:id="@+id/movv"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="5dp"
        android:padding="3dp" />
        
        moVisualizerView.setUiType(0);  //设置样式  0~7
        moVisualizerView.updateDataWithAnim(fft, 1);  //更新数据   byte[]数组,数据取值密度
        mVisualizer.release();  //onDestory
```


具体用法参照App代码

  


  [1]: ./images/1.gif "1.gif"
  [2]: ./images/2.gif "2.gif"
  [3]: ./images/3.gif "3.gif"
  [4]: ./images/4.gif "4.gif"
  [5]: ./images/5.gif "5.gif"
  [6]: ./images/6.gif "6.gif"
  [7]: ./images/7.gif "7.gif"
