package org.xmetdb.rest.protocol.db.protocol.test;

import junit.framework.Assert;
import net.idea.modbcum.i.query.IQueryUpdate;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ITable;
import org.xmetdb.rest.protocol.DBProtocol;
import org.xmetdb.rest.protocol.db.CreateProtocolVersion;
import org.xmetdb.rest.protocol.db.test.CRUDTest;

public class ProtocolVersion_crud_test<T extends Object>  extends CRUDTest<T,DBProtocol>  {
	

	@Override
	protected IQueryUpdate<T,DBProtocol> createQuery() throws Exception {
		DBProtocol ref = new DBProtocol(id2v1);
		ref.setAbstract("abstrakt");
		ref.setTitle("title");
		return (IQueryUpdate<T,DBProtocol>)new CreateProtocolVersion(DBProtocol.generateIdentifier(),ref);
	}

	@Override
	protected void createVerify(IQueryUpdate<T,DBProtocol> query)
			throws Exception {
        IDatabaseConnection c = getConnection();	
		ITable table = 	c.createQueryTable("EXPECTED",
				String.format("SELECT idprotocol,version,published_status,title FROM protocol where idprotocol=2"));
		
		Assert.assertEquals(3,table.getRowCount());
		c.close();	
	}
	
	/**
	 * Create a version of a published protocol
	 */
	@Override
	protected IQueryUpdate<T, DBProtocol> createQueryNew() throws Exception {
		DBProtocol ref = new DBProtocol(id119v1);
		ref.setAbstract("abstrakt");
		ref.setTitle("title");
		return (IQueryUpdate<T,DBProtocol>)new CreateProtocolVersion(DBProtocol.generateIdentifier(),ref);
	}


	@Override
	protected void createVerifyNew(IQueryUpdate<T, DBProtocol> query)
			throws Exception {
        IDatabaseConnection c = getConnection();	
		ITable table = 	c.createQueryTable("EXPECTED",
				String.format("SELECT idprotocol,version,qmrf_number,published_status,title FROM protocol where idprotocol=119 order by idprotocol,version"));
		
		Assert.assertEquals(2,table.getRowCount());
		Assert.assertEquals("published",table.getValue(1,"published_status"));
		Assert.assertEquals("archived",table.getValue(0,"published_status"));
		Assert.assertEquals("Q2-10-14-119-v1",table.getValue(0,"qmrf_number"));
		Assert.assertEquals("Q2-10-14-119",table.getValue(1,"qmrf_number"));
		c.close();	
		
	}


	@Override
	public void testDelete() throws Exception {
	
	}
	
	@Override
	public void testUpdate() throws Exception {
	
	}

	@Override
	protected IQueryUpdate<T, DBProtocol> updateQuery() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected IQueryUpdate<T, DBProtocol> deleteQuery() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void updateVerify(IQueryUpdate<T, DBProtocol> query)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void deleteVerify(IQueryUpdate<T, DBProtocol> query)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

}