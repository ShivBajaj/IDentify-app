package com.example.identify.data.model

import kotlinx.serialization.Serializable
//staff
@Serializable
data class StaffLoginRequest(val username: String, val password: String)


@Serializable
data class FaceScanResponse(
    val message: String,
    val similarity: Double,
    val studentDetails: StudentDetails,
)

@Serializable
data class StudentDetails(
    val vname: String,
    val vrollNo: String,
    val email: String,
    val vprogram: String,
    val vbranch: String
)

//student
@Serializable
data class StudentOtpRequest(val email: String)

@Serializable
data class StudentOtpResponse(val message: String)

@Serializable
data class StudentVerifyOtpRequest(val email: String, val otp: String)

@Serializable
data class StudentDetailsResponse(
    val vname: String,
    val vrollno: String,
    val email: String,
    val vprogram: String,
    val vbranch: String
    // add other fields present in Student model
)


@Serializable
data class LoginResponse(val message: String, val token: String)

@Serializable
data class LogoutResponse(val message: String)


@Serializable
data class ErrorResponse(val error: String)


//qr
@Serializable
data class VerifyQrRequest(
    val qrData: String
)

@Serializable
data class VerifyQrResponse(
    val message: String,
    val collegeName: String
)