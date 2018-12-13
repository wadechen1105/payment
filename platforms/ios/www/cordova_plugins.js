cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
  {
    "id": "cordova-plugin-device.device",
    "file": "plugins/cordova-plugin-device/www/device.js",
    "pluginId": "cordova-plugin-device",
    "clobbers": [
      "device"
    ]
  },
  {
    "id": "webcomm-cordova-plugin-payment.PaymentPlugin",
    "file": "plugins/webcomm-cordova-plugin-payment/www/PaymentPlugin.js",
    "pluginId": "webcomm-cordova-plugin-payment",
    "clobbers": [
      "PaymentPlugin"
    ]
  }
];
module.exports.metadata = 
// TOP OF METADATA
{
  "cordova-plugin-whitelist": "1.3.3",
  "cordova-plugin-device": "2.0.2",
  "webcomm-cordova-plugin-payment": "0.0.1"
};
// BOTTOM OF METADATA
});