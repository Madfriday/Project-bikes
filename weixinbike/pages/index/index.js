var QQMapWX = require('../../libs/qqmap-wx-jssdk.js');
var qqmapsdk;
//index.js
//获取应用实例
Page({
  /**
  * 页面的初始数据
  */
  data: {
    lat: 0,
    log: 0,
    controls: [],
    markers: []
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {

    // 实例化API核心类
    qqmapsdk = new QQMapWX({
      key: 'EBMBZ-N5FWU-JYOVP-B3LKB-63JCQ-XBFHT'
    });


    var that = this;
    wx.getLocation({
      success: function (res) {
        var lat = res.latitude;
        var log = res.longitude;
        findBikes(that, log, lat);
      },
    })
    //模拟加载的动画
    wx.showLoading({
      title: '加载中',
    })
    //1.5s关闭动画效果
    setTimeout(function () {
      wx.hideLoading()
    }, 1500)

    that.mapCtx = wx.createMapContext('map');

    wx.getSystemInfo({
      success: function (res) {
        var height = res.windowHeight;
        var width = res.windowWidth;
        that.setData({
          controls: [{
            //中心点位置
            id: 1,
            iconPath: '/image/location.png',
            position: {
              width: 20,
              height: 35,
              left: width / 2 - 10,
              top: height / 2 - 35.
            },
            //是否可点击
            clickable: true
          }, {
            //定位按钮安置
            id: 2,
            iconPath: '/image/img1.png',
            position: {
              width: 40,
              height: 40,
              left: 20,
              top: height - 60.
            },
            //是否可点击
            clickable: true
          }, {
            //扫码按钮
            id: 3,
            iconPath: '/image/qrcode.png',
            position: {
              width: 100,
              height: 40,
              left: width / 2 - 50,
              top: height - 60.
            },
            //是否可点击
            clickable: true
          }, {
            //充值按钮
            id: 4,
            iconPath: '/image/pay.png',
            position: {
              width: 40,
              height: 40,
              left: width - 45,
              top: height - 60.
            },
            //是否可点击
            clickable: true
          }, { //手动添加一辆单车
            id: 5,
            iconPath: "/image/bike.png",
            position: {
              width: 35,
              height: 40,
            },
            //是否可点击
            clickable: true
              },{ //报修
              id: 6,
              iconPath: "/image/warn.png",
              position: {
                width: 35,
                height: 35,
                left: width - 42,
                top: height - 103.
              },
              //是否可点击
              clickable: true
          }]
        })
      },
    })
  },

  regionchange(e) {
    // var that = this;
    // if (e.type == "begin") {
    //   that.mapCtx.getCenterLocation({
    //     success: function (res) {
    //       findBikes(that, res.longitude, res.latitude)
    //     }
    //   })
    // }
  },

  controltap(e) {
    var that = this;
    if (e.controlId == 2) {
      //点击定位当前位置
      that.mapCtx.moveToLocation();
    }
    if (e.controlId == 3) {
      //获取全局变量中的status属性值
      var status = getApp().globalData.status;
      if (status == 0) {
        //跳转到注册页面
        wx.navigateTo({
          url: '../register/register',
        });
      } else if (status == 1) {
        wx.navigateTo({
          url: '../deposit/deposit',
        });
      } else if (status == 2) {
        wx.navigateTo({
          url: '../identify/identify',
        });
      } else if (status == 3) {
        scanCode()
      }
    }

    if (e.controlId == 4) {
      console.log("123")
      wx.navigateTo({
        url: '../pay/pay',
      })
    }

    if (e.controlId == 5) {
      that.mapCtx.getCenterLocation({
        success: function (res) {
          var lat = res.latitude;
          var log = res.longitude;
          var bikeNo =  getApp().globalData.bikeNo;
          wx.request({
            url: "http://localhost:8888/bike0",
            method: 'POST',
            data: {
              id :bikeNo,
              location: [log,lat]
            },
            success: function () {
              getApp().globalData.bikeNo = bikeNo+ 1
              findBikes(that, log, lat)
            }
          })
        }
      })
    }

    if (e.controlId == 6) {
      wx.navigateTo({
        url: '../warn/warn',
      })
    }
  },



  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function () {
    var that = this;
    //获取当前位置
    wx.getLocation({
      success: function (res) {
        //纬度
        var lat = res.latitude;
        //经度
        var log = res.longitude;
        //从本地存储中取出唯一身份标识
        var openid = getApp().globalData.openid
        //发送request向ES中添加数据（添加一条文档）
        wx.request({
          url: "http://192.168.9.25/bike/ni",
          data: {
            time: new Date(),
            openid: openid,
            lat: lat,
            log: log
          },
          method: "POST",
          success: function () {
            getApp().globalData.openid = openid + 109
           }
        })
      },
    })

    //10关闭活动提示
    setTimeout(function () {
      that.setData({
        isHidden: "none"
      })
    }, 30000)
  }

})

function findBikes(that, log, lat) {
  //请求后端数据
  wx.request({
    url: "http://localhost:8888/bikes",
    method: 'GET',
    data: {
        longitude: log,
        latitude : lat,
    },
    success: function (res) {
      console.log(res)
      const bikes = res.data.content.map((item) => {
        var bike = item.content 
        return {
          id: bike.id,
          iconPath: "/image/bike.png",
          width: 35,
          height: 40,
          latitude: bike.location[1],
          longitude: bike.location[0]
        };
      });
      // 修改data里面的markers
      that.setData({
        lat: lat,
        log: log,
        markers: bikes
      });
    }
  })
}

function scanCode() {
  wx.scanCode({
    success: function (res) {
      var bikeNo = res.result;
      console.log(bikeNo);
      /**
      var openid = wx.getStorageSync('openid');
      
      qqmapsdk.reverseGeocoder({
        location: {
          latitude: that.data.lat,
          longitude: that.data.log
        },
        success: function (res) {
          var addr = res.result.address_component
          var province = addr.province;
          var city = addr.city;
          var district = addr.district;
          //将数据写入到es中
          
        }
      });
       */
      wx.navigateTo({
        url: '../billing/billing'
      });
    }
  })
}