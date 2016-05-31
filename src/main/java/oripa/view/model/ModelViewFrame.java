/**
 * ORIPA - Origami Pattern Editor 
 * Copyright (C) 2005-2009 Jun Mitani http://mitani.cs.tsukuba.ac.jp/

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package oripa.view.model;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;

import oripa.ORIPA;
import oripa.doc.Doc;
import oripa.doc.exporter.Exporter;
import oripa.doc.exporter.ExporterDXF;
import oripa.doc.exporter.ExporterOBJ2;
import oripa.file.FileFilterEx;
import oripa.fold.FolderTool;
import oripa.fold.OrigamiModel;
import oripa.paint.core.PaintConfig;
import oripa.resource.Constants;
import oripa.viewsetting.model.ModelFrameSettingDB;

/**
 * A frame to show a transparent folded model.
 * @author Koji
 *
 */
public class ModelViewFrame extends JFrame 
implements ActionListener, AdjustmentListener, Observer{

	private ModelFrameSettingDB setting = ModelFrameSettingDB.getInstance();

    ModelViewScreen screen;
    private JMenu menuDisp = new JMenu(ORIPA.res.getString("MENU_Disp"));
    private JMenu menuFile = new JMenu(ORIPA.res.getString("File"));
    private JMenuItem menuItemExportDXF = new JMenuItem(ORIPA.res.getString("MENU_ExportModelLine_DXF"));
    private JMenuItem menuItemExportOBJ = new JMenuItem("Export to OBJ file");
    private JMenuItem menuItemFlip = new JMenuItem(ORIPA.res.getString("MENU_Invert"));
    private JCheckBoxMenuItem menuItemCrossLine = new JCheckBoxMenuItem("Show Cross-Line", false);
    private JCheckBoxMenuItem menuItemCrossPoint = new JCheckBoxMenuItem("Show Cross-Point", false);
    public JCheckBoxMenuItem menuItemSlideFaces = new JCheckBoxMenuItem(ORIPA.res.getString("MENU_SlideFaces"), false);
    public JLabel hintLabel = new JLabel(ORIPA.res.getString("Direction_Basic"));
    private JMenu dispSubMenu = new JMenu(ORIPA.res.getString("MENU_DispType"));
    private JRadioButtonMenuItem menuItemFillColor = new JRadioButtonMenuItem(ORIPA.res.getString("MENU_FillColor"));
    private JRadioButtonMenuItem menuItemFillWhite = new JRadioButtonMenuItem(ORIPA.res.getString("MENU_FillWhite"));
    private JRadioButtonMenuItem menuItemFillAlpha = new JRadioButtonMenuItem(ORIPA.res.getString("MENU_FillAlpha"));
    private JRadioButtonMenuItem menuItemFillNone = new JRadioButtonMenuItem(ORIPA.res.getString("MENU_DrawLines"));
    private JScrollBar scrollBarHorizontal = new JScrollBar(JScrollBar.HORIZONTAL, 500, 5, 0, 1000);
    private JScrollBar scrollBarVertical = new JScrollBar(JScrollBar.VERTICAL, 500, 5, 0, 1000);

    public ModelViewFrame() {
    	
    	setting.addObserver(this);
    	
        setTitle(ORIPA.res.getString("ExpectedFoldedOrigami"));
        screen = new ModelViewScreen();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(screen, BorderLayout.CENTER);
        getContentPane().add(hintLabel, BorderLayout.SOUTH);
        getContentPane().add(scrollBarHorizontal, BorderLayout.NORTH);
        getContentPane().add(scrollBarVertical, BorderLayout.WEST);

        // Construct menu bar
        JMenuBar menuBar = new JMenuBar();

        menuFile.add(menuItemExportDXF);
        menuFile.add(menuItemExportOBJ);
        menuDisp.add(menuItemFlip);

        menuDisp.add(dispSubMenu);
        menuDisp.add(menuItemCrossLine);
        menuDisp.add(menuItemCrossPoint);
        ButtonGroup dispGroup = new ButtonGroup();
        dispGroup.add(menuItemFillAlpha);
        dispSubMenu.add(menuItemFillAlpha);
        dispGroup.add(menuItemFillNone);
        dispSubMenu.add(menuItemFillNone);
        menuItemFillAlpha.setSelected(true);
        menuItemFlip.addActionListener(this);
        menuItemSlideFaces.addActionListener(this);
        menuItemFillColor.addActionListener(this);
        menuItemFillWhite.addActionListener(this);
        menuItemFillAlpha.addActionListener(this);
        menuItemFillNone.addActionListener(this);
        menuItemExportDXF.addActionListener(this);
        menuItemExportOBJ.addActionListener(this);

        menuItemCrossLine.addActionListener(this);
        menuItemCrossPoint.addActionListener(this);
        menuBar.add(menuFile);
        menuBar.add(menuDisp);

        setJMenuBar(menuBar);

        scrollBarHorizontal.addAdjustmentListener(this);
        scrollBarVertical.addAdjustmentListener(this);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
    	Doc document = ORIPA.doc;
    	OrigamiModel origamiModel = document.getOrigamiModel();
		
    	FolderTool folderTool = new FolderTool();
		if (e.getSource() == menuItemFlip) {
    		folderTool.filpAll(origamiModel);
            screen.repaint();
        } else if (e.getSource() == menuItemSlideFaces) {
            folderTool.setFacesOutline(
            		origamiModel.getVertices(), origamiModel.getFaces(),
            		menuItemSlideFaces.isSelected());
            screen.repaint();
        } else if (e.getSource() == menuItemCrossLine) {
            PaintConfig.bDispCrossLine = menuItemCrossLine.isSelected();
            if (menuItemCrossLine.isSelected()) {
                PaintConfig.bDispCrossPoint = false;
                menuItemCrossPoint.setSelected(false);
                screen.recalcCrossLine();
            } else {
                screen.repaint();
                ORIPA.mainFrame.repaint();
            }
        } else if (e.getSource() == menuItemCrossPoint) {
            PaintConfig.bDispCrossPoint = menuItemCrossPoint.isSelected();
            if (menuItemCrossPoint.isSelected()) {
                PaintConfig.bDispCrossLine = false;
                menuItemCrossLine.setSelected(false);
                screen.recalcCrossPoint();
            } else {
                screen.repaint();
                ORIPA.mainFrame.repaint();
            }
        } else if (e.getSource() == menuItemExportDXF) {
            exportFile("dxf");
        } else if (e.getSource() == menuItemExportOBJ) {
            exportFile("obj");
        } else if (e.getSource() == menuItemFillColor
                || e.getSource() == menuItemFillWhite
                || e.getSource() == menuItemFillAlpha
                || e.getSource() == menuItemFillNone) {
            if (menuItemFillColor.isSelected()) {
                PaintConfig.modelDispMode = Constants.ModelDispMode.FILL_COLOR;
            } else if (menuItemFillWhite.isSelected()) {
                PaintConfig.modelDispMode = Constants.ModelDispMode.FILL_WHITE;
            } else if (menuItemFillAlpha.isSelected()) {
                PaintConfig.modelDispMode = Constants.ModelDispMode.FILL_ALPHA;
            } else if (menuItemFillNone.isSelected()) {
                PaintConfig.modelDispMode = Constants.ModelDispMode.FILL_NONE;
            }

            System.out.println("fillMode" + PaintConfig.modelDispMode);
            screen.repaint();
        }
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (e.getSource() == scrollBarHorizontal) {
            screen.setCrossLineAngle(e.getValue() * 185 / 1000);  // 0..185
            screen.setCrossPointX(e.getValue() * 400 / 1000 - 200);  // -200..200
        } else if (e.getSource() == scrollBarVertical) {
            screen.setCrossLinePosition(e.getValue() * 300 / 1000 - 150); // -150..150
            screen.setCrossPointY(e.getValue() * 400 / 1000 - 200);  // -200..200
        }

    }

    private void exportFile(String ext) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileFilterEx(new String[]{"." + ext}, 
                "(*." + ext + ")" + ext + ORIPA.res.getString("File")));
        if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(this)) {
            try {
                String filePath = fileChooser.getSelectedFile().getPath();
                File file = new File(filePath);
                if (file.exists()) {
                    if (JOptionPane.showConfirmDialog(
                            null, ORIPA.res.getString("Warning_SameNameFileExist"), 
                            ORIPA.res.getString("DialogTitle_FileSave"),
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) 
                            != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                if (!filePath.endsWith("." + ext)) {
                    filePath += "." + ext;
                }
                switch (ext) {
                    case "dxf":
                        ExporterDXF.exportModel(ORIPA.doc, filePath);
                        break;
                    case "obj":
                        Exporter exporter = new ExporterOBJ2();
                        exporter.export(ORIPA.doc, filePath);
                        break;
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this, e.toString(), ORIPA.res.getString("Error_FileSaveFailed"),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    @Override
    public void update(Observable o, Object arg) {
    	
    	if(setting.isFrameVisible()){
			setVisible(true);
			screen.resetViewMatrix();
			menuItemSlideFaces.setSelected(false);
			repaint();

    	}
    }
}
