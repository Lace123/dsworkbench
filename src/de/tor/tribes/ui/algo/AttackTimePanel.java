/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TestPanel.java
 *
 * Created on Mar 24, 2011, 10:33:48 PM
 */
package de.tor.tribes.ui.algo;

import com.visutools.nav.bislider.ColorisationEvent;
import com.visutools.nav.bislider.ColorisationListener;
import de.tor.tribes.types.TimeSpan;
import de.tor.tribes.types.TimeSpanDivider;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.ui.renderer.TimeFrameListCellRenderer;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.algo.TimeFrame;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang.time.DateUtils;

/**
 *
 * @author Torridity
 */
public class AttackTimePanel extends javax.swing.JPanel implements DragGestureListener, DropTargetListener, DragSourceListener {

    private DragSource dragSource = null;
    private SettingsChangedListener mListener;

    /** Creates new form TestPanel
     * @param pListener
     */
    public AttackTimePanel(SettingsChangedListener pListener) {
        initComponents();
        mListener = pListener;
        dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(jLabel5, DnDConstants.ACTION_COPY_OR_MOVE, AttackTimePanel.this);
        dragSource.createDefaultDragGestureRecognizer(jLabel6, DnDConstants.ACTION_COPY_OR_MOVE, AttackTimePanel.this);
        DropTarget dropTarget = new DropTarget(this, this);
        jTimeFrameList.setDropTarget(dropTarget);

        jSendTimeFrame.addColorisationListener(new ColorisationListener() {

            @Override
            public void newColors(ColorisationEvent ColorisationEvent_Arg) {
                updatePreview();
            }
        });
        dateTimeField.setActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                updatePreview();
            }
        });

        jTimeFrameList.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    deleteSelectedTimeSpan();
                }
            }
        });
        reset();
        updatePreview();
    }

    public void setSettingsChangedListener(SettingsChangedListener pListener) {
        mListener = pListener;
    }

    public void fireTimeFrameChangedEvent() {
        if (mListener != null) {
            mListener.fireTimeFrameChangedEvent();
        }
    }

    public final void reset() {
        jSendTimeFrame.setMinimumValue(0);
        jSendTimeFrame.setMaximumColor(Constants.DS_BACK_LIGHT);
        jSendTimeFrame.setMinimumColor(Constants.DS_BACK_LIGHT);
        jSendTimeFrame.setMaximumValue(24);
        jSendTimeFrame.setSegmentSize(1);
        jSendTimeFrame.setUnit("h");
        jSendTimeFrame.setBackground(getBackground());
        jSendTimeFrame.setDecimalFormater(new DecimalFormat("##"));
        DefaultListModel model = new DefaultListModel();
        model.addElement(new TimeSpanDivider());
        jTimeFrameList.setModel(model);
        jTimeFrameList.setCellRenderer(new TimeFrameListCellRenderer());
        jTribeOnlyBox.setModel(new DefaultComboBoxModel(new Object[]{"Alle"}));
        Calendar c = Calendar.getInstance();
        minSendTimeField.setDate(c.getTime());
        c.setTimeInMillis(System.currentTimeMillis() + DateUtils.MILLIS_PER_HOUR);
        maxArriveTimeField.setDate(c.getTime());
        dateTimeField.setDate(c.getTime());
    }

    private void deleteSelectedTimeSpan() {
        if (jTimeFrameList.getSelectedValues() == null || jTimeFrameList.getSelectedValues().length == 0) {
            return;
        }
        List<Object> selection = new LinkedList<Object>();
        for (Object o : jTimeFrameList.getSelectedValues()) {
            if (!(o instanceof TimeSpanDivider)) {
                selection.add(o);
            }
        }
        if (!selection.isEmpty()) {
            if (JOptionPaneHelper.showQuestionConfirmBox(this, "Gewählte Zeitrahmen entfernen?", "Entfernen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                for (Object o : selection) {
                    ((DefaultListModel) jTimeFrameList.getModel()).removeElement(o);
                }
            }
        }
    }

    public void addTribe(Tribe t) {
        DefaultComboBoxModel model = (DefaultComboBoxModel) jTribeOnlyBox.getModel();
        List<Tribe> tribes = new LinkedList<Tribe>();
        for (int i = 0; i < model.getSize(); i++) {
            try {
                tribes.add((Tribe) model.getElementAt(i));
            } catch (Exception e) {
            }
        }
        if (!tribes.contains(t)) {
            tribes.add(t);
            Collections.sort(tribes);
            model = new DefaultComboBoxModel();
            model.addElement("Alle");
            for (Tribe tribe : tribes) {
                model.addElement(tribe);
            }
            jTribeOnlyBox.setModel(model);
        }
    }

    public void removeTribe(Tribe pTribe) {
        DefaultComboBoxModel model = (DefaultComboBoxModel) jTribeOnlyBox.getModel();
        List<Tribe> tribes = new LinkedList<Tribe>();
        for (int i = 0; i < model.getSize(); i++) {
            try {
                tribes.add((Tribe) model.getElementAt(i));
            } catch (Exception e) {
            }
        }
        tribes.remove(pTribe);
        Collections.sort(tribes);
        model = new DefaultComboBoxModel();
        model.addElement("Alle");
        for (Tribe tribe : tribes) {
            model.addElement(tribe);
        }
        jTribeOnlyBox.setModel(model);
    }

    public List<TimeSpan> getTimeSpans() {
        List<TimeSpan> timeSpans = new LinkedList<TimeSpan>();

        //add time frames
        DefaultListModel model = (DefaultListModel) jTimeFrameList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            TimeSpan span = (TimeSpan) model.getElementAt(i);
            try {
                timeSpans.add(span.clone());
            } catch (CloneNotSupportedException cnse) {
                //its the divider
            }
        }
        return timeSpans;
    }

    public Date getStartTime() {
        return minSendTimeField.getSelectedDate();
    }

    public void setStartTime(Date pDate) {
        minSendTimeField.setDate(pDate);
    }

    public Date getArriveTime() {
        return maxArriveTimeField.getSelectedDate();
    }

    public void setArriveTime(Date pDate) {
        maxArriveTimeField.setDate(pDate);
    }

    public void validateSettings() throws RuntimeException {
        if (getTimeSpans().isEmpty()) {
            throw new RuntimeException("Es muss mindestens ein Abschick- und ein Ankunftszeitrahmen angegeben werden.");
        }
        boolean haveStart = false;
        boolean haveArrive = false;
        boolean intersectsWithNightBonus = false;
        for (TimeSpan s : getTimeSpans()) {
            if (s.getDirection().equals(TimeSpan.DIRECTION.SEND)) {
                haveStart = true;
            }
            if (s.getDirection().equals(TimeSpan.DIRECTION.ARRIVE)) {
                haveArrive = true;
            }
            //check night bonus
            if (s.getDirection().equals(TimeSpan.DIRECTION.ARRIVE) && s.intersectsWithNightBonus()) {
                intersectsWithNightBonus = true;
            }
        }

        if (!haveStart) {
            throw new RuntimeException("Es muss mindestens ein Abschickzeitrahmen angegeben werden.");
        } else if (!haveArrive) {
            throw new RuntimeException("Es muss mindestens ein Ankunftszeitrahmen angegeben werden.");
        }

        if (maxArriveTimeField.getSelectedDate().getTime() < System.currentTimeMillis()) {
            throw new RuntimeException("Das Enddatum liegt in der Vergangenheit. Eine Planung von Angriffen ist nicht möglich.");
        }

        if (intersectsWithNightBonus) {
            throw new RuntimeException("Mindestens ein Ankunftszeitrahmen liegt im Nachtbonus.");
        }

    }

    /**Set the timespans manually e.g. while loading a stored state
     * @param pSpans
     */
    public void setTimeSpans(List<TimeSpan> pSpans) {
        DefaultListModel model = new DefaultListModel();
        for (TimeSpan span : pSpans) {
            model.addElement(span);
        }
        jTimeFrameList.setModel(model);
    }

    private void updatePreview() {
        TimeSpan start = getSendSpan();
        TimeSpan arrive = getArriveSpan();
        if (start != null) {
            jLabel5.setText(start.toString());
        }
        if (arrive != null) {
            jLabel6.setText(arrive.toString());
        }
    }

    /**Get the currently set up send span
     * @return TimeSpan The send span
     */
    private TimeSpan getSendSpan() {
        TimeSpan start = null;
        IntRange range = new IntRange(Math.round(jSendTimeFrame.getMinimumColoredValue()), Math.round(jSendTimeFrame.getMaximumColoredValue()));
        if (jAlwaysButton.isSelected()) {
            start = new TimeSpan(range);
        } else {
            start = new TimeSpan(dateTimeField.getSelectedDate(), range);
        }
        if (start != null) {
            start.setDirection(TimeSpan.DIRECTION.SEND);
        }
        return start;
    }

    /**Get the currently set up arrive span
     * @return TimeSpan The arrive span
     */
    private TimeSpan getArriveSpan() {
        TimeSpan arrive = null;
        IntRange range = new IntRange(Math.round(jSendTimeFrame.getMinimumColoredValue()), Math.round(jSendTimeFrame.getMaximumColoredValue()));
        Object o = jTribeOnlyBox.getSelectedItem();
        Tribe t = null;
        if (o != null && o instanceof Tribe) {
            t = (Tribe) o;
        }
        if (jDayButton.isSelected()) {
            arrive = new TimeSpan(dateTimeField.getSelectedDate(), range, t);
        } else if (jAlwaysButton.isSelected()) {
            arrive = new TimeSpan(range, t);
        } else if (jExactTimeButton.isSelected()) {
            arrive = new TimeSpan(dateTimeField.getSelectedDate(), t);
        }
        if (arrive != null) {
            arrive.setDirection(TimeSpan.DIRECTION.ARRIVE);
        }
        return arrive;
    }

    /**Get the entire timeframe based on the panel settings
    @return TimeFrame The timeframe*/
    public TimeFrame getTimeFrame() {

        Date correctedArrive = DateUtils.addDays(maxArriveTimeField.getSelectedDate(), 1);
        correctedArrive = DateUtils.addSeconds(correctedArrive, -1);
        TimeFrame frame = new TimeFrame(minSendTimeField.getSelectedDate(), minSendTimeField.getSelectedDate(), correctedArrive, correctedArrive);
        DefaultListModel model = (DefaultListModel) jTimeFrameList.getModel();
        for (int i = 0; i < jTimeFrameList.getModel().getSize(); i++) {
            TimeSpan s = (TimeSpan) model.getElementAt(i);
            if (s.getDirection().equals(TimeSpan.DIRECTION.SEND)) {
                frame.addStartTimeSpan(s);
            } else if (s.getDirection().equals(TimeSpan.DIRECTION.ARRIVE)) {
                frame.addArriveTimeSpan(s);
            }
        }
        return frame;
    }

    /**Try to add a new timespan. Before it is checked for intersection
    @param s The new timespan*/
    private void addTimeSpan(TimeSpan s) {
        if (s.getSpan() != null && s.getSpan().getMinimumInteger() == s.getSpan().getMaximumInteger()) {
            JOptionPaneHelper.showWarningBox(this, "Der angegebene Zeitrahmen ist ungültig. Der Zeitraum muss mindestens eine Stunde betragen.", "Warnung");
            return;
        }
        //check if timeframe exists or intersects with other existing frame
        int intersection = -1;

        DefaultListModel model = (DefaultListModel) jTimeFrameList.getModel();
        int entryId = 0;
        for (int i = 0; i < model.getSize(); i++) {
            TimeSpan existingSpan = (TimeSpan) model.getElementAt(i);
            if (!existingSpan.getDirection().equals(TimeSpan.DIRECTION.NONE)) {
                //not for divider!
                if (s.intersects(existingSpan)) {
                    intersection = entryId + 1;
                    break;
                }
                entryId++;
            }
        }

        if (intersection == -1) {
            //add span
            if (s.getDirection().equals(TimeSpan.DIRECTION.SEND)) {
                ((DefaultListModel) jTimeFrameList.getModel()).add(0, s);
            } else {
                ((DefaultListModel) jTimeFrameList.getModel()).add(jTimeFrameList.getModel().getSize(), s);
            }

            List<TimeSpan> spans = new LinkedList<TimeSpan>();
            for (int i = 0; i < model.getSize(); i++) {
                spans.add((TimeSpan) model.getElementAt(i));
            }

            Collections.sort(spans);
            model = new DefaultListModel();
            for (TimeSpan span : spans) {
                model.addElement(span);
            }
            jTimeFrameList.setModel(model);
        } else {
            JOptionPaneHelper.showWarningBox(this, "Das gewählte Zeitfenster überschneidet sich mit dem " + intersection + ". Eintrag.\n"
                    + "Bitte wähle die Zeitfenster so, dass es zu keinen Überschneidungen kommt.", "Warnung");
            return;
        }
        fireTimeFrameChangedEvent();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTimeFrameList = new javax.swing.JList();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        minSendTimeField = new de.tor.tribes.ui.components.DateTimeField();
        maxArriveTimeField = new de.tor.tribes.ui.components.DateTimeField();
        jPanel3 = new javax.swing.JPanel();
        jSendTimeFrame = new com.visutools.nav.bislider.BiSlider();
        dateTimeField = new de.tor.tribes.ui.components.DateTimeField();
        jLabel4 = new javax.swing.JLabel();
        jTribeOnlyBox = new javax.swing.JComboBox();
        jPanel4 = new javax.swing.JPanel();
        jAlwaysButton = new javax.swing.JRadioButton();
        jDayButton = new javax.swing.JRadioButton();
        jExactTimeButton = new javax.swing.JRadioButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        jScrollPane1.setMinimumSize(new java.awt.Dimension(262, 60));

        jTimeFrameList.setToolTipText("<html>Liste der verwendeten Zeitrahmen<br/>\nUm Zeitrahmen zu entfernen, markieren einen oder mehrere Zeitrahmen und drücke <i>Entf</i>\n</html>");
        jScrollPane1.setViewportView(jTimeFrameList);

        jLabel3.setText("Zeitrahmen");
        jLabel3.setPreferredSize(new java.awt.Dimension(90, 16));

        jLabel2.setText("Enddatum");

        minSendTimeField.setTimeEnabled(false);

        maxArriveTimeField.setTimeEnabled(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(minSendTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 167, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addComponent(maxArriveTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(minSendTimeField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(maxArriveTimeField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Einstellungen"));

        jSendTimeFrame.setToolTipText("Zeitfenster des Zeitrahmens");

        dateTimeField.setToolTipText("Datum und Uhrzeit des Zeitrahmens");
        dateTimeField.setEnabled(false);
        dateTimeField.setTimeEnabled(false);

        jLabel4.setText("Angreifer");

        jTribeOnlyBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Alle" }));
        jTribeOnlyBox.setToolTipText("Zeitrahmen nur für gewählten Angreifer verwenden");

        jPanel4.setLayout(new java.awt.GridBagLayout());

        buttonGroup1.add(jAlwaysButton);
        jAlwaysButton.setSelected(true);
        jAlwaysButton.setText("Immer");
        jAlwaysButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_remove.png"))); // NOI18N
        jAlwaysButton.setRolloverEnabled(false);
        jAlwaysButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/month.png"))); // NOI18N
        jAlwaysButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireValidityStateChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 0.33;
        jPanel4.add(jAlwaysButton, gridBagConstraints);

        buttonGroup1.add(jDayButton);
        jDayButton.setText("Tag");
        jDayButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_remove.png"))); // NOI18N
        jDayButton.setRolloverEnabled(false);
        jDayButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/day.png"))); // NOI18N
        jDayButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireValidityStateChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 0.33;
        jPanel4.add(jDayButton, gridBagConstraints);

        buttonGroup1.add(jExactTimeButton);
        jExactTimeButton.setText("Zeitpunkt");
        jExactTimeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_remove.png"))); // NOI18N
        jExactTimeButton.setRolloverEnabled(false);
        jExactTimeButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/date-time.png"))); // NOI18N
        jExactTimeButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireValidityStateChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weighty = 0.33;
        jPanel4.add(jExactTimeButton, gridBagConstraints);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        jPanel4.add(jSeparator1, gridBagConstraints);

        jLabel8.setText("Datum/Zeit");

        jLabel9.setText("Zeitraum");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dateTimeField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                    .addComponent(jTribeOnlyBox, 0, 297, Short.MAX_VALUE)
                    .addComponent(jSendTimeFrame, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(dateTimeField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jSendTimeFrame, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jTribeOnlyBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        jLabel1.setText("Startdatum");
        jLabel1.setPreferredSize(new java.awt.Dimension(90, 16));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Zeitrahmenvorschau"));

        jLabel5.setBackground(new java.awt.Color(204, 204, 204));
        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/move_out.png"))); // NOI18N
        jLabel5.setText("Am 13.04.11, von 10 bis 24 Uhr (Alle)");
        jLabel5.setToolTipText("Abschickzeitrahmen für die gewählten Einstellungen");
        jLabel5.setMaximumSize(new java.awt.Dimension(230, 16));
        jLabel5.setMinimumSize(new java.awt.Dimension(230, 16));
        jLabel5.setOpaque(true);
        jLabel5.setPreferredSize(new java.awt.Dimension(230, 16));

        jLabel6.setBackground(new java.awt.Color(204, 204, 204));
        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/move_in.png"))); // NOI18N
        jLabel6.setText("Am 13.04.11, von 10 bis 24 Uhr (Alle)");
        jLabel6.setToolTipText("Ankunftszeitrahmen für die gewählten Einstellungen");
        jLabel6.setMaximumSize(new java.awt.Dimension(230, 16));
        jLabel6.setMinimumSize(new java.awt.Dimension(230, 16));
        jLabel6.setOpaque(true);
        jLabel6.setPreferredSize(new java.awt.Dimension(230, 16));

        jLabel7.setBackground(new java.awt.Color(204, 204, 204));
        jLabel7.setFont(new java.awt.Font("sansserif", 0, 10)); // NOI18N
        jLabel7.setText("(Passenden Zeitrahmen per Drag&Drop in die Zeitrahmenliste ziehen)");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(13, 13, 13))
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void fireValidityStateChangedEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireValidityStateChangedEvent
        if (evt.getSource() == jDayButton) {
            dateTimeField.setEnabled(true);
            dateTimeField.setTimeEnabled(false);
        } else if (evt.getSource() == jAlwaysButton) {
            dateTimeField.setEnabled(false);
        } else if (evt.getSource() == jExactTimeButton) {
            dateTimeField.setEnabled(true);
            dateTimeField.setTimeEnabled(true);
        }
        updatePreview();
    }//GEN-LAST:event_fireValidityStateChangedEvent
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private de.tor.tribes.ui.components.DateTimeField dateTimeField;
    private javax.swing.JRadioButton jAlwaysButton;
    private javax.swing.JRadioButton jDayButton;
    private javax.swing.JRadioButton jExactTimeButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private com.visutools.nav.bislider.BiSlider jSendTimeFrame;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JList jTimeFrameList;
    private javax.swing.JComboBox jTribeOnlyBox;
    private de.tor.tribes.ui.components.DateTimeField maxArriveTimeField;
    private de.tor.tribes.ui.components.DateTimeField minSendTimeField;
    // End of variables declaration//GEN-END:variables

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
    }

    @Override
    public void dragEnter(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragExit(DragSourceEvent dse) {
    }

    @Override
    public void dragOver(DragSourceDragEvent dsde) {
    }

    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        TimeSpan span = null;
        if (dge.getComponent().equals(jLabel5)) {
            span = getSendSpan();
        } else if (dge.getComponent().equals(jLabel6)) {
            span = getArriveSpan();
        }
        if (span != null) {
            dge.startDrag(null, new StringSelection(span.toPropertyString()), this);
        }
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (dtde.getDropTargetContext().getComponent().equals(jTimeFrameList)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        }
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        if (dtde.getDropTargetContext().getComponent().equals(jTimeFrameList)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        }
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        if (dtde.getDropTargetContext().getComponent().equals(jTimeFrameList)) {
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            try {
                String data = (String) dtde.getTransferable().getTransferData(DataFlavor.stringFlavor);
                TimeSpan s = TimeSpan.fromPropertyString(data);
                if (s == null) {
                    throw new UnsupportedFlavorException(DataFlavor.stringFlavor);
                }

                addTimeSpan(s);

            } catch (UnsupportedFlavorException usfe) {
                //invalid data
            } catch (IOException ioe) {
                //failed to get data
            }
        }
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    public static void main(String[] args) {
        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
        }
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new AttackTimePanel(null));
        f.pack();
        f.setVisible(true);
    }
}