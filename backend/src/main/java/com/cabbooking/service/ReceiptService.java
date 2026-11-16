package com.cabbooking.service;

import com.cabbooking.entity.Ride;
import com.cabbooking.exception.ResourceNotFoundException;
import com.cabbooking.repository.RideRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class ReceiptService {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptService.class);
    private final RideRepository rideRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public ReceiptService(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    public byte[] generateInvoicePdf(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + rideId));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, java.awt.Color.DARK_GRAY);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 11, java.awt.Color.GRAY);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, java.awt.Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, java.awt.Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, java.awt.Color.BLACK);
            Font successFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new java.awt.Color(22, 101, 52));

            // Header
            Paragraph title = new Paragraph("CAB BOOKING APPLICATION", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("Official Trip Receipt & Tax Invoice", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);

            document.add(new Paragraph(" "));

            // Invoice Meta Table
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            String tripDate = ride.getCompletedAt() != null ? ride.getCompletedAt().format(dtf) :
                    (ride.getCreatedAt() != null ? ride.getCreatedAt().format(dtf) : "N/A");

            PdfPCell c1 = new PdfPCell(new Phrase("Invoice #: INV-RIDE-" + ride.getId(), boldFont));
            c1.setBorder(Rectangle.NO_BORDER);
            metaTable.addCell(c1);

            PdfPCell c2 = new PdfPCell(new Phrase("Date: " + tripDate, normalFont));
            c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c2.setBorder(Rectangle.NO_BORDER);
            metaTable.addCell(c2);

            PdfPCell c3 = new PdfPCell(new Phrase("Payment Status: " + ride.getPaymentStatus(), successFont));
            c3.setBorder(Rectangle.NO_BORDER);
            metaTable.addCell(c3);

            PdfPCell c4 = new PdfPCell(new Phrase("Vehicle: " + ride.getVehicleType(), normalFont));
            c4.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c4.setBorder(Rectangle.NO_BORDER);
            metaTable.addCell(c4);

            document.add(metaTable);
            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------", subtitleFont));

            // Passenger & Driver Section
            PdfPTable partyTable = new PdfPTable(2);
            partyTable.setWidthPercentage(100);

            PdfPCell passengerCell = new PdfPCell();
            passengerCell.setBorder(Rectangle.NO_BORDER);
            passengerCell.addElement(new Paragraph("RIDER DETAILS", sectionFont));
            if (ride.getRider() != null) {
                passengerCell.addElement(new Paragraph("Name: " + ride.getRider().getName(), normalFont));
                passengerCell.addElement(new Paragraph("Email: " + ride.getRider().getEmail(), normalFont));
                passengerCell.addElement(new Paragraph("Phone: " + (ride.getRider().getPhone() != null ? ride.getRider().getPhone() : "N/A"), normalFont));
            }
            partyTable.addCell(passengerCell);

            PdfPCell driverCell = new PdfPCell();
            driverCell.setBorder(Rectangle.NO_BORDER);
            driverCell.addElement(new Paragraph("DRIVER DETAILS", sectionFont));
            if (ride.getDriver() != null) {
                String dName = ride.getDriver().getUser() != null ? ride.getDriver().getUser().getName() : "Professional Driver";
                driverCell.addElement(new Paragraph("Driver: " + dName, normalFont));
                driverCell.addElement(new Paragraph("Vehicle Model: " + ride.getDriver().getVehicleModel(), normalFont));
                driverCell.addElement(new Paragraph("Plate Number: " + ride.getDriver().getVehicleNumber(), normalFont));
            } else {
                driverCell.addElement(new Paragraph("Driver: Not Assigned", normalFont));
            }
            partyTable.addCell(driverCell);

            document.add(partyTable);
            document.add(new Paragraph(" "));

            // Trip Route Details
            document.add(new Paragraph("ROUTE DETAILS", sectionFont));
            PdfPTable routeTable = new PdfPTable(2);
            routeTable.setWidthPercentage(100);

            routeTable.addCell(createCell("Pickup Location:", true));
            routeTable.addCell(createCell(ride.getPickupAddress(), false));

            routeTable.addCell(createCell("Drop Location:", true));
            routeTable.addCell(createCell(ride.getDropAddress(), false));

            routeTable.addCell(createCell("Total Distance:", true));
            routeTable.addCell(createCell(ride.getDistanceKm() + " km", false));

            routeTable.addCell(createCell("Trip Duration:", true));
            routeTable.addCell(createCell(ride.getDurationMinutes() + " mins", false));

            document.add(routeTable);
            document.add(new Paragraph(" "));

            // Fare Breakdown Table
            document.add(new Paragraph("FARE BREAKDOWN", sectionFont));
            PdfPTable fareTable = new PdfPTable(2);
            fareTable.setWidthPercentage(100);

            fareTable.addCell(createCell("Base Fare & Distance Charges", false));
            fareTable.addCell(createCell("$" + ride.getFare(), false));

            fareTable.addCell(createCell("Promotion / Voucher Discount", false));
            fareTable.addCell(createCell("-$" + ride.getDiscount(), false));

            fareTable.addCell(createCell("Taxes & Service Fees", false));
            fareTable.addCell(createCell("$0.00 (Included)", false));

            PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL AMOUNT PAID", boldFont));
            totalLabelCell.setBackgroundColor(new java.awt.Color(243, 244, 246));
            totalLabelCell.setPadding(8);
            fareTable.addCell(totalLabelCell);

            PdfPCell totalValCell = new PdfPCell(new Phrase("$" + ride.getFinalFare(), boldFont));
            totalValCell.setBackgroundColor(new java.awt.Color(243, 244, 246));
            totalValCell.setPadding(8);
            fareTable.addCell(totalValCell);

            document.add(fareTable);

            // Footer
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph("Thank you for riding with us! For assistance, contact support@cabbooking.com", subtitleFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (Exception ex) {
            logger.error("Error generating PDF receipt", ex);
            throw new RuntimeException("Could not generate invoice PDF: " + ex.getMessage());
        }

        return out.toByteArray();
    }

    public void emailReceipt(Long rideId, String recipientEmail) {
        if (mailSender == null) {
            logger.info("JavaMailSender not configured. Skipping email dispatch for ride {}", rideId);
            return;
        }

        try {
            byte[] pdfBytes = generateInvoicePdf(rideId);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(recipientEmail);
            helper.setSubject("Your Cab Booking Receipt - Trip #" + rideId);
            helper.setText("Dear Customer,\n\nPlease find attached your official trip receipt for Ride #" + rideId + ".\n\nBest regards,\nCab Booking Team");
            helper.addAttachment("Receipt-Ride-" + rideId + ".pdf", new org.springframework.core.io.ByteArrayResource(pdfBytes));

            mailSender.send(message);
            logger.info("Receipt email sent to {}", recipientEmail);
        } catch (Exception e) {
            logger.error("Failed to send receipt email to {}: {}", recipientEmail, e.getMessage());
        }
    }

    private PdfPCell createCell(String text, boolean isHeader) {
        Font font = isHeader ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10) : FontFactory.getFont(FontFactory.HELVETICA, 10);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        return cell;
    }
}
