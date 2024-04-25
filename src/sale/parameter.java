/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sale;

/**
 *
 * @author 50700
 */
public class parameter {

    public static final String goods_info = "goods_info.xls";
    
    public static final String customer_info = "customers.json";
    public static final String file_extension = ".thsv";
    public static final String print_path = "Printer";
    public static final String print_bill_path = print_path + "\\Printer_bill";
    public static final String print_barcode_path = print_path + "\\Print_barcode";

    public static final String datarepository = "data";
    public static final String config_path = datarepository + "\\configuration" + file_extension; //billConfigFile
    public static final String invoice_path = datarepository + "\\HD";
    public static final String saveBill_path = datarepository + "\\SaveBill" + file_extension;

    public static final String barcode_icon = "/sale/img/Icon_barcode.png";
    public static final String dateformat = "dd/MM/yyyy HH:mm:ss";
    
    public static final String CUSTOMERS = "SVK";
    public static final String COMMAND = "SVC";
    public static final String COMMAND_PRINT = "IN";
    
    public static final String TITLE = "Tạp hóa SÁU VÂN";
    public static final String PHONE = "(083)592.33.79";
    
    public static final String COPY_CMD = "cmd /C copy ";
    public static final String COPY_ERR = "ERROR! COPY";
    
    public static final String MARKER_CONSUMER = "==+";
    public static final String MARKER_ITEMS = "===";    
    public static final String MARKER_LOAN = "==-";


}
