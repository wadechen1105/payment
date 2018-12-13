/*
	Copyright (C) 2016 Apple Inc. All Rights Reserved.
	See LICENSE.txt for this sample’s licensing information
	
	Abstract:
	A shared class for handling payment across an app and its related extensions
 */

import UIKit
import PassKit

typealias PaymentCompletionHandler = (Bool) -> Void

class Configuration {
    /*
     The value of the `prefix` user-defined build setting is
     written to the Info.plist file of every target in Swift version of the
     the project.
     */
    
    private struct MainBundle {
        static var prefix = Bundle.main.object(forInfoDictionaryKey: "MerchantID") ?? ""
    }
    
    struct Merchant {
        static let identififer = "merchant.\(MainBundle.prefix)"
        static let countryCode = "TW"
        static let currencyCode = "TWD"
    }
    
}

class PaymentHandler: NSObject {

    static let supportedNetworks: [PKPaymentNetwork] = [
        .amex,
        .discover,
        .masterCard,
        .visa
    ]
    
    var paymentController: PKPaymentAuthorizationController?
    var paymentSummaryItems = [PKPaymentSummaryItem]()
    var paymentStatus = PKPaymentAuthorizationStatus.failure
    var completionHandler: PaymentCompletionHandler?

    class func applePayStatus() -> (canMakePayments: Bool, canSetupCards: Bool) {
        return (PKPaymentAuthorizationController.canMakePayments(),
                PKPaymentAuthorizationController.canMakePayments(usingNetworks: supportedNetworks));
    }

    func startPayment(amount: [Any], completion: @escaping PaymentCompletionHandler) {
        let fare = amount[0] as! Double
        let tax = amount[1] as! Double

        print("price: \(fare), tax: \(tax), \(fare + tax)")

        let fareItem = PKPaymentSummaryItem(label: "價格", amount: NSDecimalNumber(string: "\(fare)"), type: .final)
        let taxItem = PKPaymentSummaryItem(label: "稅金", amount: NSDecimalNumber(string: "\(tax)"), type: .final)
        let totalItem = PKPaymentSummaryItem(label: "商場", amount: NSDecimalNumber(string: "\(fare + tax)"), type: .pending)

        paymentSummaryItems = [fareItem, taxItem, totalItem];
        completionHandler = completion

        // Create our payment request
        let paymentRequest = PKPaymentRequest()
        paymentRequest.paymentSummaryItems = paymentSummaryItems
        paymentRequest.merchantIdentifier = Configuration.Merchant.identififer
        paymentRequest.merchantCapabilities = .capability3DS
        paymentRequest.countryCode = Configuration.Merchant.countryCode
        paymentRequest.currencyCode = Configuration.Merchant.currencyCode
        paymentRequest.requiredShippingAddressFields = [.phone, .email]
        paymentRequest.supportedNetworks = PaymentHandler.supportedNetworks

        // Display our payment request
        paymentController = PKPaymentAuthorizationController(paymentRequest: paymentRequest)
        paymentController?.delegate = self
        paymentController?.present(completion: { (presented: Bool) in
            if presented {
                NSLog("Presented payment controller")
            } else {
                NSLog("Failed to present payment controller")
                self.completionHandler!(false)
            }
        })
    }
}

/*
    PKPaymentAuthorizationControllerDelegate conformance.
 */
extension PaymentHandler: PKPaymentAuthorizationControllerDelegate {

    func paymentAuthorizationController(_ controller: PKPaymentAuthorizationController, didAuthorizePayment payment: PKPayment, completion: @escaping (PKPaymentAuthorizationStatus) -> Void) {

        // Perform some very basic validation on the provided contact information
        if payment.shippingContact?.emailAddress == nil || payment.shippingContact?.phoneNumber == nil {
            paymentStatus = .invalidShippingContact
        } else {
            // Here you would send the payment token to your server or payment provider to process
            // Once processed, return an appropriate status in the completion handler (success, failure, etc)
            paymentStatus = .success
        }

        completion(paymentStatus)
    }

    func paymentAuthorizationControllerDidFinish(_ controller: PKPaymentAuthorizationController) {
        controller.dismiss {
            DispatchQueue.main.async {
                if self.paymentStatus == .success {
                    self.completionHandler!(true)
                } else {
                    self.completionHandler!(false)
                }
            }
        }
    }

//    func paymentAuthorizationController(_ controller: PKPaymentAuthorizationController, didSelectPaymentMethod paymentMethod: PKPaymentMethod, completion: @escaping ([PKPaymentSummaryItem]) -> Void) {
//        // The didSelectPaymentMethod delegate method allows you to make changes when the user updates their payment card
//        // Here we're applying a $2 discount when a debit card is selected
//        if paymentMethod.type == .debit {
//            var discountedSummaryItems = paymentSummaryItems
//            let discount = PKPaymentSummaryItem(label: "Debit Card Discount", amount: NSDecimalNumber(string: "-2.00"))
//            discountedSummaryItems.insert(discount, at: paymentSummaryItems.count - 1)
//            if let total = paymentSummaryItems.last {
//                total.amount = total.amount.subtracting(NSDecimalNumber(string: "5.00"))
//                print("---------------------")
//            }
//            completion(discountedSummaryItems)
//        } else {
//            completion(paymentSummaryItems)
//        }
//    }
}
