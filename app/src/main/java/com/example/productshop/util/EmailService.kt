package com.example.productshop.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailService {
    private val username = "donotengage007@gmail.com"
    private val password = "aenm sytg aram ukrz"

    suspend fun sendOtpEmail(recipientEmail: String, otp: String) = withContext(Dispatchers.IO) {
        val props = Properties()
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.host"] = "smtp.gmail.com"
        props["mail.smtp.port"] = "587"

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })

        try {
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(username))
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
            message.setSubject("Your OTP for ProductShop")
            message.setText("Your verification code is: $otp")

            Transport.send(message)
        } catch (e: MessagingException) {
            e.printStackTrace()
            throw e
        }
    }
}
