var QQMapWX = require('../../libs/qqmap-wx-jssdk.js');
var qqmapsdk;
// pages/warn/warn.js
Page({
  data: {
    // 故障车编号
    bikeNo: "",
    // 故障类型数组
    types: []
  },

  onLoad: function (options) {
    // 实例化API核心类
    qqmapsdk = new QQMapWX({
      key: 'EBMBZ-N5FWU-JYOVP-B3LKB-63JCQ-XBFHT'
    });
  },
  // 勾选故障类型，获取类型值存入checkboxValue
  checkboxChange: function (e) {
    var values = e.detail.value;
    console.log(values)
    this.setData({
      types: values
    })
  },

  // 提交到服务器
  formSubmit: function () {
    var bikeNo = getApp().globalData.bikeNo;
    var types = this.data.types;
    console.log(bikeNo)
    console.log(types)
    wx.getLocation({
      success: function (res) {
        var lat = res.latitude;
        var log = res.longitude;
        //请求腾讯地图api查找省市区
        qqmapsdk.reverseGeocoder({
          location: {
            latitude: lat,
            longitude: log
          },
          success: function (res) {
            var address = res.result.address_component;
            var province = address.province;
            var city = address.city;
            var district = address.district;
            //向日志服务器发送请求
            wx.request({
              url: "http://192.168.9.12/kafka/warn",
              method: "POST",
              data: {
                bikeNo :bikeNo,
                types:types,
                lat: lat,
                log: log,
                province: province,
                city: city,
                district: district
              },
              method: "POST",
              success: function () {
                getApp().globalData.bikeNo = bikeNo + Math.floor(Math.random() * 5000 + 5000)
               }
            })
          }
        })
      },
    })
  }
})