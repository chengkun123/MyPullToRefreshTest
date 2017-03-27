# MyPullToRefreshTest

下拉刷新Demo，熟悉了自定义ViewGroup。

- ViewGroup具有四种状态：

  完成、继续下拉以刷新、释放以刷新、正在刷新，维护上一个状态和当前状态，如果状态不一样，会对header显示进行更新。

~~~java
    private int mCurrentStatus = STATUS_REFRESH_FINISHED;
    private int mLastStatus = mCurrentStatus;
    public static final int STATUS_PULL_TO_REFRESH = 0;
    public static final int STATUS_RELEASE_TO_REFRESH = 1;
    public static final int STATUS_REFRESHING = 2;
    public static final int STATUS_REFRESH_FINISHED = 3;
~~~

- 在构造器中添加header布局，并在onLayout()中布局header和ListView，让header移出该ViewGroup。
- 在onTouch()中首先判断是否可以下拉，判断的条件是：数据的第0条是否是显示在ListView的第一个item中、该item上边界是否与ViewGroup重合。

~~~java
View firstChild = mListView.getChildAt(0);
        if(firstChild != null){
            int firstVisiblePos = mListView.getFirstVisiblePosition();
            if(firstVisiblePos == 0 && firstChild.getTop() == 0){
~~~

- 如果可以下拉，在onTouch()的ACTION_MOVE中下滑header形成下拉效果同时更新状态且更新header，ACTION_UP中根据不同的状态进行不同的动作（回滚回初始态或者刷新）。状态转移图如下：

![StatusChange.png](https://github.com/chengkun123/MyPullToRefreshTest/blob/master/ScreenShots/StatusChange.png?raw=true)

- 效果图：

![comenstration.gif](https://github.com/chengkun123/MyPullToRefreshTest/blob/master/ScreenShots/comenstration.gif?raw=true)

- TO-DO:

  尝试不同的header效果！