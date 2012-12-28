package org.xmetdb.rest.endpoints;

import java.net.URI;

import ambit2.base.data.Dictionary;

public class Enzyme extends Dictionary {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5987449761837302780L;
	protected String code;
	protected String parentCode;
	protected String[] alleles = null;
	protected URI uri = null;
	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public String[] getAlleles() {
		return alleles;
	}

	public void setAlleles(String[] alleles) {
		this.alleles = alleles;
	}

	public String getParentCode() {
		return parentCode;
	}

	public void setParentCode(String parentCode) {
		this.parentCode = parentCode;
	}
	protected int id;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Enzyme(String arg1,String arg2) {
		super(arg1,arg2);
	}
	
	public Enzyme(String arg1,String arg2,String arg3) {
		super(arg1,arg2,arg3);
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	@Override
	public String toString() {
		return String.format("%s.%s",getCode(),getName());
	}
}