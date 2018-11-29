cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
    {
        "id": "com.webcomm.cordova.plugin.payment.PaymentPlugin",
        "file": "plugins/com.webcomm.cordova.plugin.payment/www/PaymentPlugin.js",
        "pluginId": "com.webcomm.cordova.plugin.payment",
        "clobbers": [
            "PaymentPlugin"
        ]
    }
];
module.exports.metadata = 
// TOP OF METADATA
{
    "cordova-plugin-whitelist": "1.3.3",
    "com.webcomm.cordova.plugin.payment": "0.0.1"
};
// BOTTOM OF METADATA
});