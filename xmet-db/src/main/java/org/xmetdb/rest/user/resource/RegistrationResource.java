package org.xmetdb.rest.user.resource;

import java.io.Writer;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.idea.modbcum.i.exceptions.AmbitException;
import net.idea.modbcum.i.processors.IProcessor;
import net.idea.modbcum.i.reporter.Reporter;
import net.idea.restnet.c.ResourceDoc;
import net.idea.restnet.c.TaskApplication;
import net.idea.restnet.c.html.HTMLBeauty;
import net.idea.restnet.c.resource.CatalogResource;
import net.idea.restnet.c.task.FactoryTaskConvertor;
import net.idea.restnet.db.DBConnection;
import net.idea.restnet.i.task.ICallableTask;
import net.idea.restnet.i.task.ITaskStorage;
import net.idea.restnet.rdf.FactoryTaskConvertorRDF;
import net.idea.restnet.user.DBUser;
import net.idea.restnet.user.resource.UserURIReporter;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.xmetdb.rest.DBRoles;
import org.xmetdb.rest.protocol.XmetdbHTMLBeauty;
import org.xmetdb.rest.task.RegistrationTaskHTMLReporter;
import org.xmetdb.rest.user.XMETCallableUserCreator;
import org.xmetdb.xmet.client.Resources;
import org.xmetdb.xmet.client.Resources.Config;
import org.xmetdb.xmet.client.XMETDBRoles;

public class RegistrationResource extends CatalogResource<DBUser> {
	protected List<DBUser> dummyuser = new ArrayList<DBUser>();
	public RegistrationResource() {
		super();
		setHtmlbyTemplate(true);
		dummyuser.add(null);
	}
	
	@Override
	protected Iterator<DBUser> createQuery(Context context, Request request,
			Response response) throws ResourceException {
		return dummyuser.iterator();
	}

	@Override
	public String getTemplateName() {
		return "register.ftl";
	}

	
	@Override
	public void configureTemplateMap(Map<String, Object> map) {
	
		map.put("searchURI",Resources.register);
		map.put(XMETDBRoles.xmetdb_admin.name(), Boolean.FALSE);
		map.put(XMETDBRoles.xmetdb_curator.name(), Boolean.FALSE);
		
		if (getClientInfo()!=null) {
			if (getClientInfo().getUser()!=null)
				map.put("username", getClientInfo().getUser().getIdentifier());
			if (getClientInfo().getRoles()!=null) {
				if (DBRoles.isAdmin(getClientInfo().getRoles()))
					map.put(XMETDBRoles.xmetdb_admin.name(), Boolean.TRUE);
				if (DBRoles.isCurator(getClientInfo().getRoles()))
					map.put(XMETDBRoles.xmetdb_curator.name(), Boolean.TRUE);
				if (DBRoles.isUser(getClientInfo().getRoles()))
					map.put(XMETDBRoles.xmetdb_user.name(), Boolean.TRUE);	
			}
		}
		map.put("creator","Ideaconsult Ltd.");
	    map.put("xmet_root",getRequest().getRootRef());
	    map.put("queryService",((TaskApplication)getApplication()).getProperty(Resources.Config.xmet_ambit_service.name()));
        map.put(Resources.Config.xmet_email.name(),((TaskApplication)getApplication()).getProperty(Resources.Config.xmet_email.name()));
        map.put(Resources.Config.xmet_about.name(),((TaskApplication)getApplication()).getProperty(Resources.Config.xmet_about.name()));
        map.put(Resources.Config.xmet_guide.name(),((TaskApplication)getApplication()).getProperty(Resources.Config.xmet_guide.name()));

	}
	
	@Override
	protected Reference getSourceReference(Form form, DBUser model)
			throws ResourceException {
		return null;
	}
	
	@Override
	protected Representation put(Representation entity, Variant variant)
			throws ResourceException {
		throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
	}
	@Override
	protected Representation delete(Variant variant) throws ResourceException {
		throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
	}
	@Override
	protected ICallableTask createCallable(Method method, Form form, DBUser item)
			throws ResourceException {
		Connection conn = null;
		try {
			String usersdbname = getContext().getParameters().getFirstValue(Config.users_dbname.name());
			UserURIReporter reporter = new UserURIReporter(getRequest(),"");
			DBConnection dbc = new DBConnection(getApplication().getContext(),getConfigFile());
			conn = dbc.getConnection();
			return new XMETCallableUserCreator(method,item,reporter, form,getRequest().getRootRef().toString(),
					conn,getToken(),false,usersdbname==null?"xmet_users":usersdbname);
		} catch (Exception x) {
			try { conn.close(); } catch (Exception xx) {}
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL,x);
		}
	}
	
	public String getConfigFile() {
		return "conf/xmetdb.pref";
	}
	
	@Override
	protected HTMLBeauty getHTMLBeauty() {
		return new XmetdbHTMLBeauty(Resources.register);
	}
	
	@Override
	public IProcessor<Iterator<DBUser>, Representation> createJSONConvertor(
			Variant variant, String filenamePrefix) throws AmbitException,
			ResourceException {
		return super.createJSONConvertor(variant, filenamePrefix);
	}
	@Override
	protected FactoryTaskConvertor getFactoryTaskConvertor(ITaskStorage storage)
			throws ResourceException {
		return new FactoryTaskConvertorRDF<Object>(storage,getHTMLBeauty()) {
			@Override
			public synchronized Reporter<Iterator<UUID>, Writer> createTaskReporterHTML(
					Request request,ResourceDoc doc,HTMLBeauty htmlbeauty) throws AmbitException, ResourceException {
				return	new RegistrationTaskHTMLReporter(storage,request,doc,htmlbeauty);
			}	
			@Override
			public synchronized Representation createTaskRepresentation(
					UUID task, Variant variant, Request request,
					Response response, ResourceDoc doc)
					throws ResourceException {
				String locationRef = String.format("%s/task/%s", getRequest().getRootRef(),task);
				Representation r = new StringRepresentation(locationRef+"\n",MediaType.TEXT_URI_LIST);				
				response.redirectSeeOther(locationRef);
				return r;
			}
			
		};
	}
	
	@Override
	protected String getTaskTitle(DBUser item, Reference source) {
		return "New user registration";
	}
	@Override
	protected Representation processAndGenerateTask(Method method,
			Representation entity, Variant variant, boolean async)
			throws ResourceException {
		return super.processAndGenerateTask(method, entity, variant, async);
	}
	
}
