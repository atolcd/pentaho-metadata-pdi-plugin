package com.atolcd.pdi.plugin.pentahometadata;

import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PentahoMetadataHelper {

	protected String serverName;
	protected String userName;
	protected String password;
	protected String domain;	// Un "domain" correspond à un fichier metadata.xmi dans un répertoire de type "SolutionId" 

	public PentahoMetadataHelper() {
	}

	public PentahoMetadataHelper(String serverName, String userName, String password, String domain) {
		this.serverName = serverName;
		this.userName = userName;
		this.password = password;
		this.domain = domain;
	}

	/**
	 * Méthode qui renvoie la liste des Business Models
	 * 
	 * @return String[][] tableau à 2 dimensions permettant de stocker le nom et
	 *         le libellé
	 * @throws OdaException
	 */
	public Map getModels() {

		HashMap<String, String> modelList = new HashMap<String, String>();
		
		try {

			URL url = new URL(serverName + "/AdhocWebService?userid=" + userName + "&password=" + password + "&domain=" + domain + "&component=listbusinessmodels" );

			System.out.println("URL récup models: " + url);
			
			URLConnection conn = url.openConnection();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(conn.getInputStream());
			doc.getDocumentElement().normalize();

			NodeList nodeLst = doc.getElementsByTagName("model");

			for (int s = 0; s < nodeLst.getLength(); s++) {

				Node node = nodeLst.item(s);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					
					Element model_Element = (Element) node;					
					Element modelId_Element = (Element) model_Element.getElementsByTagName("model_id").item(0);
					Element modelName_Element = (Element) model_Element.getElementsByTagName("model_name").item(0);
										
					modelList.put(modelId_Element.getTextContent(), modelName_Element.getTextContent());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		return modelList;
	}

	/**
	 * Méthode qui instancie un Objet PentahoModel à partir de son nom
	 * @param modelName
	 * @return PentahoModel
	 */
	public PentahoMetadataModel getModel(String modelName) {
		return new PentahoMetadataModel(serverName, userName, password, domain, modelName);
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getUsername() {
		return userName;
	}

	public void setUsername(String username) {
		this.userName = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

}
