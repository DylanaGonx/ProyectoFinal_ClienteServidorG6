/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.fidelitas.java.quickdelivery;

import ui.Login;
/**
 *
 * @author ssanc
 */
public class QuickDelivery {

    public static void main(String[] args) {
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Login().setVisible(true);
            }
            
        });
    }
}
