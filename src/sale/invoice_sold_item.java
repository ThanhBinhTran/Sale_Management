/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sale;

/**
 *
 * @author Binh
 */
public class invoice_sold_item {
    private String name;
    private double price;    
    private double quantity;
    private double subtotal;
    public invoice_sold_item(String name, double quantity, double price, double subtotal){
        this.name =  name;
        this.price = price;
        this.quantity = quantity ;
        this.subtotal = subtotal ;
    }

    public String get_name() {return this.name ;}
    public String get_price() {return String.format("%.2f",this.price) ;}
    public String get_quantity() {return String.format("%.1f",this.quantity) ;}
    public String get_subtotal() {return String.format("%.2f",this.subtotal) ;}
    public void set_name(String name) { this.name = name;}
    public void set_price(double price) { this.price = price;}
    public void set_quantity(double quantity) { this.quantity = quantity;}
    public void set_subtotal(double subtotal) { this.subtotal = subtotal;}

}