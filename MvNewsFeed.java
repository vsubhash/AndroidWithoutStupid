/*
 * AndroidWithoutStupid Java Library
 * Created by V. Subhash 
 * http://www.VSubhash.com
 * Released as Public Domain Software in 2014
 */
package com.vsubhash.droid.androidwithoutstupid;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.text.Html;


/**
 * This class can be used to read articles from a local RSS XML file. (Use
 * {@link MvAsyncDownload} or {@link MvGeneral#startSyncDownload(String, String)}
 * to download the RSS XML from the Net.) Calling the constructor immediately
 * parses the file. The articles in the XML file will then be available in the
 * {link {@link #moMessages} arraylist.
 * 
 * @author V. Subhash (<a href="http://www.VSubhash.com/">www.VSubhash.com</a>)
 * @version 2016.08.15         
 *
 */
public class MvNewsFeed {
	
  /**
   * Returns the value of a node. (This method fixes a bug in Android
   * version older 4.0)
   * 
   * @param aoNode DOM node whose value needs to be returned
   * @return value of the DOM node
   */
  public String get_NodeValueFix(Node aoNode) {
  	int i, n; 
  	String sNodeValue = "";
  	
  	if (aoNode.getChildNodes().getLength() == 1) {
  		sNodeValue = aoNode.getFirstChild().getNodeValue();
  	} else if (aoNode.getChildNodes().getLength() > 1) { 
  		n = aoNode.getChildNodes().getLength();
  		for (i = 0; i < n; i++) {
  			if (aoNode.getChildNodes().item(i).getNodeValue() != null) {
  				sNodeValue = sNodeValue + aoNode.getChildNodes().item(i).getNodeValue();
  			}
  		}
  	} 
  	return(sNodeValue);
  }	
	
	final String FEED_TYPE_RSS = "RSS";
	final String FEED_TYPE_ATOM = "ATOM";
	final String FEED_TYPE_RDF = "RDF";	
	final String FEED_TYPE_UNKNOWN_OR_INVALID = "UNKNOWN/INVALID";
	public final int MIN_URL_LENGTH = "http://a.aa/".length();
	
	/**
	 * Name of the feed in the XML.
	 */
	public String msFeedTitle  = "";
	/**
	 * Description of the feed in the XML.
	 */
	public String msFeedDescription = "";
	/**
	 * Self-referencing URL of the feed in the XML.
	 */
	public String msFeedLocation  = "";
	/**
	 * Articles or messages in the XML.
	 */
	public ArrayList<MvNewsFeedMessage> moMessages;
	/**
	 * URL of the logo of the feed in the XML.
	 */
	public String msFeedLogo = "";
	/**
	 * Whether the XML is RSS or XML - not implemented yet.
	 */
	public String msFeedType = "";
	/**
	 * Whether the XML is available.
	 */
	public boolean mbIsDownloaded = false;
	/**
	 * Whether the XML is valid.
	 */
	public boolean mbIsAvailable = false;
	/**
	 * Error encountered while reading the XML.
	 */
	public String msError = "";
	
	/**
	 * Creates an instance of this class and initializes it with articles loaded
	 * from specified local RSS XML file. Calling this constructor immediately
	 * parses the XML. After calling this constructor, the XML file is not required
	 * and can be discarded. The {@link #moMessages} list contains the 
	 * articles/messages.
	 * 
	 * @param asFile pathname of the RSS XML file.
	 * @param asUrl Self-referencing URL of the feed in the XML.
	 */
	public MvNewsFeed(String asFile, String asUrl) {
		DocumentBuilder oDocBuilder;
		Document doc;
		FileInputStream oInputStream;
		NodeList oDomNodes;
		int n;
		moMessages = new ArrayList<MvNewsFeed.MvNewsFeedMessage>();
		
		try {
			oInputStream = new FileInputStream(asFile);
			
			oDocBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = oDocBuilder.parse(oInputStream);
			
			oDomNodes = doc.getElementsByTagName("channel");
			n = oDomNodes.getLength();			
			if (n > 0) {
				this.msFeedType = this.FEED_TYPE_RSS;
				processRSS(oDomNodes);
				if (this.msFeedLocation.length() < 1) {
					this.msFeedLocation = asUrl;
				}
			} else {
				MvMessages.logMessage("no channel nodes");
				oDomNodes = doc.getElementsByTagName("rdf:RDF");
				n = oDomNodes.getLength();
				if (n > 0) {
					this.msFeedType = this.FEED_TYPE_RDF;
					processRDF(oDomNodes);
				} else {
					oDomNodes = doc.getElementsByTagName("feed");
					n = oDomNodes.getLength();
					if (n > 0) {
						this.msFeedType = this.FEED_TYPE_ATOM;
						processATOM(oDomNodes);
					} else {
						this.msFeedType = this.FEED_TYPE_UNKNOWN_OR_INVALID;
					}
				}
			}

			oInputStream.close();
			this.mbIsDownloaded = true;
			this.mbIsAvailable = true;
		} catch (ParserConfigurationException e) {
			this.mbIsAvailable = false;
			this.msError = "XML configuration error.";
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			this.mbIsAvailable = false;
			this.msError = "XML configuration error.";
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			this.mbIsAvailable = false;
			this.msError = "The XML file could not be found.";
			e.printStackTrace();
		} catch (SAXException e) {
			this.mbIsAvailable = false;
			this.msError = "The file is not valid XML.";
			e.printStackTrace();
		} catch (IOException e) {
			this.mbIsAvailable = false;
			this.msError = "The XML file could not be read.";
			e.printStackTrace();
		}
	}
	

	void processRDF(NodeList aoRDFNodes) {	
		int i, j, k, n, o, p;
		String sArticleTitle="", sArticleLink="", sArticleDate="", 
			   sArticleContent="", sArticleGuid="", sArticleEnclosure="";  		
		Date dtArticleDate = null;
		Node oRdfNode, oRdfChild, oChannelChild, oItemChild;
		NodeList oRdfChildren, oChannelChildren, oItemChildren;
		MvNewsFeedMessage oTempMessage;		
		
		
		if (aoRDFNodes.getLength() > 0) {
			n = aoRDFNodes.getLength();
			for (i = 0; i < n; i++) {
				oRdfNode = aoRDFNodes.item(i);
				if (oRdfNode.getNodeType() == Node.ELEMENT_NODE) {
					if (oRdfNode.getNodeName().toLowerCase().contentEquals("rdf:rdf")) {
						oRdfChildren = oRdfNode.getChildNodes();
						if (oRdfChildren.getLength() > 0) {
							o = oRdfChildren.getLength();
							for (j = 0; j < o; j++) {
								oRdfChild = oRdfChildren.item(j);
							  if (oRdfChild.getNodeType() == Node.ELEMENT_NODE) {
							  	if (oRdfChild.getNodeName().toLowerCase().contentEquals("channel")) {
							  	  oChannelChildren = oRdfChild.getChildNodes();
							  	  if (oChannelChildren.getLength() > 0) {
							  	  	p = oChannelChildren.getLength();
							  	  	for (k = 0; k < p; k++) {
							  	  	  oChannelChild = oChannelChildren.item(k);
							  	  	  if (oChannelChild.getNodeType() == Node.ELEMENT_NODE) {
							  	  	  	if (oChannelChild.getNodeName().toLowerCase().contentEquals("title")) {
							  	  	  		this.msFeedTitle = get_NodeValueFix(oChannelChild);
							  	  	  		this.msFeedTitle = this.msFeedTitle.replace('"', ' ').trim();
							  	  	  		this.msFeedTitle = this.msFeedTitle.replace(Pattern.quote("  "), " ");
							  	  	  		this.msFeedTitle = this.msFeedTitle.replace(Pattern.quote("  "), " ");
			  										this.msFeedTitle = Html.fromHtml(this.msFeedTitle).toString();
			  										this.msFeedTitle = Html.fromHtml(this.msFeedTitle).toString();
							  	  	  	} else if (oChannelChild.getNodeName().toLowerCase().contentEquals("link")) {
							  	  	  		this.msFeedLocation = get_NodeValueFix(oChannelChild);
							  	  	  	} else if (oChannelChild.getNodeName().toLowerCase().contentEquals("description")) {
							  	  	  		this.msFeedDescription = get_NodeValueFix(oChannelChild);
							  	  	  	} else if (oChannelChild.getNodeName().toLowerCase().contentEquals("dc:title")) {
							  	  	  		this.msFeedTitle = get_NodeValueFix(oChannelChild);
							  	  	  		this.msFeedTitle = this.msFeedTitle.replace('"', ' ').trim();
							  	  	  	}							  	  	  	
							  	  	  }
							  	  	}
							  	  }
							  	} else if (oRdfChild.getNodeName().toLowerCase().contentEquals("item")) {
							  	  oItemChildren = oRdfChild.getChildNodes();
							  	  if (oItemChildren.getLength() > 0) {
							  	  	p = oItemChildren.getLength();
		  	  	  	  		sArticleTitle = "";
		  	  	  	  		sArticleLink = "";
		  	  	  	  		sArticleDate = ""; 
		  	  	  	 			sArticleContent = "";
		  	  	  	 			sArticleGuid = ""; 
							  	  	for (k = 0; k < p; k++) {
							  	  	  oItemChild = oItemChildren.item(k);
							  	  	  if (oItemChild.getNodeType() == Node.ELEMENT_NODE) {
							  	  	  	
							  	  	  	if (oItemChild.getNodeName().toLowerCase().contentEquals("title") ||
							  	  	  			oItemChild.getNodeName().toLowerCase().contentEquals("dc:title")) {
							  	  	  		sArticleTitle = get_NodeValueFix(oItemChild);
							  	  	  		sArticleTitle = sArticleTitle.replace('"', ' ').trim();
							  	  	  		sArticleTitle = sArticleTitle.replaceAll(Pattern.quote("  "), " ");
							  	  	  		sArticleTitle = sArticleTitle.replaceAll(Pattern.quote("  "), " ");
		    	  	  	  		    sArticleTitle = Html.fromHtml(sArticleTitle).toString();
		    	  	  	  		    sArticleTitle = Html.fromHtml(sArticleTitle).toString();
							  	  	  	} else if (oItemChild.getNodeName().toLowerCase().contentEquals("link")) {
							  	  	  		sArticleLink = get_NodeValueFix(oItemChild);
							  	  	  	} else if (oItemChild.getNodeName().toLowerCase().contentEquals("description") ||
							  	  	  			       oItemChild.getNodeName().toLowerCase().contentEquals("dc:description") ||
							  	  	  			       oItemChild.getNodeName().toLowerCase().contentEquals("content:encoded")) {
							  	  	  		sArticleContent = get_NodeValueFix(oItemChild);
							  	  	  	} else if (oItemChild.getNodeName().toLowerCase().contentEquals("dc:date")) {
							  	  	  		sArticleDate = get_NodeValueFix(oItemChild);
							  	  	  		dtArticleDate = MvGeneral.getDateFromString(sArticleDate);
							  	  	  	}							  	  	  	
							  	  	  	sArticleGuid = sArticleLink + MvGeneral.getRandomNumber();
							  	  	  }
							  	  	}
							  	  	
							  	  	if (sArticleDate.contentEquals("")) {
							  	  		dtArticleDate = MvGeneral.getCurrentDateWithZeroedTime();
							  	  		sArticleDate = dtArticleDate.toLocaleString();
							  	  	}
							  	  	
							  	  	oTempMessage = new MvNewsFeedMessage(sArticleTitle, sArticleLink, dtArticleDate, sArticleContent, sArticleGuid, sArticleEnclosure);
							  	  	if ((sArticleLink.length() < 1) && (sArticleEnclosure.length() > 13)) {
							  	  	  oTempMessage.msMessageLink = sArticleEnclosure;
							  	  		
							  	  	} else if (sArticleLink.endsWith(".ogg") || 
				  	  	  			sArticleLink.endsWith(".ogv") ||
				  	  	  			sArticleLink.endsWith(".mp3") ||
				  	  	  			sArticleLink.endsWith(".mp4") ||
				  	  	  			sArticleLink.endsWith(".mov") ||
				  	  	  			sArticleLink.endsWith(".wma") ||
				  	  	  			sArticleLink.endsWith(".wmv")) {
					  	  	  		oTempMessage.msMessageEnclosure = sArticleLink;
					  	  	  	}
							  	  	this.moMessages.add(oTempMessage);							  	  	
							  	  }							  		
							  	}
							  	
							  }							  
							}
						}						
						break; // one rdf node is enough
					}				
				}
			}
		}
	}

  void processATOM(NodeList aoFeedNodes) {
  	int i, j, k, l, n, o, p;
  	Node oFeedNode, oFeedChild, oArticleChild, oMediaGroupChild;
  	NodeList oFeedNodeChildren,  oArticleChildren, oMediaGroupChildren;
		String sArticleTitle="", sArticleLink="", sArticleDate="", 
			   sArticleContent="", sArticleEnclosure="", sArticleGuid="", sMediaContentDescription = "", sArticleImage = "";  		
		Date dtArticleDate = null;
  	Element oFeedElement, oArticleElement, oMediaElement;
  	MvNewsFeedMessage oTempMessage;
  	
  	if (aoFeedNodes.getLength() > 0) {
  	  n = aoFeedNodes.getLength();
  	  for (i = 0; i < n; i++) {
  	  	oFeedNode = aoFeedNodes.item(i);
  	  	if (oFeedNode.getNodeType() == Node.ELEMENT_NODE) {
    	  	if (oFeedNode.getNodeName().toLowerCase().contentEquals("feed")) {
    	  		oFeedNodeChildren = oFeedNode.getChildNodes();
    	  		o = oFeedNodeChildren.getLength();
    	  	  for (j = 0; j < o; j++) {
    	  	  	oFeedChild = oFeedNodeChildren.item(j);
    	  	  	if (oFeedChild.getNodeType() == Node.ELEMENT_NODE) {
    	  	  		oFeedElement = (Element) oFeedChild;
    	   	  	  if (oFeedChild.getNodeName().contentEquals("title")) {
    	  	  	  	this.msFeedTitle = get_NodeValueFix(oFeedChild); 
    	  	  	  	this.msFeedTitle = this.msFeedTitle.replace('"', ' ').trim();
    	  	  	  	this.msFeedTitle = this.msFeedTitle.replace(Pattern.quote("  "), " ");
    	  	  	  	this.msFeedTitle = this.msFeedTitle.replace(Pattern.quote("  "), " ");
									this.msFeedTitle = Html.fromHtml(this.msFeedTitle).toString();
									this.msFeedTitle = Html.fromHtml(this.msFeedTitle).toString();
    	  	  	  } else if (oFeedChild.getNodeName().contentEquals("link")) {
    	  	  	  	this.msFeedLocation = oFeedElement.getAttribute("href");
    	  	  	  } else if (oFeedChild.getNodeName().contentEquals("entry")) {
    	  	  	  	oArticleChildren = oFeedChild.getChildNodes();
    	  	  	  	p = oArticleChildren.getLength();
  	  	  	  		sArticleTitle = "";
  	  	  	  		sArticleLink = "";
  	  	  	  		sArticleDate = ""; 
  	  	  	 			sArticleContent = "";
  	  	  	 			sArticleEnclosure = "";
  	  	  	 			sArticleGuid = ""; 
  	  	  	 			sMediaContentDescription = "";
    	  	  	  	for (k = 0; k < p; k++) {
    	  	  	  		oArticleChild = oArticleChildren.item(k);  	  	  		
    	  	  	  		if (oArticleChild.getNodeType() == Node.ELEMENT_NODE) {
    	  	  	  			oArticleElement = (Element) oArticleChild;
    	  	  	  			if (oArticleChild.getNodeName().contentEquals("title")) {
    	  	  	  		    sArticleTitle = get_NodeValueFix(oArticleChild);
    	  	  	  		    sArticleTitle = sArticleTitle.replaceAll(Pattern.quote("  "), " ");
    	  	  	  		    sArticleTitle = sArticleTitle.replaceAll(Pattern.quote("  "), " ");
    	  	  	  		    sArticleTitle = Html.fromHtml(sArticleTitle).toString();
    	  	  	  		    sArticleTitle = Html.fromHtml(sArticleTitle).toString();
    	  	  	  			} else if (oArticleChild.getNodeName().contentEquals("media:group")) {
    	  	  	  				if (oArticleChild.getChildNodes().getLength() > 0) {
    	  	  	  					oMediaGroupChildren = oArticleChild.getChildNodes();
    	  	  	  					for (l = 0; l < oMediaGroupChildren.getLength(); l++) {
    	  	  	  						oMediaGroupChild = oMediaGroupChildren.item(l);
    	  	  	  						if (oMediaGroupChild.getNodeName().contentEquals("media:thumbnail")) {
    	  	  	  							if (oMediaGroupChild.getNodeType() == Node.ELEMENT_NODE) {
    	  	  	  								oMediaElement = (Element) oMediaGroupChild;
    	  	  	  								if (oMediaElement.hasAttribute("url")) {
    	  	  	  									if (oMediaElement.getAttribute("url").length() > "http://www.w.w".length()) {
    	  	  	  										sArticleImage = oMediaElement.getAttribute("url");
    	  	  	  									}
    	  	  	  								}
    	  	  	  							}
    	  	  	  						}
    	  	  	  						if (oMediaGroupChild.getNodeName().contentEquals("media:description")) {
    	  	  	  						  sMediaContentDescription += get_NodeValueFix(oMediaGroupChild);
    	  	  	  						}
    	  	  	  					}
    	  	  	  				}
    	  	  	  				
    	  	  	  			} else if (oArticleChild.getNodeName().contentEquals("link")) {
    	  	  	  				if (oArticleElement.hasAttribute("href")) {
    	  	  	  					if (oArticleElement.hasAttribute("rel")) {
    	  	  	  						if ((oArticleElement.getAttribute("rel").contentEquals("enclosure")) ||  	  
    	  	  	  								(oArticleElement.getAttribute("href").toLowerCase().endsWith(".ogv")) ||
    	  	  	  								(oArticleElement.getAttribute("href").toLowerCase().endsWith(".ogg")) ||
    	  	  	  								(oArticleElement.getAttribute("href").toLowerCase().endsWith(".mp3")) ||
    	  	  	  								(oArticleElement.getAttribute("href").toLowerCase().endsWith(".mp4")) ||
    	  	  	  								(oArticleElement.getAttribute("href").toLowerCase().endsWith(".mov")) ||
    	  	  	  								(oArticleElement.getAttribute("href").toLowerCase().endsWith(".wma")) ||
    	  	  	  								(oArticleElement.getAttribute("href").toLowerCase().endsWith(".wmv"))) {
    	  	  	  						  sArticleEnclosure = oArticleElement.getAttribute("href");
    	  	  	  						} else {
    	  	  	  							sArticleLink = oArticleElement.getAttribute("href");
    	  	  	  						}
    	  	  	  					} else {
    	  	  	  					  sArticleLink = oArticleElement.getAttribute("href");
    	  	  	  					}
    	  	  	  				}  	  	  				
    	  	  	  			} else if (oArticleChild.getNodeName().contentEquals("published")) {
    	  	  	  		    sArticleDate = get_NodeValueFix(oArticleChild);
    	  	  	  		    dtArticleDate = MvGeneral.getDateFromString(sArticleDate); 
    	  	  	  			} else if (oArticleChild.getNodeName().contentEquals("updated") &&
    	  	  	  					       (sArticleDate.contentEquals(""))) {
    	  	  	  		    sArticleDate = get_NodeValueFix(oArticleChild);
    	  	  	  		    dtArticleDate = MvGeneral.getDateFromString(sArticleDate); 
    	  	  	  			} else if (oArticleChild.getNodeName().contentEquals("id")) {
    	  	  	  		    sArticleGuid = get_NodeValueFix(oArticleChild);
    	  	  	  			} else if (oArticleChild.getNodeName().contentEquals("summary") ||
    	  	  	  					       oArticleChild.getNodeName().contentEquals("content")) {
    	  	  	  		    sArticleContent = get_NodeValueFix(oArticleChild);
    	  	  	  			}    
    	  	  	  		}  	  	  	  
    	  	  	  	}
    	  	  	  	
    	  	  	  	if (sArticleDate.contentEquals("")) {
    	  	  	  		dtArticleDate = MvGeneral.getCurrentDateWithZeroedTime();
    	  	  	  		sArticleDate = dtArticleDate.toLocaleString();
    	  	  	  	}
    	  	  	  	
    	  	  	  	if ((sArticleContent.trim().length() <= sArticleTitle.trim().length()) &&  // content is no different than title
    	  	  	  			(sArticleContent.trim().length() < sMediaContentDescription.trim().length())) { // media description has more info
    	  	  	  		sArticleContent = sMediaContentDescription;
    	  	  	  	}
    	  	  	  	
    	  	  	  	oTempMessage = new MvNewsFeedMessage(sArticleTitle, sArticleLink, dtArticleDate, sArticleContent, sArticleGuid);
    	  	  	  	if (sArticleImage.length() > "http://www.a.a".length()) {
    	  	  	  		oTempMessage.msMessageImageLink = sArticleImage;
    	  	  	  	}
    	  	  	  	if (sArticleEnclosure.length() > 13) {
    	  	  	  	  oTempMessage.msMessageEnclosure = sArticleEnclosure;
    	  	  	  	  if (sArticleLink.length() < 13) {
    	  	  	  	  	oTempMessage.msMessageLink = sArticleEnclosure;
    	  	  	  	  }
    	  	  	  	} else if (sArticleLink.endsWith(".ogg") || 
				  	  	  					 sArticleLink.endsWith(".ogv") ||
				  	  	  					 sArticleLink.endsWith(".mp3") ||
				  	  	  					 sArticleLink.endsWith(".mp4") ||
				  	  	  					 sArticleLink.endsWith(".mov") ||
				  	  	  					 sArticleLink.endsWith(".wma") ||
				  	  	  					 sArticleLink.endsWith(".wmv")) {
    	  	  	  		oTempMessage.msMessageEnclosure = sArticleLink;
    	  	  	  	} 
    	  	  	  	this.moMessages.add(oTempMessage);
    	  	  	  }
    	  	  	}
    	  	  }
    	  		break;  // one feed node is enough
    	  	}
  	  	}
  	  }
  	}    	
  }
  
  void processRSS(NodeList aoChannelNodes) {
  	int i, j, k, l, n, o, p, q;
  	Node oChannelNode, oChannelChild, oArticleChild;
  	NodeList oChannelChildren, oArticleChildren;
  	Element /*oChannelChildElement,*/ oArticleElement;
  	MvNewsFeedMessage oNewArticle;
		String sArticleTitle="", sArticleLink="", sArticleDate="", 
			   sArticleContent="", sArticleEnclosure="", sArticleGuid="", sArticleImage = "";
  	Date dtArticlePublished = null;
		
		o = aoChannelNodes.getLength();
		for (j = 0; j < o; j++) {
			oChannelNode = aoChannelNodes.item(j);
			if (oChannelNode.getNodeType() == Node.ELEMENT_NODE) {
				oChannelChildren = oChannelNode.getChildNodes();
				p = oChannelChildren.getLength();
  			for (k = 0; k < p; k++) {
  			 	oChannelChild = oChannelChildren.item(k);
  			 	if (oChannelChild.getNodeType() == Node.ELEMENT_NODE) {
  			 		if (oChannelChild.getNodeName().toLowerCase().contentEquals("title")) {
  						this.msFeedTitle = get_NodeValueFix(oChannelChild);
  						this.msFeedTitle = this.msFeedTitle.replace('"', ' ').trim();  		
  						this.msFeedTitle = this.msFeedTitle.replace(Pattern.quote("  "), " ");
  						this.msFeedTitle = this.msFeedTitle.replace(Pattern.quote("  "), " ");
  						this.msFeedTitle = Html.fromHtml(this.msFeedTitle).toString();
  						this.msFeedTitle = Html.fromHtml(this.msFeedTitle).toString();
  					} else if (oChannelChild.getNodeName().toLowerCase().contentEquals("description")) {
  						this.msFeedDescription = get_NodeValueFix(oChannelChild);
  					} else if (oChannelChild.getNodeName().toLowerCase().contentEquals("link")) {
  						this.msFeedLocation = get_NodeValueFix(oChannelChild);
  					} else if (oChannelChild.getNodeName().toLowerCase().contentEquals("item")) {
  						oArticleChildren = oChannelChild.getChildNodes();
  						q = oArticleChildren.getLength();
							sArticleTitle=""; 
							sArticleLink=""; 
							sArticleDate=""; 
							sArticleContent=""; 
							sArticleEnclosure=""; 
							sArticleGuid="";  	
							dtArticlePublished = null;
							for (l = 0; l < q; l++) {
								oArticleChild = oArticleChildren.item(l);
								if (oArticleChild.getNodeType() == Node.ELEMENT_NODE) {
									oArticleElement = (Element) oArticleChild;
									if (oArticleChild.getNodeName().toLowerCase().contentEquals("title")) {
									  sArticleTitle = get_NodeValueFix(oArticleChild);	
									  sArticleTitle = sArticleTitle.replaceAll(Pattern.quote("  "), " ");
									  sArticleTitle = sArticleTitle.replaceAll(Pattern.quote("  "), " ");
									  sArticleTitle = Html.fromHtml(sArticleTitle).toString(); // bad formatting
									  sArticleTitle = Html.fromHtml(sArticleTitle).toString(); // bad formatting
									} else if (oArticleChild.getNodeName().toLowerCase().contentEquals("description")) {
									  sArticleContent = get_NodeValueFix(oArticleChild);
									  sArticleContent = // crooked google news
									  		sArticleContent.replaceAll(Pattern.quote(" src=\"//"), " src=\"http://");
									  sArticleContent = // crooked google news
									  		sArticleContent.replaceAll(Pattern.quote(" src=&quot;//"), "src=\"http://");
									} else if (oArticleChild.getNodeName().toLowerCase().contentEquals("content:encoded")) {
									  sArticleContent = get_NodeValueFix(oArticleChild);  									  
									} else if (oArticleChild.getNodeName().toLowerCase().contentEquals("link")) {
									  sArticleLink = get_NodeValueFix(oArticleChild);	
									} else if (oArticleChild.getNodeName().toLowerCase().contentEquals("guid")) {
									  sArticleGuid = get_NodeValueFix(oArticleChild);	
									} else if (oArticleChild.getNodeName().toLowerCase().contentEquals("pubdate")) {
									  sArticleDate = get_NodeValueFix(oArticleChild);
									  dtArticlePublished = MvGeneral.getDateFromString(sArticleDate);
									} else if (oArticleChild.getNodeName().toLowerCase().contentEquals("dc:date")) {
									  sArticleDate = get_NodeValueFix(oArticleChild);
									  dtArticlePublished = MvGeneral.getDateFromString(sArticleDate);
									} else if (oArticleChild.getNodeName().toLowerCase().contentEquals("media:thumbnail")) {
										if (oArticleElement.hasAttribute("url")) {
										  if (oArticleElement.getAttribute("url").length() > "http://www.w.w".length()) {
										  	sArticleImage = oArticleElement.getAttribute("url");
										  }	
										}
									} else if (oArticleChild.getNodeName().toLowerCase().contentEquals("enclosure")) {
										if (oArticleElement.hasAttribute("url")) {
									    sArticleEnclosure = oArticleElement.getAttribute("url");
										}
									}  
								}  								
  						}
  										
							if (sArticleDate.contentEquals("")) {
								dtArticlePublished = MvGeneral.getCurrentDateWithZeroedTime();
								sArticleDate = dtArticlePublished.toLocaleString();
							}
  										
							oNewArticle = new MvNewsFeedMessage(sArticleTitle, sArticleLink, dtArticlePublished, sArticleContent, sArticleGuid);
							if (sArticleImage.length() > "http://www.a.i".length()) {
								oNewArticle.msMessageImageLink = sArticleImage;
							}
							if (sArticleEnclosure.length() > MIN_URL_LENGTH) {
								oNewArticle.msMessageEnclosure = sArticleEnclosure;
	  	  	  	  if (sArticleLink.length() < MIN_URL_LENGTH) {
	  	  	  	  	oNewArticle.msMessageLink = sArticleEnclosure;
	  	  	  	  }
							} else if (sArticleLink.endsWith(".ogg") || 
					  	  	  		 sArticleLink.endsWith(".ogv") ||
					  	  	  		 sArticleLink.endsWith(".mp3") ||
					  	  	  		 sArticleLink.endsWith(".mp4") ||
					  	  	  		 sArticleLink.endsWith(".mov") ||
					  	  	  		 sArticleLink.endsWith(".wma") ||
					  	  	  		sArticleLink.endsWith(".wmv")) {
  	  	  	  	oNewArticle.msMessageEnclosure = sArticleLink;  					  	  	  	  	  	  	
	  	  	  	}
							this.moMessages.add(oNewArticle);
  					} 
  			 	}
  			}
  			break; // only one channel node needed
  		}
  	}
	}
	
	
	/**
	 * Internal class representing an article in the RSS XML file.
	 * 
	 * @author V. Subhash (<a
	 *         href="http://www.vsubhash.com/">www.VSubhash.com</a>)
	 * @version 2015.02.13
	 */
	static public class MvNewsFeedMessage {
		/**
		 * Title of the article
		 */
		public String msMessageTitle = "";
		/**
		 * ID of the article - for your implementation 
		 */
		public int miMessageID;
		/**
		 * ID of the RSS feed - for your implementation
		 */
		public int miMessageFeedID;
		/**
		 * Name of the RSS feed - for your implementation
		 */
		public String msMessageFeedName = "";
		/**
		 * Whether the article has been deleted by user - for your implementation
		 */
		public boolean mbDeleted = false;
		/**
		 * Whether the article has been starred (protected from deletion) by user -
		 * for your implementation
		 */
		public boolean mbStarred = false;
		/**
		 * Link to the article
		 */
		public String msMessageLink = "";
		/**
		 * Link to the audio/video podcast (MP3) file
		 */
		public String msMessageEnclosure = "";
		/**
		 * ID of the article
		 */
		public String msMessageGuid = "";
		/**
		 * Date of publication of the article
		 */
		public Date mdMessagePublished;
		/**
		 * Content/snippet of the article
		 */
		public String msMessageContent  = "";
		/**
		 * URL of a cover image for the article
		 */
		public String msMessageImageLink  = "";
		
		/**
		 * Creates an instance of this class.
		 * 
		 * @param asMessageTitle title of the article
		 * @param asMessageLink link to the article
		 * @param adMessagePublished date when the article was published
		 * @param asMessageContent content/snippet of the article
		 * @param asMessageGuid ID of the article
		 */
		public MvNewsFeedMessage(
				String asMessageTitle,
				String asMessageLink,
				Date adMessagePublished,
				String asMessageContent,
				String asMessageGuid) {
			this(
				asMessageTitle,
				asMessageLink,
				adMessagePublished,
				asMessageContent,
				asMessageGuid, "");
		}

		/**
		 * Creates an instance of this class.
		 * 
		 * @param asMessageTitle title of the article
		 * @param asMessageLink link to the article
		 * @param adMessagePublished date when the article was published
		 * @param asMessageContent content/snippet of the article
		 * @param asMessageGuid ID of the article
		 * @param asMessageEnclosure link to the audio/video podcast file
		 */
		public MvNewsFeedMessage(
				String asMessageTitle,
				String asMessageLink,
				Date adMessagePublished,
				String asMessageContent,
				String asMessageGuid,
				String asMessageEnclosure) {
			msMessageTitle = asMessageTitle;
			msMessageLink = asMessageLink;
			mdMessagePublished = adMessagePublished;
			msMessageContent = asMessageContent;
			msMessageGuid = asMessageGuid;
			msMessageEnclosure = asMessageEnclosure;
		}		
		
	}
	
  /**
   * Internal utility class representing an RSS feed. (This class is not needed
   * for using {@link MvNewsFeed}.)
   * 
   * @author V. Subhash
   *
   */
  static public class FeedInfo {
  	/**
  	 * URL of the feed - for your implementation
  	 */
  	public String msURL;
  	/**
  	 * ID of the feed - for your implementation
  	 */
  	public int miID;
  	/**
  	 * Name given by the end-user for the feed - for your implementation
  	 */
  	public String msGivenName;
  	/**
  	 * Name given in the XML for the feed - for your implementation
  	 */
  	public String msDeclaredName;
  	
  	/**
  	 * User-defined filter to limit article storage/retrieval - for your implementation
  	 */
  	public String msFilter;
  	
  	/**
  	 * Whether the feed is enabled by the end-user - for your implementation
  	 */
  	public boolean mbEnabled;

  	public FeedInfo(String asURL, int aiID) {
  		msURL = asURL;
  		miID = aiID;
  	}
  }
  


	
}
