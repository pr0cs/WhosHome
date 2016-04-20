package net.pixelsystems.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import net.pixelsystems.server.CameraData;

@SuppressWarnings("serial")
public class CameraListTableModel extends AbstractTableModel {
	
	private static final String NAME_COLUMN="Name";
	private static final String ENABLED_COLUMN="Enabled";
	private static final String IS_MOTION_DETECT="Is Motion Detecting";
	private static final String IS_INCLUDED="Include?";
	private static String[]columnNames=new String[]{NAME_COLUMN,ENABLED_COLUMN,IS_MOTION_DETECT,IS_INCLUDED};
	private List<CameraData>cams = new ArrayList<CameraData>();
	private Map<CameraData,Boolean>includeCam=new HashMap<CameraData,Boolean>();
	
	public void addCam(CameraData cam){
		cams.add(cam);
		includeCam.put(cam, true);
		fireTableDataChanged();
		//fireTableCellUpdated(cams.size()-1, 0);
	}
	
	public int getIncludedColIndex(JTable camTable){
		return camTable.getColumn(IS_INCLUDED).getModelIndex();
	}
	public int getNameColIndex(JTable camTable){
		return camTable.getColumn(NAME_COLUMN).getModelIndex();
	}
	public int getMotionColIndex(JTable camTable){
		return camTable.getColumn(IS_MOTION_DETECT).getModelIndex();
	}
	
	public void updateMotionStateForRow(JTable camTable, int row, boolean state){
		int col = getMotionColIndex(camTable);
		this.setValueAt(state, row, col);
	}
	
	@Override
	public void setValueAt(Object value,int row, int col){
		if(col==3){
			CameraData cam = cams.get(row);
			includeCam.put(cam, (Boolean)value);
			fireTableCellUpdated(row,col);
		}
	}

	@Override
	public int getRowCount() {
		return cams.size();
	}
	@Override
	public String getColumnName(int col){
		return columnNames[col];
	}
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		CameraData cam = cams.get(rowIndex);
		switch(columnIndex){
		case 0:
			return cam.getName();
		case 1:
			return cam.isEnabled();
		case 2:
			return cam.isMotionDetector();
		case 3:
			return includeCam.get(cam);
		}
		return "UNKNOWN";
	}
	@Override
	public Class getColumnClass(int c){
		if(c==0){
			return String.class;
		}
		return Boolean.class;
		
	}
	@Override
	public boolean isCellEditable(int row,int col){
		if(col==3){
			return true;
		}
		return false;
	}
}
