import JavaScriptCore

let handler = PaymentHandler()

@objc(PaymentPlugin)
class PaymentPlugin : CDVPlugin {
    
    
    @objc(checkIsReadyToPay:)
    func checkIsReadyToPay(_ command: CDVInvokedUrlCommand) {
        let result = PaymentHandler.applePayStatus()
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: result.canSetupCards && result.canSetupCards ? 1: 0)
        self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
    }
    
    @objc(requestPayment:)
    func requestPayment(_ command: CDVInvokedUrlCommand) {
        let money = command.arguments ?? [0, 0]
        handler.startPayment(amount: money) {
            (success) in
            //            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: success ? 1: 0)
            //            self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
            self.commandDelegate!.evalJs("eventsHandler.exec(\(success ? "1": "0"));")
        }
    }
}
