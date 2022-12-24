# BetterMiuiExpress

[English](README.md) | 中文

BetterMiuiExpress 用于禁止 MIUI 负一屏快递跳转到淘宝、菜鸟等第三方应用，同时使用一个自定义的界面来显示快递详情。

**不支持淘宝的隐私手机号功能。如果顺丰快递的收件人信息使用了隐私手机号，将无法查询到快递详情。**

## 系统要求

- 基于 Android 7.0 或更高版本的 MIUI
- 装有 MIUI 智能助理应用
- **支持 XSharedPreferences** 的 Xposed 框架，如 LSPosed

## 用法

以下用法基于 LSPosed。

- 确保已经安装 LSPosed 或其他**支持 XSharedPreferences** 的 Xposed 框架
- 下载或自行编译模块
- 在 LSPosed 管理器中启用模块，并在作用域中勾选“智能助理”。可能需要在右上角的菜单中取消勾选“系统应用”
- 如需使用快递 100 数据源，需要申请 API 得到 customer 和 key
- 通过 LSPosed 管理器或系统设置强制停止“智能助理”运行
- 在桌面上滑到负一屏，现在点击快递卡片不会再跳转到第三方应用，同时可以在卡片中看到最新的快递动态

## 需要取件码的快递

如果有需要取件码的快递，点击快递详情页面右上角的菜单，选择“转到第三方应用”即可。

## 参与本项目

欢迎提交 Issue 或者 PR。**请不要提交无意义的 Issue，否则会被删除。**

## 授权协议

BetterMiuiExpress 基于 GPLv3 开源，详细的协议内容在 Repository 内可以查看.

## 鸣谢

- BetterMiuiExpress 现在基于 [YukiHookAPI](https://github.com/fankes/YukiHookAPI) 开发
- @YifePlayte 对于菜鸟裹裹 API 调用的帮助
