package sale;

import java.util.ArrayList;
import java.util.List;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author thanh
 */
public class invoice_sold_table {
    public static List<invoice_sold_item> data = new ArrayList<>();
    public String consumer_name = "";
    public String Year = "";
    public String month = "";
    public String day = "";
    public String hours = "";
    public String minus = "";
    public String second = "";

    public double sub_total = 0.0;
    public double tax = 0.0;
    public double total = 0.0;
    
    public void clear(){
        if (this.data != null){
            this.data.clear();
        }
        this.consumer_name = "";
        this.sub_total = 0.0;
        this.tax = 0.0;
        this.total = 0.0;
    }
    public void set_subtotal(Double subtotal){
        this.sub_total = subtotal;
        this.tax = subtotal*0.1;
        this.total = subtotal*1.1;
    }
    public String get_subtotal(){
        return String.format("%.2f",this.sub_total);
    }
    public String get_tax(){
        return String.format("%.2f",this.tax);
    }
    public String get_total(){
        return String.format("%.2f",this.total);
    }
}
