![http://www.blogjava.net/images/blogjava_net/allenny/logo_64.png](http://www.blogjava.net/images/blogjava_net/allenny/logo_64.png)

### AFW是一款免费的Android手机短信防火墙程序，用于拦截垃圾短信。支持短信号码全部及部分匹配；支持系统预制黑名单和用户自定义黑名单。运行在Android 2.1以上版本。 ###

![http://www.blogjava.net/images/blogjava_net/allenny/main_my_sms.png](http://www.blogjava.net/images/blogjava_net/allenny/main_my_sms.png)
![http://www.blogjava.net/images/blogjava_net/allenny/main_blocked.png](http://www.blogjava.net/images/blogjava_net/allenny/main_blocked.png)
![http://www.blogjava.net/images/blogjava_net/allenny/add_to_black_list.png](http://www.blogjava.net/images/blogjava_net/allenny/add_to_black_list.png)
![http://www.blogjava.net/images/blogjava_net/allenny/setting.png](http://www.blogjava.net/images/blogjava_net/allenny/setting.png)

# 功能特性： #
  1. 多种拦截方式：可以匹配完整的短信号码，也可以匹配短信号码的一部分（前缀）
  1. 预制的黑名单列表：常见的自定义黑名单往往效果不佳，因为发垃圾短信的会经常更换号码，我会帮你收集垃圾短信号码制成系统黑名单，系统预制黑名单数量越大，拦截命中率就越高（目前数量还很小，将会持续更新）。
  1. 用户自定义黑名单列表：如果你收到的垃圾短信号码不在预制黑名单中，你可以自己添加黑名单。匹配方式同上。
  1. 被拦截的短信会被删除，为避免可能的误杀，所有删除的短信都在AFW中记录，以便恢复（除非在AFW中清除）。
  1. 可以设置系统启动时自动开始拦截服务。
注意：目前只能用于中国大陆地区。

# 将要实现的功能： #
  1. 用户白名单（包括用户联系人及任何自添加的号码）
  1. 用户之间共享黑名单。

# 安装使用方法： #
  1. 下载最新版的APK安装包：https://smart-dev.googlecode.com/files/AFW_1.0_release.apk
  1. 将安装包通过USB线或者其他可行的方法上传到您的手机上。
  1. 用手机中的文件浏览器打开上传的APK安装包进行安装。
  1. 安装完成启动后，进入设置界面，点击“启动拦截服务“，AFW将启动为后台服务按照黑名单规则进行短信拦截。（重启手机需要重新启动服务）

# 操作方法： #
> ### 添加号码至用户黑名单： ###
在“我接收的短信”中找到需要拦截其短信的号码，单击它，然后选择”将号码添加至黑名单“，出现对话框后选择匹配是全部匹配还是部分匹配短信号码，确定即可完成添加。

> ### 从用户黑名单中删除号码 ###
Menu -> 设置 -> 用户黑名单，点击要删除的号码，弹出菜单时选择“删除”即可。

> ### 恢复误拦截的短信： ###
在“已拦截短信“中找到误拦截的短信，单击它，选择”恢复“即可将被删除的短信恢复到短信收件箱内。

> ### 导入系统黑名单： ###
当下载到更新文件后，将文件放置与SD卡的根目录下（/sdcard/），进入”设置“界面，按“Menu“键，选择”导入黑名单“确认后导入数据至系统黑名单。



# 版本更新 #
  * 1.0 release: 2012-8-11
    * “显示短信内容”对话框增加了快捷操作按钮，简化了操作流程；
    * 修复了手动关闭后台服务后无法再次启动的问题；
    * 修复了“添加号码至黑名单”后没有删除所有该号码的短信问题；
    * 更新了默认系统黑名单；
    * 做了几处界面友好性的提升。
  * 1.0 RC: 2012-7-31
    * 将来自同一号码的所有短信合并显示为一项；
    * 开机时启动后台拦截服务；
    * 修复了一些小bug
  * 1.0 beta: 2012-7-20
    * 初始测试版本