/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sale;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JTextField;
//import net.sf.json.JSONSerializer;

/**
 *
 * @author Binh
 */
public final class Main_form extends javax.swing.JFrame {

    /**
     * Creates new form Main_form
     */
    Goods_price SauVan = new Goods_price();
    private Bill_Printer in_hoa_don = new Bill_Printer();

    private List<customer> Customers;
    private Vector<String> Paid_MaSP;
    private Vector<String> Paid_TenSP;
    private Vector<Double> Paid_GiaSSP;
    private Vector<Double> Paid_GiaLSP;
    private Vector<Double> Paid_SoLuongSP;
    private Vector<Double> Paid_TongGiaSP;

    private Vector<String> search_list;
    private int hand_offset = 1;
    private static DecimalFormat moneyFormat;
    private static DecimalFormat slFormat0, slFormat1, slFormat2;
    private double Money_count_Items = 0;
    private Double Money_total_bill = 0.0;
    private Double Money_custumer = 0.0;
    private Double Money_return = 0.0;
    private Vector<String> New_MaSP;

    private int result = 0;
    private int diffFloatPoint = 0;
    private String Consumer_name;
    private String file_path;
    private DefaultTableModel dm_hoa_don;
    private boolean hd_inMethodClean = false;
    private DefaultTableModel dm_info_SP;
    private DefaultComboBoxModel Cbm_history;
    private Vector curSP;
    private String curMaSP;
    private Boolean Updated_mode;
    private Boolean giaS_mode;
    //private Boolean Process_pay_mode = false;
    private History_info history_tracking;
    private static String owner_name = "";
    private static String phone_number = "";
    private static int MAX_NAME_CHAR = 1;
    private static final int saveBillBound = 15;
    private static int saveBillCount = 0;
    private int saveBillIdxNoti = 1;
    private String sDate_time;
    private double loan_amount = 0.0;
    private boolean has_loan = false;
    private boolean loan_already_on_table = false;
    private static File f;
    private static FileChannel channel;
    private static FileLock lock;

    public Main_form() {

        check_program();
        variables_allocation();
        initComponents();
        my_initialization();
        setPayTableColumnSize();
        setInfoTableColumnSize();
        display_date();
        get_bill_configure();
        load_history();
        load_customers(parameter.customer_info);
        show_status(parameter.TITLE, Color.BLUE);
        refresh();
    }

    private void variables_allocation() {

        this.Customers = new ArrayList<>();
        this.Paid_MaSP = new Vector<>();
        this.Paid_TenSP = new Vector<>();
        this.Paid_GiaSSP = new Vector<>();
        this.Paid_GiaLSP = new Vector<>();
        this.Paid_SoLuongSP = new Vector<>();
        this.Paid_TongGiaSP = new Vector<>();
        this.New_MaSP = new Vector<>();
        this.curSP = new Vector();
        this.search_list = new Vector<>();
        history_tracking = new History_info();
    }

    private static void check_program() {
        try {
            f = new File("RingOnRequest.lock");
            // Check if the lock exist
            if (f.exists()) {
                // if exist try to delete it
                f.delete();
            }
            // Try to get the lock
            channel = new RandomAccessFile(f, "rw").getChannel();
            lock = channel.tryLock();
            if (lock == null) {
                // File is lock by other application
                channel.close();
                show_dialog_OK(message.RUN_ONLY_A_PROGRAM);
                throw new RuntimeException("Only 1 instance of MyApp can run.");
            }
        } catch (IOException e) {
            show_dialog_OK(message.RUN_ONLY_A_PROGRAM);
            throw new RuntimeException("Could not start process.", e);
        }
    }

    private static void get_bill_configure() {
        try {
            try ( FileInputStream in = new FileInputStream(parameter.config_path);  BufferedReader bufffile = new BufferedReader(new InputStreamReader(in, "UTF8"))) {
                String strLine;
                strLine = bufffile.readLine();
                while (strLine != null) {
                    if (strLine.substring(0, 3).equals("=01")) {
                        phone_number = strLine.substring(3);
                    } else if (strLine.substring(0, 3).endsWith("=00")) {
                        owner_name = strLine.substring(3);
                    } else if (strLine.substring(0, 3).endsWith("=09")) {
                        MAX_NAME_CHAR = Integer.parseInt(strLine.substring(3));
                    }
                    strLine = bufffile.readLine();
                }
            }
        } catch (IOException | NumberFormatException e) {//Catch exception if any
            phone_number = parameter.PHONE;
            owner_name = parameter.TITLE;
            MAX_NAME_CHAR = 20;
        }
    }

    private static void unlockFile() {
        // release and delete file lock
        try {
            if (lock != null) {
                lock.release();
                channel.close();
                f.delete();
            }
        } catch (IOException e) {
        }
    }

    private void my_initialization() {
        button_update_database_item.setEnabled(false);
        dm_hoa_don = (DefaultTableModel) table_transaction_items.getModel();
        dm_info_SP = (DefaultTableModel) table_database_items.getModel();
        Cbm_history = (DefaultComboBoxModel) combobox_history_transaction.getModel();
        Updated_mode = false;
        giaS_mode = false;
        button_add_to_cart.setEnabled(false);
        normal_mode(false);
        giaS_mode = select_price.isSelected();
        select_price();
        lable_makhachhang.setText("Mã khách: " + parameter.CUSTOMERS);
        moneyFormat = new DecimalFormat("#,##0.0");
        slFormat0 = new DecimalFormat("#0");
        slFormat1 = new DecimalFormat("#0.0");
        slFormat2 = new DecimalFormat("#0.00");
        setframeicon();
    }

    /*BINH function*/
    private void gohome() {
        textbox_clear(textbox_barcode);
        textbox_barcode.grabFocus();
    }

    /*
     * GUI functions and initialation
     */
    private String get_textbox_text(JTextField tb) {
        return tb.getText().trim();
    }

    private boolean is_textbox_empty(JTextField tb) {
        return tb.getText().isEmpty();
    }

    private void textbox_clear(JTextField tb) {
        tb.setText("");
    }

    private void select_price() {
        cal_price_items();
        gohome();
    }

    private void setframeicon() {
        try {
            InputStream imgStream = this.getClass().getResourceAsStream(parameter.barcode_icon);
            BufferedImage bi = ImageIO.read(imgStream);
            ImageIcon myImg = new ImageIcon(bi);
            this.setIconImage(myImg.getImage());
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void get_date() {
        DateFormat dateFormat = new SimpleDateFormat(parameter.dateformat);
        Date date = new Date();
        sDate_time = dateFormat.format(date);
    }

    private void display_date() {
        DateFormat dateFormat = new SimpleDateFormat(parameter.dateformat);
        Date date = new Date();
        sDate_time = dateFormat.format(date);
        date_time.setText(sDate_time.substring(0, 10));

        String[] file_date = dateFormat.format(date).split(" ");
        String[] file_info = file_date[0].split("/");

        File File_path = new File(file_info[2]);
        if (!File_path.exists()) {
            File_path.mkdirs();
            System.out.println(message.NOT_EXITSTED_FILE + file_info[2]);
        } else {
            System.out.println(message.EXISTED_FILE);
        }

        if (File_path.exists()) {
            String backup_file = file_info[2] + "\\\\" + file_info[1] + "_"
                    + file_info[0] + parameter.goods_info;
            File bk_file_path = new File(backup_file);
            if (!bk_file_path.exists()) //copy if not exist
            {
                backup_file(parameter.goods_info, backup_file);
            }
        }
    }

    private final void backup_file(String src_file, String dest_file) {
        try {

            String line = parameter.COPY_CMD + src_file + " " + dest_file;
            System.out.println(line);
            Process p = Runtime.getRuntime().exec(line);
            p.waitFor();
        } catch (IOException | InterruptedException ex) {
            System.out.println(parameter.COPY_ERR);
        }
    }

    private final void update_bang_hoadon() {
        int row_count = table_transaction_items.getRowCount();
        Object temp;
        //int i = bang_HoaDon.getSelectedRow();
        //if( 0 <= i && i <= (bang_HoaDon.getRowCount() -2))
        for (int i = 0; i < row_count - 1; i++) {
            temp = table_transaction_items.getValueAt(i, 2);
            Paid_TenSP.set(i, temp.toString());
            temp = table_transaction_items.getValueAt(i, 3);
            Paid_GiaSSP.set(i, Double.parseDouble(temp.toString()));
            temp = table_transaction_items.getValueAt(i, 4);
            Paid_GiaLSP.set(i, Double.parseDouble(temp.toString()));
            temp = table_transaction_items.getValueAt(i, 5);
            Paid_SoLuongSP.set(i, Double.parseDouble(temp.toString()));

            calculate_item_price_row(i);
            update_item_row(i);
            System.out.println("@@" + Paid_GiaSSP.get(i) + " # " + Paid_GiaLSP.get(i)
                    + " # " + Paid_SoLuongSP.get(i) + "#" + Paid_TongGiaSP.get(i));
        }
        update_total_Paid_row();
    }

    private final void setPayTableColumnSize() {
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table_transaction_items.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 16));
        TableColumn column;
        column = table_transaction_items.getColumnModel().getColumn(0);
        column.setPreferredWidth(35);
        column.setCellRenderer(rightRenderer);
        column = table_transaction_items.getColumnModel().getColumn(1);
        column.setPreferredWidth(175);
        column = table_transaction_items.getColumnModel().getColumn(2);
        column.setPreferredWidth(495);
        column = table_transaction_items.getColumnModel().getColumn(3);
        column.setPreferredWidth(117);
        column.setCellRenderer(rightRenderer);
        column = table_transaction_items.getColumnModel().getColumn(4);
        column.setPreferredWidth(117);
        column.setCellRenderer(rightRenderer);
        column = table_transaction_items.getColumnModel().getColumn(5);
        column.setPreferredWidth(117);
        column.setCellRenderer(rightRenderer);
        column = table_transaction_items.getColumnModel().getColumn(6);
        column.setPreferredWidth(140);
        column.setCellRenderer(rightRenderer);
        column = table_transaction_items.getColumnModel().getColumn(7);
        column.setPreferredWidth(70);
        //bang_HoaDon.moveColumn(4, 5);
    }

    private final void setInfoTableColumnSize() {
        table_database_items.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 14));
        TableColumn column;
        column = table_database_items.getColumnModel().getColumn(0);
        column.setPreferredWidth(10);
        column = table_database_items.getColumnModel().getColumn(1);
        column.setPreferredWidth(250);
        column = table_database_items.getColumnModel().getColumn(2);
        column.setPreferredWidth(20);
        column = table_database_items.getColumnModel().getColumn(3);
        column.setPreferredWidth(20);
        column = table_database_items.getColumnModel().getColumn(4);
        column.setPreferredWidth(20);
        column = table_database_items.getColumnModel().getColumn(5);
        column.setPreferredWidth(5);
    }

    private int search_onpaylist(String maSP) {
        int zise_list = Paid_MaSP.size();
        for (int i = 0; i < zise_list; i++) {
            if (Paid_MaSP.get(i).equals(maSP)) {
                return i;
            }
        }
        return -1;
    }

    private void add_items_table(String goodsID) {
        int idx = SauVan.tim_sp(goodsID);
        show_status("Mã Sản phẩm :" + goodsID, Color.RED);
        if (idx == -1) //not found in data
        {
            button_update_database_item.setEnabled(true);
            add_info_SP2BTK(goodsID);
        } else //found indata
        {
            if (Updated_mode) //update mode
            {
                System.out.println("mode updates san pham " + goodsID);
                //clear_list_sp();
                clear_table(dm_hoa_don);
                update_info_SP2BTK(goodsID);
            } else //add into pay list
            {
                System.out.println("mode ban san pham " + goodsID + " index " + idx);
                addtopay(idx);
            }
        }
    }

    private void addtopay(int idx) {
        Vector CurSP;
        CurSP = SauVan.get_sp(idx);
        result = search_onpaylist(CurSP.elementAt(0).toString());
        //not found in buying list
        Double temp_Paid_soluong;
        int index_new_one;
        if (result == -1) //add new one into pay list
        {
            show_status("[Thêm] sản phẩm : " + CurSP.get(1).toString(), Color.BLUE);
            Paid_MaSP.add(CurSP.get(0).toString());
            Paid_TenSP.add(CurSP.get(1).toString());
            Paid_GiaSSP.add(Double.parseDouble(CurSP.get(2).toString()));
            Paid_GiaLSP.add(Double.parseDouble(CurSP.get(3).toString()));
            Paid_SoLuongSP.add(Double.parseDouble(CurSP.get(4).toString()));

            //calculate price for this item then add to Paid_TongGiaSP vector
            index_new_one = Paid_MaSP.size();
            calculate_item_price_row(index_new_one - 1);

            //add row into table
            add_item_row();
            add_payment_rows();
        } else //already in pay list
        {
            show_status("[đã có trong giỏ hàng] sản phẩm : " + CurSP.get(1).toString(), Color.BLUE);

            temp_Paid_soluong = Paid_SoLuongSP.get(result) + 1;
            Paid_SoLuongSP.set(result, temp_Paid_soluong);
            display_text_paylist();
            calculate_item_price_row(result);
            update_item_row(result);
            update_total_Paid_row();
        }
        dm_hoa_don.fireTableDataChanged();

    }

    private void handle_commands(String inCmd) {
        switch (inCmd) {
            case parameter.COMMAND_PRINT:
                print_receipt(true);    // true = quick print
                break;
            default:
                break;

        }
    }

    private void calculate_item_price_row(int i) {
        double temp_tong;
        if (i >= 0) {
            giaS_mode = select_price.isSelected();
            if (i == Paid_TongGiaSP.size()) //add mode
            {
                if (giaS_mode) {
                    temp_tong = Paid_SoLuongSP.get(i) * Paid_GiaSSP.get(i);
                } else {
                    temp_tong = Paid_SoLuongSP.get(i) * Paid_GiaLSP.get(i);
                }
                Paid_TongGiaSP.add(temp_tong);
            } else {
                if (giaS_mode) {
                    temp_tong = Paid_SoLuongSP.get(i) * Paid_GiaSSP.get(i);
                } else {
                    temp_tong = Paid_SoLuongSP.get(i) * Paid_GiaLSP.get(i);

                }
                Paid_TongGiaSP.set(i, temp_tong);
            }
        } else {
            show_status("CODE ERROR! :calculate_items_paid_row:" + i, Color.RED);
        }
    }

    private void update_item_row(int i) {
        table_transaction_items.getModel().setValueAt(Paid_GiaSSP.get(i), i, 3);
        table_transaction_items.getModel().setValueAt(Paid_GiaLSP.get(i), i, 4);
        table_transaction_items.getModel().setValueAt(Paid_SoLuongSP.get(i), i, 5);
        table_transaction_items.getModel().setValueAt(Paid_TongGiaSP.get(i), i, 6);
    }



    private void update_payment_rows() {
        remove_payment_rows();
        add_payment_rows();
    }

    private void remove_payment_rows() {
        remove_last_row();
        if (loan_already_on_table) {
            remove_last_row();
            remove_last_row();
        }
    }

    private void remove_last_row() {
        int rowcount = table_transaction_items.getRowCount();
        if (rowcount != 0) {
            dm_hoa_don.removeRow(rowcount - 1);
        }
    }

    private void add_item_row() {
        remove_payment_rows();

        int last_row = Paid_MaSP.size() - 1;

        Vector data_row = new Vector();
        data_row.add(last_row + 1);
        data_row.add(Paid_MaSP.get(last_row));
        data_row.add(Paid_TenSP.get(last_row));
        data_row.add(Paid_GiaSSP.get(last_row));
        data_row.add(Paid_GiaLSP.get(last_row));
        data_row.add(Paid_SoLuongSP.get(last_row));
        data_row.add(Paid_TongGiaSP.get(last_row));
        data_row.add(Boolean.TRUE);
        dm_hoa_don.addRow(data_row);
    }

    private void get_loan_from_textbox(JTextField tb) {
        get_loan_from_string(tb.getText());
    }

    private void get_loan_from_string(String str_loan) {
        if (!str_loan.trim().isEmpty()) {
            try {
                loan_amount = Double.parseDouble(str_loan);

            } catch (NumberFormatException a) {
                show_status("Nhập sai!", Color.RED);
            }
        } else {
            loan_amount = 0.0;
        }
        has_loan = loan_amount > 0;
    }



    private void add_payment_rows() {

        add_sub_total_row();
        add_total_and_loan_row();
    }

    private void add_sub_total_row() {
        calculate_sub_total_price();
        add_vector_row(null, null, null, null, null, Money_count_Items, Money_total_bill, Boolean.FALSE);
    }

    private void add_item_total_row() {
        double temp_total = Money_total_bill + loan_amount;
        add_vector_row(null, null, null, null, null, "Tổng tiền", temp_total, Boolean.FALSE);

    }

    private void add_total_and_loan_row() {
        if (has_loan) {
            loan_already_on_table = true;
            add_loan_row();
            add_item_total_row();
        }

    }

    private void add_loan_row() {
        add_vector_row(null, null, null, null, null, "Nợ cũ", loan_amount, Boolean.FALSE);
    }

    private void add_vector_row(Object c0, Object c1, Object c2, Object c3,
            Object c4, Object c5, Object c6, Boolean c7) {
        Vector sVector = new Vector();
        sVector.add(c0);
        sVector.add(c1);
        sVector.add(c2);
        sVector.add(c3);
        sVector.add(c4);
        sVector.add(c5);  //so luong
        sVector.add(c6);  // tien
        sVector.add(c7);
        dm_hoa_don.addRow(sVector);
    }

    private void calculate_sub_total_price() {
        int sizerow = Paid_MaSP.size();
        Money_count_Items = 0;
        Money_total_bill = 0.0;
        for (int i = 0; i < sizerow; i++) {
            if ((boolean) table_transaction_items.getValueAt(i, 7)) {
                Money_count_Items += Paid_SoLuongSP.get(i);
                Money_total_bill += Paid_TongGiaSP.get(i);
            }
        }
    }

    private void update_total_Paid_row() {
        int sizerow = Paid_MaSP.size();
        calculate_sub_total_price();
        table_transaction_items.getModel().setValueAt(Money_count_Items, sizerow, 5);
        table_transaction_items.getModel().setValueAt(Money_total_bill, sizerow, 6);
    }

    private void add_info_SP2BTK(String iMaSP) {
        int new_MaSP_size = New_MaSP.size();
        int i;
        int isoluong_update;
        Object osoluong_update;
        result = -1;
        Vector data_row = new Vector();
        for (i = 0; i < new_MaSP_size; i++) {
            if (New_MaSP.get(i).equals(iMaSP)) {
                result = i;
                break;
            }
        }

        if (result == -1) {
            New_MaSP.add(iMaSP);
            data_row.add(iMaSP);
            data_row.add("Ten");
            data_row.add("0");
            data_row.add("0");
            data_row.add("1");
            data_row.add(Boolean.TRUE);
            dm_info_SP.addRow(data_row);
        } else //already have one
        {

            osoluong_update = table_database_items.getModel().getValueAt(i, 4);
            isoluong_update = Integer.parseInt(osoluong_update.toString()) + 1;
            table_database_items.setValueAt(isoluong_update, i, 4);
            show_status("warning: Đã có trong bảng cập nhập!", Color.BLUE);
        }
    }

    private void update_info_SP2BTK(String updateMaSP) {
        int j;
        int i = 0;
        int update_MaSP_size = New_MaSP.size();
        result = -1;
        Vector update_info_sp;
        show_status("", Color.BLUE);
        for (j = 0; j < update_MaSP_size; j++) {
            if (New_MaSP.get(j).equals(updateMaSP)) {
                result = 0;
                break;
            }
        }
        if (result != 0) {
            i = SauVan.tim_sp(updateMaSP);
            update_info_sp = SauVan.get_sp(i);
            update_info_sp.add(Boolean.TRUE);
            dm_info_SP.addRow(update_info_sp);
            New_MaSP.add(updateMaSP);
        } else {
            show_status("Đã có trong danh sách: hàng " + i + 1, Color.RED);
        }
    }

    private void cal_price_items() {
        int paid_count = Paid_MaSP.size();
        double temp_price;
        giaS_mode = select_price.isSelected();
        if (giaS_mode) {
            select_price.setText("Đang chọn giá Sỹ");
            select_price.setToolTipText("GIÁ SỸ");
        } else {
            select_price.setText("Đang chọn giá Lẻ");
            select_price.setToolTipText("GIÁ LẺ");
        }
        if (paid_count > 0) {
            for (int i = 0; i < paid_count; i++) {
                if ((boolean) table_transaction_items.getValueAt(i, 7)) {
                    if (giaS_mode) //gia sy
                    {
                        temp_price = Paid_GiaSSP.get(i) * (Paid_SoLuongSP.get(i));
                    } else {
                        temp_price = Paid_GiaLSP.get(i) * (Paid_SoLuongSP.get(i));
                    }
                    Paid_TongGiaSP.set(i, temp_price);
                }
                table_transaction_items.setValueAt(Paid_TongGiaSP.get(i), i, 6);
            }
            update_payment_rows();
            //update_total_Paid_row();
        }
    }

    private int review_bill() {
        String sNotification = "";
        String sPadding = "...............................................";
        String ten_sp;
        int items_count = Paid_MaSP.size();
        for (int i = 0; i < items_count; i++) {
            ten_sp = Paid_TenSP.get(i) + sPadding + sPadding;
            ten_sp = ten_sp.substring(0, 40).trim();
            sNotification += Paid_SoLuongSP.get(i) + " " + ten_sp
                    + " " + Paid_TongGiaSP.get(i) + "\n";
            if (i >= 25) {
                sNotification += "Không đủ không gian để hiển thị\n";
                break;
            }
        }
        sNotification += "-------------------------------------------------\n";
        sNotification += "Tổng sl: " + Money_count_Items + ", Tiền: "
                + Money_total_bill + "\n";

        get_loan_from_textbox(textbox_loan);

        double tong_gia = Money_total_bill + loan_amount;
        sNotification += "Nợ cũ: " + loan_amount + ", Tổng tiền: " + tong_gia + "\n";
        int n = JOptionPane.showOptionDialog(null,
                sNotification,
                message.CONFIRM_BILL,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"In", "Chỉnh lại"}, "default");
        return n;
    }

    private int confirm_customer(String inmessage) {
        int n = JOptionPane.showOptionDialog(null,
                inmessage,
                message.CONFIRM_UPDATE,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                new String[]{"Cập nhập mới", "Chọn mã khách mới"}, "default");
        return n;
    }

    private void update_customer(String customerID, String customerName) {
        // create then add new customer
        int max_name_length;
        customer newCustomer = new customer(customerID, customerName, 0);
        for (int i = 0; i < Paid_MaSP.size(); i++) {
            Item newitem = new Item();
            newitem.ID = Paid_MaSP.elementAt(i);
            newitem.Name = Paid_TenSP.elementAt(i);
            newitem.Quanlity = Paid_SoLuongSP.elementAt(i);
            newitem.Wholesale_price = Paid_GiaSSP.elementAt(i);
            newitem.Retail_price = Paid_GiaLSP.elementAt(i);
            newCustomer.Items.add(newitem);
        }
        Customers.add(newCustomer);

        // strim long name by 50 chars
        for (int i = 0; i < Customers.size(); i++) {
            max_name_length = Math.min(Customers.get(i).Name.length(), 50);
            Customers.get(i).Name = Customers.get(i).Name.substring(0, max_name_length);
            for (int j = 0; j < Customers.get(i).Items.size(); j++) {
                max_name_length = Math.min(Customers.get(i).Items.get(j).Name.length(), 50);
                Customers.get(i).Items.get(j).Name = Customers.get(i).Items.get(j).Name.substring(0, max_name_length);
            }

        }
        DateFormat dateFormat = new SimpleDateFormat(parameter.dateformat);
        Date date = new Date();
        sDate_time = dateFormat.format(date);
        date_time.setText(sDate_time.substring(0, 10));

        String[] file_date = dateFormat.format(date).split(" ");
        String[] file_info = file_date[0].split("/");

        String bk_file = file_info[2] + "\\\\" + file_info[1] + "_"
                + file_info[0] + parameter.customer_info;

        System.out.println(bk_file);
        backup_file(parameter.customer_info, bk_file);
        save_customers(parameter.customer_info);    //update database
        show_dialog_OK("Cập nhập thành công\nKhách hàng: " + customerName
                + "\nMã: " + customerID);
    }

    private void pre_paid() {
        int i, count = 1;
        get_date();
        if (!Paid_MaSP.isEmpty() && Money_count_Items != 0) {
            normal_mode(true);
            combobox_history_transaction.setEnabled(false);
            Vector Final_paid_SelectSP = new Vector<>();
            for (i = 0; i < Paid_MaSP.size(); i++) {
                Final_paid_SelectSP.add(table_transaction_items.getValueAt(i, 7));
            }
            clear_table(dm_hoa_don);
            clear_table(dm_info_SP);

            for (i = 0; i < Paid_MaSP.size(); i++) {
                System.out.println(i + "--" + Final_paid_SelectSP.get(i));
                Vector data_row = new Vector();
                data_row.add(Paid_MaSP.get(i));
                data_row.add(Paid_TenSP.get(i));
                data_row.add(Paid_GiaSSP.get(i));
                data_row.add(Paid_GiaLSP.get(i));
                data_row.add(Paid_SoLuongSP.get(i));
                if ((boolean) Final_paid_SelectSP.get(i)) //add row to pay table
                {
                    data_row.add(0, count++);
                    data_row.add(Paid_TongGiaSP.get(i));
                    data_row.add(Boolean.TRUE);
                    dm_hoa_don.addRow(data_row);
                } else //add row to info table
                {
                    data_row.add(Boolean.TRUE);
                    New_MaSP.add(Paid_MaSP.get(i));
                    remove_element_paylist(i);
                    Final_paid_SelectSP.removeElementAt(i);
                    i--;
                    dm_info_SP.addRow(data_row);
                }
                display_text_paylist();
                dm_hoa_don.fireTableDataChanged();
            }
            add_sub_total_row();
        } else {
            show_status(message.BILL_IS_EMPTY, Color.BLUE);
        }

        gohome();
    }

    private int paid_function() {
        int return_val;
        Consumer_name = get_textbox_text(textbox_consumer_name);
        if (Consumer_name.equals("")) {
            Consumer_name = "..........";
        }
        get_loan_from_textbox(textbox_loan);
        //loan_amount = 0.0;
        //if (! is_textbox_empty(textbox_loan)){
        //if (!textbox_loan.getText().isEmpty()) {
        //    try {
        //        loan_amount = Double.parseDouble(textbox_loan.getText());
        //    } catch (NumberFormatException e) {
        //        show_status("Nhập sai số nợ!", Color.RED);
        //        loan_amount = 0.0;
        //    }
        //}
        cal_price_items();
        //handle_Consumer_name();
        try {
            Money_return = Money_custumer - Money_total_bill;
            write2datafile();
            return_val = 0;
        } catch (NumberFormatException e) {
            show_status("Nhập sai số tiền lấy vào!", Color.RED);
            return_val = -1;
        }
        return return_val;
    }

    private int write2datafile() {
        int return_val = 0;
        int paid_count = Paid_MaSP.size();
        String file_date = sDate_time;
        System.out.println("_________________________________________" + file_date);
        String[] time_info = file_date.split(" ");
        String[] file_info = time_info[0].split("/");
        String file_dir = file_info[2] + "\\" + file_info[1];
        if (paid_count != 0) {
            try {
                // Create file 
                File Fdir = new File(file_dir);
                boolean a = Fdir.mkdirs();
                if (a || Fdir.isDirectory()) {
                    file_path = file_dir + "\\" + file_info[0] + parameter.file_extension;
                    combobox_history_transaction.addItem(time_info[1] + "<>" + Consumer_name);
                    FileOutputStream fos = new FileOutputStream(file_path, true);
                    Writer out = new OutputStreamWriter(fos, "UTF8");
                    {
                        out.write("-->" + file_date + "<>" + Consumer_name + "\n");

                        for (int i = 0; i < paid_count; i++) {
                            out.write("---" + Paid_MaSP.get(i) + "<>"
                                    + Paid_TenSP.get(i) + "<>" + Paid_SoLuongSP.get(i)
                                    + "<>" + Paid_TongGiaSP.get(i)
                                    + "<>" + Paid_GiaSSP.get(i)
                                    + "<>" + Paid_GiaLSP.get(i) + "\n");
                            SauVan.update_distribution(Paid_MaSP.get(i));
                        }

                        out.write("==>" + Money_count_Items + "<>"
                                + Money_total_bill + "<>" + Money_custumer + "<>"
                                + Money_return + "\n");
                        out.write("==-" + loan_amount + "\n");
                        out.close();
                    }
                }
            } catch (IOException e) {//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            }
        }
        return return_val;
    }

    private void normal_mode(Boolean ibool) {

        button_pay_print_bill.setVisible(ibool);
        button_update_items.setEnabled(!ibool);
    }

    private int show_message(String imessage) {
        int n = JOptionPane.showOptionDialog(null,
                imessage,
                parameter.TITLE,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Có", "Không"}, "default");
        return n;
    }

    private static void show_dialog_OK(String in_message) {
        JOptionPane.showOptionDialog(null,
                in_message,
                parameter.TITLE,
                JOptionPane.OK_OPTION, JOptionPane.WARNING_MESSAGE,
                null, new String[]{"OK"}, "default");
    }

    private void Print_bill_process() {
        try {
            File Fdir = new File("Printer");
            boolean a = Fdir.mkdirs();
            if (a || Fdir.isDirectory()) {
                FileOutputStream fos = new FileOutputStream("Printer\\Printer_bill");
                try ( Writer out = new OutputStreamWriter(fos, "UTF8")) {
                    //get format number of item row
                    diffFloatPoint = diff_slFormat();

                    String raw_data = Print_header2s();
                    out.write(raw_data);
                    raw_data = Print_contents2s();
                    out.write(raw_data);
                    raw_data = Print_money2s();
                    out.write(raw_data);
                    if (loan_amount > 0) {
                        out.write("==-" + loan_amount + "\n");
                    }
                }
                in_hoa_don.print_handle(0);
            }
        } catch (IOException ex) {
            System.out.println(message.NO_RECEIPT_FILE);
        }
    }

    private String Print_header2s() {
        String sData_row;
        boolean savespace = false;
        Consumer_name = get_textbox_text(textbox_consumer_name);
        //ten_khach_hang.getText().trim();
        if (Consumer_name.isEmpty()) {
            Consumer_name = ".........................";
        }
        if (Consumer_name.contains("......")) {
            savespace = true;
        }
        sData_row = "Ngày: " + sDate_time + "\n";
        if (!owner_name.isEmpty()) {
            sData_row += owner_name;
            if (!savespace) {
                sData_row += "\n";
            } else {
                sData_row += ", ";
            }
        }
        if (!phone_number.isEmpty()) {
            sData_row += "ĐT: " + phone_number + "\n";
        }
        if (!savespace) {
            sData_row += "Khách hàng: " + Consumer_name + "\n";
        }
        return sData_row;
    }

    private void Print_barcode() {
        in_hoa_don.print_handle(1);  //1 mean print barcode
    }

    private int get_info_barcode() {
        int return_val = -1;

        int iprint_size = table_transaction_items.getRowCount();
        if (iprint_size > 0) {
            pre_paid();
            normal_mode(false);
            return_val = 0;
            iprint_size = Paid_MaSP.size();
            try {
                // Create file 
                File Fdir = new File(parameter.print_path);
                boolean a = Fdir.mkdirs();
                if (a || Fdir.isDirectory()) {
                    FileOutputStream fos = new FileOutputStream(parameter.print_barcode_path);
                    Writer out = new OutputStreamWriter(fos, "UTF8");
                    {
                        for (int i = 0; i < iprint_size; i++) {
                            out.write(Paid_MaSP.get(i) + "<<>>"
                                    + Paid_TenSP.get(i) + "\n");
                        }
                        out.close();
                    }
                }

            } catch (FileNotFoundException ex) {
                show_status("Print_barcode: FileNotFoundException", Color.red);
                return_val = -1;
            } catch (UnsupportedEncodingException ex) {
                return_val = -2;
                show_status("Print_barcode: FileNotFoundException", Color.red);
            } catch (IOException ex) {
                return_val = -3;
                show_status("Print_barcode: FileNotFoundException", Color.red);
            }
        } else {
            show_status("Không có Barcode trong bảng!", Color.RED);
        }
        return return_val;
    }

    private String Print_contents2s() {
        String sData_row;
        int idata_size;
        sData_row = "Stt Tên sản phẩm             sl  Tiền(1000đ)\n";
        sData_row = sData_row + "-----------------------------------------------------------\n";
        idata_size = Paid_MaSP.size();
        for (int i = 0; i < idata_size; i++) {
            sData_row = sData_row + print_get_content(i);
        }
        sData_row = sData_row + "-----------------------------------------------------------\n";
        return sData_row;
    }

    private String Print_money2s() {
        String sPrice = moneyFormat.format(Money_total_bill);
        String sSumitem = formatNumber(Money_count_Items, diffFloatPoint);
        String sMoney = "==>" + sSumitem + "<<>>" + sPrice + "\n";
        return sMoney;
    }

    private String print_get_content(int i) {
        String print_row;
        String ten_sanpham = Paid_TenSP.get(i).trim();
        String soluong = formatNumber(Paid_SoLuongSP.get(i), diffFloatPoint);
        String Thanh_tien = moneyFormat.format(Paid_TongGiaSP.get(i));
        if (ten_sanpham.length() > MAX_NAME_CHAR) {
            ten_sanpham = ten_sanpham.substring(0, MAX_NAME_CHAR);
        }
        if (ten_sanpham.length() >= 2) {
            if (ten_sanpham.length() < 15) {
                ten_sanpham = ten_sanpham.substring(0, 1).toUpperCase()
                        + ten_sanpham.substring(1);
            } else {
                ten_sanpham = ten_sanpham.substring(0, 1).toUpperCase()
                        + ten_sanpham.substring(1).toLowerCase();
            }
        }
        if (ten_sanpham.isEmpty()) {
            ten_sanpham = "Lỗi: Tên ngắn quá!";
        }
        print_row = "--+" + ten_sanpham + "<<>>" + soluong + "<<>>" + Thanh_tien + "\n";
        return print_row;
    }

    private void print_receipt(boolean quickprint) {
        if (table_transaction_items.getRowCount() != 0) {
            int return_val;
            pre_paid();

            if (quickprint) // print by cmd, skip review part
            {
                return_val = 0;
            } else {
                return_val = review_bill();
            }
            if (return_val == 0) {
                return_val = paid_function();
            }
            if (return_val == 0) {
                Print_bill_process();
                show_status("Đã thanh toán: " + Money_count_Items + " mặt hàng, Giá: "
                        + moneyFormat.format(Money_total_bill) + ", Nợ cũ: "
                        + loan_amount + ", Tổng giá: "
                        + moneyFormat.format(Money_total_bill + loan_amount), Color.BLUE);
                refresh();

            }
        } else {
            show_status(message.BILL_IS_EMPTY, Color.BLUE);
        }
        gohome();
    }

    private void in_ma_vach() {
        int return_val = get_info_barcode();
        if (return_val == 0) {
            return_val = show_message("Xác nhận in barcode cho sản phẩm\n");
            if (return_val == 0) {
                Print_barcode();
            }
        }
        combobox_history_transaction.setEnabled(true);
    }

    private void update_customers() {
        int cont;
        pre_paid();

        if (!is_textbox_empty(textbox_new_consumer_ID)) {
            lable_makhachhang.setForeground(Color.blue);
            String customerID = parameter.CUSTOMERS + " " + get_textbox_text(textbox_new_consumer_ID);
            String customerName = get_textbox_text(textbox_consumer_name);
            int idx = is_exist_customer(customerID);
            if (idx != -1) {
                String message = "Mã khách hàng:[" + customerID + "] \nđã gắng cho khách["
                        + Customers.get(idx).Name + "]";
                cont = confirm_customer(message);
                if (cont == 0) {   // force to update database
                    // remove existed customer then update/add new one
                    Customers.remove(idx);

                    update_customer(customerID, customerName);
                    refresh();

                } else {// come back and pick new customer ID
                    // do nothing
                }
            } else {
                update_customer(customerID, customerName);
                refresh();
            }

        } else {
            show_dialog_OK(message.CUSTOMER_ID_NULL);
            lable_makhachhang.setForeground(Color.RED);
        }

    }

    private void update_paid_table() {
        Updated_mode = true;
        button_update_database_item.setEnabled(true);

        clear_Updated_list();
        clear_table(dm_info_SP);
        clear_table(dm_hoa_don);
        clear_Paid_list();
        gohome();
    }

    private double calprice(String foo) {
        double temp_result = -99999.0;
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        if (!foo.trim().isEmpty()) {
            try {

                temp_result = Double.parseDouble(engine.eval(foo).toString());
            } catch (ScriptException ex) {
                temp_result = -99999;
            }
        }
        return temp_result;
    }

    private String formatNumber(Double inNum, int mode) {
        String return_string;
        switch (mode) {
            case 0:
                return_string = slFormat0.format(inNum);
                break;
            case 1:
                return_string = slFormat1.format(inNum);
                break;
            default:
                return_string = slFormat2.format(inNum);
                break;
        }
        return return_string;
    }

    private void refresh() {
        // clear data 
        clear_Paid_list();
        clear_Updated_list();
        clear_hand_list();
        clear_loan();
        
        // clear GUI
        clear_GUI_table();
        clear_GUI_textbox();
        normal_mode(false);
        Updated_mode = false;

        combobox_history_transaction.setEnabled(true);
        
        gohome();
    }

    private void setup_configuration() {
        Configuration_system SVtool = new Configuration_system();
        boolean return_result = SVtool.LoadConfigureInfo();
        if (!return_result) {
            SVtool.defaulConfigureInfo();
        }
        SVtool.setVisible(true);
    }

    private boolean savebill(String saveFile) {
        boolean return_val = true;
        String unknownComsumer = "......................";

        if (is_textbox_empty(textbox_loan)) {
            textbox_loan.setText("0.0");
        }
        try {
            Double.parseDouble(textbox_loan.getText());
        } catch (NumberFormatException e) {
            show_message("Lỗi, Coi lại số nợ");
            return_val = false;
        }
        if (return_val) {
            try {
                FileOutputStream fos = new FileOutputStream(saveFile);
                Writer out = new OutputStreamWriter(fos, "UTF8");
                {
                    int paid_count = Paid_MaSP.size();

                    if (is_textbox_empty(textbox_consumer_name)) {
                        //if (textbox_consumer_name.getText().trim().isEmpty()) {
                        textbox_consumer_name.setText(unknownComsumer);
                    }
                    out.write(parameter.MARKER_CONSUMER + get_textbox_text(textbox_consumer_name) + "\n");
                    for (int i = 0; i < paid_count; i++) {
                        out.write(parameter.MARKER_ITEMS + Paid_MaSP.get(i) + "<>"
                                + Paid_TenSP.get(i) + "<>"
                                + Paid_GiaSSP.get(i) + "<>"
                                + Paid_GiaLSP.get(i) + "<>"
                                + Paid_SoLuongSP.get(i) + "<>"
                                + Paid_TongGiaSP.get(i) + "\n");
                    }
                    out.write(parameter.MARKER_LOAN + get_textbox_text(textbox_loan) + "\n");
                    out.close();
                }
            } catch (IOException e) {//Catch exception if any
                show_status(message.OPEN_FILE_ERR + saveFile, Color.RED);
                return_val = false;
            }
        }
        return return_val;
    }

    private void buttonSaveBillsetText(int i) {
        button_save_temp_transaction.setText("Lưu hóa đơn [" + i + "]");
        if (i != 0) {
            button_save_temp_transaction.setForeground(Color.red);
        } else {
            button_save_temp_transaction.setForeground(Color.blue);
        }
    }

    private void buttonSaveBill() {
        boolean pass_result;
        if (!Paid_MaSP.isEmpty() && Money_count_Items != 0) {
            int curSaveBillIdx = saveBillCount % saveBillBound;
            String savefile = parameter.saveBill_path + curSaveBillIdx;
            pass_result = savebill(savefile);

            if (pass_result) {
                buttonSaveBillsetText(saveBillIdxNoti++);
                saveBillCount++;
                ComboBox_SaveBill.insertItemAt("Hóa đơn[" + saveBillCount + "]"
                        + get_textbox_text(textbox_consumer_name), curSaveBillIdx);
                if (ComboBox_SaveBill.getItemCount() > saveBillBound) {
                    ComboBox_SaveBill.removeItemAt(curSaveBillIdx + 1);
                }
                clear_table(dm_hoa_don);
                clear_Paid_list();
            } else {
                show_status(message.SAVE_ERR, Color.RED);
            }
        } else {
            show_status(message.CART_IS_EMPTY, Color.blue);
        }

    }

    private void LoadSavedBill() {
        if (ComboBox_SaveBill.getItemCount() > 0) {
            int idxFile = ComboBox_SaveBill.getSelectedIndex();
            String loadFile = parameter.saveBill_path + idxFile;
            int count = 1;
            clear_Paid_list();
            clear_table(dm_hoa_don);
            try {
                try ( FileInputStream in = new FileInputStream(loadFile)) {
                    show_status(loadFile, Color.blue);
                    try ( BufferedReader bufffile = new BufferedReader(new InputStreamReader(in, "UTF8"))) {
                        String strLine;
                        strLine = bufffile.readLine();
                        while (strLine != null) {
                            switch (strLine.substring(0, 3)) {
                                case parameter.MARKER_CONSUMER:
                                    textbox_consumer_name.setText(strLine.substring(3));
                                    break;
                                case parameter.MARKER_ITEMS:
                                    String sdata[] = strLine.substring(3).split("<>");
                                    Vector sVector = new Vector();
                                    sVector.add(count++);
                                    sVector.add(sdata[0]);  //ma sp
                                    sVector.add(sdata[1]);  //ten sp
                                    sVector.add(sdata[2]);  //gia s
                                    sVector.add(sdata[3]);  //gia l
                                    sVector.add(sdata[4]);  //so luong
                                    sVector.add(sdata[5]);  // tien
                                    sVector.add(Boolean.TRUE);
                                    dm_hoa_don.addRow(sVector);
                                    dm_hoa_don.fireTableDataChanged();
                                    Paid_MaSP.add(sdata[0]);
                                    Paid_TenSP.add(sdata[1]);
                                    Paid_GiaSSP.add(Double.parseDouble(sdata[2]));
                                    Paid_GiaLSP.add(Double.parseDouble(sdata[3]));
                                    Paid_SoLuongSP.add(Double.parseDouble(sdata[4]));
                                    Paid_TongGiaSP.add(Double.parseDouble(sdata[5]));
                                    //
                                    break;
                                case parameter.MARKER_LOAN:
                                    textbox_loan.setText(strLine.substring(3));
                                    break;
                                default:
                                    break;
                            }
                            strLine = bufffile.readLine();
                        }
                        if (dm_hoa_don.getRowCount() > 0) {
                            add_sub_total_row();
                        } else {
                            clear_Paid_list();
                            clear_table(dm_hoa_don);
                            show_status("[Lỗi] refresh để làm lại", Color.RED);
                        }
                    }
                }
            } catch (IOException | NumberFormatException e) {//Catch exception if any
                show_status("Lỗi" + e.getMessage(), Color.RED);
            }
        } else {
            show_status("Chưa có hóa đơn nào được lựa", Color.blue);
        }
    }

    private void add_to_history() {
        String sHistory = Cbm_history.getSelectedItem().toString();
        System.out.println("sHistory:" + sHistory + ", file name " + file_path);
        if (!sHistory.isEmpty()) {
            String name[] = sHistory.split("<>");
            boolean found = false;
            int count = 1;
            textbox_consumer_name.setText(name[1]);

            try {
                try ( FileInputStream in = new FileInputStream(file_path);  BufferedReader bufffile = new BufferedReader(new InputStreamReader(in, "UTF8"))) {
                    String strLine;
                    strLine = bufffile.readLine();
                    OUTER:
                    while (strLine != null) {
                        if (strLine.contains(sHistory)) {
                            found = true;
                            clear_Paid_list();
                            clear_table(dm_hoa_don);
                        }
                        if (found) {
                            switch (strLine.substring(0, 3)) {
                                case "---": {
                                    String sdata[] = strLine.substring(3).split("<>");
                                    add_vector_row(count++, sdata[0], sdata[1], sdata[4], sdata[5], sdata[2], sdata[3], Boolean.TRUE);
                                    dm_hoa_don.fireTableDataChanged();
                                    
                                    Paid_MaSP.add(sdata[0]);
                                    Paid_TenSP.add(sdata[1]);
                                    Paid_GiaSSP.add(Double.parseDouble(sdata[4]));
                                    Paid_GiaLSP.add(Double.parseDouble(sdata[5]));
                                    Paid_SoLuongSP.add(Double.parseDouble(sdata[2]));
                                    Paid_TongGiaSP.add(Double.parseDouble(sdata[3]));
                                    display_text_paylist();
                                    break;
                                }
                                case parameter.MARKER_LOAN: //no cu
                                {
                                    String sdata[] = strLine.substring(3).split("<>");
                                    get_loan_from_string(sdata[0]);
                                    textbox_loan.setText(sdata[0]);
                                    add_payment_rows();
                                    break OUTER;
                                }
                                default:
                                    
                                    break;
                            }
                        }
                        strLine = bufffile.readLine();
                    }
                }
            } catch (IOException | NumberFormatException e) {//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private int writefile(String filename, String filedata) {

        int return_val = -1;
        FileOutputStream fos;
        Writer out;
        try {
            fos = new FileOutputStream(filename, false);
            out = new OutputStreamWriter(fos, "UTF8");
            out.write(filedata);
            out.close();
            return_val = 0;

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main_form.class
                    .getName()).log(Level.SEVERE, null, ex);

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Main_form.class
                    .getName()).log(Level.SEVERE, null, ex);

        } catch (IOException ex) {
            Logger.getLogger(Main_form.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return return_val;
    }

    private int load_history() {
        int returned_result = 0;
        String file_date = sDate_time;
        String[] time_info = file_date.split(" ");
        String[] file_info = time_info[0].split("/");
        String file_dir = file_info[2] + "\\" + file_info[1];

        file_path = file_dir + "\\" + file_info[0] + parameter.file_extension;
        FileInputStream in;
        try {
            in = new FileInputStream(file_path);
            BufferedReader bufffile = new BufferedReader(new InputStreamReader(in, "UTF8"));
            String strLine;
            strLine = bufffile.readLine();
            OUTER:
            while (strLine != null) {
                if (strLine.substring(0, 3).equals("-->")) {
                    String[] sold_bill = strLine.split(" ", 2);
                    combobox_history_transaction.addItem(sold_bill[1]);
                }
                System.out.println(strLine);
                strLine = bufffile.readLine();
            }
        } catch (FileNotFoundException ex) {
            returned_result = -1;
            Logger.getLogger(Main_form.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            returned_result = -1;
            Logger.getLogger(Main_form.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            returned_result = -1;
            Logger.getLogger(Main_form.class.getName()).log(Level.SEVERE, null, ex);
        }

        return returned_result;
    }

    private int load_customers(String customer_file) {
        int return_num = 0;
        try {
            // TODO add your handling code here:
            System.out.println("customer_file: " + customer_file);
            String str_curtomer = new String(
                    Files.readAllBytes(Paths.get(customer_file)));
            //System.out.println("str_curtomer: " + str_curtomer);
            Customers
                    = new JSONDeserializer<List<customer>>()
                            .use(null, ArrayList.class)
                            .use("values", customer.class)
                            .deserialize(str_curtomer);

            return return_num;
        } catch (IOException ex) {
            return_num = -1;
            Logger
                    .getLogger(Main_form.class
                            .getName()).log(Level.SEVERE, null, ex);
        }
        return return_num;
    }

    private int save_customers(String customer_file) {
        JSONSerializer jsonserializer = new JSONSerializer();
        jsonserializer.prettyPrint(true);
        String str_json = jsonserializer.deepSerialize(Customers);
        writefile(customer_file, str_json);
        return result;
    }

    private int is_exist_customer(String customerID) {
        int idx = 0;
        boolean found = false;
        for (customer Customer : Customers) {
            if (Customer.ID.equals(customerID)) {
                found = true;
                break;
            }
            idx++;
        }
        if (!found) {
            idx = -1;
        }
        return idx;
    }

    private void exit_program() {
        result = show_message(message.CONFIRM_EXIT);
        if (result == 0) {
            SauVan.reorder_file_data();
            System.exit(0);
        }
        unlockFile();
    }

    private void show_status(String message, Color color) {
        Thong_bao_text.setText(message);
        Thong_bao_text.setForeground(color);
    }

    private void display_result2infoTable(Vector array_result) {
        int index_found;
        Vector update_info_sp;
        search_list.removeAllElements();
        //clear_table(dm_info_SP);
        int old_result_count = dm_info_SP.getRowCount();

        for (int i = old_result_count - 1; i >= 0; i--) {
            if (!(boolean) dm_info_SP.getValueAt(i, 5)) {
                dm_info_SP.removeRow(i);
            } else {
                search_list.add(dm_info_SP.getValueAt(i, 0).toString());
            }
        }
        for (int i = 0; i < array_result.size(); i++) {
            index_found = Integer.parseInt(array_result.get(i).toString());
            int new_masp_size = search_list.size();
            int ibreak = 0;
            update_info_sp = SauVan.get_sp(index_found);
            System.out.println("size " + new_masp_size);
            for (int j = 0; j < new_masp_size; j++) {
                if (search_list.get(j).equals(update_info_sp.get(0))) {
                    ibreak = -1;
                    break;
                }
            }
            if (ibreak != -1) {
                update_info_sp.add(Boolean.FALSE);
                dm_info_SP.insertRow(0, update_info_sp);
            }

        }
    }

    private int diff_slFormat() {
        int size = Paid_SoLuongSP.size();
        double dnum;
        double dnum1;
        int return_val = 0;
        int inum;
        int diff;
        for (int i = 0; i < size; i++) {
            dnum = Paid_SoLuongSP.get(i);
            inum = Paid_SoLuongSP.get(i).intValue();
            dnum1 = Double.parseDouble(slFormat1.format(Paid_SoLuongSP.get(i)));
            if (dnum == inum) {
                diff = 0;
            } else if (dnum == dnum1) {
                diff = 1;
            } else {
                diff = 2;
            }
            if (diff > return_val) {
                return_val = diff;
                if (return_val == 2) {
                    break;
                }
            }
        }
        return return_val;
    }

    private void clear_Updated_list() {
        New_MaSP.clear();
    }

    private void clear_hand_list() {
        hand_offset = 1;
    }

    private void clear_Paid_list() {
        Paid_GiaLSP.clear();
        Paid_GiaSSP.clear();
        Paid_MaSP.clear();
        Paid_TenSP.clear();
        Paid_TongGiaSP.clear();
        Paid_SoLuongSP.clear();
    }
    private void clear_loan()
    {
        loan_amount = 0;
        has_loan = false;
        loan_already_on_table = false;
    }
    
    private void clear_table(DefaultTableModel tableModel) {
        tableModel.getDataVector().removeAllElements();
        tableModel.fireTableDataChanged(); // notifies the JTable that the model has changed
    }
    
    private void clear_GUI_table()
    {
        clear_table(dm_hoa_don);
        clear_table(dm_info_SP);
    }
    
    private void clear_GUI_textbox()
    {
        textbox_clear(textbox_search_ID);
        textbox_clear(textbox_search_name);
        textbox_clear(textbox_new_consumer_ID);
        textbox_clear(textbox_consumer_name);
        textbox_clear(textbox_loan);
    }
    private URL getResource(String myimagejpg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void display_text_paylist() {
        System.out.println("Size :" + Paid_MaSP.size() + "-"
                + Paid_TenSP.size() + "-" + Paid_GiaLSP.size() + "-"
                + Paid_GiaSSP.size() + "-" + Paid_SoLuongSP.size() + "-"
                + Paid_TongGiaSP.size());
    }

    private void remove_element_paylist(int i) {
        Paid_MaSP.removeElementAt(i);
        Paid_TenSP.removeElementAt(i);
        Paid_GiaLSP.removeElementAt(i);
        Paid_GiaSSP.removeElementAt(i);
        Paid_SoLuongSP.removeElementAt(i);
        Paid_TongGiaSP.removeElementAt(i);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel_thongTin = new javax.swing.JPanel();
        textbox_search_name = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        button_add_to_cart = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        table_database_items = new javax.swing.JTable();
        button_update_database_item = new javax.swing.JButton();
        button_clear_update_table = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        textbox_search_ID = new javax.swing.JTextField();
        textbox_barcode = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jPanel_hoa_don = new javax.swing.JPanel();
        jScrollPane_banghoadon = new javax.swing.JScrollPane();
        table_transaction_items = new javax.swing.JTable();
        Thong_bao_text = new javax.swing.JLabel();
        date_time = new javax.swing.JLabel();
        select_price = new javax.swing.JCheckBox();
        date_time1 = new javax.swing.JLabel();
        textbox_consumer_name = new javax.swing.JTextField();
        button_add_temp_item = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        textbox_loan = new javax.swing.JTextField();
        button_save_temp_transaction = new javax.swing.JButton();
        ComboBox_SaveBill = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        button_exit = new javax.swing.JButton();
        button_transaction_history = new javax.swing.JButton();
        button_update_items = new javax.swing.JButton();
        button_pre_transaction = new javax.swing.JButton();
        button_pay_print_bill = new javax.swing.JButton();
        combobox_history_transaction = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        button_print_barcode = new javax.swing.JButton();
        button_update_consumer = new javax.swing.JButton();
        lable_makhachhang = new javax.swing.JLabel();
        textbox_new_consumer_ID = new javax.swing.JTextField();
        button_refreshment = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        Menu_tool = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Cửa hàng tạp hóa SÁU VÂN");
        setIconImage(Toolkit.getDefaultToolkit().getImage("/sale/img/bar-code.png"));
        setIconImages(null);
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                formMouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                formComponentMoved(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                formKeyReleased(evt);
            }
        });

        jPanel_thongTin.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        jPanel_thongTin.setFont(new java.awt.Font("Times New Roman", 1, 12)); // NOI18N

        textbox_search_name.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        textbox_search_name.setForeground(new java.awt.Color(0, 0, 102));
        textbox_search_name.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textbox_search_nameKeyReleased(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 0, 102));
        jLabel2.setText("Tìm SP (tên) :");

        button_add_to_cart.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        button_add_to_cart.setForeground(new java.awt.Color(102, 0, 0));
        button_add_to_cart.setText("Thêm sp vào hóa đơn");
        button_add_to_cart.setToolTipText("");
        button_add_to_cart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_add_to_cartActionPerformed(evt);
            }
        });

        table_database_items.setFont(new java.awt.Font("Times New Roman", 0, 24)); // NOI18N
        table_database_items.setForeground(new java.awt.Color(0, 0, 204));
        table_database_items.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Mã sản phẩm", "Tên sản phẩm", "Giá S sp", "Giá L sp", "Số lượng", "chọn/bỏ chọn"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Integer.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        table_database_items.setGridColor(new java.awt.Color(0, 0, 204));
        table_database_items.setRowHeight(26);
        jScrollPane1.setViewportView(table_database_items);

        button_update_database_item.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        button_update_database_item.setForeground(new java.awt.Color(102, 0, 0));
        button_update_database_item.setText("Cập nhập");
        button_update_database_item.setToolTipText("cap nhap ");
        button_update_database_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_update_database_itemActionPerformed(evt);
            }
        });

        button_clear_update_table.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        button_clear_update_table.setForeground(new java.awt.Color(102, 0, 0));
        button_clear_update_table.setText("Hủy cập nhập");
        button_clear_update_table.setToolTipText("huy cap nhap");
        button_clear_update_table.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_clear_update_tableActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 0, 102));
        jLabel3.setText("Tìm SP(Mã) :");

        textbox_search_ID.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        textbox_search_ID.setForeground(new java.awt.Color(0, 0, 102));
        textbox_search_ID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textbox_search_IDActionPerformed(evt);
            }
        });
        textbox_search_ID.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textbox_search_IDKeyReleased(evt);
            }
        });

        textbox_barcode.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        textbox_barcode.setForeground(new java.awt.Color(0, 0, 102));
        textbox_barcode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textbox_barcodeActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 102));
        jLabel1.setText("Barcode SP");

        jPanel_hoa_don.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Thông tin hóa đơn", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Times New Roman", 1, 14), new java.awt.Color(102, 0, 0))); // NOI18N
        jPanel_hoa_don.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N

        table_transaction_items.setAutoCreateRowSorter(true);
        table_transaction_items.setBorder(new javax.swing.border.MatteBorder(null));
        table_transaction_items.setFont(new java.awt.Font("Times New Roman", 0, 24)); // NOI18N
        table_transaction_items.setForeground(java.awt.Color.red);
        table_transaction_items.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Stt", "Mã SP", "Tên SP", "Giá S SP", "Giá L SP", "Số Lượng SP", "Thành Tiền", "Chọn mua"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true, true, true, true, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        table_transaction_items.setToolTipText("");
        table_transaction_items.setGridColor(new java.awt.Color(0, 0, 153));
        table_transaction_items.setRowHeight(30);
        table_transaction_items.setRowSorter(null);
        table_transaction_items.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                table_transaction_itemsMouseClicked(evt);
            }
        });
        table_transaction_items.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                table_transaction_itemsKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                table_transaction_itemsKeyTyped(evt);
            }
        });
        jScrollPane_banghoadon.setViewportView(table_transaction_items);

        Thong_bao_text.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        Thong_bao_text.setForeground(new java.awt.Color(0, 0, 204));
        Thong_bao_text.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Thong_bao_text.setText("thong bao");
        Thong_bao_text.setFocusable(false);

        date_time.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        date_time.setForeground(new java.awt.Color(0, 0, 153));
        date_time.setText("Date- Time");

        select_price.setFont(new java.awt.Font("Times New Roman", 1, 12)); // NOI18N
        select_price.setSelected(true);
        select_price.setText("Giá Sỹ/ giá lẻ");
        select_price.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                select_priceActionPerformed(evt);
            }
        });

        date_time1.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        date_time1.setForeground(new java.awt.Color(0, 0, 153));
        date_time1.setText("Tên khách hàng:");

        textbox_consumer_name.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        textbox_consumer_name.setText(".....................");
        textbox_consumer_name.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                textbox_consumer_nameMouseClicked(evt);
            }
        });
        textbox_consumer_name.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textbox_consumer_nameActionPerformed(evt);
            }
        });

        button_add_temp_item.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        button_add_temp_item.setForeground(new java.awt.Color(0, 0, 204));
        button_add_temp_item.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sale/img/button_add.png"))); // NOI18N
        button_add_temp_item.setText("Thêm");
        button_add_temp_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_add_temp_itemActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel4.setText("Nợ cũ:");

        textbox_loan.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        textbox_loan.setText("0.0");
        textbox_loan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                textbox_loanMouseClicked(evt);
            }
        });
        textbox_loan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textbox_loanActionPerformed(evt);
            }
        });
        textbox_loan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textbox_loanKeyReleased(evt);
            }
        });

        button_save_temp_transaction.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        button_save_temp_transaction.setForeground(new java.awt.Color(0, 0, 204));
        button_save_temp_transaction.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sale/img/button_SavebillInfo.png"))); // NOI18N
        button_save_temp_transaction.setText("Lưu hóa đơn [0]");
        button_save_temp_transaction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_save_temp_transactionActionPerformed(evt);
            }
        });

        ComboBox_SaveBill.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        ComboBox_SaveBill.setForeground(new java.awt.Color(0, 0, 153));
        ComboBox_SaveBill.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ComboBox_SaveBillActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel_hoa_donLayout = new javax.swing.GroupLayout(jPanel_hoa_don);
        jPanel_hoa_don.setLayout(jPanel_hoa_donLayout);
        jPanel_hoa_donLayout.setHorizontalGroup(
            jPanel_hoa_donLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_hoa_donLayout.createSequentialGroup()
                .addComponent(date_time, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(date_time1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textbox_consumer_name, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textbox_loan, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(91, 91, 91)
                .addComponent(button_add_temp_item)
                .addGap(29, 29, 29)
                .addComponent(button_save_temp_transaction)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ComboBox_SaveBill, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 150, Short.MAX_VALUE)
                .addComponent(select_price))
            .addComponent(Thong_bao_text, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane_banghoadon)
        );
        jPanel_hoa_donLayout.setVerticalGroup(
            jPanel_hoa_donLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_hoa_donLayout.createSequentialGroup()
                .addGroup(jPanel_hoa_donLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(select_price)
                    .addComponent(date_time)
                    .addComponent(date_time1)
                    .addComponent(textbox_consumer_name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button_add_temp_item)
                    .addComponent(jLabel4)
                    .addComponent(textbox_loan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button_save_temp_transaction)
                    .addComponent(ComboBox_SaveBill, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane_banghoadon, javax.swing.GroupLayout.PREFERRED_SIZE, 409, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Thong_bao_text, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel_hoa_donLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {button_add_temp_item, date_time, date_time1, jLabel4, textbox_consumer_name, textbox_loan});

        Thong_bao_text.getAccessibleContext().setAccessibleName("label_message");

        button_exit.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        button_exit.setForeground(new java.awt.Color(102, 0, 0));
        button_exit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sale/img/button_exit.png"))); // NOI18N
        button_exit.setToolTipText("Thoát");
        button_exit.setIconTextGap(1);
        button_exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_exitActionPerformed(evt);
            }
        });

        button_transaction_history.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        button_transaction_history.setForeground(new java.awt.Color(102, 0, 0));
        button_transaction_history.setText("Xem lại h.đơn");
        button_transaction_history.setToolTipText("xem lại các hóa đơn đã bán, mở file sau đó chọn năm/ chọn tháng/ rồi chọn ngày");
        button_transaction_history.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_transaction_historyActionPerformed(evt);
            }
        });

        button_update_items.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        button_update_items.setForeground(new java.awt.Color(102, 0, 0));
        button_update_items.setText("C.nhập giá,tên");
        button_update_items.setToolTipText("Cập nhập giá cả, tên sản phẩm. số lượng hàng nhập ....");
        button_update_items.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_update_itemsActionPerformed(evt);
            }
        });

        button_pre_transaction.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        button_pre_transaction.setForeground(new java.awt.Color(102, 0, 0));
        button_pre_transaction.setText("Thanh toán");
        button_pre_transaction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_pre_transactionActionPerformed(evt);
            }
        });

        button_pay_print_bill.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        button_pay_print_bill.setForeground(new java.awt.Color(102, 0, 0));
        button_pay_print_bill.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sale/img/menu_Printer.png"))); // NOI18N
        button_pay_print_bill.setText("In hóa đơn");
        button_pay_print_bill.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_pay_print_billActionPerformed(evt);
            }
        });

        combobox_history_transaction.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        combobox_history_transaction.setForeground(new java.awt.Color(102, 0, 0));
        combobox_history_transaction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                combobox_history_transactionActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 0, 153));
        jLabel5.setText("Hóa đơn đã bán:");

        button_print_barcode.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        button_print_barcode.setForeground(new java.awt.Color(102, 0, 0));
        button_print_barcode.setText("In mã");
        button_print_barcode.setToolTipText("In mã vạch");
        button_print_barcode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_print_barcodeActionPerformed(evt);
            }
        });

        button_update_consumer.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        button_update_consumer.setForeground(new java.awt.Color(102, 0, 0));
        button_update_consumer.setText("C.nhập khách");
        button_update_consumer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_update_consumerActionPerformed(evt);
            }
        });

        lable_makhachhang.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        lable_makhachhang.setText("Mã khách: SVK");

        textbox_new_consumer_ID.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        textbox_new_consumer_ID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textbox_new_consumer_IDActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(combobox_history_transaction, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(textbox_new_consumer_ID)
                    .addComponent(lable_makhachhang, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(button_pay_print_bill)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_pre_transaction)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_update_items)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_update_consumer)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_transaction_history)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_print_barcode)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_exit)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(button_exit, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(button_transaction_history, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(button_update_items, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(button_pre_transaction, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(button_pay_print_bill, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(button_print_barcode, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(button_update_consumer, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 19, Short.MAX_VALUE)
                        .addGap(8, 8, 8)
                        .addComponent(combobox_history_transaction, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(lable_makhachhang)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textbox_new_consumer_ID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(10, 10, 10))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {button_exit, button_pay_print_bill, button_pre_transaction, button_transaction_history, button_update_items});

        button_refreshment.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        button_refreshment.setForeground(new java.awt.Color(102, 0, 0));
        button_refreshment.setText("refresh");
        button_refreshment.setToolTipText("refresh");
        button_refreshment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_refreshmentActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel_thongTinLayout = new javax.swing.GroupLayout(jPanel_thongTin);
        jPanel_thongTin.setLayout(jPanel_thongTinLayout);
        jPanel_thongTinLayout.setHorizontalGroup(
            jPanel_thongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_thongTinLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textbox_barcode, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textbox_search_ID)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textbox_search_name, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_add_to_cart)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_update_database_item)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_clear_update_table)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_refreshment))
            .addComponent(jPanel_hoa_don, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1)
        );
        jPanel_thongTinLayout.setVerticalGroup(
            jPanel_thongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_thongTinLayout.createSequentialGroup()
                .addGroup(jPanel_thongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(textbox_search_ID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(textbox_search_name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button_add_to_cart)
                    .addComponent(button_update_database_item)
                    .addComponent(button_clear_update_table)
                    .addComponent(jLabel1)
                    .addComponent(textbox_barcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button_refreshment))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel_hoa_don, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel_thongTinLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {button_add_to_cart, button_clear_update_table, button_refreshment, button_update_database_item, jLabel1, jLabel2, jLabel3, textbox_barcode, textbox_search_ID, textbox_search_name});

        Menu_tool.setText("Công cụ");

        jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sale/img/menu_tool.png"))); // NOI18N
        jMenuItem1.setText("cấu hình hệ thống");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        Menu_tool.add(jMenuItem1);

        jMenuItem2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sale/img/menu_load_config.png"))); // NOI18N
        jMenuItem2.setText("load cấu hình");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        Menu_tool.add(jMenuItem2);

        jMenuBar1.add(Menu_tool);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel_thongTin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel_thongTin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        getAccessibleContext().setAccessibleDescription("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_formKeyReleased

    private void formMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_formMouseReleased

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_formKeyPressed

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        // TODO add your handling code here:


    }//GEN-LAST:event_formComponentShown

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        // TODO add your handling code here:
        gohome();
    }//GEN-LAST:event_formMouseClicked

    private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved
        // TODO add your handling code here:
        gohome();
    }//GEN-LAST:event_formComponentMoved

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        // TODO add your handling code here:
        gohome();
    }//GEN-LAST:event_formComponentResized

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        // TODO add your handling code here:
        gohome();
    }//GEN-LAST:event_formWindowGainedFocus

    private void button_refreshmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_refreshmentActionPerformed
        // TODO add your handling code here:
        refresh();
    }//GEN-LAST:event_button_refreshmentActionPerformed

    private void button_print_barcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_print_barcodeActionPerformed
        // TODO add your handling code here:
        in_ma_vach();

    }//GEN-LAST:event_button_print_barcodeActionPerformed

    private void combobox_history_transactionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_combobox_history_transactionActionPerformed
        // TODO add your handling code here:
        add_to_history();
        gohome();
    }//GEN-LAST:event_combobox_history_transactionActionPerformed

    private void button_pay_print_billActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_pay_print_billActionPerformed
        // TODO add your handling code here:
        print_receipt(false);   // false = review print
    }//GEN-LAST:event_button_pay_print_billActionPerformed

    private void button_pre_transactionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_pre_transactionActionPerformed
        // TODO add your handling code here:
        diff_slFormat();
        pre_paid();

    }//GEN-LAST:event_button_pre_transactionActionPerformed

    private void button_update_itemsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_update_itemsActionPerformed
        // TODO add your handling code here:
        update_paid_table();
    }//GEN-LAST:event_button_update_itemsActionPerformed

    private void button_transaction_historyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_transaction_historyActionPerformed
        // TODO add your handling code here:
        history_tracking.setVisible(true);
    }//GEN-LAST:event_button_transaction_historyActionPerformed

    private void button_exitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_exitActionPerformed
        // TODO add your handling code here:
        exit_program();

    }//GEN-LAST:event_button_exitActionPerformed

    private void textbox_loanKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textbox_loanKeyReleased
        // TODO add your handling code here:
        //show_status("", Color.black);
        //String loanstr = textfield_loan.getText();
        //get_loan_from_string(loanstr);
    }//GEN-LAST:event_textbox_loanKeyReleased

    private void textbox_loanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_textbox_loanMouseClicked
        // TODO add your handling code here:
        textbox_clear(textbox_loan);
    }//GEN-LAST:event_textbox_loanMouseClicked

    private void button_add_temp_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_add_temp_itemActionPerformed
        // TODO add your handling code here:
        if (hand_offset <= 40) {
            String themtay_hoadon = "SV_THEMTAY" + hand_offset;
            int them_tay_index = SauVan.tim_sp(themtay_hoadon);
            addtopay(them_tay_index);
            hand_offset++;
        } else {
            show_status("chỉ được thêm tối đa 40 sp! bấm refresh để làm lại", Color.BLUE);
        }
        gohome();
    }//GEN-LAST:event_button_add_temp_itemActionPerformed

    private void textbox_consumer_nameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textbox_consumer_nameActionPerformed
        // TODO add your handling code here:
        gohome();
    }//GEN-LAST:event_textbox_consumer_nameActionPerformed

    private void textbox_consumer_nameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_textbox_consumer_nameMouseClicked
        // TODO add your handling code here:
        textbox_clear(textbox_consumer_name);
    }//GEN-LAST:event_textbox_consumer_nameMouseClicked

    private void select_priceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_select_priceActionPerformed
        // TODO add your handling code here:
        // update order table
        select_price();

    }//GEN-LAST:event_select_priceActionPerformed

    private void table_transaction_itemsKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_table_transaction_itemsKeyTyped
        // TODO add your handling code here:
        if (hd_inMethodClean) {
            hd_inMethodClean = false;
            int Scol = table_transaction_items.getSelectedColumn();
            int Srow = table_transaction_items.getSelectedRow();
            if (2 == Scol && Scol <= 5 && (Srow + 1) != table_transaction_items.getRowCount()) {
            }
        }
    }//GEN-LAST:event_table_transaction_itemsKeyTyped

    private void table_transaction_itemsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_table_transaction_itemsKeyReleased
        // TODO add your handling code here:
        update_bang_hoadon();
    }//GEN-LAST:event_table_transaction_itemsKeyReleased

    private void table_transaction_itemsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_table_transaction_itemsMouseClicked
        // TODO add your handling code here:
        if (evt.getClickCount() == 1) //a click
        {
            hd_inMethodClean = true;
        }
        update_total_Paid_row();
    }//GEN-LAST:event_table_transaction_itemsMouseClicked

    private void textbox_barcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textbox_barcodeActionPerformed
        // TODO add your handling code here:
        int idx;

        if (!is_textbox_empty(textbox_barcode)) {
            //if (!textbox_barcode.getText().isEmpty()) {
            curMaSP = textbox_barcode.getText().toUpperCase().trim();
            String[] in_barcode = curMaSP.split(" ");
            if (in_barcode.length == 2) {
                switch (in_barcode[0]) {
                    case parameter.CUSTOMERS:
                        idx = is_exist_customer(curMaSP);
                        if (idx == -1) {
                            show_status(message.CUSTOMERS_NOT_FOUND + curMaSP, Color.RED);
                        } else {
                            refresh();
                            show_status("khách có mã: " + curMaSP, Color.RED);
                            textbox_consumer_name.setText(Customers.get(idx).Name);
                            textbox_new_consumer_ID.setText(Customers.get(idx).ID.substring(4));
                            int count = 0;
                            for (Item item : Customers.get(idx).Items) {
                                add_items_table(item.ID);
                                Paid_SoLuongSP.set(count, (double) (item.Quanlity));

                                calculate_item_price_row(count);
                                update_item_row(count);
                                count++;
                            }
                            update_total_Paid_row();
                        }
                        break;
                    case parameter.COMMAND:
                        handle_commands(in_barcode[1]);
                        break;
                    default:
                        break;
                }
            } else {
                add_items_table(curMaSP);
            }
        }
        gohome();
    }//GEN-LAST:event_textbox_barcodeActionPerformed

    private void textbox_search_IDKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textbox_search_IDKeyReleased
        // TODO add your handling code here:
        String tim_bar_codeSP = get_textbox_text(textbox_search_ID);
//                String tim_bar_codeSP = textbox_search_ID.getText().trim();

        if (!tim_bar_codeSP.isEmpty()) {
            Vector array_result = SauVan.timThongMinh_MaSP(get_textbox_text(textbox_search_ID));

            //clear table
            display_result2infoTable(array_result);

            if (!array_result.isEmpty()) {
                button_add_to_cart.setEnabled(true);
                button_update_database_item.setEnabled(true);
            } else {
                button_add_to_cart.setEnabled(false);
                button_update_database_item.setEnabled(false);
            }
        } else {
            //clear_table(dm_info_SP);
            int old_result_count = dm_info_SP.getRowCount();

            for (int i = old_result_count - 1; i >= 0; i--) {
                if (!(boolean) dm_info_SP.getValueAt(i, 5)) {

                    dm_info_SP.removeRow(i);
                }
            }
            button_add_to_cart.setEnabled(false);
            button_update_database_item.setEnabled(false);
        }
    }//GEN-LAST:event_textbox_search_IDKeyReleased

    private void textbox_search_IDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textbox_search_IDActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_textbox_search_IDActionPerformed

    private void button_clear_update_tableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_clear_update_tableActionPerformed
        // TODO add your handling code here:
        clear_table(dm_info_SP);
        clear_Updated_list();
        Updated_mode = false;
        button_update_database_item.setEnabled(false);
        jScrollPane_banghoadon.setVisible(true);
        gohome();
    }//GEN-LAST:event_button_clear_update_tableActionPerformed

    private void button_update_database_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_update_database_itemActionPerformed
        // TODO add your handling code here:
        //get and check all data form table
        //consilidated_table();
        int row_Count;
        row_Count = table_database_items.getRowCount();
        int test_soluong;
        Object sample;
        show_status("", Color.BLUE);
        for (int i = 0; i < row_Count; i++) {
            try {
                sample = table_database_items.getValueAt(i, 4);
                test_soluong = Integer.parseInt(sample.toString());
                if (test_soluong <= 0) {
                    show_status("[lỗi nhập số]bảng thông tin sản phẩm, cột 5 hàng " + (i + 1), Color.red);
                    row_Count = 0;  // break next loop
                    break;
                }
            } //Catch error if input was not in_barcode number and repeat while loop.
            catch (NumberFormatException e) {
                show_status("[lỗi nhập số] Hàng " + (i + 1), Color.RED);
                row_Count = 0;  // break next loop
                break;
            }
        }

        for (int i = 0; i < row_Count; i++) {
            show_status("Đang cập nhập sản phẩm :" + table_database_items.getValueAt(i, 2), Color.RED);
            Vector inputSP = new Vector();
            inputSP.add(table_database_items.getValueAt(i, 0));
            inputSP.add(table_database_items.getValueAt(i, 1));
            inputSP.add(Double.parseDouble(table_database_items.getValueAt(i, 2).toString()));
            inputSP.add(Double.parseDouble(table_database_items.getValueAt(i, 3).toString()));
            inputSP.add(Integer.parseInt(table_database_items.getValueAt(i, 4).toString()));
            test_soluong = SauVan.update_data(inputSP);
            if (test_soluong == -1) {
                show_status("[Chưa cập nhập]File dữ liệu đang mở. đóng để cập nhập lại", Color.RED);
                row_Count = 0;
            } else if (test_soluong == -2) {
                row_Count = 0;
                show_status("[Chưa cập nhập]Không tìm thấy file dữ liệu", Color.RED);
            }
        }

        if (row_Count != 0) {
            //clear data
            clear_table(dm_info_SP);
            clear_Updated_list();
            show_status("Hoàn thành quá trình cập nhập:", Color.BLUE);
        }
        jScrollPane_banghoadon.setVisible(true);
        gohome();
    }//GEN-LAST:event_button_update_database_itemActionPerformed

    private void button_add_to_cartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_add_to_cartActionPerformed
        // TODO add your handling code here:
        int tim_row_count = table_database_items.getRowCount();
        Object them_MaSP2PAY;
        int index_sp;
        for (int i = 0; i < tim_row_count; i++) {
            if ((boolean) table_database_items.getValueAt(i, 5)) {
                them_MaSP2PAY = table_database_items.getValueAt(i, 0);
                index_sp = SauVan.tim_sp(them_MaSP2PAY.toString());
                if (index_sp == -1) {
                    show_status("Chưa có Mã sản phẩm trong dữ liệu: " + them_MaSP2PAY, Color.RED);
                } else {
                    addtopay(index_sp);
                }
            }
        }
        clear_table(dm_info_SP);
        clear_Updated_list();
        button_add_to_cart.setEnabled(false);
        jScrollPane_banghoadon.setVisible(true);
        gohome();
    }//GEN-LAST:event_button_add_to_cartActionPerformed

    private void textbox_search_nameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textbox_search_nameKeyReleased
        // TODO add your handling code here:
        String ten_sp = get_textbox_text(textbox_search_name);
        //String ten_sp = textbox_search_name.getText().trim();

        if (!ten_sp.isEmpty()) {
            Vector array_result;
            array_result = SauVan.timThongMinh_sp(ten_sp);

            //clear table
            display_result2infoTable(array_result);

            if (!array_result.isEmpty()) {
                button_add_to_cart.setEnabled(true);
                button_update_database_item.setEnabled(true);
            } else {
                button_add_to_cart.setEnabled(false);
                button_update_database_item.setEnabled(false);
            }
        } else {
            //clear_table(dm_info_SP);
            int old_result_count = dm_info_SP.getRowCount();

            for (int i = old_result_count - 1; i >= 0; i--) {
                if (!(boolean) dm_info_SP.getValueAt(i, 5)) {

                    dm_info_SP.removeRow(i);
                }
            }
            button_add_to_cart.setEnabled(false);
            button_update_database_item.setEnabled(false);
        }
    }//GEN-LAST:event_textbox_search_nameKeyReleased

    private void button_save_temp_transactionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_save_temp_transactionActionPerformed
        // TODO add your handling code here:
        buttonSaveBill();
        refresh();
        //gohome();
    }//GEN-LAST:event_button_save_temp_transactionActionPerformed

    private void ComboBox_SaveBillActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ComboBox_SaveBillActionPerformed
        // TODO add your handling code here:
        LoadSavedBill();
        buttonSaveBillsetText(0);
        saveBillIdxNoti = 1;
        gohome();
    }//GEN-LAST:event_ComboBox_SaveBillActionPerformed

    private void textbox_loanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textbox_loanActionPerformed
        // TODO add your handling code here:
        get_loan_from_textbox(textbox_loan);
        update_payment_rows();
    }//GEN-LAST:event_textbox_loanActionPerformed

    private void button_update_consumerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_update_consumerActionPerformed
        // TODO add your handling code here:
        update_customers();

    }//GEN-LAST:event_button_update_consumerActionPerformed

    private void textbox_new_consumer_IDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textbox_new_consumer_IDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textbox_new_consumer_IDActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        // TODO add your handling code here:
        get_bill_configure();
        in_hoa_don.get_bill_configure();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
        setup_configuration();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            System.out.println("ERROR: main : ClassNotFoundException");
        } catch (InstantiationException ex) {
            System.out.println("ERROR: main : InstantiationException");
        } catch (IllegalAccessException ex) {
            System.out.println("ERROR: main : IllegalAccessException");
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            System.out.println("ERROR: main : UnsupportedLookAndFeelException");
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Main_form().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox ComboBox_SaveBill;
    private javax.swing.JMenu Menu_tool;
    private javax.swing.JLabel Thong_bao_text;
    private javax.swing.JButton button_add_temp_item;
    private javax.swing.JButton button_add_to_cart;
    private javax.swing.JButton button_clear_update_table;
    private javax.swing.JButton button_exit;
    private javax.swing.JButton button_pay_print_bill;
    private javax.swing.JButton button_pre_transaction;
    private javax.swing.JButton button_print_barcode;
    private javax.swing.JButton button_refreshment;
    private javax.swing.JButton button_save_temp_transaction;
    private javax.swing.JButton button_transaction_history;
    private javax.swing.JButton button_update_consumer;
    private javax.swing.JButton button_update_database_item;
    private javax.swing.JButton button_update_items;
    private javax.swing.JComboBox combobox_history_transaction;
    private javax.swing.JLabel date_time;
    private javax.swing.JLabel date_time1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel_hoa_don;
    private javax.swing.JPanel jPanel_thongTin;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane_banghoadon;
    private javax.swing.JLabel lable_makhachhang;
    private javax.swing.JCheckBox select_price;
    private javax.swing.JTable table_database_items;
    private javax.swing.JTable table_transaction_items;
    private javax.swing.JTextField textbox_barcode;
    private javax.swing.JTextField textbox_consumer_name;
    private javax.swing.JTextField textbox_loan;
    private javax.swing.JTextField textbox_new_consumer_ID;
    private javax.swing.JTextField textbox_search_ID;
    private javax.swing.JTextField textbox_search_name;
    // End of variables declaration//GEN-END:variables

}
