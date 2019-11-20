package irvinc.example.com.inicioprincipal.Mail;

import android.os.StrictMode;
import android.util.Log;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EnviarCorreo {

    private String correo;
    private String contra;

    public void enviar(String correoRecicladora){
        correo = "soporte.recicladora@gmail.com";
        contra = "soporte123";

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.googlemail.com");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465");

        try{
            Session session = Session.getDefaultInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(correo, contra);
                }
            });

            if (session != null){
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(correo));
                // Set To: header field of the header.
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(correoRecicladora));
                message.setSubject("Reporte recicladora");
                // Create the message part
                BodyPart messageBodyPart = new MimeBodyPart();
                // Now set the actual message
                messageBodyPart.setText("No contestar a este correo.");
                // Create a multipar message
                Multipart multipart = new MimeMultipart();
                // Set text message part
                multipart.addBodyPart(messageBodyPart);
                // Part two is attachment
                messageBodyPart = new MimeBodyPart();
                String filename = "/storage/emulated/0/PDF/Reporte.pdf";
                DataSource source = new FileDataSource(filename);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(filename);
                multipart.addBodyPart(messageBodyPart);
                // Send the complete message parts
                message.setContent(multipart);
                // Send message
                Transport.send(message);
            }
        } catch (Exception e){
            Log.e("enviar", e.toString());
        }
    }
}