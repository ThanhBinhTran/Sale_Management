package sale;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author thanh
 */
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class invoice_create_pdf {
    class Watermark extends PdfPageEventHelper {

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte canvas = writer.getDirectContentUnder();
                Image image = Image.getInstance(ipre_text.logo_path);
                float x = document.getPageSize().getWidth()*(1-0.8f);
                float y = document.getPageSize().getHeight()*(1-0.55f);
                image.scalePercent(80f);
                image.setAbsolutePosition(x, y);
                image.setRotationDegrees(45);
                
                canvas.addImage(image);

                //font.setColor(BaseColor.LIGHT_GRAY);
                //font.setSize(45);
                //Phrase watermark = new Phrase(ipre_text.company_name, font);
                //ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, watermark, 298, 421, 45);
            } catch (BadElementException ex) {
                Logger.getLogger(invoice_create_pdf.class.getName()).log(Level.SEVERE, null, ex);
            } catch (DocumentException | IOException ex) {
                Logger.getLogger(invoice_create_pdf.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private Document document;
    private Font font;
    private File file;
    private String invoice_number;
    private final invoice_predefined_text ipre_text = new invoice_predefined_text();

    private void insert_cell_text(PdfPTable table, String text, int align, boolean border) {
        Phrase phrase = new Phrase(text, font);
        PdfPCell cell_text = new PdfPCell(phrase);
        if (!border) {
            cell_text.setBorder(0); // remove border
        }
        cell_text.setHorizontalAlignment(align);
        table.addCell(cell_text);
    }

    private void insert_cell_text(PdfPTable table, String text, int align,
            float font_size, BaseColor font_color, boolean border) {
        Font new_font = new Font(font);
        new_font.setSize(font_size);
        new_font.setColor(font_color);
        new_font.setStyle(Font.BOLD);
        PdfPCell cell_text = new PdfPCell(new Phrase(text, new_font));
        if (!border) {
            cell_text.setBorder(0); // remove border
        }
        cell_text.setHorizontalAlignment(align);
        table.addCell(cell_text);
    }

    private void insert_cell_nestedTable_company(PdfPTable table, float font_size, BaseColor font_color, boolean border) {
        float[] columnWidths = {1f};
        PdfPTable nestedTable = new PdfPTable(columnWidths);
        String company_name = ipre_text.company_name;
        String company_address = ipre_text.address + ": " + ipre_text.company_address;
        String company_phone = ipre_text.phone + ": " + ipre_text.company_phone;
        String company_email = ipre_text.email + ": " + ipre_text.company_email + "\n";
        insert_cell_text(nestedTable, company_name, Element.ALIGN_CENTER, font_size, font_color, Boolean.FALSE);
        insert_cell_text(nestedTable, company_address, Element.ALIGN_LEFT,  Boolean.FALSE);
        insert_cell_text(nestedTable, company_phone, Element.ALIGN_LEFT,  Boolean.FALSE);
        insert_cell_text(nestedTable, company_email, Element.ALIGN_LEFT,  Boolean.FALSE);
        insert_cell_text(nestedTable, ipre_text.title, Element.ALIGN_CENTER, 15, BaseColor.BLUE, false);
        PdfPCell cell_text = new PdfPCell(nestedTable);
        if (!border) {
            cell_text.setBorder(0); // remove border
        }

        
        table.addCell(cell_text);
    }
        
    private void insert_cells_text(PdfPTable table, List<String> texts, int align, boolean border) {
        for (String str_item : texts) {
            insert_cell_text(table, str_item, align, border);
        }
    }

    public void insert_cells_item(PdfPTable table, List<invoice_sold_item> invoice_row, boolean border) {
        int i = 1;
        for (invoice_sold_item item_row : invoice_row) {
            String stt = String.format("%s", i++);
            insert_cell_text(table, stt, Element.ALIGN_RIGHT, border);
            
            insert_cell_text(table, item_row.get_name(), Element.ALIGN_LEFT, border);
            insert_cell_text(table, item_row.get_quantity(), Element.ALIGN_RIGHT, border);
            insert_cell_text(table, item_row.get_price(), Element.ALIGN_RIGHT, border);
            insert_cell_text(table, item_row.get_subtotal(), Element.ALIGN_RIGHT, border);
        }
    }

    private void insert_cell_empty(PdfPTable table, boolean border) {
        PdfPCell cell_text = new PdfPCell(new Phrase(""));
        if (!border) {
            cell_text.setBorder(0); // remove border
        }
        table.addCell(cell_text);
    }

    private void insert_cell_image(PdfPTable table, String img_path, boolean border) throws IOException, BadElementException {
        Image image = Image.getInstance(img_path);
        //image.scaleToFit(PageSize.A4.getWidth() / 3, PageSize.A4.getHeight());
        PdfPCell imageCell = new PdfPCell(image, true);
        if (!border) {
            imageCell.setBorder(0); // remove border
        }

        table.addCell(imageCell);
    }

    public void set_watermark_transparent(PdfWriter writer) {
        // Set transparency
        PdfGState gs1 = new PdfGState();
        gs1.setFillOpacity(0.2f);
        writer.getDirectContentUnder().setGState(gs1);
    }
    
    public void set_company_info() throws IOException, BadElementException, DocumentException {
        float[] col_widths = {0.15f, 0.05f, 0.845f};
        PdfPTable table = new PdfPTable(col_widths);
        insert_cell_image(table, ipre_text.logo_path, false);
        insert_cell_empty(table, false);
        insert_cell_nestedTable_company(table, 14, BaseColor.BLACK, Boolean.FALSE);
 

        
        document.add(table);
    }

    public void set_consumer_info(invoice_sold_table sold_items) throws DocumentException {       
        float[] date_columnWidths = {0.7f, 0.3f};
        PdfPTable date_table = new PdfPTable(date_columnWidths);
        String date_text = String.format("%s %s, %s %s, %s %s.", 
                ipre_text.day, sold_items.day,
                ipre_text.month, sold_items.month,
                ipre_text.year, sold_items.Year
                );
        String invoice_code_text = String.format("%s:%s", ipre_text.invoice_number, invoice_number);
        insert_cell_text(date_table, date_text, Element.ALIGN_RIGHT, false);
        insert_cell_text(date_table, invoice_code_text, Element.ALIGN_RIGHT, false);

        String consumer_info = ipre_text.consumer + ": " + sold_items.consumer_name + "\n";
        consumer_info += ipre_text.address + ":..........................................................\n";
        consumer_info += ipre_text.phone + ":..........................................................\n";
        float[] consumer_info_columnWidths = {0.1f};
        PdfPTable consumer_info_table = new PdfPTable(consumer_info_columnWidths);
        insert_cell_text(consumer_info_table, consumer_info, Element.ALIGN_LEFT, false);
        
        document.add(date_table);
        document.add(consumer_info_table);
    }

    public void set_sold_item(invoice_sold_table sold_data_table) throws DocumentException {
        float[] col_w = {4F, 42F, 14F, 14F, 14F};
        float sum_4first_col_w = col_w[0] + col_w[1]+ col_w[2]+ col_w[3];
        
        float[] tax_col_width = {sum_4first_col_w, col_w[4]};
        PdfPTable table_sold_info = new PdfPTable(col_w);

        List<String> header = new ArrayList<>();
        header.add(ipre_text.item_stt);
        header.add(ipre_text.item_name);
        header.add(ipre_text.item_quantity);
        header.add(ipre_text.item_price);
        header.add(ipre_text.item_subtotal);
        insert_cells_text(table_sold_info, header, Element.ALIGN_CENTER, true);
        insert_cells_item(table_sold_info, sold_data_table.data, true);

        
        PdfPTable table_tax_total = new PdfPTable(tax_col_width);
        insert_cell_text(table_tax_total, ipre_text.sub_total, Element.ALIGN_RIGHT, true);
        insert_cell_text(table_tax_total, sold_data_table.get_subtotal(), Element.ALIGN_RIGHT, true);
        
        insert_cell_text(table_tax_total, ipre_text.item_tax, Element.ALIGN_RIGHT, true);
        insert_cell_text(table_tax_total, sold_data_table.get_tax(), Element.ALIGN_RIGHT, true);
        
        insert_cell_text(table_tax_total, ipre_text.total, Element.ALIGN_RIGHT, true);
        insert_cell_text(table_tax_total, sold_data_table.get_total(), Element.ALIGN_RIGHT, true);
        document.add(table_sold_info);
        document.add(table_tax_total);
    }

    public void set_signature() throws DocumentException {
        float[] signature_col_width = {1F, 1F, 1F};
        PdfPTable table_signature = new PdfPTable(signature_col_width);
        List<String> signature = new ArrayList<>();
        String text_consumer = ipre_text.consumer + "\n" + ipre_text.sign_name;
        String text_receiver = ipre_text.reciever + "\n" + ipre_text.sign_name;
        //String text_warehouse = ipre_text.warehouse_manager + "\n" + ipre_text.sign_name;
        //String text_accountant = ipre_text.accountant + "\n" + ipre_text.sign_name;
        String text_general = ipre_text.general + "\n" + ipre_text.sign_name;

        signature.add(text_consumer);
        signature.add(text_receiver);
        //signature.add(text_warehouse);
        //signature.add(text_accountant);
        signature.add(text_general);
        insert_cells_text(table_signature, signature, Element.ALIGN_CENTER, false);
        document.add(table_signature);

    }

    public void create_pdf(invoice_sold_table sold_items ) throws DocumentException, IOException {
        invoice_number = String.format("%s%s%s%s%s", 
                                    sold_items.Year, sold_items.month, sold_items.day,
                                    sold_items.hours, sold_items.minus
                                    );
        String pdf_filename = String.format("%s/%s/%s/%s.pdf", parameter.invoice_path, 
                                    sold_items.Year, sold_items.month, invoice_number);
        file = new File(pdf_filename);
        file.getParentFile().mkdirs();

        document = new Document(PageSize.A4);
        // Set margins: left, right, top, bottom
        document.setMargins(20, 20, 20, 20);

        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdf_filename));
        writer.setPageEvent(new Watermark());
        //PdfWriter.setPageEvent(new Watermark());
        document.open();
        String font_file = getClass().getResource("/sale/font/vuTimes.ttf").toString();
        BaseFont bf = BaseFont.createFont(font_file, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        font = new Font(bf);
        
        set_watermark_transparent(writer);
        set_company_info();
        set_consumer_info(sold_items);
        set_sold_item(sold_items);
        set_signature();

        document.close();
    }
    
    public String get_absolute_path(){
        return file.getAbsolutePath();
    }
}