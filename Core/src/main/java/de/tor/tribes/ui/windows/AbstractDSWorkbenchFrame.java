/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.ui.windows;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.dnd.VillageTransferable;
import de.tor.tribes.ui.panels.MapPanel;
import de.tor.tribes.util.interfaces.DSWorkbenchFrameListener;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author Charon
 */
public abstract class AbstractDSWorkbenchFrame extends DSWorkbenchGesturedFrame implements DropTargetListener, DragGestureListener, DragSourceListener {

    private static Logger logger = LogManager.getLogger("AbstractDSWorkbenchFrame");

    @Override
    public void fireCloseGestureEvent() {
        setVisible(false);
    }

    @Override
    public void fireExportAsBBGestureEvent() {
    }

    @Override
    public void fireNextPageGestureEvent() {
    }

    @Override
    public void firePlainExportGestureEvent() {
    }

    @Override
    public void firePreviousPageGestureEvent() {
    }

    @Override
    public void fireRenameGestureEvent() {
    }

    @Override
    public void fireToBackgroundGestureEvent() {
        toBack();
    }
    private List<DSWorkbenchFrameListener> mFrameListeners = null;
    private DragSource dragSource;

    public AbstractDSWorkbenchFrame() {
        mFrameListeners = new LinkedList<>();
        // getContentPane().setBackground(Constants.DS_BACK);
        dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
        DropTarget dropTarget = new DropTarget(this, this);
        this.setDropTarget(dropTarget);
    }

    public abstract void resetView();

    public abstract void storeCustomProperties(Configuration pConfig);

    public abstract void restoreCustomProperties(Configuration pConfig);

    public abstract String getPropertyPrefix();

    private static final List<AbstractDSWorkbenchFrame> toSave = new ArrayList<>();
    private static PropertiesConfiguration frameProperties;
    public static void saveAllProperties() {
        String dataDir = DataHolder.getSingleton().getDataDirectory();
        if (!new File(dataDir).exists()) {
            logger.warn("Data directory '" + dataDir + "' does not exist. Skip writing properties");
            
            //set to uninitialised
            frameProperties = null;
            toSave.clear();
            
            return;
        }
        
        for(AbstractDSWorkbenchFrame s: toSave) {
            String prefix = s.getPropertyPrefix();
            frameProperties.setProperty(prefix + ".width", s.getWidth());
            frameProperties.setProperty(prefix + ".height", s.getHeight());
            frameProperties.setProperty(prefix + ".x", s.getX());
            frameProperties.setProperty(prefix + ".y", s.getY());
            frameProperties.setProperty(prefix + ".alwaysOnTop", s.isAlwaysOnTop());

            s.storeCustomProperties(frameProperties);
        }

        try {
            frameProperties.write(new FileWriter(dataDir + "/usergui.properties"));
        } catch (IOException | ConfigurationException ex) {
            logger.error("Failed to write properties", ex);
        }
        
        //set to uninitialised
        frameProperties = null;
        toSave.clear();
    }

    public void restoreProperties() {
        if(frameProperties == null) {
            frameProperties = new PropertiesConfiguration();
            String dataDir = DataHolder.getSingleton().getDataDirectory();
            if (!new File(dataDir).exists()) {
                logger.warn("Data directory '" + dataDir + "' does not exist. Skip reading properties");
                return;
            }
            
            try {
                frameProperties.read(new FileReader(dataDir + "/usergui.properties"));
                frameProperties.setThrowExceptionOnMissing(false);
            } catch (IOException | ConfigurationException ex) {
                logger.info("Cannot read properties", ex);
                return;
            }
        }
        String prefix = getPropertyPrefix();
        
        Dimension size = new Dimension(frameProperties.getInteger(prefix + ".width", getWidth()),
                frameProperties.getInteger(prefix + ".height", getHeight()));
        setPreferredSize(size);
        setSize(size);
        setLocation(frameProperties.getInteger(prefix + ".x", getX()),
                frameProperties.getInteger(prefix + ".y", getY()));
        setAlwaysOnTop(frameProperties.getBoolean(prefix + ".alwaysOnTop", false));

        restoreCustomProperties(frameProperties);
        toSave.add(this);
    }

    public synchronized void addFrameListener(DSWorkbenchFrameListener pListener) {
        if (pListener == null) {
            return;
        }
        if (!mFrameListeners.contains(pListener)) {
            mFrameListeners.add(pListener);
        }
    }

    public synchronized void removeFrameListener(DSWorkbenchFrameListener pListener) {
        mFrameListeners.remove(pListener);
    }

    @Override
    public void setVisible(boolean v) {
        super.setVisible(v);
        fireVisibilityChangedEvents(v);
    }

    public synchronized void fireVisibilityChangedEvents(boolean v) {
        for (DSWorkbenchFrameListener listener : mFrameListeners) {
            listener.fireVisibilityChangedEvent(this, v);
        }
    }

    public abstract void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation);

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(VillageTransferable.villageDataFlavor) || dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        if (dtde.isDataFlavorSupported(VillageTransferable.villageDataFlavor) || dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        } else {
            dtde.rejectDrop();
            return;
        }

        Transferable t = dtde.getTransferable();
        List<Village> v;
        MapPanel.getSingleton().setCurrentCursor(MapPanel.getSingleton().getCurrentCursor());
        try {
            v = (List<Village>) t.getTransferData(VillageTransferable.villageDataFlavor);
            fireVillagesDraggedEvent(v, dtde.getLocation());
        } catch (Exception ignored) {
        }
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
    }

    @Override
    public void dragEnter(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragOver(DragSourceDragEvent dsde) {
    }

    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragExit(DragSourceEvent dse) {
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
    }

    public void processGesture(String pGestureString) {
    }
}
