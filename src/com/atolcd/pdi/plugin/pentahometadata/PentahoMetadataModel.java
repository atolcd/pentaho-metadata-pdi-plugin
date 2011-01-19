package com.atolcd.pdi.plugin.pentahometadata;

import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PentahoMetadataModel extends PentahoMetadataHelper {

	private String modelName;

	// Stockage des métadonnées du modèle métier
	private String[] pentahoModelColumnId = null;
	private String[] pentahoModelViewId = null;
	private String[] pentahoModelColumnName = null;
	private String[] pentahoModelViewName = null;
	private String[] pentahoModelColumnType = null;

	// Stockage des métadonnées de la Query (ResultSet)
	private String[] pentahoResultSetColumnNamesMetaData = null;
	private String[] pentahoResultSetViewNamesMetaData = null;
	private String[] pentahoResultSetColumnLabelsMetaData = null;
	private String[] pentahoResultSetColumnTypesMetaData = null;

	private Integer nbBusinessColumns = 0;
	private Integer nbRowsPentahoResultSet = 0;

	private Integer nbSelectedColumns = 0;

	private String[] headerValues = null;
	private String[][] cellValues = null;

	private String selectionFilter;
	private String constraintFilter;

	public PentahoMetadataModel(String serverName, String userName, String password, String domain, String modelName) {
		this.serverName = serverName;
		this.userName = userName;
		this.password = password;
		this.modelName = modelName;
		this.domain = domain;
	}

	/**
	 * Méthode de génération des métadonnées d'un Business Model Initialise les
	 * tableaux pentahoModelColumnNames, pentahoModelViewNames,
	 * pentahoModelColumnLabels, pentahoModelColumnTypes
	 */

	public void genereMetaData() {

		try {
			URL url = new URL(serverName + "/AdhocWebService?userid=" + userName + "&password=" + password + "&model=" + modelName + "&component=getbusinessmodel&domain=" + domain
					+ "/metadata.xmi");

			URLConnection conn = url.openConnection();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(conn.getInputStream());
			doc.getDocumentElement().normalize();

			// parcours des noeuds <view>
			NodeList viewNodeLst = doc.getElementsByTagName("view");
			String view_id = null;
			String view_name = null;

			int nbBusinessColumns = getNbBusinessColumns(doc);

			pentahoModelColumnId = new String[nbBusinessColumns];
			pentahoModelViewId = new String[nbBusinessColumns];
			pentahoModelColumnName = new String[nbBusinessColumns];
			pentahoModelViewName = new String[nbBusinessColumns];
			pentahoModelColumnType = new String[nbBusinessColumns];

			int colCount = 0;

			for (int s = 0; s < viewNodeLst.getLength(); s++) {

				Node viewNode = viewNodeLst.item(s);

				if (viewNode.getNodeType() == Node.ELEMENT_NODE) {

					Element view_Element = (Element) viewNode;
					Element viewId_Element = (Element) view_Element.getElementsByTagName("view_id").item(0);
					Element viewName_Element = (Element) view_Element.getElementsByTagName("view_name").item(0);
					view_id = viewId_Element.getTextContent();
					view_name = viewName_Element.getTextContent();

				}

				NodeList columnNodeList = viewNode.getChildNodes();
				for (int t = 0; t < columnNodeList.getLength(); t++) {

					Node columnNode = columnNodeList.item(t);

					if (columnNode.getNodeName().equals("column")) {

						Element column_Element = (Element) columnNode;

						Element columnId_Element = (Element) column_Element.getElementsByTagName("column_id").item(0);
						Element columnName_Element = (Element) column_Element.getElementsByTagName("column_name").item(0);
						Element columnType_Element = (Element) column_Element.getElementsByTagName("column_type").item(0);

						pentahoModelColumnId[colCount] = columnId_Element.getTextContent();
						pentahoModelColumnName[colCount] = columnName_Element.getTextContent();

						if (columnType_Element.getTextContent().equalsIgnoreCase("Numeric")) {
							pentahoModelColumnType[colCount] = "Number";

						} else if (columnType_Element.getTextContent().equalsIgnoreCase("Date")) {
							pentahoModelColumnType[colCount] = "Date";

						} else if (columnType_Element.getTextContent().equalsIgnoreCase("Boolean")) {
							pentahoModelColumnType[colCount] = "Boolean";

						} else if (columnType_Element.getTextContent().equalsIgnoreCase("Unknown")) {
							pentahoModelColumnType[colCount] = "String";

						} else if (columnType_Element.getTextContent().equalsIgnoreCase("String")) {
							pentahoModelColumnType[colCount] = "String";

						} else {
							pentahoModelColumnType[colCount] = "String";
						}

						pentahoModelViewId[colCount] = view_id;
						pentahoModelViewName[colCount] = view_name;

						colCount++;
					}
				}
			}

			for (int i = 0; i < pentahoModelColumnId.length; i++) {
				System.out.println(pentahoModelColumnId[i] + "|" + pentahoModelColumnName[i] + "|" + pentahoModelColumnType[i]);
			}

		} catch (Exception e) {
		
			e.printStackTrace();
		}
	}

	/**
	 * Méthode permettant de récupérer un tableau avec les intitulés des
	 * colonnes d'un Business Model
	 * 
	 * @return tableau des libéllés de colonnes
	 */
	public String[] getColumnLabels() {
		return pentahoModelColumnName;
	}

	/**
	 * Méthode renvoyant les données suite à une Query
	 * 
	 * @param targetUrl
	 *            url qui permet de récupérer les datas
	 * @param nbColumns
	 *            nombre de colonnes du ResultSet
	 * @return tableau à 2 dimensions String[row][column]=value
	 */
	public void setPentahoResultSet(String targetUrl, int nbColumns) {

		String[][] rowSet = null;

		try {

			URL url = new URL(targetUrl);

			URLConnection conn = url.openConnection();

			// On parse le flux de données renvoyé par l'appel URL
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(conn.getInputStream());
			doc.getDocumentElement().normalize();

			NodeList dataRowNodeList = doc.getElementsByTagName("DATA-ROW");

			nbRowsPentahoResultSet = dataRowNodeList.getLength();

			rowSet = new String[dataRowNodeList.getLength()][nbColumns];

			for (int rowNum = 0; rowNum < dataRowNodeList.getLength(); rowNum++) {

				Node dataRowNode = dataRowNodeList.item(rowNum);

				for (int colNum = 0; colNum < nbColumns; colNum++) {

					Element dataRow_Element = (Element) dataRowNode;
					Element dataItem_Element = (Element) dataRow_Element.getElementsByTagName("DATA-ITEM").item(colNum);

					if (dataItem_Element == null) {
						rowSet[rowNum][colNum] = "null";
					} else {
						rowSet[rowNum][colNum] = dataItem_Element.getTextContent();
					}
				}				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		cellValues = rowSet;

	}

	/**
	 * Méthode interne qui permet de retourner le nombre de colonnes métiers
	 * pour le Business Model
	 * 
	 * @param doc
	 * @return Integer Nombre de colonnes Métiers
	 */

	private Integer getNbBusinessColumns(Document doc) {

		NodeList viewNodeLst = doc.getElementsByTagName("view");
		int colCount = 0;

		for (int s = 0; s < viewNodeLst.getLength(); s++) {

			Node viewNode = viewNodeLst.item(s);
			NodeList columnNodeList = viewNode.getChildNodes();
			for (int t = 0; t < columnNodeList.getLength(); t++) {
				Node columnNode = columnNodeList.item(t);
				if (columnNode.getNodeName().equals("column")) {
					colCount++;
				}
			}
		}
		this.nbBusinessColumns = colCount;
		return colCount;
	}

	/**
	 * Méthode de génération des métadonnées d'un ResultSet Initialise les
	 * tableaux pentahoResultSetColumnNamesMetaData,
	 * pentahoResultSetViewNamesMetaData, pentahoResultSetColumnLabelsMetaData,
	 * pentahoResultSetColumnTypesMetaData
	 * 
	 * @param String
	 *            [] selectedColumns tableau stockant les colonnes sélectionnées
	 */
	/*
	 * public void genereResultSetMetaData(String[] selectedColumns) {
	 * 
	 * pentahoResultSetColumnNamesMetaData = new String[selectedColumns.length];
	 * pentahoResultSetViewNamesMetaData = new String[selectedColumns.length];
	 * pentahoResultSetColumnLabelsMetaData = new
	 * String[selectedColumns.length]; pentahoResultSetColumnTypesMetaData = new
	 * String[selectedColumns.length];
	 * 
	 * for (int i = 0; i < selectedColumns.length; i++) {
	 * pentahoResultSetColumnNamesMetaData[i] = pentahoModelColumnId[new
	 * Integer(selectedColumns[i])]; pentahoResultSetViewNamesMetaData[i] =
	 * pentahoModelViewId[new Integer(selectedColumns[i])];
	 * pentahoResultSetColumnLabelsMetaData[i] = pentahoModelColumnName[new
	 * Integer(selectedColumns[i])]; pentahoResultSetColumnTypesMetaData[i] =
	 * pentahoModelColumnType[new Integer(selectedColumns[i])];
	 * 
	 * }
	 * 
	 * // on affecte le nb de colonnes sélectionnées :
	 * this.setNbSelectedColumns(selectedColumns.length);
	 * 
	 * headerValues = pentahoResultSetColumnLabelsMetaData; }
	 */

	/**
	 * Méthode permettant de constuire le filtre pour la requête web
	 * 
	 * @return String paramètre 'selections'
	 */
	public String getSelectionFilter() {

		return selectionFilter;
	}

	public void setSelectionFilter(String selectionFilter) {
		this.selectionFilter = selectionFilter;
	}

	public String getConstraintFilter() {
		return constraintFilter;
	}

	public void setConstraintFilter(String constraintFilter) {
		this.constraintFilter = constraintFilter;
	}	

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String[] getPentahoModelColumnId() {
		return pentahoModelColumnId;
	}

	public String[] getPentahoModelViewId() {
		return pentahoModelViewId;
	}

	public String[] getPentahoModelColumnName() {
		return pentahoModelColumnName;
	}

	public String[] getPentahoModelViewName() {
		return pentahoModelViewName;
	}

	public String[] getPentahoModelColumnType() {
		return pentahoModelColumnType;
	}

	public Integer getNbBusinessColumns() {
		return nbBusinessColumns;
	}

	public void setNbBusinessColumns(Integer nbBusinessColumns) {
		this.nbBusinessColumns = nbBusinessColumns;
	}

	public String[] getPentahoResultSetColumnNamesMetaData() {
		return pentahoResultSetColumnNamesMetaData;
	}

	public String[] getPentahoResultSetViewNamesMetaData() {
		return pentahoResultSetViewNamesMetaData;
	}

	public String[] getPentahoResultSetColumnLabelsMetaData() {
		return pentahoResultSetColumnLabelsMetaData;
	}

	public String[] getPentahoResultSetColumnTypesMetaData() {
		return pentahoResultSetColumnTypesMetaData;
	}

	public Integer getNbRowsPentahoResultSet() {
		return nbRowsPentahoResultSet;
	}

	public String[][] getRows() {
		return cellValues;
	}

	public String[] getHeaderValues() {
		return headerValues;
	}

	public String[][] getCellValues() {
		return cellValues;
	}

	public Integer getNbSelectedColumns() {
		return nbSelectedColumns;
	}

	public void setNbSelectedColumns(Integer nbSelectedColumns) {
		this.nbSelectedColumns = nbSelectedColumns;
	}
}
