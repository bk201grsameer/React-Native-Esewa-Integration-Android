package com.esewaintegrationwithreactnative

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.*
import com.f1soft.esewapaymentsdk.ESewaConfiguration
import com.f1soft.esewapaymentsdk.ESewaPayment
import com.f1soft.esewapaymentsdk.ui.ESewaPaymentActivity

class EsewaServiceModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext), ActivityEventListener {


    companion object {
        const val MODULE_NAME = "EsewaService"
        const val REQUEST_CODE_PAYMENT = 100
    }

    init {
        reactContext.addActivityEventListener(this)
    }

    private var paymentPromise: Promise? = null

    /*THE MODULE NAME WILL BE USED LATER TO ACCESS THE PAYMENT METHODS*/
    override fun getName(): String {
        return MODULE_NAME
    }

    @ReactMethod
    fun pay(
        clientId: String?,
        secretKey: String?,
        envParam: String?,
        amount: String?,
        productName: String?,
        productId: String?,
        callBackUrl: String?,
        promise: Promise
    ) {
        try {
            // Check if any of the required parameters are null or empty
            if (clientId.isNullOrEmpty() || secretKey.isNullOrEmpty() || amount.isNullOrEmpty() ||
                productName.isNullOrEmpty() || callBackUrl.isNullOrEmpty() || productId.isNullOrEmpty() || envParam.isNullOrEmpty()
            ) {
                throw IllegalArgumentException("One or more required parameters are missing")
            }

            // Log all parameters
            Log.d("PAY", "clientId: $clientId")
            Log.d("PAY", "secretKey: $secretKey")
            Log.d("PAY", "amount: $amount")
            Log.d("PAY", "productName: $productName")
            Log.d("PAY", "productId: $productId")
            Log.d("PAY", "callBackUrl: $callBackUrl")
            Log.d("PAY", "envParam: $envParam")

            /*Esewa Configuration*/
            val eSewaConfiguration = ESewaConfiguration()
                .clientId(clientId)
                .secretKey(secretKey)
                .environment(envParam)

            val eSewaPayment = ESewaPayment(amount, productName, productId, callBackUrl)
            val currentActivity = currentActivity ?: reactApplicationContext.currentActivity

            if (currentActivity != null) {
                paymentPromise = promise
                val intent = Intent(currentActivity, ESewaPaymentActivity::class.java)
                intent.putExtra(ESewaConfiguration.ESEWA_CONFIGURATION, eSewaConfiguration)
                intent.putExtra(ESewaPayment.ESEWA_PAYMENT, eSewaPayment)
                currentActivity.startActivityForResult(intent, REQUEST_CODE_PAYMENT)
            } else {
                promise.reject("ACTIVITY_NULL", "Activity is null")
            }

        } catch (e: Exception) {
            // you can catch it here and handle it accordingly
            Log.e("ERROR", e.toString())
            promise.reject("ERROR", e) // Reject the promise with the exception
        }
    }

    override fun onActivityResult(
        activity: Activity?,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            val promise = paymentPromise
            if (resultCode == Activity.RESULT_OK) {
                val message = data?.getStringExtra(ESewaPayment.EXTRA_RESULT_MESSAGE)
                promise?.resolve(message)
            } else if (resultCode == Activity.RESULT_CANCELED) {
                promise?.reject("PAYMENT_CANCELLED", "Payment was cancelled by the user")
            } else if (resultCode == ESewaPayment.RESULT_EXTRAS_INVALID) {
                val message = data?.getStringExtra(ESewaPayment.EXTRA_RESULT_MESSAGE)
                promise?.reject("INVALID_PAYMENT", message ?: "Invalid payment")
            }
            // Reset the promise after handling the result
            paymentPromise = null
        }
    }


    @ReactMethod
    fun getEsewaTestEnvironment(promise: Promise) {
        try {
            val testEnvironment = ESewaConfiguration.ENVIRONMENT_TEST
            promise.resolve(testEnvironment)
        } catch (e: Exception) {
            promise.reject("ERROR", e)
        }
    }

    @ReactMethod
    fun getEsewaProductionEnvironment(promise: Promise) {
        try {
            val productionEnvironment = ESewaConfiguration.ENVIRONMENT_PRODUCTION
            promise.resolve(productionEnvironment)
        } catch (e: Exception) {
            promise.reject("ERROR", e)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        Log.d("onNewIntent", "onNewIntent : ${intent?.data?.toString()}")
    }

    override fun initialize() {
        super.initialize()
        reactApplicationContext.addActivityEventListener(this)
    }

    override fun onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy()
        reactApplicationContext.removeActivityEventListener(this)
    }
}
