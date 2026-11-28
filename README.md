# 🚀 Battery Plus — A clean, zero-overhead utility for real-time battery hardware metrics

![Battery Plus](Screenshot.png?raw=true)

[![](https://img.shields.io/badge/Battery%20Plus-v0.7-green)](https://play.google.com/store/apps/details?id=in.sunilpaulmathew.batteryplus)
![](https://img.shields.io/github/languages/top/sunilpaulmathew/BatteryPlus)
![](https://img.shields.io/github/contributors/sunilpaulmathew/BatteryPlus)
![](https://img.shields.io/github/license/sunilpaulmathew/BatteryPlus)

Battery Plus is a lightweight, privacy-first Android utility designed to give you deep, real-time insights into your device's battery performance. Built with a native custom rendering engine and zero external charting dependencies, it tracks everything from instantaneous power draw to long-term capacity health without draining your battery.

---

## ✨ Key Features

* **Comprehensive Hardware Tracking:** Monitor real-time **Capacity (mAh)**, **Voltage (V)**, **Current (mA)**, **Power (W)**, **Temperature (°C)**, and **Battery Percentage (%)**.
* **Live Custom Notifications:** Keep tabs on your battery status directly from the notification bar. Fully customizable to display your preferred metrics, alongside intelligent low-battery warnings and 100% full-charge alerts.
* **Dynamic Custom XY Plotting:** Features a custom hardware-accelerated view complete with auto-scaling Y-axes, dynamic unit formatting, smart time-based X-axis rendering, and smooth curve plotting.
* **Unified Timeline History:** Seamlessly bridges charging and discharging cycles into a continuous, long-term performance graph.
* **Zero Bloat & High Performance:** Maintains a strict rolling data cap with lightweight local persistence, ensuring zero UI lag and a negligible memory footprint.

---

## 🚀 Getting Started

Get it from [Google Play](https://play.google.com/store/apps/details?id=in.sunilpaulmathew.batteryplus), [IzzyOnDroid](https://apt.izzysoft.de/fdroid/index/apk/in.sunilpaulmathew.batteryplus) or download and install the pre-compiled APK directly from the [Releases](../../releases) page of this repository.

[<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png"
alt="Get it on Google Play"
height="80">](https://play.google.com/store/apps/details?id=in.sunilpaulmathew.batteryplus)
[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png"
alt="Get it on IzzyOnDroid"
height="80">](https://apt.izzysoft.de/fdroid/index/apk/in.sunilpaulmathew.batteryplus)

---

## 🔒 Privacy & Minimum Permissions

Battery Plus is built with a strict **offline-first and privacy-focused** philosophy:
* **Zero Data Collection:** The app does not collect, track, analytics-log, or transmit any of your personal or device data.
* **100% Local Storage:** Every single history point, timestamp, and metric is stored securely and exclusively *inside your device's local storage* (`SharedPreferences`). Nothing ever leaves your phone.
* **Minimum Permissions:** The app requests only the bare-minimum system permissions required to read hardware battery states, ensuring a secure and lightweight footprint.

---

## 🌍 Translations

Want to use **Battery Plus** in your language? Contributions are always welcome!

- 📝 **Recommended:** [Join the POEditor project](https://poeditor.com/join/project/gKeunSFFsL) to translate directly in your browser.
- 💻 **GitHub PR:** Copy [`strings.xml`](app/src/main/res/values/strings.xml) into `res/values-<lang>/strings.xml` and open a pull request.

> **Note for translators:** Please keep placeholders like `%s` intact so dynamic values (such as battery percentages) display properly.

---

## 📄 License

    Copyright (C) 2026 sunilpaulmathew <sunil.kde@gmail.com>

    Battery Plus is a free softwares: you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the
    Free Software Foundation, either version 3 of the License, or (at
    your option) any later version.

    Battery Plus is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
    for more details.

    You should have received a copy of the GNU General Public License along
    with Battery Plus. If not, see <http://www.gnu.org/licenses/>.