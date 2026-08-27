package com.sunrisedental.service;

import com.sunrisedental.model.Bill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Properties;

/**
 * Email Service — sends HTML invoice emails to patients via Gmail SMTP.
 * Credentials are loaded from WEB-INF/application.properties.
 */
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private final String smtpHost;
    private final int smtpPort;
    private final String fromEmail;
    private final String appPassword;
    private final String fromName;

    /** Singleton instance loaded from application.properties */
    private static EmailService instance;

    private EmailService() {
        Properties props = loadProperties();
        this.smtpHost   = props.getProperty("mail.smtp.host",  "smtp.gmail.com");
        this.smtpPort   = Integer.parseInt(props.getProperty("mail.smtp.port", "587"));
        this.fromEmail  = props.getProperty("mail.from",       "noreply@sunrisedental.lk");
        this.appPassword= props.getProperty("mail.app.password","");
        this.fromName   = props.getProperty("mail.from.name",  "Sunrise Dental Clinic");
    }

    public static synchronized EmailService getInstance() {
        if (instance == null) instance = new EmailService();
        return instance;
    }

    /** Load properties from WEB-INF/application.properties on the classpath. */
    private Properties loadProperties() {
        Properties p = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) p.load(is);
        } catch (Exception e) {
            LOGGER.warn("Could not load application.properties for email config: {}", e.getMessage());
        }
        return p;
    }

    /**
     * Send a beautiful HTML invoice email to the patient.
     *
     * @param toEmail       patient email address
     * @param bill          the fully populated Bill object
     * @return true if sent successfully
     */
    public boolean sendInvoiceEmail(String toEmail, Bill bill) {
        if (toEmail == null || toEmail.isBlank()) {
            LOGGER.warn("Cannot send invoice email — patient email is blank for bill {}", bill.getBillNo());
            return false;
        }
        if (appPassword == null || appPassword.isBlank()) {
            LOGGER.warn("Cannot send invoice email — mail.app.password not configured.");
            return false;
        }

        try {
            Session session = buildMailSession();
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, fromName));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("🦷 Invoice " + bill.getBillNo() + " — Sunrise Dental Clinic");

            MimeMultipart multipart = new MimeMultipart("alternative");
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(buildHtmlInvoice(bill), "text/html; charset=UTF-8");
            multipart.addBodyPart(htmlPart);
            message.setContent(multipart);

            Transport.send(message);
            LOGGER.info("Invoice email sent to {} for bill {}", toEmail, bill.getBillNo());
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to send invoice email to {}: {}", toEmail, e.getMessage(), e);
            return false;
        }
    }

    private Session buildMailSession() {
        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.auth",            "true");
        mailProps.put("mail.smtp.starttls.enable", "true");
        mailProps.put("mail.smtp.host",            smtpHost);
        mailProps.put("mail.smtp.port",            String.valueOf(smtpPort));
        mailProps.put("mail.smtp.ssl.trust",       smtpHost);

        return Session.getInstance(mailProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, appPassword);
            }
        });
    }

    /** Build a premium HTML invoice email body. */
    private String buildHtmlInvoice(Bill bill) {
        String patientName    = nvl(bill.getPatientName(),    "Valued Patient");
        String patientCode    = nvl(bill.getPatientCode(),    "—");
        String billNo         = nvl(bill.getBillNo(),         "—");
        String appointmentNo  = nvl(bill.getAppointmentNo(),  "—");
        String dentistName    = nvl(bill.getDentistName(),    "—");
        String treatmentName  = nvl(bill.getTreatmentName(),  "—");
        String paymentMethod  = nvl(bill.getPaymentMethod(),  "CASH");
        String billDate       = bill.getBillDate() != null
                ? bill.getBillDate().toLocalDateTime().toLocalDate().toString()
                : LocalDate.now().toString();

        BigDecimal consultFee   = bd(bill.getConsultationFee());
        BigDecimal treatCost    = bd(bill.getTreatmentCost());
        BigDecimal subtotal     = consultFee.add(treatCost);
        BigDecimal discountAmt  = bd(bill.getDiscountAmount());
        BigDecimal taxAmt       = bd(bill.getTaxAmount());
        BigDecimal total        = bd(bill.getTotalAmount());
        String discRate         = bd(bill.getDiscountRate()).stripTrailingZeros().toPlainString() + "%";
        String taxRate          = bd(bill.getTaxRate()).stripTrailingZeros().toPlainString() + "%";

        return "<!DOCTYPE html>" +
            "<html lang='en'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
            "<style>" +
            "  *{box-sizing:border-box;margin:0;padding:0}" +
            "  body{font-family:'Segoe UI',Arial,sans-serif;background:#f0f4f8;padding:32px 16px}" +
            "  .wrap{max-width:620px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;" +
            "        box-shadow:0 4px 24px rgba(0,0,0,.12)}" +
            "  .header{background:linear-gradient(135deg,#1e40af 0%,#0ea5e9 100%);padding:36px 32px;text-align:center;color:#fff}" +
            "  .header h1{font-size:26px;letter-spacing:.04em;margin-bottom:4px}" +
            "  .header p{font-size:13px;opacity:.85}" +
            "  .badge{display:inline-block;background:rgba(255,255,255,.2);border-radius:20px;" +
            "         padding:4px 16px;font-size:12px;margin-top:12px;letter-spacing:.08em}" +
            "  .body{padding:32px}" +
            "  .greeting{font-size:16px;color:#1e293b;margin-bottom:20px}" +
            "  .meta{display:flex;gap:16px;flex-wrap:wrap;background:#f8fafc;border-radius:8px;" +
            "        padding:16px;margin-bottom:24px;border:1px solid #e2e8f0}" +
            "  .meta-item{flex:1;min-width:120px}" +
            "  .meta-item .label{font-size:11px;color:#64748b;text-transform:uppercase;letter-spacing:.06em}" +
            "  .meta-item .value{font-size:14px;font-weight:600;color:#0f172a;margin-top:2px}" +
            "  table{width:100%;border-collapse:collapse;font-size:14px;margin-bottom:20px}" +
            "  th{background:#1e40af;color:#fff;padding:10px 14px;text-align:left;font-weight:600}" +
            "  td{padding:10px 14px;border-bottom:1px solid #e2e8f0;color:#374151}" +
            "  td:last-child,th:last-child{text-align:right}" +
            "  .totals{background:#f8fafc;border-radius:8px;padding:16px;border:1px solid #e2e8f0;font-size:14px}" +
            "  .totals .row{display:flex;justify-content:space-between;padding:4px 0}" +
            "  .totals .row.total{border-top:2px solid #1e40af;margin-top:8px;padding-top:10px;" +
            "                      font-size:16px;font-weight:700;color:#1e40af}" +
            "  .paid-stamp{text-align:center;margin:24px 0}" +
            "  .paid-stamp span{display:inline-block;border:3px solid #16a34a;color:#16a34a;" +
            "                    border-radius:8px;padding:6px 24px;font-size:18px;font-weight:800;" +
            "                    letter-spacing:.12em;transform:rotate(-3deg)}" +
            "  .footer{background:#f1f5f9;padding:20px 32px;text-align:center;font-size:12px;color:#64748b}" +
            "  .footer a{color:#1e40af}" +
            "</style></head><body>" +
            "<div class='wrap'>" +
            "  <div class='header'>" +
            "    <h1>🦷 Sunrise Dental Clinic</h1>" +
            "    <p>No. 120, Galle Road, Colombo 03, Sri Lanka | Tel: +94 11 234 5678</p>" +
            "    <div class='badge'>OFFICIAL PAYMENT INVOICE</div>" +
            "  </div>" +
            "  <div class='body'>" +
            "    <p class='greeting'>Dear <strong>" + patientName + "</strong>,<br>" +
            "    Thank you for choosing Sunrise Dental Clinic. Your payment has been received and this is your official invoice.</p>" +
            "    <div class='meta'>" +
            "      <div class='meta-item'><div class='label'>Invoice No</div><div class='value'>" + billNo + "</div></div>" +
            "      <div class='meta-item'><div class='label'>Appointment</div><div class='value'>" + appointmentNo + "</div></div>" +
            "      <div class='meta-item'><div class='label'>Patient Code</div><div class='value'>" + patientCode + "</div></div>" +
            "      <div class='meta-item'><div class='label'>Date</div><div class='value'>" + billDate + "</div></div>" +
            "    </div>" +
            "    <table>" +
            "      <thead><tr><th>Description</th><th>Amount (LKR)</th></tr></thead>" +
            "      <tbody>" +
            "        <tr><td>Dentist Consultation — " + dentistName + "</td><td>" + fmt(consultFee) + "</td></tr>" +
            "        <tr><td>Treatment — " + treatmentName + "</td><td>" + fmt(treatCost) + "</td></tr>" +
            "      </tbody>" +
            "    </table>" +
            "    <div class='totals'>" +
            "      <div class='row'><span>Subtotal</span><span>LKR " + fmt(subtotal) + "</span></div>" +
            "      <div class='row'><span>Discount (" + discRate + ")</span><span style='color:#16a34a'>− LKR " + fmt(discountAmt) + "</span></div>" +
            "      <div class='row'><span>Tax / VAT (" + taxRate + ")</span><span>+ LKR " + fmt(taxAmt) + "</span></div>" +
            "      <div class='row total'><span>TOTAL PAID</span><span>LKR " + fmt(total) + "</span></div>" +
            "      <div class='row' style='font-size:12px;color:#64748b;margin-top:6px'>" +
            "        <span>Payment Method</span><span><strong>" + paymentMethod.replace("_"," ") + "</strong></span>" +
            "      </div>" +
            "    </div>" +
            "    <div class='paid-stamp'><span>✔ PAID</span></div>" +
            "    <p style='font-size:13px;color:#475569;text-align:center'>" +
            "      This is a computer-generated invoice. Valid without signature.<br>" +
            "      Wish you excellent oral health! 😊" +
            "    </p>" +
            "  </div>" +
            "  <div class='footer'>" +
            "    Sunrise Dental Clinic | billing@sunrisedental.lk | " +
            "    <a href='http://www.sunrisedental.lk'>www.sunrisedental.lk</a>" +
            "  </div>" +
            "</div></body></html>";
    }

    private String nvl(String val, String fallback) {
        return (val != null && !val.isBlank()) ? val : fallback;
    }

    private BigDecimal bd(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }

    private String fmt(BigDecimal val) {
        return String.format("%,.2f", val);
    }
}
