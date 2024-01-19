package com.jgs.collegeexamsystemback.util;

import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

/**
 * @author Administrator
 * @version 1.0
 * @description 邮件发送工具类
 * @date 2023/7/24 0024 16:37
 */
@Component
public class SendMailUtil {
    private static final String Account = "clp-js@qq.com";
    private static final String Password = "aztrfzqfhbesdhgj";
    private static final String SMTPHost = "smtp.qq.com";
    private Properties properties;
    public void sendMail(String receiveMail,String temPassword) throws MessagingException, UnsupportedEncodingException {
        properties = new Properties();
        properties.setProperty("mail.transport.protocol","smtp");
        properties.setProperty("mail.smtp.host",SMTPHost);
        properties.setProperty("mail.smtp.auth","true");
        properties.setProperty("mail.smtp.port","465");
        properties.setProperty("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.smtp.socketFactory.fallback","false");
        properties.setProperty("mail.smtp.socketFactory.port","465");
        Session session = Session.getInstance(properties);
        session.setDebug(true);
        MimeMessage mimeMessage = createMimeMessage(session,Account,receiveMail,temPassword);
        Transport transport = session.getTransport();
        transport.connect(Account,Password);
        transport.sendMessage(mimeMessage,mimeMessage.getAllRecipients());
        transport.close();
    }
    public static MimeMessage createMimeMessage(Session session,String sendMail,String receiveMail,String temPassword) throws UnsupportedEncodingException, MessagingException {
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(sendMail,"cc","UTF-8"));
        mimeMessage.setRecipient(MimeMessage.RecipientType.TO,new InternetAddress(receiveMail,"收件人","UTF-8"));
        mimeMessage.setSubject("高校排考系统重置密码","UTF-8");
        mimeMessage.setContent("您好" + receiveMail + ",您的临时密码是" + temPassword + ",请您登录后即时修改密码!","text/html;charset=UTF-8");
        mimeMessage.setSentDate(new Date());
        return mimeMessage;
    }
}
