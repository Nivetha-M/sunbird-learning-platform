package org.ekstep.platform.content;

import org.ekstep.platform.domain.BaseTest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.platform.domain.BaseTest;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

public class CopyContentV3APITests extends BaseTest{


	int rn = generateRandomInt(0, 9999999);
	String jsonCreateValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_" + rn+ "\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
	String jsonUpdateHierarchyTwoChild = "{\"request\":{\"data\":{\"nodesModified\":{\"unitId1\":{\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\", \"name\":\"LP_FT_CourseUnit1_+rn+\",\"contentType\":\"TextBookUnit\",\"code\":\"Test_QA\"}},\"unitId2\":{\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"name\":\"LP_FT_CourseUnit2_+rn+\",\"contentType\":\"TextBookUnit\",\"code\":\"Test_QA\"}}},"
			+ "\"hierarchy\":{\"TextbookId\":{\"name\":\"LP_NFT_Collection_"+rn+"\",\"contentType\":\"TextBook\",\"children\":[\"unitId1\",\"unitId2\"],\"root\":true},\"unitId1\":{\"name\":\"LP_FT_CourseUnit1_"+rn+"\",\"contentType\":\"TextBookUnit\",\"children\":[\"contentId1\"],\"root\":false},\"unitId2\":{\"name\":\"LP_FT_CourseUnit2_"+rn+"\",\"contentType\":\"TextBookUnit\",\"children\":[\"contentId2\"],\"root\":false},\"contentId1\":{\"name\":\"LP_FT_Content1_"+rn+"\",\"root\":false},"
			+ "\"contentId2\":{\"name\":\"LP_FT_Content2_"+rn+"\",\"root\":false}}}}}";
	String jsonUpdateHierarchyOneChild = "{\"request\":{\"data\":{\"nodesModified\":{\"unitId1\":{\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"name\":\"LP_FT_CourseUnit1_"+rn+"\",\"contentType\":\"TextBookUnit\",\"code\":\"Test_QA\"}}},\"hierarchy\":{\"TextbookId\":{\"name\":\"LP_NFT_Collection_"+rn+"\",\"contentType\":\"TextBook\",\"children\":[\"unitId1\"],\"root\":true},\"unitId1\":{\"name\":\"LP_FT_CourseUnit1_"+rn+"\","
			+ "\"contentType\":\"TextBookUnit\",\"children\":[\"contentId1\"],\"root\":false},\"contentId1\":{\"name\":\"LP_FT_Content1_"+rn+"\",\"root\":false}}}}}";
	String jsonCreateValidTextBookUnit = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_Unit" + rn+ "\", \"mediaType\": \"content\",\"visibility\": \"Parent\",\"name\": \"LP_NFT_Unit_"+ rn+ "\",\"contentType\": \"TextBookUnit\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"tags\":[\"LP_functionalTest\"]}}}";
	String jsonCreateValidTextBook = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_TBook" + rn+ "\", \"mediaType\": \"content\",\"visibility\": \"Parent\",\"name\": \"LP_NFT_TBook_"+ rn+ "\",\"contentType\": \"TextBook\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"tags\":[\"LP_functionalTest\"]}}}";
	String jsonUpdateMetadata = "{\"request\":{\"content\":{\"versionKey\":\"version_key\",\"name\":\"updatedContentName\"}}}";
	String jsonCopyContent = "{\"request\":{\"content\":{\"createdBy\":\"Test\",\"createdFor\":[\"Test\"],\"organization\":[\"Test\"]}}}";
	
	static ClassLoader classLoader = ContentPublishWorkflowTests.class.getClassLoader();
	static URL url = classLoader.getResource("DownloadedFiles");
	static File downloadPath;
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());

	private static String contentId1 = null;
	private static String contentId2 = null;
	private static String unitId1 = null;
	private static String unitId2 = null;
	private static String artifactURL1 = null;
	private static String artifactURL2 = null;
	private static String versionKey1 = null;
	private static String versionKey2 = null;
	private static String name1 = null;
	private static String name2 = null;
	private String PROCESSING = "Processing";
	private String PENDING = "Pending";
	
	@BeforeClass
	public static void setup() throws URISyntaxException {
		downloadPath = new File(url.toURI().getPath());
	}

	@AfterClass
	public static void end() throws IOException {
		// FileUtils.cleanDirectory(downloadPath);
	}
	
	@Before
	public void init() {
		if ((StringUtils.isBlank(contentId1)) || (StringUtils.isBlank(contentId2)))
			createContent();
		if ((StringUtils.isBlank(unitId1)) || (StringUtils.isBlank(unitId2)))
			createTextBookUnit();
		try {Thread.sleep(3000);} catch (InterruptedException e) {e.printStackTrace();}
	}	
	
	private void createContent(){
		for (int i=1; i<=2; i++){
			if(i==1)
				jsonCreateValidContent = jsonCreateValidContent.replace("LP_NFT_", "LP_NFT_Content1_");
			if(i==2)
				jsonCreateValidContent = jsonCreateValidContent.replace("LP_NFT_", "LP_NFT_Content2_");
			setURI();
			Response R = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					body(jsonCreateValidContent).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					log().all().
					spec(get200ResponseSpec()).
					extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			
			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, userId, APIToken)).
			multiPart(new File(path + "/ExternalJsonItemDataCdata.zip")).
			when().
			post("/content/v3/upload/" + nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Publish the content
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/"+nodeId).
			then().
			log().all().
			spec(get200ResponseSpec());
			
			// Get Content and validate
			setURI();
			Response Res2 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();
			
			JsonPath jPath1 = Res2.jsonPath();
			String identifier = jPath1.get("result.content.identifier");
			String versionKey = jPath1.get("result.content.versionKey");
			String artifactUrl = jPath1.get("result.content.artifactUrl");
			String status = jPath1.get("result.content.status");
			String name = jPath1.get("result.content.name");
			if(i==1)
				contentId1 = nodeId;
				artifactURL1 = artifactUrl;
				name1 = name;
				versionKey1 = versionKey;
			if(i==2)
				contentId2 = nodeId;
				artifactURL2 = artifactUrl;
				name2 = name;
				versionKey2 = versionKey;
				
			Assert.assertTrue(versionKey != null);
			Assert.assertEquals(nodeId, identifier);
			System.out.println(nodeId);

		}
	}
	
	private void createTextBookUnit(){
	// Create TextBookUnint
		for (int i=1; i<=2; i++){
			if(i==1)
				jsonCreateValidTextBookUnit = jsonCreateValidTextBookUnit.replace("LP_NFT_Unit", "LP_NFT_Unit1_");
			if(i==2)
				jsonCreateValidTextBookUnit = jsonCreateValidTextBookUnit.replace("LP_NFT_Unit", "LP_NFT_Unit2_");
		setURI();
		Response Res2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidTextBookUnit).
				when().
				post("/content/v3/create").
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().response();
		
		JsonPath jPath2 = Res2.jsonPath();
		String unitId = jPath2.get("result.node_id");
		
		if (i==1)
			unitId1 = unitId;
		if (i==2)
			unitId2 = unitId;
		}
	}

	// Copy live content
	@Test
	public void copyLiveContentExpectSuccess200(){
		setURI();
		Response Res1 =
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCopyContent).
				with().
				contentType(JSON).
				when().
				post("/content/v3/copy/" +contentId1).
				then().
				spec(get200ResponseSpec()).
				extract().response();
		
		JsonPath jPath = Res1.jsonPath();
		String copyId = jPath.get("result.nodeId");
		
		// Get Content and validate
		setURI();
		Response Res2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/"+copyId).
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();
		
		JsonPath jPath1 = Res2.jsonPath();
		String identifier = jPath1.get("result.content.identifier");
	//	String artifactUrl = jPath1.get("result.content.artifactUrl");
		String status = jPath1.get("result.content.status");
		Assert.assertTrue(identifier!=contentId1);
		Assert.assertTrue(status.equals("Draft"));
		
	}
	
	// Copy draft content
	@Test
	public void copyDraftContentExpect4xx(){
		setURI();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");
		
		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/ExternalJsonItemDataCdata.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Copy draft content
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonCopyContent).
		with().
		contentType(JSON).
		when().
		post("/content/v3/copy/" +contentId1).
		then().
		spec(get400ResponseSpec());
	}
	
	// Copy content which image node
	@Test
	public void copyContentWithImageNodeExpectSuccess200(){
		// Update content metadata
		jsonUpdateMetadata = jsonUpdateMetadata.replace("version_key", versionKey1);
		//System.out.println(jsonUpdateMetadata);
		try{Thread.sleep(5000);}catch(Exception e){e.printStackTrace();};
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonUpdateMetadata).
		with().
		contentType("application/json").
		when().
		patch("/content/v3/update/" + contentId1).
		then().
		//log().all().
		spec(get200ResponseSpec()).
		extract().response();

		// Check for image node
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/content/v3/read/"+contentId1+".img").
		then().
		//log().all().
		spec(get200ResponseSpec()).
		extract().
		response();
	
		// Copy content
		setURI();
		Response Res =
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCopyContent).
				with().
				contentType(JSON).
				when().
				post("/content/v3/copy/" +contentId1).
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().response();
		
		JsonPath jPath = Res.jsonPath();
		String copyId = jPath.get("result.node_id");
				
		// Get content and validate
		setURI();
		Response Res2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/"+copyId).
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();
		
		JsonPath jPath1 = Res2.jsonPath();
		String identifier = jPath1.get("result.content.identifier");
		String name = jPath1.get("result.content.name");
	//	String artifactUrl = jPath1.get("result.content.artifactUrl");
		String status = jPath1.get("result.content.status");
		Assert.assertTrue(name!="updatedContentName");
		Assert.assertTrue(identifier!=contentId1);
		Assert.assertTrue(status.equals("Draft"));
	}
	
	// Copy flagged content
	@Test
	public void copyFlaggedContentExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("").
		with().
		contentType(JSON).
		when().
		post("/content/v3/flag/"+contentId1).
		then().
		log().all().
		spec(get200ResponseSpec());
		
		// Copy flagged content
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonCopyContent).
		with().
		contentType(JSON).
		when().
		post("/content/v3/copy/" +contentId1).
		then().
		spec(get400ResponseSpec());
		
	}
	
	// Copy asset
	@Test
	public void copyAssetExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("contentType", "Asset").put("mimeType", "image/jpeg");
		String jsonCreateImageAssetValid = js.toString();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateImageAssetValid).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/jpegImage.jpeg")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		//log().all().
		spec(get200ResponseSpecUpload());
		
		// Copy Asset
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonCopyContent).
		with().
		contentType(JSON).
		when().
		post("/content/v3/copy/" +nodeId).
		then().
		spec(get400ResponseSpec());		
	}
	
	// Copy invalid identifier
	@Test
	public void copyInvalidIdentifierExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonCopyContent).
		with().
		contentType(JSON).
		when().
		post("/content/v3/copy/ahdsuanav").
		then().
		spec(get400ResponseSpec());	
	}
	
	// Copy with identifier of other object types
	@Test
	public void copyDiffObjTypesExpect4xx(){
		
	}
	
	// Copy with blank identifier
	@Test
	public void copyBlankIdentifierExpect5xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonCopyContent).
		with().
		contentType(JSON).
		when().
		post("/content/v3/copy/ahdsuanav").
		then().
		spec(get500ResponseSpec());
	}
	
	// Copy retired content
	@Test
	public void copyRetiredContentExpect4xx(){
		// Retire content
		setURI();
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 	
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		delete("/content/v3/retire/" + contentId1).
		then().
		spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R1 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + contentId1).
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP1 = R1.jsonPath();
		String status = jP1.get("result.content.status");
		//System.out.println(status);
		Assert.assertEquals(status, "Retired");
		
		// Copy content
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonCopyContent).
		with().
		contentType(JSON).
		when().
		post("/content/v3/copy/" +contentId1).
		then().
		spec(get400ResponseSpec());	
		
	}
	
	// Copy textbook with no children
	@Test
	public void copyTextBookWithNoChildrenExpectSuccess200(){
		// Create TextBook 
		setURI();
		Response Res3 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidTextBook).
				when().
				post("/content/v3/create").
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().response();
		
		JsonPath jPath3 = Res3.jsonPath();
		String textBookId = jPath3.get("result.node_id");
		
		// Publish textbook
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/"+textBookId).
		then().
		log().all().
		spec(get200ResponseSpec());
		
		// Copy textbook
		setURI();
		Response Res1 =
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCopyContent).
				with().
				contentType(JSON).
				when().
				post("/content/v3/copy/" +contentId1).
				then().
				spec(get200ResponseSpec()).
				extract().response();
		
		JsonPath jPath = Res1.jsonPath();
		String copyId = jPath.get("result.nodeId");
		
		// Get Content and validate
		setURI();
		Response Res2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/"+copyId).
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();
		
		JsonPath jPath1 = Res2.jsonPath();
		String identifier = jPath1.get("result.content.identifier");
	//	String artifactUrl = jPath1.get("result.content.artifactUrl");
		String status = jPath1.get("result.content.status");
		Assert.assertTrue(identifier!=textBookId);
		Assert.assertTrue(status.equals("Draft"));
	}
	
	// Copy textbook with one unit and one children
	@Test
	public void copyTextBookWithUnitExpectSuccess200(){
		// Create TextBook 
		setURI();
		Response Res3 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidTextBook).
				when().
				post("/content/v3/create").
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().response();
		
		JsonPath jPath3 = Res3.jsonPath();
		String textBookId = jPath3.get("result.node_id");
		
		// Update Hierarchy
		setURI();
		jsonUpdateHierarchyOneChild = jsonUpdateHierarchyOneChild.replaceAll("TextbookId", textBookId).replaceAll("unitId1", unitId1).replaceAll("contentId1", contentId1);
		System.out.println(jsonUpdateHierarchyOneChild);
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonUpdateHierarchyTwoChild).
		with().
		contentType(JSON).
		when().
		patch("/content/v3/hierarchy/update/").
		then().
		log().all().
		spec(get200ResponseSpec());
		
		// Publish textbook
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/"+textBookId).
		then().
		log().all().
		spec(get200ResponseSpec());
		
		// Copy textbook
		setURI();
		Response Res1 =
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCopyContent).
				with().
				contentType(JSON).
				when().
				post("/content/v3/copy/" +contentId1).
				then().
				spec(get200ResponseSpec()).
				extract().response();
		
		JsonPath jPath = Res1.jsonPath();
		String copyId = jPath.get("result.nodeId");
		
		// Get Content and validate
		setURI();
		Response Res2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/"+copyId).
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();
		
		JsonPath jPath1 = Res2.jsonPath();
		String identifier = jPath1.get("result.content.identifier");
	//	String artifactUrl = jPath1.get("result.content.artifactUrl");
		ArrayList<String> childNodes = jPath1.get("result.content.childNodes");
		int nodesCount = childNodes.size();
		String status = jPath1.get("result.content.status");
		Assert.assertTrue(nodesCount==1);
		Assert.assertTrue(identifier!=textBookId);
		Assert.assertTrue(status.equals("Draft"));
	}
}