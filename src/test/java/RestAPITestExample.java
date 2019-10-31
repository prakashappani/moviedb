import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class RestAPITestExample extends BaseTest {

	static String token;
	static Properties properties;

	/**
	 * @param env - the properties filename
	 * @throws Exception
	 */
	@Parameters({ "env" })
	@BeforeSuite
	public static void opts(@Optional("input") String env)  {
		token = getToken(env);
		RestAssured.baseURI = getBaseUrl(env);
		properties = loadProperties(env);
	}

	/**
	 * @param query - movie name
	 */	
	public Response getResponse(String query) {
		return RestAssured.given()
				.params("api_key", token,"query", query)
				.get();
	}

	/**
	 * Verify user is able to fetch all the movie records when queried with the
	 * default parameters search query is moviename
	 *
	 * @throws IOException
	 */

	@Test()
	public void verifymovieresults_searchedwithTitle_requiredparams() {
		String moviename = properties.getProperty("moviename");
		Response response = getResponse(moviename);
		JsonPath jsonpath = response.jsonPath();
		Assert.assertEquals(200, response.getStatusCode());
		List<String> l = jsonpath.getList("results.title");
		for (String s : l) {
			System.out.println("List is " + s);
		}
		for (int i = 0; i < l.size(); i++) {
			Assert.assertEquals(l.get(i).contains(moviename), true);
		}
		System.out.println(response.asString());
	}

	/**
	 * Verify user is able to fetch all the expected total_results for the when the
	 * movie title has trail end spaces
	 *
	 * @throws IOException
	 */
	@Test()
	public void verifymovieresults_ignoreTrailEndSpaces() {
		String moviename = " " + properties.getProperty("moviename") + " ";
		Response response = getResponse(moviename);
		JsonPath jsonpath = response.jsonPath();
		System.out.println(response.getBody().prettyPrint());

		Assert.assertEquals(200, response.getStatusCode());
		List<String> l = jsonpath.getList("results.title");

		for (String s : l) {
			System.out.println("List is " + s);
		}
		for (int i = 0; i < l.size(); i++) {
			Assert.assertEquals(l.get(i).contains(properties.getProperty("moviename")), true);
		}

	}

	/***
	 * Verify user is able to fetch all the paginated results when queried with
	 * movie name and page The below testcase counts the total_results and paginate
	 * the end point, count the results in each page and assert it with the
	 * total_results from the response
	 * 
	 * @throws IOException
	 */

	@Test()
	public void verifymovieresults_paginatedResults_matchTotalResults() {
		String moviename = properties.getProperty("moviename");
		Response response = getResponse(moviename);
		int total_pages = response.path("total_pages");
		int total_results = response.path("total_results");
		int current_pagecount = 0;

		for (int i = 1; i <= total_pages; i++) {
			Response response2 = RestAssured.given()
					.params("api_key", token,"query", moviename,"page", i)
					.get();
			JsonPath jsonpath2 = response2.jsonPath();
			List<String> l2 = jsonpath2.getList("results.title");
			System.out.println("size is " + l2.size());
			current_pagecount = current_pagecount + l2.size();
			System.out.println("current pagecount" + current_pagecount);
		}
		Assert.assertEquals(total_results, current_pagecount);
	}

	/**
	 * Verify no results are obtained for the searched movie title that does not
	 * exist
	 */
	@Test()
	public void verifyMovieResults_MovieNotExists() {
		String moviename = properties.getProperty("Nonexisting_moviename");
		Response response = getResponse(moviename);

		// int total_pages= response.path("total_pages");
		int total_results = response.path("total_results");
		Assert.assertEquals(total_results, 0);
		Assert.assertEquals(200, response.getStatusCode());
	}

	/**
	 * Performance test Verify the time taking to fetch all the expected
	 * total_results for the searched movie title when there are 1000 (maximum)
	 * pages. We can use the Thread to count the time and assert based on the SLA.
	 */
	@Test()
	public void verifyMovieResults_PaginationwithMaxPages() {

	}

	/**
	 * Verify user with no token provided and queried with the movie title, the
	 * response code is 401 unauthorized
	 **/
	@Test()
	public void verifyMovieResults_NotokenProvided() {

	}

	/**
	 * Verify user with Invalid token key provided and queried with the movie title,
	 * the response code is 401 unauthorized
	 **/
	@Test()
	public void verifyMovieResults_InvalidAPIkey() {

	}

	/**
	 * Verify user is able to fetch all the expected total_results for the searched
	 * movie title. Verify with optional parameters include_adult, region, year and
	 * primary_release_year,page and language. We should get the correct movie
	 * titles as expected.
	 *
	 * @throws IOException
	 */
	@Test()
	public void verifyMovieResults_optionalParams() {

	}

	/**
	 * Verify user is able to fetch the expected total_results for the optional
	 * parameters combination with query, primary release_year and year.
	 */
	@Test()
	public void verifyMovieResults_fewoptionalParams() {

	}

	/**
	 * Verify user is able to fetch all the expected total_results for the searched
	 * movie title. Verify error thrown for incorrect formats submitted in the
	 * optional parameters like language and region.
	 *
	 * @throws IOException
	 */
	@Test()
	public void verifyMovieResults_ErroroptionalParams() {

	}

	/**
	 * Verify user is able to fetch all the expected total_results for the searched
	 * movie title.Verify when the query parameter for title is of minimum length (1
	 * in this case)
	 *
	 * @throws IOException
	 */
	@Test()
	public void verifyMovieResults_minTitleSearch() {

	}

	/**
	 * Verify user is able to request for resource that does not exist and get404
	 * error with message
	 */
	@Test()
	public void verifyMovieResults_resourceNotFound() {

	}

	/**
	 * Verify user is able to fetch all the expected total_results for the searched
	 * movie title with only one result
	 * 
	 * @throws IOException
	 */

	/**
	 * Verify user is able to fetch all the expected total_results for the searched
	 * movie title with proper error message when queried for an invalid or
	 * incorrect page number that does not exist.
	 * 
	 * @throws IOException
	 */

}