# BetterMiuiExpress

BetterMiuiExpress 用于禁止 MIUI/HyperOS 的快递小部件跳转到淘宝、菜鸟等第三方应用，同时使用一个自定义的界面来显示快递详情。

同时对于新版本智能助理中，京东快递会跳转京东 app 的场景进行了屏蔽，恢复原有跳转行为。

**如果顺丰快递的收件人信息使用了隐私手机号，无法保证一定能查询到快递详情。**

## 系统要求

- 基于 Android 7.0 或更高版本的 MIUI/HyperOS
- 装有 MIUI/HyperOS 的智能助理 app
- **支持 XSharedPreferences** 的 Xposed 框架，如 LSPosed

## 用法

以下用法基于 LSPosed。

- 确保已经安装 LSPosed 或其他**支持 XSharedPreferences** 的 Xposed 框架
- 下载或自行编译模块。**如果从 1.6.0 或更低的版本升级，建议卸载旧版本后再安装**
- 在 LSPosed 管理器中启用模块，并在作用域中勾选“智能助理”。可能需要在右上角的菜单中取消勾选“系统应用”
- **首次使用前，请先进入模块主界面，等待提示“初始化完成”**
- 通过 LSPosed 管理器或系统设置强制停止“智能助理”运行
- 现在点击负一屏或桌面上的快递小部件，将不会再跳转到第三方应用，同时可以在卡片中看到最新的快递动态

## 需要取件码的快递

如果有需要取件码的快递，点击快递详情页面右上角的菜单，选择“转到第三方应用”即可。

## 参与本项目

欢迎提交 issue 或者 PR。**请不要提交无意义的 issue，否则会被删除。**

## 授权协议

BetterMiuiExpress 基于 GPLv3 开源，详细的协议内容在 Repository 内可以查看.

## 鸣谢

- BetterMiuiExpress 现在基于 [YukiHookAPI](https://github.com/fankes/YukiHookAPI) 开发
- @YifePlayte 对于老版本中调用菜鸟裹裹 API 的帮助
- @dreamy06 对于极兔快递跳转的修改
- @wlt233 对于新的 API 使用的帮助
