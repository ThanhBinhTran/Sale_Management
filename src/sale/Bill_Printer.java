/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sale;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.print.PageFormat;
import java.awt.print.Printable;
import static java.awt.print.Printable.PAGE_EXISTS;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 *
 * @author Binh
 * print pager [-70;130]
 */

public final class Bill_Printer implements Printable, ActionListener {
  int pY_offset = 0;
  int pX_align = -70;
  int x_name_offset     = -55;
  int x_count_endoffset = 80;
  int x_price_endoffset = 130;
  int x_moneyCol        = 30;

  double  cur_money;
  double  old_money;
  double  final_money;
  public Graphics2D gHoa_don ;
  public int print_type = 0;
  private static DecimalFormat moneyFormat;
  
  Bill_Printer (){
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        get_bill_configure();
        moneyFormat = new DecimalFormat("#,##0.0");
  }
  private static final Font PFONT_TIME = new Font("Times new roman",Font.PLAIN,12);
  @Override
  public int print(Graphics g, PageFormat pf, int page) throws
                                                        PrinterException 
    {
        try {
            if (page > 0) { /* We have only one page, and 'page' is zero-based */
                return NO_SUCH_PAGE;
            }
            gHoa_don = (Graphics2D)g;
            gHoa_don.setFont(PFONT_TIME);
            gHoa_don.translate(pf.getImageableX(), pf.getImageableY()-65);
            gHoa_don.setClip(pX_align, 0,200,10000);
            switch (print_type)
            {
                        
                case 0:
                    System.out.println("print_bill();");
                    print_bill();
                    break;
                case 1:
                    System.out.println("print_barcode();");
                    print_barcode(pf);
                    break;
                default :
                    
            }
            return PAGE_EXISTS;
        } catch (WriterException | IOException ex) {
            Logger.getLogger(Bill_Printer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return PAGE_EXISTS;
    }
    public void get_bill_configure()
    {
        try{
            try (FileInputStream in = new FileInputStream(parameter.config_path);BufferedReader bufffile = new BufferedReader(new InputStreamReader(in, "UTF8"))) 
            {
                String strLine;
                strLine = bufffile.readLine();
                while (strLine != null)   {
                    if(strLine.substring(0, 3).equals("=08"))
                    {
                        String[] raw_data = strLine.substring(3).split("<<>>");
                        pX_align          = Integer.parseInt(raw_data[0]);
                        x_name_offset     = Integer.parseInt(raw_data[1]);
                        x_count_endoffset = Integer.parseInt(raw_data[2]);
                        x_price_endoffset = Integer.parseInt(raw_data[3]);
                        x_moneyCol        = Integer.parseInt(raw_data[4]);            
                    }
                    strLine = bufffile.readLine();
                }
            }
        }catch (IOException | NumberFormatException e){//Catch exception if any
            pX_align = -70;
            x_name_offset     = -55;
            x_count_endoffset = 80;
            x_price_endoffset = 130;
            x_moneyCol        = 30;
        }
    }
    public void print_bill()
    {
        pY_offset =-5;
        try{
            String strLine;
            try (FileInputStream in = new FileInputStream(parameter.print_bill_path)) 
            {
                int items_num =1;
                try (BufferedReader bufffile = new BufferedReader(new InputStreamReader(in, "UTF8")))
                {
                    strLine = bufffile.readLine();
                    while (strLine != null)   {
                        System.out.println("line:" + items_num + ", contents:" + strLine);
                        switch (strLine.substring(0, 3)) {
                            case "---":
                                pY_offset += 8;
                                gHoa_don.drawString(strLine, pX_align, pY_offset);
                                break;
                            case "--+":
                                pY_offset += 14;
                                print_items_row(strLine.substring(3), items_num++);
                                break;
                            case "==>":
                                pY_offset += 14;
                                print_money_row(strLine.substring(3));
                                System.out.println("Finish: print_money_row");
                                break;
                            case "==-":
                                pY_offset +=14;
                                print_old_money(strLine.substring(3));
                                System.out.println("Finish: print_old_money");
                                break;
                            default :
                                pY_offset += 14;
                                gHoa_don.drawString(strLine,pX_align,pY_offset);
                                break;
                        }
                        strLine = bufffile.readLine();
                    }
                    
                    //print brank
                    pY_offset += 40;
                    gHoa_don.drawString(".",pX_align,pY_offset);
                    bufffile.close();
                }
            }
        }catch (IOException e){//Catch exception if any
            System.err.println("Error: " + e.getMessage() + "[print(Graphics g, PageFormat pf, int page)]" );
        }
    }
    public void print_money_row(String iString)
    {
        int actual_width_price;
        String[] raw_data= iString.split("<<>>");
        raw_data[0] = "Tổng sl: " + raw_data[0] + ",";
        try{
            cur_money = Double.parseDouble(raw_data[1].replaceAll(",", ""));
        }
        catch ( NumberFormatException e)
        {
            System.out.println("ERROR! print_money_row(");
        }
        gHoa_don.drawString(raw_data[0], x_name_offset, pY_offset);
        if(raw_data[1].length()<=8)
        {
            int tail = 8 - raw_data[1].length();
            raw_data[1] = "                                    " + raw_data[1];
            int end_index = raw_data[1].length();
            raw_data[1] = raw_data[1].substring(end_index-8 - tail, end_index);
        }
        actual_width_price = gHoa_don.getFontMetrics().stringWidth(raw_data[1]);
        gHoa_don.drawString("Tổng: ", x_moneyCol, pY_offset);
        gHoa_don.drawString(raw_data[1], x_price_endoffset - actual_width_price, pY_offset);
    }
    public void print_items_row(String iString, int i)
    {
        String[] raw_data= iString.split("<<>>");   //ten, sl, gia sp
        String  stt = String.valueOf(i);
        int actual_width_price = gHoa_don.getFontMetrics().stringWidth(raw_data[2]);
        int actual_width_item = gHoa_don.getFontMetrics().stringWidth(raw_data[1]);
        gHoa_don.drawString(stt, pX_align, pY_offset);
        gHoa_don.drawString(raw_data[0], x_name_offset, pY_offset);
        gHoa_don.drawString(raw_data[1].trim(), x_count_endoffset - actual_width_item, pY_offset);
        gHoa_don.drawString(raw_data[2].trim(), x_price_endoffset- actual_width_price , pY_offset);
    }
    public void print_old_money(String soldmoney)
    {
        int actual_width_price;
        old_money = Double.parseDouble(soldmoney);
        soldmoney = moneyFormat.format(old_money);
        actual_width_price = gHoa_don.getFontMetrics().stringWidth(soldmoney);
        
        gHoa_don.drawString("Nợ cũ :", x_moneyCol, pY_offset);
        gHoa_don.drawString(soldmoney, x_price_endoffset - actual_width_price,pY_offset);
        pY_offset += 6;
        gHoa_don.drawString("--------------------------", x_moneyCol, pY_offset);
        
        final_money = old_money + cur_money;
        String sfinal_money = moneyFormat.format(final_money);
        actual_width_price = gHoa_don.getFontMetrics().stringWidth(sfinal_money);
        pY_offset += 14;
        gHoa_don.drawString("Tổng tiền:", x_moneyCol, pY_offset);
        gHoa_don.drawString(sfinal_money, x_price_endoffset - actual_width_price, pY_offset);
    }
    public void print_handle(int iprint_type)
    {
        print_type = iprint_type;
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        //boolean ok = job.printDialog();
        if (true) {
            try {
                job.print();
            } catch (PrinterException ex) {
                /* The job did not successfully complete */
            }
        }
    }
    public String checkBarCodeStore(String barcode_img_name)
    {
        if(barcode_img_name.contains(">"))
        {
            barcode_img_name = barcode_img_name.replaceAll(">", "lớn");
        }
        if (barcode_img_name.contains("<"))
        {
            barcode_img_name = barcode_img_name.replaceAll("<", "nhỏ");
        }
        if(barcode_img_name.contains("\""))
        {
            barcode_img_name = barcode_img_name.replaceAll("\"", "'");
        }
        return barcode_img_name;
    }
    public void print_barcode(PageFormat pf) throws WriterException, IOException
    {
        
        pY_offset = 0;
        int width = 50;  
        int height = 40; // change the height and width as per your requirement  
        String imageFormat = "png"; // could be "gif", "tiff", "jpeg"       
        MultiFormatWriter barcodeWriter = new MultiFormatWriter();
        BarcodeFormat barcodeFormat;
      barcodeFormat = BarcodeFormat.CODE_128;
        //barcodeFormat   = barcodeFormat.EAN_13;
        String items_name;
        String items_barcode;
        String[] aData        ;
        String barcode_img_name;

        try{
            String strLine;
            try (FileInputStream in = new FileInputStream(parameter.print_barcode_path);BufferedReader bufffile = new BufferedReader(new InputStreamReader(in, "UTF8"))) 
            {
                strLine = bufffile.readLine();
                while (strLine != null)   {
                    System.out.println("contents:" + strLine);
                    if(!strLine.isEmpty())
                    {
                        aData = strLine.split("<<>>",2);
                        items_barcode   = aData[0];
                        items_name      = aData[1];
                        barcode_img_name= parameter.print_path + "\\" + aData[1] +"."+ imageFormat;
                        barcode_img_name = checkBarCodeStore(barcode_img_name);
                        BitMatrix bitMatrix  = barcodeWriter.encode(items_barcode,
                                barcodeFormat, width, height);
                        MatrixToImageWriter.writeToStream(bitMatrix, imageFormat,
                                new FileOutputStream(new File(barcode_img_name))); 

                        ImageIcon printImage = new javax.swing.ImageIcon(barcode_img_name);
                        gHoa_don.drawImage(printImage.getImage(), -70, pY_offset, null);
                        pY_offset += 52;
                        gHoa_don.drawString(items_name, -65, pY_offset);
                        pY_offset += 12;
                        
                    }
                    strLine = bufffile.readLine();
                }
            }
        }catch (WriterException | IOException e){//Catch exception if any
            System.err.println(e.getCause() + e.getMessage() + " cause: " + 
                    "Error!: print_barcode() + ");
        }
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
