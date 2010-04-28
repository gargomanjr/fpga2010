/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nessy20;

import javax.swing.UIManager;

/**
 *
 * @author Tony
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {


        //Para cambiar el lookAndFeel  de la aplicacion
        //Windows , Nimbus, Windows classic, metal ,....
        for(UIManager.LookAndFeelInfo laf:UIManager.getInstalledLookAndFeels()){
            if("Windows".equals(laf.getName()))
                try {
                UIManager.setLookAndFeel(laf.getClassName());
            } catch (Exception ex) {
            }
        }

       GUIPrincipal gui;
       gui = new GUIPrincipal();
       gui.setVisible(true);

    }
}
