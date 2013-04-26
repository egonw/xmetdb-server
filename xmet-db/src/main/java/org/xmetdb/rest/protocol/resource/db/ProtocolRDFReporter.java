package org.xmetdb.rest.protocol.resource.db;

import java.net.URL;

import net.idea.modbcum.i.IQueryCondition;
import net.idea.modbcum.i.IQueryRetrieval;
import net.idea.modbcum.i.exceptions.AmbitException;
import net.idea.modbcum.i.exceptions.DbAmbitException;
import net.idea.modbcum.p.DefaultAmbitProcessor;
import net.idea.modbcum.p.MasterDetailsProcessor;
import net.idea.restnet.c.ResourceDoc;
import net.idea.restnet.db.QueryURIReporter;
import net.idea.restnet.db.convertors.QueryRDFReporter;
import net.idea.restnet.groups.DBOrganisation;
import net.idea.restnet.groups.DBProject;
import net.idea.restnet.groups.IDBGroup;
import net.idea.restnet.groups.resource.GroupQueryURIReporter;
import net.idea.restnet.user.DBUser;
import net.idea.restnet.user.resource.UserURIReporter;
import net.toxbank.client.io.rdf.ProtocolIO;
import net.toxbank.client.io.rdf.TOXBANK;

import org.restlet.Request;
import org.restlet.data.MediaType;
import org.xmetdb.rest.protocol.DBProtocol;
import org.xmetdb.rest.protocol.attachments.AttachmentURIReporter;
import org.xmetdb.rest.protocol.attachments.DBAttachment;
import org.xmetdb.rest.protocol.attachments.db.ReadAttachment;
import org.xmetdb.xmet.client.Resources;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.XSD;

public class ProtocolRDFReporter<Q extends IQueryRetrieval<DBProtocol>> extends QueryRDFReporter<DBProtocol, Q> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8857789530109166243L;
	protected ProtocolIO ioClass = new ProtocolIO();
	protected GroupQueryURIReporter<IQueryRetrieval<IDBGroup>> groupReporter;
	protected AttachmentURIReporter<IQueryRetrieval<DBAttachment>> attachmentReporter;
	protected UserURIReporter<IQueryRetrieval<DBUser>> userReporter;
	
	public ProtocolRDFReporter(Request request,MediaType mediaType,ResourceDoc doc) {
		super(request,mediaType,doc);
		groupReporter = new GroupQueryURIReporter<IQueryRetrieval<IDBGroup>>(request);
		attachmentReporter = new AttachmentURIReporter<IQueryRetrieval<DBAttachment>>(request);
		userReporter = new UserURIReporter<IQueryRetrieval<DBUser>>(request);
		getProcessors().clear();
		/*
		IQueryRetrieval<DBUser> queryP = new ReadAuthor(null,null); 
		MasterDetailsProcessor<DBProtocol,DBUser,IQueryCondition> authersReader = new MasterDetailsProcessor<DBProtocol,DBUser,IQueryCondition>(queryP) {
			@Override
			protected DBProtocol processDetail(DBProtocol target, DBUser detail)
					throws Exception {

				detail.setResourceURL(new URL(userReporter.getURI(detail)));
				target.addAuthor(detail);
				return target;
			}
		};
		getProcessors().add(authersReader);
		*/
		IQueryRetrieval<DBAttachment> queryP = new ReadAttachment(null,null); 
		MasterDetailsProcessor<DBProtocol,DBAttachment,IQueryCondition> attachmentReader = new MasterDetailsProcessor<DBProtocol,DBAttachment,IQueryCondition>(queryP) {
			@Override
			protected DBProtocol processDetail(DBProtocol target, DBAttachment detail)
					throws Exception {
				
				detail.setResourceURL(new URL(attachmentReporter.getURI(detail)));
				target.getAttachments().add(detail);
				return target;
			}
		};
		getProcessors().add(attachmentReader);		
		processors.add(new DefaultAmbitProcessor<DBProtocol,DBProtocol>() {
			@Override
			public DBProtocol process(DBProtocol target) throws AmbitException {
				processItem(target);
				return target;
			};
		});				
	}
	@Override
	protected QueryURIReporter createURIReporter(Request reference,ResourceDoc doc) {
		return new ProtocolQueryURIReporter(reference);
	}
	@Override
	public void setOutput(Model output) throws AmbitException {
		this.output = output;
		if (output!=null) {
			output.setNsPrefix("tbpl", String.format("%s%s/",uriReporter.getBaseReference().toString(),Resources.protocol));
			output.setNsPrefix("tbpt", String.format("%s%s/",uriReporter.getBaseReference().toString(),Resources.project));
			output.setNsPrefix("tbo", String.format("%s%s/",uriReporter.getBaseReference().toString(),Resources.organisation));
			output.setNsPrefix("tbu", String.format("%s%s/",uriReporter.getBaseReference().toString(),Resources.user));
			output.setNsPrefix("tb", TOXBANK.URI);
			output.setNsPrefix("dcterms", DCTerms.getURI());
			output.setNsPrefix("xsd", XSD.getURI());
			output.setNsPrefix("foaf", FOAF.NS);
		}
	}
	@Override
	public Object processItem(DBProtocol item) throws AmbitException {
		try {
			if ((item.getProject()!=null) && (item.getProject().getResourceURL()==null))
				item.getProject().setResourceURL(new URL(groupReporter.getURI((DBProject)item.getProject())));
			if ((item.getOrganisation()!=null) && (item.getOrganisation().getResourceURL()==null))
				item.getOrganisation().setResourceURL(new URL(groupReporter.getURI((DBOrganisation)item.getOrganisation())));
			if ((item.getOwner()!=null) && (item.getOwner().getResourceURL()==null))
				item.getOwner().setResourceURL(new URL(userReporter.getURI((DBUser)item.getOwner())));
						
		
			String uri = uriReporter.getURI(item);
			//output.setNsPrefix(item.getIdentifier(), String.format("%s/",uri));
			item.setResourceURL(new URL(uri));
			//no local file names should be serialized!
			//if (item.getDocument()!=null) item.getDocument().setResourceURL(new URL(String.format("%s%s",uri,Resources.document)));
			//if (item.getDataTemplate()!=null) item.getDataTemplate().setResourceURL(new URL(String.format("%s%s",uri,Resources.datatemplate)));
			
			ioClass.objectToJena(
				getJenaModel(), // create a new class
				item
			);
			return item;
		} catch (Exception x) {
			throw new AmbitException(x);
		}
	}
	
	public void open() throws DbAmbitException {
		
	}

}
