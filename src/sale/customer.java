/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sale;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 50700
 */
public class customer {
    public String ID;
    public String Name;
    public List<Item> Items;
    public int Owned_money;
    public customer()
    {
        ID = "";
        Name = "";
        Items = new ArrayList<>();
        Owned_money = 0;
    }
      
    public customer(String inID, String inName,  
            int inOwned_money)
    {
        ID = inID;
        Name = inName;
        Items = new ArrayList<>();
        Owned_money = inOwned_money;
    }
    public void print()
    {
        System.out.println("---------------------------------------");
        System.out.println("ID: " + ID);
        System.out.println("Name: " + Name);
        for (Item item : Items) {
            item.print();
        }
        System.out.println("Owned_money: " + Owned_money);
        System.out.println("---------------------------------------");
    }
}


