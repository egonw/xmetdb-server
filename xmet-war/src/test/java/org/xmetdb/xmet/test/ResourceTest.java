package org.xmetdb.xmet.test;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.Assert;
import net.idea.modbcum.i.config.Preferences;
import net.idea.opentox.cli.task.RemoteTask;
import net.idea.restnet.c.ChemicalMediaType;
import net.idea.restnet.c.task.ClientResourceWrapper;
import net.idea.restnet.rdf.ns.OT;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opentox.aa.opensso.OpenSSOToken;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.w3c.dom.Document;
import org.xmetdb.rest.protocol.db.test.DbUnitTest;
import org.xmetdb.xmet.client.Resources;
import org.xmetdb.xmet.rest.XMETRESTComponent;
import org.xml.sax.InputSource;

import com.hp.hpl.jena.ontology.OntModel;

public abstract class ResourceTest extends DbUnitTest {
	protected Component component=null;
	protected OpenSSOToken ssoToken = null;

	protected int port = 8181;
	@Before
	public void setUp() throws Exception {
		super.setUp();
		setDatabase();

        Context context = new Context();
        context.getParameters().add(Preferences.DATABASE, getDatabase());
        context.getParameters().add(Preferences.USER, getUser());
        context.getParameters().add(Preferences.PASSWORD, getPWD());
        context.getParameters().add(Preferences.PORT, getPort());
        context.getParameters().add(Preferences.HOST, getHost());
        context.getParameters().add(Resources.Config.xmet_protected.name(), "false");
        context.getParameters().add("TESTAUTHZ", Boolean.TRUE.toString());
        context.getParameters().add(Resources.Config.users_dbname.name(),"aalocal_test");
        
        // Create a component
        component = new XMETRESTComponent(context);
        Server server = component.getServers().add(Protocol.HTTP, port);
        component.getServers().add(Protocol.HTTPS, port);   
      
        server.getContext().getParameters().set("tracing", "true",true);             
        component.start();        
	}

	protected String dbFile = "src/test/resources/org/xmetdb/xmet/xmet.xml";	
	public String getDbFile() {
		return dbFile;
	}

	protected void setDatabase() throws Exception {
		setUpDatabase("src/test/resources/org/xmetdb/xmet/tables.xml");
	}

	@After
	public void tearDown() throws Exception {
		
		if (component != null) {
			component.stop();
			component = null;
		}
	}
	public void testGet(String uri, MediaType media) throws Exception {
		testGet(uri, media,Status.SUCCESS_OK);
	}
	public Response testPost(String uri, MediaType media, Form queryForm) throws Exception {
		return testPost(uri, media, queryForm==null?null:queryForm.getWebRepresentation());
	}
	public Response testPost(String uri, MediaType media, String inputEntity) throws Exception {
		return testPost(uri, media, new StringRepresentation(inputEntity,media));
	}
	
	public Response testPut(String uri, MediaType media, Representation inputEntity) throws Exception {
		return testUpdate(uri, media, inputEntity, Method.PUT);
	}
	public Response testPost(String uri, MediaType media, Representation inputEntity) throws Exception {
		return testUpdate(uri, media, inputEntity, Method.POST);
	}
	public Response testDelete(String uri, MediaType media, Representation inputEntity) throws Exception {
		return testUpdate(uri, media, inputEntity, Method.DELETE);
	}	
	public Response testUpdate(String uri, MediaType media, Representation inputEntity,Method method) throws Exception {
		Request request = new Request();
		Client client = new Client(Protocol.HTTP);
		addToken2Header(request);
		ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;  
		ChallengeResponse authentication = new ChallengeResponse(scheme,  
		         "guest", "guest");  
		request.setChallengeResponse(authentication);  		
		
		request.setResourceRef(uri);
		//request.setResourceRef(String.format("%s?%s",uri,queryForm.getQueryString()));
		request.setMethod(method);

		if (inputEntity==null) request.setEntity(null);
		//else request.setEntity(inputEntity,media);
		else request.setEntity(inputEntity);
		request.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(media));
		return client.handle(request);
	}
	
	protected RemoteTask testAsyncPoll(Reference ref, MediaType media, HttpEntity rep, Method method) throws Exception {
		HttpClient cli = new DefaultHttpClient();
		RemoteTask task = new RemoteTask(cli,new URL(ref.toString()),media.toString(),rep,method.toString());
		while (!task.poll()) {
			Thread.yield();
			Thread.sleep(200);
		}
		if (task.isERROR()) throw task.getError();
		cli.getConnectionManager().shutdown();
		Assert.assertEquals(Status.SUCCESS_OK.getCode(), task.getStatus());
		return task;
		
       
	}
	
	public Reference testAsyncTask(String uri,HttpEntity rep,Status expected, String uriExpected) throws Exception {
		HttpClient cli = new DefaultHttpClient();
		RemoteTask task = new RemoteTask(cli,new URL(uri),
				MediaType.TEXT_URI_LIST.toString(),
				rep,
				Method.POST.toString());
		if (task.isERROR()) throw task.getError();
		Assert.assertNotNull(task.getResult());
		while (!task.poll()) {
			Thread.yield();
			Thread.sleep(2000);
		}
		Assert.assertEquals(expected, task.getStatus());
		if (task.getStatus()==Status.SUCCESS_OK.getCode())
			Assert.assertEquals(uriExpected, task.getResult().toString());
		else if (task.getError()!=null)
			throw task.getError();
		return new Reference(task.getResult());
		
	}
	/*
	public Reference testAsyncTask(String uri,Form headers,Status expected, String uriExpected) throws Exception {

		Response response  =  testPost(uri,MediaType.TEXT_URI_LIST,headers.getWebRepresentation());
		Status status = response.getStatus();
		Assert.assertEquals(Status.SUCCESS_ACCEPTED,status);
		
		uri = response.getEntity().getText().trim();
		Assert.assertNotNull(uri);
		
		Request request = new Request();
		Client client = new Client(Protocol.HTTP);
		request.setResourceRef(uri);
		request.setMethod(Method.GET);
		request.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.TEXT_URI_LIST));
		Reference ref = new Reference(uri);
		response.release();
		while (status.equals(Status.REDIRECTION_SEE_OTHER) || status.equals(Status.SUCCESS_ACCEPTED)) {
			System.out.println(status);
			System.out.println(ref);

			Response response1 = null;
			try  {
				response1 = client.handle(request);
				status = response1.getStatus();
				if (Status.SUCCESS_OK.equals(status)) {
					ref = new Reference(response1.getEntityAsText().trim());
					break;
				} else 		if (Status.SUCCESS_ACCEPTED.equals(status)) {
					ref = new Reference(response1.getEntityAsText().trim());
					request.setResourceRef(ref);
					if (ref == null) break;
				} 
			} finally {
				if (response1!=null) response1.release();	
			}
			Thread.yield();
			Thread.sleep(200);

		}
		Assert.assertEquals(uriExpected,ref==null?ref:ref.toString());
		Assert.assertEquals(expected, status);
		return ref;
	}	
	*/
	protected void addToken2Header(Request request) {
		if ((ssoToken!=null) && (ssoToken.getToken()!=null)) {
		    Object extraHeaders =  request.getAttributes().get("org.restlet.http.headers");
		    if (extraHeaders ==null) extraHeaders = new Form();
	        ((Form)extraHeaders).add("subjectid", ssoToken.getToken());
	        request.getAttributes().put("org.restlet.http.headers",extraHeaders);
		}
	}
	public Response testGet(String uri, MediaType media, Status expectedStatus) throws Exception {
		Request request = new Request();
		Client client = new Client(Protocol.HTTP);
		addToken2Header(request);
		request.setResourceRef(uri);
		request.setMethod(Method.GET);
		request.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(media));
		Response response = client.handle(request);
		Assert.assertEquals(expectedStatus.getCode(),response.getStatus().getCode());
		if (expectedStatus.equals(Status.SUCCESS_OK)) {
			Assert.assertTrue(response.isEntityAvailable());
			InputStream in = response.getEntity().getStream();
			Assert.assertTrue(verifyResponse(uri,media,in));
			in.close();	
		}
		return response;
	}
	@Test
	public void testGetJavaObject() throws Exception {
		testGetJavaObject(getTestURI(),MediaType.APPLICATION_JAVA_OBJECT,org.restlet.data.Status.SUCCESS_OK);
	}
	
	public void testGetJavaObject(String uri, MediaType media, Status expectedStatus) throws Exception {
		ClientResourceWrapper resource  = new ClientResourceWrapper(uri);
		resource.setMethod(Method.GET);
		resource.get(media);
		Assert.assertEquals(expectedStatus,resource.getStatus());
		if (resource.getStatus().isSuccess()) {
			verifyResponseJavaObject(uri,media,resource.getResponseEntity());
		}
	}	
	public Object verifyResponseJavaObject(String uri, MediaType media,Representation rep) throws Exception {
		ObjectRepresentation<Serializable> repObject = new ObjectRepresentation<Serializable>(rep);
		Serializable object = repObject.getObject();
		Assert.assertNotNull(object);
		return object;
		
	}		
	
	public void testGetRepresentation(String uri, MediaType media) throws Exception {
		ClientResourceWrapper client = new ClientResourceWrapper(uri);
		Assert.assertTrue(verifyRepresentation(uri,media,client.get(media)));
	}	
	
	public boolean verifyRepresentation(String uri, MediaType media,Representation representation) throws Exception {
		throw new Exception("Not implemented");
	}
	public Status testHandleError(String uri, MediaType media) throws Exception {
		Request request = new Request();
		Client client = new Client(Protocol.HTTP);
		addToken2Header(request);
		request.setResourceRef(uri);
		request.setMethod(Method.GET);
		request.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(media));
		Response response = client.handle(request);
		return response.getStatus();
	}	
	public String getTestURI() { return null; }
	
	public boolean verifyResponsePDF(String uri, MediaType media,InputStream in) throws Exception {
		throw new Exception("Not implemented");
	}
	public boolean verifyResponseHTML(String uri, MediaType media,InputStream in) throws Exception {
		throw new Exception("Not implemented");
	}	
	/*
	public boolean verifyResponseXML(String uri, MediaType media,InputStream in) throws Exception {
		throw new Exception("Not implemented");
	}
	*/		
	public boolean verifyResponseCML(String uri, MediaType media,InputStream in) throws Exception {
		throw new Exception("Not implemented");
	}		
	public boolean verifyResponseMOL(String uri, MediaType media,InputStream in) throws Exception {
		throw new Exception("Not implemented");
	}		
	public boolean verifyResponseSDF(String uri, MediaType media,InputStream in) throws Exception {
		throw new Exception("Not implemented");
	}		
	public boolean verifyResponseSMILES(String uri, MediaType media,InputStream in) throws Exception {
		throw new Exception("Not implemented");
	}		
	public boolean verifyResponseInChI(String uri, MediaType media,InputStream in) throws Exception {
		throw new Exception("Not implemented");
	}		
	public boolean verifyResponsePNG(String uri, MediaType media,InputStream in) throws Exception {
		throw new Exception("Not implemented");
	}		

	public boolean verifyResponseURI(String uri, MediaType media,InputStream in) throws Exception {
		throw new Exception("Not implemented");
	}	
	public boolean verifyResponseRDFN3(String uri, MediaType media,InputStream in) throws Exception {
		throw new Exception("Not implemented");
	}	
	public boolean verifyResponseTXT(String uri, MediaType media,InputStream in) throws Exception {
		throw new Exception("Not implemented");
	}	
	public boolean verifyResponseCSV(String uri, MediaType media,InputStream in) throws Exception {
		throw new Exception("Not implemented");
	}		
		
	public boolean verifyResponseWADL(String uri, MediaType media,InputStream in) throws Exception {
		throw new Exception("Not implemented");
	}	
	public OntModel verifyResponseRDFXML(String uri, MediaType media, InputStream in)
			throws Exception {
		OntModel model = OT.createModel();
		model.read(in,null);
		Assert.assertTrue(model.size()>0);
		return model;
	}	

	
	public OntModel verifyResponseRDFTurtle(String uri, MediaType media,InputStream in) throws Exception {
		OntModel model = OT.createModel();
		model.read(in,null,"TURTLE");
		Assert.assertTrue(model.size()>0);
		return model;
		//Predefined values for lang are "RDF/XML", "N-TRIPLE", "TURTLE" (or "TTL") and "N3". null represents the default language, "RDF/XML". "RDF/XML-ABBREV" is a synonym for "RDF/XML". 
	}		
	
	public boolean verifyResponse(String uri, MediaType media,InputStream in) throws Exception {
		if (MediaType.APPLICATION_PDF.equals(media))
			return verifyResponsePDF(uri, media, in);
		else if (MediaType.APPLICATION_WADL.equals(media))
			return verifyResponseWADL(uri, media, in);
		else if (MediaType.TEXT_HTML.equals(media))
			return verifyResponseHTML(uri, media, in);
		/*
		else if (MediaType.TEXT_XML.equals(media))
			return verifyResponseXML(uri, media, in);
			*/
		else if (MediaType.TEXT_URI_LIST.equals(media))
			return verifyResponseURI(uri, media, in);		
		else if (MediaType.TEXT_PLAIN.equals(media))
			return verifyResponseTXT(uri, media, in);			
		else if (MediaType.IMAGE_PNG.equals(media))
			return verifyResponsePNG(uri, media, in);
		else if (ChemicalMediaType.CHEMICAL_MDLMOL.equals(media))
			return verifyResponseMOL(uri, media, in);
		else if (ChemicalMediaType.CHEMICAL_SMILES.equals(media))
			return verifyResponseSMILES(uri, media, in);
		else if (ChemicalMediaType.CHEMICAL_INCHI.equals(media))
			return verifyResponseInChI(uri, media, in);		
		else if (ChemicalMediaType.CHEMICAL_MDLSDF.equals(media))
			return verifyResponseSDF(uri, media, in);
		else if (ChemicalMediaType.CHEMICAL_CML.equals(media))
			return verifyResponseCML(uri, media, in);

		else if (MediaType.TEXT_CSV.equals(media))
			return verifyResponseCSV(uri, media, in);
		else if (MediaType.APPLICATION_RDF_XML.equals(media))
			return verifyResponseRDFXML(uri, media, in)!=null;
		else if (MediaType.TEXT_RDF_N3.equals(media))
			return verifyResponseRDFN3(uri, media, in);		
		else if (MediaType.APPLICATION_RDF_TURTLE.equals(media))
			return verifyResponseRDFTurtle(uri, media, in)!=null;				
		else throw new Exception("Unknown format "+media);
	}
	protected Document createDOM(InputSource in) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(in);     
        return doc;
	}
	protected Document createDOM(InputStream in) throws Exception {
        return createDOM(new InputSource(in));     
	}
	protected Document createDOM(Reader reader) throws Exception {
        return createDOM(new InputSource(reader));
	}
	
	protected String getUserToken() {
		return null;
	}
	/**
	 * 
	 * @param formFields names
	 * @param formValues values
	 * @param file
	 * @return
	 * @throws Exception
	 */

	protected MultipartEntity getMultipartWebFormRepresentation(
				String[] formFields, String[] formValues, 
				File file, String mediaType) throws Exception {
		return getMultipartWebFormRepresentation(formFields, formValues, "filename",file, mediaType);
	}
	protected MultipartEntity getMultipartWebFormRepresentation(
				String[] formFields, String[] formValues, 
				String fileFieldName, File file, String mediaType) throws Exception {
		
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE,null,Charset.forName("UTF-8"));
	    for (int i=0;i< formFields.length;i++ ) {
	    	if (formValues[i]==null) continue;
	    	entity.addPart(formFields[i], new StringBody(formValues[i],Charset.forName("UTF-8")));
	    }
	   if (file !=null) entity.addPart(fileFieldName, new FileBody(file));	      
	    return entity;
	}	
	protected MultipartEntity getMultipartWebFormRepresentation(
			String[] formFields, String[] formValues, 
			String[] fileFieldNames, File[] files, String mediaType) throws Exception {
	
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE,null,Charset.forName("UTF-8"));
	    for (int i=0;i< formFields.length;i++ ) {
	    	if (formValues[i]==null) continue;
	    	entity.addPart(formFields[i], new StringBody(formValues[i],Charset.forName("UTF-8")));
	    }
	   if (files !=null) 
		   for (int i=0; i < files.length; i++) 
			   	entity.addPart(fileFieldNames[i], new FileBody(files[i]));	      
	    return entity;
	}
}
