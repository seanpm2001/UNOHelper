/*
 * Hilfen f�r die Arbeit mit BrowseNodes
* Dateiname: BrowseNode.java
* Projekt  : n/a
* Funktion : Hilfen f�r die Arbeit mit BrowseNodes
* 
* Copyright: Landeshauptstadt M�nchen
*
* �nderungshistorie:
* Nr. |  Datum     |   Autor   | �nderungsgrund
* -------------------------------------------------------------------
* 001 | 07.07.2005 |    BNK    | Erstellung
* 002 | 16.08.2005 |    BNK    | korrekte Dienststellenbezeichnung
* 003 | 17.08.2005 |    BNK    | bessere Kommentare
* -------------------------------------------------------------------
*
* @author D-III-ITD 5.1 Matthias S. Benkmann
* @version 1.0
* 
*/
package de.muenchen.allg.afid;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

import com.sun.star.script.browse.XBrowseNode;
import com.sun.star.uno.UnoRuntime;

/**
 * Diese Klasse vereinfacht die Arbeit mit Objekten, die den Dienst BrowseNode
 * unterst�tzen. Diese sind vor allem
 * n�tzlich beim Durchsuchen des Baumes aller installierten Skripts.
 * @author bnk
 */
public class BrowseNode 
{
	private XBrowseNode node;
	public BrowseNode(XBrowseNode node) {this.node = node;}
	
	/**
	 * Gibt die URL des Makros zur�ck, wie sie f�r den Aufruf �ber 
	 * das Skripting-Framework ben�tigt wird. Nicht zu verwechseln mit "macro:" URLs!
	 * @author bnk
	 */
	public String URL() {return (String)UNO.getProperty(node, "URI");}
	
	/**
	 * Liefert das ungewrappte Objekt f�r die direkte �bergabe an UNO-Funktionen.
	 * @author bnk
	 */
	public XBrowseNode unwrap() {return node;}
	
	/**
	 * Liefert den Namen des Knoten.
	 * @author bnk
	 */
	public String getName() {return node.getName(); }
	
	/**
	 * Liefert den Typ des Knoten. Im Zusammenhang mit Makros sind die m�glichen 
	 * Werte aus der Konstantengruppe {@link com.sun.star.script.browse.BrowseNodeTypes}.
	 * @author bnk
	 */
	public short getType() {return node.getType();}
	
	/**
	 * Abk�rzung f�r queryInterface(). 
	 * @param c spezifiziert das Interface das gequeryt werden soll.
	 * @author bnk
	 */
	public Object as(Class c) 
	{
		return UnoRuntime.queryInterface(c, node);
	}
	
	/** Dieser Iterator liefert den Knoten und alle Abk�mmlinge als 
	 * {@link BrowseNode}s.*/
	public Iterator iterator() {return new ChildIterator(node);};
	
	protected static class ChildIterator implements Iterator 
	{
		private Vector toVisit = new Vector();
		
		public ChildIterator(XBrowseNode root)
		{
			toVisit.add(root);
		}
		
		/** @throws UnsupportedOperationException*/
		public void remove() {throw new UnsupportedOperationException();}
		public boolean hasNext() { return !toVisit.isEmpty() ;}
		
		/** Returns an {@link Object} of class {@BrowseNode}. */
		public Object next() throws NoSuchElementException
		{
			XBrowseNode retval = (XBrowseNode)toVisit.lastElement(); 
			expandLast();
			return new BrowseNode(retval); 
		}
		
		protected void expandLast()
		{
			if (toVisit.isEmpty()) throw new NoSuchElementException();
			
			/* Die try-catch Bl�cke sind zum Schutz gegen Bugs im SFW. Ich hatte z.B.
			 * einen Bug im Python-Modul, der eine Exception geworfen hat beim Aufruf
			 * von hasChildNodes(). */
			try{
				XBrowseNode xBrowseNode = (XBrowseNode)toVisit.remove(toVisit.size()-1);
				
				if (xBrowseNode.hasChildNodes())
				{
					XBrowseNode[] child = xBrowseNode.getChildNodes();
					for (int i = child.length - 1; i >= 0; --i)
						toVisit.add(child[i]);	  	
				}
			}catch(Exception e){}
		}
	}
}