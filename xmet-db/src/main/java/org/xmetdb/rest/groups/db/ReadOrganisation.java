package org.xmetdb.rest.groups.db;

import org.xmetdb.rest.groups.DBOrganisation;

public class ReadOrganisation extends ReadGroup<DBOrganisation> {


	/**
	 * 
	 */
	private static final long serialVersionUID = 3457882832757258639L;
	public ReadOrganisation(DBOrganisation group) {
		super(group);
	}
	@Override
	public DBOrganisation createObject() {
		return new DBOrganisation();
	}

}
