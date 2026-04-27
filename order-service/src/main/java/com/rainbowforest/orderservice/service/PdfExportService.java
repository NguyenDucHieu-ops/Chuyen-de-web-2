package com.rainbowforest.orderservice.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.rainbowforest.orderservice.domain.Item;
import com.rainbowforest.orderservice.domain.Order;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class PdfExportService {

    // ✅ LẤY TRỰC TIẾP FONT ARIAL CỦA WINDOWS ĐỂ ĐẢM BẢO KHÔNG BAO GIỜ LỖI TIẾNG
    // VIỆT
    private Font getUnicodeFont(float size, int style, Color color) {
        try {
            String fontPath = "C:\\Windows\\Fonts\\arial.ttf";
            File fontFile = new File(fontPath);
            if (!fontFile.exists()) {
                // Backup nếu xài font Tahoma
                fontPath = "C:\\Windows\\Fonts\\tahoma.ttf";
            }
            BaseFont baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            return new Font(baseFont, size, style, color);
        } catch (Exception e) {
            System.out.println("⚠️ Không tải được font Windows. Chữ có thể bị lỗi.");
            return FontFactory.getFont(FontFactory.HELVETICA, size, style, color);
        }
    }

    public byte[] generateOrderPdf(Order order) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 60, 60, 50, 50);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Load fonts chuẩn Tiếng Việt
            Font titleFont = getUnicodeFont(24, Font.BOLD, new Color(21, 128, 61)); // Màu xanh lá cây (Success)
            Font normalFont = getUnicodeFont(11, Font.NORMAL, Color.BLACK);
            Font boldFont = getUnicodeFont(11, Font.BOLD, Color.BLACK);
            Font tableHeaderFont = getUnicodeFont(11, Font.BOLD, Color.WHITE);
            Font totalFont = getUnicodeFont(18, Font.BOLD, new Color(220, 38, 38)); // Màu đỏ cho tổng tiền

            // --- HEADER CHUẨN BIÊN LAI ---
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[] { 3f, 1f });
            headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell logoCell = new PdfPCell(
                    new Phrase("F FIXTSTORE", getUnicodeFont(20, Font.BOLD, new Color(37, 99, 235))));
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerTable.addCell(logoCell);

            PdfPCell dateCell = new PdfPCell(
                    new Phrase("Ngày GD: " + order.getOrderedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            normalFont));
            dateCell.setBorder(Rectangle.NO_BORDER);
            dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            dateCell.setVerticalAlignment(Element.ALIGN_TOP);
            headerTable.addCell(dateCell);

            document.add(headerTable);
            document.add(new Paragraph(" "));

            // --- TITLE (ĐỔI THÀNH BIÊN LAI) ---
            Paragraph title = new Paragraph("BIÊN LAI XÁC NHẬN THANH TOÁN", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // ✅ Đóng mộc "ĐÃ THANH TOÁN"
            Paragraph statusStamp = new Paragraph("(ĐÃ THANH TOÁN QUA VNPAY)",
                    getUnicodeFont(12, Font.BOLD | Font.ITALIC, new Color(21, 128, 61)));
            statusStamp.setAlignment(Element.ALIGN_CENTER);
            statusStamp.setSpacingAfter(25f);
            document.add(statusStamp);

            // --- THÔNG TIN KHÁCH HÀNG ---
            PdfPTable customerInfoTable = new PdfPTable(2);
            customerInfoTable.setWidthPercentage(100);
            customerInfoTable.setWidths(new float[] { 1.5f, 3.5f });
            customerInfoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            customerInfoTable.addCell(new Phrase("Mã giao dịch: ", boldFont));
            customerInfoTable.addCell(new Phrase("#VNPAY-" + order.getId(), boldFont));

            String customerName = order.getCustomerName() != null ? order.getCustomerName() : "Khách Hàng";
            customerInfoTable.addCell(new Phrase("Người chuyển tiền: ", normalFont));
            customerInfoTable.addCell(new Phrase(customerName, normalFont));

            customerInfoTable.addCell(new Phrase("Điện thoại: ", normalFont));
            customerInfoTable
                    .addCell(new Phrase(order.getPhoneNumber() != null ? order.getPhoneNumber() : "N/A", normalFont));

            customerInfoTable.addCell(new Phrase("Địa chỉ nhận hàng: ", normalFont));
            customerInfoTable.addCell(
                    new Phrase(order.getShippingAddress() != null ? order.getShippingAddress() : "N/A", normalFont));

            document.add(customerInfoTable);
            document.add(new Paragraph(" "));

            LineSeparator line = new LineSeparator();
            line.setOffset(-2);
            document.add(line);
            document.add(new Paragraph(" "));

            // --- BẢNG SẢN PHẨM ---
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 4f, 1f, 2f, 2f });
            table.setSpacingBefore(10f);

            Color headerBgColor = new Color(59, 130, 246);
            String[] headers = { "Chi tiết sản phẩm", "SL", "Đơn Giá", "Thành Tiền" };
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
                cell.setBackgroundColor(headerBgColor);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPaddingTop(8f);
                cell.setPaddingBottom(8f);
                table.addCell(cell);
            }

            NumberFormat vndFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
            BigDecimal exchangeRate = new BigDecimal(25000);

            boolean isAltRow = false;
            Color altRowBgColor = new Color(241, 245, 249);

            for (Item item : order.getItems()) {
                // Tên
                PdfPCell nameCell = new PdfPCell(new Phrase(item.getProductName() != null ? item.getProductName()
                        : "Sản phẩm #" + item.getProduct().getId(), normalFont));
                nameCell.setPaddingLeft(10f);
                nameCell.setPaddingTop(8f);
                nameCell.setPaddingBottom(8f);
                if (isAltRow)
                    nameCell.setBackgroundColor(altRowBgColor);
                table.addCell(nameCell);

                // SL
                PdfPCell qtyCell = new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
                qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                qtyCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                if (isAltRow)
                    qtyCell.setBackgroundColor(altRowBgColor);
                table.addCell(qtyCell);

                // Giá
                BigDecimal priceVnd = item.getProductPrice() != null ? item.getProductPrice().multiply(exchangeRate)
                        : BigDecimal.ZERO;
                PdfPCell priceCell = new PdfPCell(new Phrase(vndFormat.format(priceVnd) + " ₫", normalFont));
                priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                priceCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                priceCell.setPaddingRight(10f);
                if (isAltRow)
                    priceCell.setBackgroundColor(altRowBgColor);
                table.addCell(priceCell);

                // Tổng
                BigDecimal subTotalVnd = item.getSubTotal() != null ? item.getSubTotal().multiply(exchangeRate)
                        : priceVnd.multiply(new BigDecimal(item.getQuantity()));
                PdfPCell subTotalCell = new PdfPCell(new Phrase(vndFormat.format(subTotalVnd) + " ₫", boldFont));
                subTotalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                subTotalCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                subTotalCell.setPaddingRight(10f);
                if (isAltRow)
                    subTotalCell.setBackgroundColor(altRowBgColor);
                table.addCell(subTotalCell);

                isAltRow = !isAltRow;
            }
            document.add(table);

            // --- TỔNG CỘNG ---
            document.add(new Paragraph(" "));
            BigDecimal totalVnd = order.getTotal() != null ? order.getTotal().multiply(exchangeRate) : BigDecimal.ZERO;
            PdfPTable totalTable = new PdfPTable(1);
            totalTable.setWidthPercentage(100);
            totalTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell totalCell = new PdfPCell();
            totalCell.setBorder(Rectangle.NO_BORDER);
            totalCell.addElement(new Paragraph("SỐ TIỀN ĐÃ THU: " + vndFormat.format(totalVnd) + " ₫", totalFont));
            totalCell.setPaddingRight(10f);
            totalTable.addCell(totalCell);

            totalTable.setSpacingAfter(40f);
            document.add(totalTable);

            // --- CHỮ KÝ ---
            PdfPTable signatureTable = new PdfPTable(2);
            signatureTable.setWidthPercentage(100);
            signatureTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell buyerSignCell = new PdfPCell();
            buyerSignCell.setBorder(Rectangle.NO_BORDER);
            buyerSignCell.addElement(new Paragraph("Khách hàng", boldFont));
            buyerSignCell.setPaddingLeft(50f);
            signatureTable.addCell(buyerSignCell);

            PdfPCell companySignCell = new PdfPCell();
            companySignCell.setBorder(Rectangle.NO_BORDER);
            companySignCell.addElement(new Paragraph("Xác nhận từ FixtStore", boldFont));
            companySignCell.addElement(
                    new Paragraph("(Hệ thống xác nhận tự động)", getUnicodeFont(10, Font.ITALIC, Color.DARK_GRAY)));
            companySignCell.setPaddingRight(30f);
            signatureTable.addCell(companySignCell);

            document.add(signatureTable);

        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }

        return baos.toByteArray();
    }
}