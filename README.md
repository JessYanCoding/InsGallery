![Logo](art/banner.jpg)
<p align="center">
  <img src="https://img.shields.io/badge/%F0%9F%93%81-Albums-brightgreen.svg?style=?style=flat-square"/>
  <img src="https://img.shields.io/badge/%F0%9F%93%B7-Photo-brightgreen.svg?style=?style=flat-square"/>
  <img src="https://img.shields.io/badge/%F0%9F%8E%A5-Video-brightgreen.svg?style=?style=flat-square"/>
  <img src="https://img.shields.io/badge/%F0%9F%96%A5-Preview%20Photo/Video-brightgreen.svg?style=?style=flat-square"/>
  <img src="https://img.shields.io/badge/%E2%9C%82-Crop-brightgreen.svg?style=?style=flat-square"/>
  <img src="https://img.shields.io/badge/%F0%9F%96%BC-Filters-brightgreen.svg?style=?style=flat-square"/>
  <img src="https://img.shields.io/badge/%F0%9F%93%8F-Video%20Trim-brightgreen.svg?style=?style=flat-square"/>
  <img src="https://img.shields.io/badge/%F0%9F%8F%9E-Cover%20Selection-brightgreen.svg?style=?style=flat-square"/>
  <img src="https://img.shields.io/badge/%F0%9F%8C%99-Dark%20Mode-brightgreen.svg?style=?style=flat-square"/>
</p>

<h2 align="center">InsGallery</h1>

<p align="center">
  <b>Instagram-like image picker for Android (一款 UI 炫酷高仿 Instagram 的图片、视频选择器)</b>
</p>

<p align="center">
  <a href="https://github.com/LuckSiege/PictureSelector">
    <b>Powered by PictureSelector</b>
  </a>
</p>

<p align="center">
  <a href="https://gitee.com/JessYanCoding/InsGallery">
    <b>国内仓库</b>
  </a>
</p>

<p align="center">
  <a href="https://github.com/JessYanCoding/InsGallery/raw/master/apk/v0.7.0_2020_08_04.apk">
    <b>APK</b>
  </a>
</p>

<p align="center">
⇣
</p>

<p>
  <img src="art/overview_pick.gif" width="30%" height="30%"/>
  <img src="art/overview_filter.gif" width="30%" height="30%"/>
  <img src="art/overview_video_trim.gif" width="30%" height="30%"/>
</p>

## Download
``` gradle
 implementation 'me.jessyan:insgallery:0.7.0'
```

## Usage
```java
InsGallery.openGallery(Activity, GlideEngine.createGlideEngine(), new OnResultCallbackListenerImpl(mAdapter));
```

## Overview
### Feature
<p>
   <img src="art/feature_crop_photo.jpg" width="30%" height="30%">
   <img src="art/feature_multiple_filters.jpg" width="30%" height="30%">
   <img src="art/feature_single_filters.jpg" width="30%" height="30%">
</p>

<p>
   <img src="art/feature_play_video.jpg" width="30%" height="30%">
   <img src="art/feature_video_trim.jpg" width="30%" height="30%">
   <img src="art/feature_video_cover.jpg" width="30%" height="30%">
</p>

### Default Style
<p>
   <img src="art/style_default_gallery.jpg" width="30%" height="30%">
   <img src="art/style_default_photo.jpg" width="30%" height="30%">
   <img src="art/style_default_video.jpg" width="30%" height="30%">
</p>

### Dark Style
<p>
   <img src="art/style_dark_gallery.jpg" width="30%" height="30%">
   <img src="art/style_dark_photo.jpg" width="30%" height="30%">
   <img src="art/style_dark_video.jpg" width="30%" height="30%">
</p>

### Dark Blue Style
<p>
   <img src="art/style_dark_blue_gallery.jpg" width="30%" height="30%">
   <img src="art/style_dark_blue_photo.jpg" width="30%" height="30%">
   <img src="art/style_dark_blue_video.jpg" width="30%" height="30%">
</p>

  
 ## About Me
 * **微信公众号**: **JessYan**
 * **Email**: <jess.yan.effort@gmail.com>
 * **Home**: <http://jessyan.me>
 * **掘金**: <https://juejin.im/user/57a9dbd9165abd0061714613>
 * **简书**: <https://www.jianshu.com/u/1d0c0bc634db>

 ## License
 ```
  Copyright 2020, JessYan

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 ```

