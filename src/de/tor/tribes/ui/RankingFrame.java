/*
 * RankingFrame.java
 *
 * Created on 12. Juni 2008, 14:45
 */
package de.tor.tribes.ui;

import de.tor.tribes.types.Tribe;
import de.tor.tribes.util.GlobalOptions;
import java.util.Enumeration;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author  Jejkal
 */
public class RankingFrame extends javax.swing.JFrame {

    /** Creates new form RankingFrame */
    public RankingFrame() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jRankingTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jRankingTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Platz", "Spieler", "Stamm", "Punkte", "Dörfer", "Punkte/Dorf"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jRankingTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public void buildRanking() {
        jRankingTable.invalidate();
        DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Platz", "Spieler", "Stamm", "Punkte", "Dörfer", "Punkte/Dorf"
                }) {

            Class[] types = new Class[]{
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };

        Enumeration<Integer> players = GlobalOptions.getDataHolder().getTribes().keys();
        while (players.hasMoreElements()) {
            Object[] row = new Object[6];
            Tribe current = GlobalOptions.getDataHolder().getTribes().get(players.nextElement());
            row[0] = current.getRank();
            row[1] = current.getName();
            try {
                row[2] = GlobalOptions.getDataHolder().getAllies().get(current.getAllyID());
            } catch (Exception e) {
                row[2] = "";
            }
            row[3] = current.getPoints();
            row[4] = current.getVillages();
            if (current.getVillages() != 0) {
                row[5] = (int) Math.rint(current.getPoints() / current.getVillages());
            } else {
                row[5] = 0;
            }
            model.addRow(row);
        }

        jRankingTable.setModel(model);
        jRankingTable.revalidate();

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new RankingFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable jRankingTable;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}