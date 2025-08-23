package com.example.identify.data.network


object ApiRoutes {
    const val BASE_URL = "https://ab6180630d49.ngrok-free.app"
//    const val BASE_URL2 = "https://17ada859f20e.ngrok-free.app"
    const val BASE_URL2 = "https://gqvvhfofej5y.share.zrok.io"

    object Student {
        const val SEND_OTP = "$BASE_URL/student/send-otp"
        const val VERIFY_OTP = "$BASE_URL/student/verify-otp"
        const val LOGOUT = "$BASE_URL/student/logout"
        const val DETAILS = "$BASE_URL/student/details"
        const val DOWNLOAD = "$BASE_URL/student/download"
    }

    object Staff {
        const val LOGIN = "$BASE_URL/staff/login"
        const val LOGOUT = "$BASE_URL/staff/logout"
        const val SCAN = "$BASE_URL2/face-recognition/"
    }

    object VerifyQr {
        const val VERIFY_QR = "$BASE_URL/verification/verifyQR"
    }
}
