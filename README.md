# BetterMiuiExpress

English | [中文](https://coolapk.com/apk/com.moefactory.bettermiuiexpress)

BetterMiuiExpress is an Xposed module that prevents express details to be showed in third-party apps such as Taobao, CaiNiao, etc. and shows a custom activity to display the details.

## Prerequisites

- MIUI based on Android 7.0 or above (Tested on Mi 11 Ultra with MIUI 12.5.11 based on Android 11).

- MIUI Personal Assistant (智能助理) app installed

## Usages

The following instructions are based on LSPosed.

- Make sure that your device has installer LSPosed or other Xposed-compatible frameworks
- Download this module from CoolApk or compile by yourself
- Enable this module and check MIUI Personal Assistant as the scope. You may unchecked the System Apps option in the menu to find it
- Kill the MIUI Personal Assistant apps manually in LSPosed or Settings
- Swipe to MIUI Personal Assistant from the launcher. Now clicking the express items will not lead to jumping to third-party apps
- Starting from 1.1.0, you can view the latest express detail in launcher
- Starting from 1.2.0, this module supports the latest Personal Assistant

## For express delivered to CaiNiao Post House

You may need a code (取件码) to get your express from CaiNiao post house. Click the menu in express details activity and you will find an option to jump to third-apps manually.

## Contribution

You can submit issues or make merge requests to contribute. **But do not submit meaningless issues, or they will be deleted.**

## License

BetterMiuiExpress is licensed under the GPLv3. For details, check the license in the repository.