package be.pipauwel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.type.CollectionType;

import no.catenda.peregrine.model.objects.json.IfdConcept;
import no.catenda.peregrine.model.objects.json.IfdName;

public class DataLoader {	

	private OntModel om = null;

	public DataLoader() {
		om = null;
	}

	public static void main(String[] args) {
		DataLoader dl = new DataLoader();
		dl.LoadOntology();
		dl.PerformQueries();
		dl.RewriteOntology();
	}

	private void RewriteOntology() {
		
	}

	private void PerformQueries() {
		ExtendedIterator<OntClass> classes = om.listClasses();
		
		while (classes.hasNext())
	    {
	      OntClass thisClass = (OntClass) classes.next();
	      System.out.println(thisClass.toString());
	      if(om.getProperty(thisClass, RDFS.seeAlso)==null)
	    	  continue;
	      
	      RDFNode r = om.getProperty(thisClass, RDFS.seeAlso).getObject();
	      if(!r.asNode().getLocalName().endsWith("EnumType")){
	    	  	String q = r.asNode().getLocalName();
	  			String baseURL = "http://bsdd.buildingsmart.org/api/4.0";
	  			try {
	  				System.out.println("Querying bSDD for : " + q);
	  				java.net.URL url = new java.net.URL(baseURL + "/IfdConcept/search/" + q);
	  				ObjectMapper mapper = new ObjectMapper();
	  				mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
	  				mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
	  				java.net.URLConnection connection = url.openConnection();
	  				connection.setRequestProperty("Content-Type", "application/json");
	  				connection.setRequestProperty("Accept", "application/json");
	  				connection.connect();

	  				InputStream is = connection.getInputStream();
	  				List<IfdConcept> concepts = mapper.readerFor(new TypeReference<List<IfdConcept>>() { }).withRootName("IfdConcept").readValue(is);
	  			
	  				IfdConcept conc = null;
	  				for(IfdConcept ic : concepts){
	  					if(conc==null){
	  						conc = ic;
	  						continue;
	  					}
	  					
	  					try {
							Date prev = new SimpleDateFormat("yyyy.MM.dd", Locale.ENGLISH).parse(conc.getVersionDate().substring(0,10));
		  					Date next = new SimpleDateFormat("yyyy.MM.dd", Locale.ENGLISH).parse(ic.getVersionDate().substring(0,10));

		  					//"2016.01.25"
		  					if(!prev.before(next))
		  						continue;
		  					else{
		  						conc = ic;
		  					}
						} catch (ParseException e) {
							e.printStackTrace();
						}
	  				}
	  				
	  				//List<IfdName> names = conc.getFullNames();
//  					for(IfdName n : names){
//	  					System.out.println(n.getName());	  						
//  					}
  					System.out.println("---" + conc.getGuid());
  					System.out.println("---" + conc.getVersionDate());
	  			} catch (JsonParseException e) {
	  				e.printStackTrace();
	  			} catch (JsonMappingException e) {
	  				e.printStackTrace();
	  			} catch (IOException e) {
	  			}
	      }
	      else{
		      	//System.out.println(r);
		      	String q = r.asNode().getLocalName(); //EnumType
		      	String p = thisClass.getLocalName().split("-")[1];
		      	
	  			String baseURL = "http://bsdd.buildingsmart.org/api/4.0";
	  			try {
	  				System.out.println("Querying bSDD for : " + p);
	  				java.net.URL url = new java.net.URL(baseURL + "/IfdConcept/search/" + p);
	  				ObjectMapper mapper = new ObjectMapper();
	  				mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
	  				mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
	  				java.net.URLConnection connection = url.openConnection();
	  				connection.setRequestProperty("Content-Type", "application/json");
	  				connection.setRequestProperty("Accept", "application/json");
	  				connection.connect();

	  				InputStream is = connection.getInputStream();
	  				List<IfdConcept> concepts = mapper.readerFor(new TypeReference<List<IfdConcept>>() { }).withRootName("IfdConcept").readValue(is);
	  				
	  				for(IfdConcept ic : concepts){
	  					System.out.println("found concept: " + ic.getGuid());
	  				}
	  			} catch (JsonParseException e) {
	  				e.printStackTrace();
	  			} catch (JsonMappingException e) {
	  				e.printStackTrace();
	  			} catch (IOException e) {
	  			}
	      }	      
	    }
	}

	private void LoadOntology() {
        InputStream in = null;
        om = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
        in = DataLoader.class.getResourceAsStream("/prod_building_elements.ttl");
        om.read(in, null, "TTL"); 		
	}

}
