# BetterMiuiExpress

English | [中文](README.zh-Hans.md)

BetterMiuiExpress is an Xposed module that prevents express details to be displayed in third-party apps such as Taobao, CaiNiao, etc. and shows a custom activity to display the details.

**Privacy phone number in Taobao is not supported. If enabled, querying details of packages delivered by SF will fail.**

## Prerequisites

- MIUI based on Android 7.0 or above.
- MIUI Personal Assistant (智能助理) app installed
- LSPosed or other Xposed-compatible frameworks **with XSharedPreferences support**

## Usages

The following instructions are based on LSPosed.

- Make sure that your device has installed LSPosed or other Xposed-compatible frameworks **with XSharedPreferences support**
- Download this module from CoolApk or compile by yourself. **If you are updating from 1.4.6 and below to newer, it is recommended to uninstall the old version first**
- Enable this module and check MIUI Personal Assistant as the scope. You may unchecked the System Apps option in the menu to find it
- The new KuaiDi100 provider does not require customer and key anymore. However, if you want to retrieve express details from legacy **KuaiDi100 (without 'New' tag)**, you still need KuaiDi100 customer and key
- Kill the MIUI Personal Assistant apps manually in LSPosed or Settings
- Swipe to MIUI Personal Assistant in the launcher. Now clicking the express items will not lead to jumping to third-party apps, and the latest express details will be displayed in the app widget

## Supported data provider

- KuaiDi100 (New)
- KuaiDi100: **Customer and key required**
- Cainiao

## For express delivered to CaiNiao Post House

You may need a code (取件码) to get your express from CaiNiao post house. Click the menu in express details activity and you will find an option to jump to third-apps manually.

## Contribution

You can submit issues or make merge requests to contribute. **But do not submit meaningless issues, or they will be deleted.**

## License

BetterMiuiExpress is licensed under the GPLv3. For details, check the license in the repository.

## Acknowledgement

- BetterMiuiExpress is now based on [YukiHookAPI](https://github.com/fankes/YukiHookAPI).
- @YifePlayte for CaiNiao API usages
