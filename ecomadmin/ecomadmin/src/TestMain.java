import java.net.MalformedURLException;

import com.fdt.common.exception.rs.HttpStatusCodes;


public class TestMain {

	/**
	 * @param args
	 * @throws MalformedURLException
	 */
	public static void main(String[] args) throws Exception {
		System.out.println(Integer.valueOf(HttpStatusCodes.USERNAME_NOTFOUND_EXCEPTION.toString()));
		/*String url = "http://localhost:1001/ecom/service/EComFacadeService/loadUserByUsername/mgeorgieva@amcad.com/RECORDSMANAGEMENT";
		RestTemplate restTemplate = new RestTemplate();
		MappingJackson2HttpMessageConverter jackSonConverter = new MappingJackson2HttpMessageConverter();
		ObjectMapper objectMapper = new ObjectMapper();
		jackSonConverter.setObjectMapper(objectMapper);
//		List<MediaType> mediaTypes =  new ArrayList<MediaType>();
//		MediaType mediaType =  new MediaType(MediaType.APPLICATION_JSON.toString());
//		mediaTypes.add(mediaType);
//		jackSonConverter.setSupportedMediaTypes(mediaTypes);
		List msg = new ArrayList();
		msg.add(jackSonConverter);
		restTemplate.setMessageConverters(msg);
		User user = restTemplate.getForObject(url, User.class);
		System.out.println("User Name is ==" + user.getUsername());
		try {
			String url = "http://localhost:1001/ecom/service/EComAdminFacadeServiceRS/getNodeConfiguration/RECORDSMANAGEMENT";
			RestTemplate restTemplate = new RestTemplate();
			MappingJackson2HttpMessageConverter jackSonConverter = new MappingJackson2HttpMessageConverter() {
				protected JavaType getJavaType(Class<?> clazz) {
					System.out.println("Vivek");
					if (List.class.isAssignableFrom(clazz)) {
						return TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, UserCountDTO.class);
					}
					else {
						return super.getJavaType(ArrayList.class, UserCountDTO.class);
					}
				}
			};
			ObjectMapper objectMapper = new ObjectMapper();
			//objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
			jackSonConverter.setObjectMapper(objectMapper);
			//objectMapper.setTypeFactory(TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, UserCountDTO.class));
			List messageConverters = new ArrayList<HttpMessageConverter>();
			messageConverters.add(jackSonConverter);
			restTemplate.setMessageConverters(messageConverters);
			Map<String, Object> urlMap = new HashMap<String, Object>();
			urlMap.put("siteId", 2L);

//			MyResponseErrorHandler errorHandler = new MyResponseErrorHandler();
//			restTemplate.setErrorHandler(errorHandler);


			//NodeConfiguration nodeConfiguration = restTemplate.postForObject(url, null, NodeConfiguration.class);
			NodeConfiguration nodeConfiguration = restTemplate.getForObject(url, NodeConfiguration.class);
			System.out.println(nodeConfiguration);
			nodeConfiguration.setFromEmailAddress("vivek4348@amcad.com");
			String url2 = "http://localhost:1001/ecom/service/EComAdminFacadeServiceRS/updateNodeConfiguration";
			restTemplate.postForObject(url2, nodeConfiguration, Void.class);
			System.out.println(restTemplate.getForObject(url, NodeConfiguration.class));


			String url3 = "http://localhost:1001/ecom/service/EComAdminFacadeServiceRS/archiveUser/{userName}/{comments}/{modifiedBy}/{machineName}";
			Map<String, Object> urlMap3 = new HashMap<String, Object>();
			urlMap3.put("userName", "abc@amcad.com1356712880116");
			urlMap3.put("comments", "Testing");
			urlMap3.put("modifiedBy", "valampally@amcad.com");
			urlMap3.put("machineName", "LP-VALAMPALLY");

			restTemplate.delete(url3,urlMap3);




			String url3 = "http://localhost:1001/ecom/service/EComAdminFacadeServiceRS/calculateAccountBalance/{siteId}/{paymentType}/{createdBy}/{machineName}";
			Map<String, Object> urlMap3 = new HashMap<String, Object>();
			urlMap3.put("siteId", 4L);
			urlMap3.put("paymentType", PaymentType.RECURRING);
			urlMap3.put("createdBy", "VALAMPALLY@amcad.com");
			urlMap3.put("machineName", "1.2.2.3");


			Double d = restTemplate.postForObject(url3,null, Double.class, urlMap3);
			System.out.println(d);



			String url3 = "http://localhost:1001/ecom/service/EComAdminFacadeServiceRS/getUserCountsBySite/{siteId}";
			Map<String, Object> urlMap3 = new HashMap<String, Object>();
			urlMap3.put("siteId", 4L);
			//urlMap3.put("userName", "sayda.eleana@me.com");
			//List<UserCountDTO>  list = restTemplate.getForObject(url3, (Class<List<UserCountDTO>>) ((Class)List.class), urlMap3);
			UserCountDTO[]  list = restTemplate.getForObject(url3, UserCountDTO[] .class, urlMap3);
			//String text = restTemplate.getForObject(url3, String.class, urlMap3);
			List<UserCountDTO> listA = objectMapper.readValue(list, new TypeReference<List<UserCountDTO>>(){});
			System.out.println(list);


			List<UserCountDTO> userCountList = Arrays.asList(list);
			for(UserCountDTO userCountDTO: userCountList) {
				System.out.println("==>" + userCountDTO);
			}





 		} catch (HttpClientErrorException exception ) {
 			System.out.println("The Status Code is ====>" + exception.getStatusCode());
 			System.out.println(exception.getResponseBodyAsString());
		} catch (HttpServerErrorException excp) {
			System.out.println("The Exception is ====>" +  excp.getMessage());
		}
//		URL url = new URL("http://localhost:1001/ecom/service/EComFacadeService/loadUserByUsername/mgeorgieva@amcad.com1/RECORDSMANAGEMENT");
//		ObjectMapper objectMapper = new ObjectMapper();
//		objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
//		JsonNode node = objectMapper.readTree(url);
//		System.out.println(node);
//		User user = objectMapper.readValue(node.toString(), User.class);
//		System.out.println(user);
	}


	public static void main(String[] args) throws Exception {
		File file = new File("C:\\Projects\\SDL\\2.9\\Enterprise\\deployment\\sdlecom\\sdlecom\\src\\JSON.txt");
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		JsonNode node = objectMapper.readTree(file);
		System.out.println(node);
		User user = objectMapper.readValue(node.toString(), User.class);
		System.out.println(user);
	}
*/
}
}
