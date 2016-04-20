package net.pixelsystems.ui;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.pixelsystems.client.Client;
import net.pixelsystems.client.Client.State;

@SuppressWarnings("serial")
public class ClientListTableModel extends AbstractTableModel {
	private static final String NAME_COLUMN="Name";
	private static final String IP_COLUMN="IP";
	private static final String STATE_COLUMN="State";
	private static String[]columnNames=new String[]{NAME_COLUMN,IP_COLUMN,STATE_COLUMN,};
	public static final Icon CLIENT_STATE_AVAILABLE= new ImageIcon("resources/client-state-available.png");
	public static final Icon CLIENT_STATE_UNKNOWN= new ImageIcon("resources/client-state-unknown.png");
	public static final Icon CLIENT_STATE_MISSING= new ImageIcon("resources/client-state-missing.png");
	List<Client>clients = new ArrayList<Client>();	
	
	public void addClient(Client client){
		clients.add(client);
		fireTableDataChanged();
		//fireTableCellUpdated(cams.size()-1, 0);
	}
	public void removeClientIndex(int row) {
		clients.remove(row);
		fireTableDataChanged();
	}

	public void saveState(JsonWriter writer)throws IOException{
		Gson saveState = new Gson();
		Type listType = new TypeToken<List<Client>>(){}.getType();
		String test = saveState.toJson(clients,listType);
		//writer.beginObject();
		writer.name("clients").value(test);
		//writer.endObject();
	}
	public void restoreState(JsonReader reader)throws IOException{
		Gson restoreState = new Gson();
		//reader.beginObject();
		String name = reader.nextName();
		if(name.equals("clients")){
			Type listType = new TypeToken<List<Client>>(){}.getType();
			clients = restoreState.fromJson(reader.nextString(), listType);
			for(Client client:clients){
				// force all to be unknown after session restore
				client.setState(State.UNKNOWN);
			}
			fireTableDataChanged();
		}
	}
	/*
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
	*/
	@Override
	public void setValueAt(Object value,int row, int col){
		Client client = clients.get(row);
		switch(col){
		case 0:
			client.setName(value.toString());
			break;
		case 1:
			client.setIP(value.toString());
			break;
		case 2:
			client.setState((Client.State)value);
			break;
		}
		fireTableCellUpdated(row,col);
	}

	@Override
	public int getRowCount() {
		return clients.size();
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
		Client client = clients.get(rowIndex);
		switch(columnIndex){
		case 0:
			return client.getName();
		case 1:
			return client.getIP();
		case 2:
			switch(client.getState()){
			case AVAILABLE:
				return CLIENT_STATE_AVAILABLE;
			case UNKNOWN:
				return CLIENT_STATE_UNKNOWN;
			case MISSING:
				return CLIENT_STATE_MISSING;
			}			
		}
		return "UNKNOWN";
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class getColumnClass(int c){
		if(c<2){
			return String.class;
		}
		return ImageIcon.class;
		
	}
	@Override
	public boolean isCellEditable(int row,int col){
		if(col<2){
			return true;
		}
		return false;
	}
	public List<Client> getClients() {
		return clients;
	}
	
	/*
	public class iconRenderer extends DefaultTableCellRenderer{
	    public Component getTableCellRendererComponent(JTable table,Object obj,boolean isSelected,boolean hasFocus,int row,int column){
	        imageicon i=(imageicon)obj;
	        if(obj==i)
	            setIcon(i.imageIcon);
	        setBorder(UIManager.getBorder("TableHeader.cellBorder"));
	        setHorizontalAlignment(JLabel.CENTER);
	        return this;
	    }
	}
	*/
}
