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
public class Item {
    public String ID;
    public String Name;
    public double Quanlity;
    public double Wholesale_price;
    public double Retail_price;
    public int Recharge;
    public float Frequence;
    public String Search_name;
    public Item ()
    {
        ID ="";
        Name = "";
        Quanlity = 1;
        Wholesale_price= 0;
        Retail_price= 0;
        Recharge=1;
        Frequence=0;
        Search_name=""; 
    }
        public Item (String inID, String inName, double inQuanlity, 
                double inWholesale_price, double inRetail_price, int inRecharge, 
                float inFrequence, String inSearch_name)
    {
        ID = inID;
        Name = inName;
        Quanlity = inQuanlity;
        Wholesale_price= inWholesale_price;
        Retail_price= inRetail_price;
        Recharge= inRecharge;
        Frequence= inFrequence;
        Search_name= inSearch_name; 
    }  
    public void print()
    {
        System.out.println("ID: " + ID);
        System.out.println("Name: " + Name);
        System.out.println("Quanlity: " + Quanlity);
        System.out.println("Wholesale_price: " + Wholesale_price);
        System.out.println("Retail_price: " + Retail_price);
        System.out.println("Recharge: " + Recharge);
        System.out.println("Frequence: " + Frequence);
        System.out.println("Search_name: " + Search_name);
    }
}
