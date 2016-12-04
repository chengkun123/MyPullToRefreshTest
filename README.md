# MyPullToRefreshTest

下拉刷新Demo，熟悉了自定义组合控件和View的绘制流程、AsyncTask用法和原理、观察者模式等。

- 构造器中动态加载header布局。
- onLayout()中布局header和ListView。
- 为组合控件中的ListView添加OnTouchListener，该组合控件为监听者，在滑动时处理具体逻辑。
- 滑动时下拉点的判断是重点。
- 一共分为静止（刷新完成）、继续下拉可刷新、释放即可刷新、刷新四个状态。

![418788027516658631](E:\笔记笔记笔记笔记笔记\所有图片\418788027516658631.jpg)

- 在刷新状态调用onRefresh()进行具体的耗时操作。