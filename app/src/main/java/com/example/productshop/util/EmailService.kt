package com.example.productshop.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailService {
    private val username = "donotengage007@gmail.com"
    private val password = "zoox qdvn lpkx qhib"

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
            message.setSubject("Secure Verification Code - InsureTechGuard")
            
            val htmlContent = getOtpHtmlTemplate(otp)
            message.setContent(htmlContent, "text/html; charset=utf-8")

            Transport.send(message)
        } catch (e: MessagingException) {
            e.printStackTrace()
            throw e
        }
    }

    private fun getOtpHtmlTemplate(otp: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px; }
                    .header { text-align: center; padding-bottom: 20px; border-bottom: 2px solid #1976D2; }
                    .logo { font-size: 24px; font-weight: bold; color: #1976D2; }
                    .content { padding: 30px 0; text-align: center; }
                    .otp-code { font-size: 42px; font-weight: bold; letter-spacing: 8px; color: #1976D2; margin: 20px 0; padding: 10px; background: #f0f7ff; border-radius: 5px; display: inline-block; }
                    .footer { font-size: 12px; color: #888; text-align: center; margin-top: 30px; border-top: 1px solid #eee; padding-top: 20px; }
                    .warning { color: #d32f2f; font-weight: bold; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">InsureTechGuard</div>
                    </div>
                    <div class="content">
                        <h2>Identity Verification</h2>
                        <p>Hello,</p>
                        <p>To complete your secure access, please enter the following One-Time Password (OTP) in the application:</p>
                        <div class="otp-code">$otp</div>
                        <p>This code is highly sensitive and is valid for <strong>10 minutes</strong>. <strong>Do not share this code</strong> with anyone, including our support team.</p>
                        <p class="warning">Security Alert: If you did not request this verification, your account security might be compromised. Please contact our support team immediately.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 InsureTechGuard Financial Services. All rights reserved.</p>
                        <p>InsureTechGuard Financial Services is an authorized financial services provider (FSP 44009).</p>
                        <p>This is an automated security notification. Replies to this email are not monitored.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    suspend fun sendSupportEmail(senderEmail: String, subject: String, body: String) = withContext(Dispatchers.IO) {
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
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(username)) // Send to support email
            message.setSubject("Support Request: $subject")
            message.setText("From: $senderEmail\n\n$body")

            Transport.send(message)
        } catch (e: MessagingException) {
            e.printStackTrace()
            throw e
        }
    }
}
