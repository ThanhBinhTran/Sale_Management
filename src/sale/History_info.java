package sale;

import com.itextpdf.text.DocumentException;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Binh
 */
public class History_info extends javax.swing.JFrame {

    /**
     * Creates new form history_info
     */
    //final JFileChooser fc;
    // FileNameExtensionFilter filter ;
    Vector vTime;
    Vector vTime_name;
    Vector vSeek_idx;
    String file_name;
    DefaultTableModel tm;
    DefaultListModel lm;

    public invoice_sold_table invoice_items_table;

    public History_info() {
        initComponents();
        initial_environment();
        setPayTableColumnSize();
        invoice_items_table = new invoice_sold_table();
    }

        public void show_message(String imessage) {
            JOptionPane.showMessageDialog(null,
                imessage,
                parameter.TITLE,
                JOptionPane.PLAIN_MESSAGE
        );
    }
    public final void initial_environment() {
        vTime = new Vector();
        vTime_name = new Vector();
        vSeek_idx = new Vector();
        tm = (DefaultTableModel) table_hoa_don.getModel();
        time_line.setModel(new DefaultListModel());
        lm = (DefaultListModel) time_line.getModel();
    }

    public final void setPayTableColumnSize() {

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table_hoa_don.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 16));
        TableColumn column = null;
        column = table_hoa_don.getColumnModel().getColumn(0);
        column.setPreferredWidth(30);
        column.setCellRenderer(rightRenderer);
        column = table_hoa_don.getColumnModel().getColumn(1);
        column.setPreferredWidth(120);
        column = table_hoa_don.getColumnModel().getColumn(2);
        column.setPreferredWidth(200);
        column = table_hoa_don.getColumnModel().getColumn(3);
        column.setPreferredWidth(70);
        column.setCellRenderer(rightRenderer);
        column = table_hoa_don.getColumnModel().getColumn(4);
        column.setPreferredWidth(80);
        column.setCellRenderer(rightRenderer);
    }

    public int choosefile() {
        int return_val = 0;
        JFileChooser fileChooser = new JFileChooser();
        String choosertitle = "chọn file lưa trữ";
        FileFilter filter = new FileNameExtensionFilter("tạp hóa Sáu Vân", "thsv");
        fileChooser.setFileFilter(filter);
        fileChooser.setCurrentDirectory(new java.io.File("."));
        fileChooser.setDialogTitle(choosertitle);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            field_file_name.setText(fileChooser.getSelectedFile().toString());
        } else {
            return_val = -1;
            notification.setText("file not found");
        }
        return return_val;
    }

    public void read_his_file() {
        vSeek_idx.clear();
        vTime.clear();
        vTime_name.clear();
        String[] raw_data;
        try {
            FileInputStream in = new FileInputStream(file_name);
            try ( BufferedReader bufffile = new BufferedReader(new InputStreamReader(in, "UTF8"))) {
                String strLine;
                //in.read(strLine);

                strLine = bufffile.readLine();
                int iline_num = 0;
                while (strLine != null) {
                    // Print the content on the console
                    iline_num++;
                    if (strLine.substring(0, 3).equals("-->")) {
                        System.out.println("+++++++" + strLine);
                        vSeek_idx.add(iline_num);
                        raw_data = strLine.substring(3).split(" ", 2);
                        String[] str_date = raw_data[0].split("/");
                        invoice_items_table.Year = str_date[2];
                        invoice_items_table.month = str_date[1];
                        invoice_items_table.day = str_date[0];

                        year_date.setText(raw_data[0]);
                        vTime_name.add(strLine.substring(3));
                        vTime.add(raw_data[1]);
                    }
                    strLine = bufffile.readLine();
                }
            }
            in.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        notification.setText(file_name);
    }

    public boolean check_file(String infile) {
        File File_path = new File(infile);
        return File_path.isFile();
    }

    public void add2List() {
        int bill_count;
        file_name = field_file_name.getText();
        lm.removeAllElements();
        if (check_file(file_name)) {
            read_his_file();
            bill_count = vTime.size();
            for (int i = 0; i < bill_count; i++) {
                System.out.println(vTime.get(i) + "<>" + vSeek_idx.get(i));
                lm.addElement(vTime.get(i));
            }
        } else {
            notification.setText("Không tìm thấy file lưa trữ");
        }

    }

    public int convert_time2lineIdx(String iString) {
        int bill_count = vTime.size();
        int return_val = -1;
        for (int i = 0; i < bill_count; i++) {
            if (vTime.get(i).toString().equals(iString)) {
                return_val = i;
                break;
            }
        }
        return return_val;
    }

    public int display_billInfo(int iline) {
        int return_val = 0;
        String[] idata;
        int data_line = 1;
        //clear table;
        tm.getDataVector().removeAllElements();
        tm.fireTableDataChanged();

        // clear sold item list
        invoice_items_table.clear();
        if (check_file(file_name)) {
            FileInputStream in = null;
            try {
                in = new FileInputStream(file_name);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(History_info.class.getName()).log(Level.SEVERE, null, ex);
            }
            try ( BufferedReader bufffile = new BufferedReader(new InputStreamReader(in, "UTF8"))) {
                String strLine;
                strLine = bufffile.readLine();
                int line_count = 0;
                double soluong;
                double subtotal = 0;
                double nocu;
                while (strLine != null) {
                    line_count++;
                    {
                        if (line_count >= iline) {
                            Vector data_row = new Vector();
                            idata = strLine.substring(3).split("<>");
                            System.out.println("lenght " + idata.length + "--" + strLine + "--" + line_count);
                            switch (strLine.substring(0, 3)) {
                                case "-->":
                                    consumer_name.setText("Khách hàng:" + idata[1]);
                                    String[] str_time = idata[0].split(" ");
                                    String[] str_hour_minus = str_time[1].split(":");
                                    invoice_items_table.hours = str_hour_minus[0];
                                    invoice_items_table.minus = str_hour_minus[1];
                                    invoice_items_table.consumer_name = idata[1];
                                    break;
                                case "---":
                                    if (idata.length < 4) {
                                        notification.setText("ERROR! " + iline);
                                        break;
                                    }
                                    data_row.add(data_line);
                                    data_row.add(idata[0]);   //ma sanpham
                                    data_row.add(idata[1]);   //ten sanpham
                                    data_row.add(idata[2]);   //so luong sanpham
                                    data_row.add(idata[3]);   //thanh tien sanpham
                                    double quantity = Double.parseDouble(idata[2]);
                                    double item_price = Double.parseDouble(idata[3])/Double.parseDouble(idata[2]);
                                    double sub_total = Double.parseDouble(idata[3]);
                                    invoice_sold_item sold_item = new invoice_sold_item(idata[1], quantity, item_price, sub_total);
                                    invoice_items_table.data.add(sold_item);
                                    tm.addRow(data_row);
                                    tm.fireTableDataChanged();
                                    data_line++;
                                    break;
                                case "==>":
                                    soluong = Double.parseDouble(idata[0]);
                                    subtotal = Double.parseDouble(idata[1]);
                                    data_row.add(null);
                                    data_row.add(null);
                                    data_row.add(null);
                                    data_row.add(soluong);   //ma sanpham
                                    data_row.add(subtotal);   //ma sanpham
                                    invoice_items_table.set_subtotal(subtotal);
                                    tm.addRow(data_row);
                                    tm.fireTableDataChanged();
                                    break;
                                case "==-":
                                    Vector data_row1 = new Vector();
                                    nocu = Double.parseDouble(idata[0]);

                                    data_row.add(null);
                                    data_row.add(null);
                                    data_row.add(null);
                                    data_row.add("Nợ cũ");
                                    data_row.add(nocu);   //ma sanpham
                                    tm.addRow(data_row);
                                    tm.fireTableDataChanged();
                                    data_row1.add(null);
                                    data_row1.add(null);
                                    data_row1.add(null);   //ma sanpham
                                    data_row1.add("Tổng cộng");
                                    data_row1.add(nocu + subtotal);   //ma sanpham
                                    tm.addRow(data_row1);
                                    tm.fireTableDataChanged();
                                    data_line = -1;
                                    break;
                            }
                            if (data_line == -1) {
                                break;
                            }
                        }
                        strLine = bufffile.readLine();
                    }
                }
                bufffile.close();
                in.close();
            } catch (Exception e) {//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            }
        } else {
            notification.setText("Không tìm thấy file");
        }
        return return_val;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        time_line = new javax.swing.JList();
        field_file_name = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        table_hoa_don = new javax.swing.JTable();
        notification = new javax.swing.JLabel();
        search_field = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        year_date = new javax.swing.JLabel();
        consumer_name = new javax.swing.JLabel();
        button_invoice_issue = new javax.swing.JButton();
        invoice_text = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Xem lại hóa đơn");

        jButton1.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        jButton1.setText("Mở file");
        jButton1.setToolTipText("chọn để mở file chứa hóa đơn/ chọn năm/ chọn tháng/ chọn ngày ví dụ 2013/03/21");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        time_line.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        time_line.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        time_line.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                time_lineMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(time_line);

        field_file_name.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        field_file_name.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                field_file_nameActionPerformed(evt);
            }
        });

        table_hoa_don.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        table_hoa_don.setForeground(new java.awt.Color(102, 0, 0));
        table_hoa_don.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "stt", "Mã sản phẩm", "Tên sản phẩm", "số lượng", "Thành giá"
            }
        ));
        jScrollPane2.setViewportView(table_hoa_don);

        notification.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        notification.setText("Thông báo");

        search_field.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        search_field.setText("Tìm giờ mua/Tên khác hàng");
        search_field.setToolTipText("tìm người mua, tìm thời gian mua. hệ thống sẽ liệt kê ra hết mốc thời gian liên qua. giờ.phút. giây");
        search_field.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                search_fieldMouseClicked(evt);
            }
        });
        search_field.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                search_fieldActionPerformed(evt);
            }
        });
        search_field.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                search_fieldKeyReleased(evt);
            }
        });

        jButton3.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        jButton3.setText("refresh");
        jButton3.setToolTipText("hiển thị lại hết hóa đơn trên list");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        year_date.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        year_date.setText("Ngày tháng năm");

        consumer_name.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        consumer_name.setText("Tên khách hàng");

        button_invoice_issue.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        button_invoice_issue.setText("Xuất hóa đơn");
        button_invoice_issue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_invoice_issueActionPerformed(evt);
            }
        });

        invoice_text.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        invoice_text.setForeground(new java.awt.Color(0, 0, 204));
        invoice_text.setText("Thông báo (xuất hóa đơn)");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(notification, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(button_invoice_issue)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(invoice_text, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(field_file_name))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(search_field))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(year_date, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(consumer_name, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(field_file_name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(notification)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(invoice_text)
                    .addComponent(button_invoice_issue))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(search_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3)
                    .addComponent(year_date)
                    .addComponent(consumer_name))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        choosefile();
        add2List();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void search_fieldMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_search_fieldMouseClicked
        // TODO add your handling code here:
        search_field.setText("");
    }//GEN-LAST:event_search_fieldMouseClicked

    private void search_fieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_search_fieldActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_search_fieldActionPerformed

    private void search_fieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_search_fieldKeyReleased
        // TODO add your handling code here:
        time_line.removeAll();
        String inString = search_field.getText().trim();
        int time_count = vTime.size();
        if (!inString.isEmpty()) {
            notification.setText("Đang tìm..." + inString);
            //clear jlist
            lm.removeAllElements();
            for (int i = 0; i < time_count; i++) {
                if (vTime_name.get(i).toString().toLowerCase().contains(inString.toLowerCase())) {
                    lm.addElement(vTime.get(i));
                }
            }
            if (lm.isEmpty()) {
                notification.setText("Không tìm thấy: " + inString);
            } else {
                notification.setText("Có " + lm.size() + " kết quả: " + inString);
            }
        }
    }//GEN-LAST:event_search_fieldKeyReleased

    private void field_file_nameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_field_file_nameActionPerformed
        // TODO add your handling code here:
        add2List();
    }//GEN-LAST:event_field_file_nameActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        add2List();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void time_lineMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_time_lineMouseClicked
        // TODO add your handling code here:
        String stime = time_line.getSelectedValue().toString();
        int i = convert_time2lineIdx(stime);

        System.out.println("converted i = " + i);
        if (i == -1) {
            notification.setText("ERROR! convert " + stime);
        } else {
            int line = Integer.parseInt(vSeek_idx.get(i).toString());
            display_billInfo(line);
        }
    }//GEN-LAST:event_time_lineMouseClicked

    private void button_invoice_issueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_invoice_issueActionPerformed
        // TODO add your handling code here:
        if (this.invoice_items_table.sub_total > 0.0) {
            try {
                invoice_create_pdf invoice_pdf = new invoice_create_pdf();
                invoice_pdf.create_pdf(invoice_items_table);
                String created_file = invoice_pdf.get_absolute_path();
                invoice_text.setText("Ðã tạo HÐ:" + created_file);
                show_message("Ðã tạo HÐ:\n" + created_file);
            } catch (DocumentException | IOException ex) {
                Logger.getLogger(History_info.class.getName()).log(Level.SEVERE, null, ex);
                show_message("CHƯA tạo được HÐ!" + ex.toString());
                invoice_text.setText("CHƯA tạo được HÐ:" + ex.toString());
            }
        }
        else{
            invoice_text.setText("Chưa chọn hóa đơn!");
        }
    }//GEN-LAST:event_button_invoice_issueActionPerformed

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
            java.util.logging.Logger.getLogger(History_info.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(History_info.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(History_info.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(History_info.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new History_info().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_invoice_issue;
    private javax.swing.JLabel consumer_name;
    private javax.swing.JTextField field_file_name;
    private javax.swing.JLabel invoice_text;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel notification;
    private javax.swing.JTextField search_field;
    private javax.swing.JTable table_hoa_don;
    private javax.swing.JList time_line;
    private javax.swing.JLabel year_date;
    // End of variables declaration//GEN-END:variables
}
