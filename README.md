# MyPullToRefreshTest

这里放一些练习的Demo，记录它们关键的知识点以便回忆。

## 1.下拉刷新

- ViewGroup具有四种状态：

  完成、继续下拉以刷新、释放以刷新、正在刷新，维护上一个状态和当前状态，如果状态不一样，会对header显示进行更新。状态转移图如下

![StatusChange.png](https://github.com/chengkun123/MyPullToRefreshTest/raw/master/ScreenShots/StatusChange.png?raw=true)

~~~java
    private int mCurrentStatus = STATUS_REFRESH_FINISHED;
    private int mLastStatus = mCurrentStatus;
    public static final int STATUS_PULL_TO_REFRESH = 0;
    public static final int STATUS_RELEASE_TO_REFRESH = 1;
    public static final int STATUS_REFRESHING = 2;
    public static final int STATUS_REFRESH_FINISHED = 3;
~~~

- 在onTouch中利用header的`setLayoutparams()`方法形成滑动。


- 效果图：

![comenstration.gif](https://github.com/chengkun123/MyPullToRefreshTest/blob/master/ScreenShots/comenstration.gif?raw=true)

- 仿美团下拉刷新效果

  - 通过设置**可见性**在不同阶段显示不同的View。
  - 根据下拉距离**改变Canvas大小实现缩放**效果。
  - 把帧动画设为背景，**播放帧动画**。

  ![meituan.gif](https://github.com/chengkun123/MyPullToRefreshTest/blob/master/ScreenShots/meituan.gif?raw=true)

- 仿京东下拉效果

  - 第一阶段的View，由于两个Bitmap的**缩放中心不一样**，使用`Canvas#save()`和`Canvas#restore()` 把两个Bitmap进行分别映射。
  - 第二阶段的View播放无限循环帧动画。

  ![jingdong.gif](https://github.com/chengkun123/MyPullToRefreshTest/blob/master/ScreenShots/jingdong.gif?raw=true)

- 仿百度外卖下拉效果

  - 对header中的view执行动画达到效果

  - 在xml中编写的动画，set中默认自带如下属性，并且set中对interpolator的设置优先级高于单独动画对interpolator的设置。

    ~~~xml
    android:interpolator="@android:anim/accelerate_interpolator"
    android:shareInterpolator="true"
    ~~~

    ​

  ![baidu.gif](https://github.com/chengkun123/MyPullToRefreshTest/blob/master/ScreenShots/baidu.gif?raw=true)

