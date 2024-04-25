/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sale;

import java.io.IOException;
import javax.swing.JFrame;

/**
 *
 * @author Binh
 */
public class Sale_main {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        Main_form main_form = new Main_form();
        main_form.setVisible(true);
        main_form.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
}
