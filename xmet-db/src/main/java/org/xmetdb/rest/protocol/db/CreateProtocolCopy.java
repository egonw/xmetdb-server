package org.xmetdb.rest.protocol.db;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import net.idea.modbcum.i.IStoredProcStatement;
import net.idea.modbcum.i.exceptions.AmbitException;
import net.idea.modbcum.i.query.QueryParam;
import net.idea.modbcum.q.update.AbstractUpdate;
import net.idea.restnet.user.DBUser;

import org.xmetdb.rest.protocol.DBProtocol;

public class CreateProtocolCopy extends AbstractUpdate<DBUser,DBProtocol> implements IStoredProcStatement {
	protected static final ReadProtocol.fields[] f = new ReadProtocol.fields[] {
			ReadProtocol.fields.idprotocol,
			ReadProtocol.fields.version,
			ReadProtocol.fields.title,
			ReadProtocol.fields.anabstract,
		//	ReadProtocol.fields.filename
	};
	protected String[] create_sql = {"{CALL createProtocolCopy(?,?,?)}"};

	public CreateProtocolCopy(DBUser user,DBProtocol ref) {
		super(ref);
		setGroup(user);
	}

	public List<QueryParam> getParameters(int index) throws AmbitException {
		if (!getObject().isValidIdentifier()) throw new AmbitException("No observation ID");
		if ((getGroup()==null) || (getGroup().getUserName()==null)) throw new AmbitException("No Owner!");
		List<QueryParam> params1 = new ArrayList<QueryParam>();
		params1.add(ReadProtocol.fields.identifier.getParam(getObject()));
		params1.add(new QueryParam<String>(String.class, getGroup().getUserName()));
		params1.add(new QueryParam<Integer>(Integer.class, -1));
		return params1;
	}

	public String[] getSQL() throws AmbitException {
		return create_sql;
	}
	public void setID(int index, int id) {
		getObject().setID(id);
	}
	@Override
	public boolean returnKeys(int index) {
		return false;
	}
	
	@Override
	public boolean isStoredProcedure() {
		return true;
	}
	
	/**
	 * Allows retrieving stored procedure output parameters; 
	 * Does nothing by default
	 */
	@Override
	public void getStoredProcedureOutVars(CallableStatement statement) throws SQLException {
		String id = statement.getString(3);
		getObject().setIdentifier(id);
	}
	@Override
	public void registerOutParameters(CallableStatement statement)
			throws SQLException {
		statement.registerOutParameter(3,Types.VARCHAR);
		
	}
}