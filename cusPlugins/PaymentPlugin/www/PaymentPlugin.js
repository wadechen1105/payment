var exec = require('cordova/exec');
const pluginName = 'PaymentPlugin'

exports.success = function () {
    return "1";
};

exports.failed = function () {
    return "-1";
};

exports.cancel = function () {
    return "0";
};

exports.checkIsReadyToPay  = function (resultCallback) {
    exec(resultCallback, function() {}, pluginName, 'checkIsReadyToPay', []);
};

exports.requestPayment  = function (price) {
    exec(function() {}, function() {}, pluginName, 'requestPayment', [price]);
};