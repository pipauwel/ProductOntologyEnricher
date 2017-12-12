package be.pipauwel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.catenda.peregrine.model.objects.json.IfdConcept;

public class DataLoader {	

	private OntModel om = null;

	public DataLoader() {
		om = null;
	}

	public static void main(String[] args) {
		DataLoader dl = new DataLoader();
		//dl.LoadOntology();
		dl.PerformQueries();
		dl.RewriteOntology();
	}

	private void RewriteOntology() {
		
	}

	private void PerformQueries() {
		String q = "IfcWindow";
		String baseURL = "http://bsdd.buildingsmart.org/api/4.0";
		try {
//			java.net.URL url = new java.net.URL(baseURL + "/IfdConcept/search/" + q);
//			JAXBContext context = JAXBContext.newInstance( IfdConcept.class );
//			java.net.URLConnection connection = url.openConnection();
//			connection.connect();
//
//			Unmarshaller unmarshaller = context.createUnmarshaller();
//			IfdConcept result = (IfdConcept) unmarshaller.unmarshal(connection.getInputStream());
			
			java.net.URL url = new java.net.URL(baseURL + "/IfdConcept/search/" + q);
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			java.net.URLConnection connection = url.openConnection();
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Accept", "application/json");
			connection.connect();			

		    JsonNode tree = null;
		    tree = new ObjectMapper().readTree(connection.getInputStream());
			JsonNode rootNode = tree.get("IfdConcept"); // Get the only element in the root node
		    // get an element in that node
		    JsonNode aField = rootNode.get("guid");
		    String guid = aField.toString();
		    System.out.println(guid);
		    
		    IfdConcept ifdc = new IfdConcept();
		    ifdc.setGuid(guid);
			
			//IfdConcept result = (IfdConcept) mapper.readValue( connection.getInputStream(), IfdConcept.class );
			
//			BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
//			StringBuilder sb = new StringBuilder();
//			String output;
//			while ((output = br.readLine()) != null) {
//				sb.append(output);
//			}
//			String x = sb.toString();
//			System.out.println(x);

		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//		} catch (JAXBException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
		
	}

	private void LoadOntology() {
        InputStream in = null;
        om = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
        in = DataLoader.class.getResourceAsStream("/prod_building_elements.ttl");
        om.read(in, null, "TTL"); 		
	}

}
