[编辑中...]

Unity3D是一款优秀的跨平台3D游戏引擎和开发工具，它可以使用C#作为游戏脚本进行开发，大大提高游戏开发效率。但是其脚本开发还是略显复杂，特别是移动平台上（Android，iOS）的游戏开发，u3dext就提供一些功能扩展的代码，你可以通过继承实现这些扩展来大大简化你的脚本。

# 主要特性 #
  * C#语言编写（不支持Javascript）。
  * 简化和加强了的鼠标和触摸操作处理。
  * 提供了触摸屏放大缩小（Zoom In, Zoom Out)操作事件探测。
  * 提供了游戏物体点击触摸事件探测。
  * 菜单的创建与控制
  * 关卡的控制。
  * GUI上现实调试信息。
  * 一些工具方法。

# 使用方法 #

u3dext最主要的一个脚本类是`BaseMonoBehaviour.cs`，它提供的大部分要扩展的功能，在Unity3D编辑器中创建你自己的脚本类，并把他继承的父类从`MonoBehaviour`改为`BaseMonoBehaviour`:

```
public class MyScript : BaseMonoBehaviour{

}
```

## 鼠标事件处理 ##
Unity的API没有提供处理鼠标事件的方法，需要通过Input这个类来检测，因此u3dext提供了鼠标事件处理方法：OnScreenMouseDown(), OnScreenMouseOver(), OnScreenMouseUp()分别在鼠标键按下，移动，抬起的时候触发。


## 触摸事件处理 ##
同样，Unity也没有提供触摸屏事件处理机制，而在移动平台上触摸屏已经是主流，因此，u3dext提供了触摸事件处理：OnTouchDown(),OnTouchMove(),OnTouchUp()分别在手指触摸，移动，抬起时触发。


## 触摸屏放大缩小操作 ##
双指同时触摸屏幕并往相反方向移动即可触发OnZoomInAndOut()事件，通常用于放大缩小游戏场景。

## 游戏物体触摸 ##
当鼠标点击或者手指触摸屏幕某一点时，屏幕上该点所投射到的游戏物理世界中的物体相应的事件也被触发：OnGameObjectHitDown(),OnGameObjectHitUp()分别在按下，抬起的时候触发。

## 创建菜单 ##

## 关卡控制 ##

## 其他 ##