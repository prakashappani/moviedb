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
	public static void opts(@Optional("input") String env) {
		token = getToken(env);
		RestAssured.baseURI = getBaseUrl(env);
		properties = loadProperties(env);
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
		Response response = RestAssured.given().params("api_key", token, "query", moviename)
				.get();
		JsonPath jsonpath = response.jsonPath();
		Assert.assertEquals(200, response.getStatusCode());
		List<String> l = jsonpath.getList("results.title");
		
		for (String s : l) {
			Assert.assertEquals(s.contains(moviename), true);
		}
	}

	/**
	 * Verify user is able to fetch all the expected total_results for the when
	 * the movie title has trail end spaces
	 *
	 * @throws IOException
	 */
	@Test()
	public void verifymovieresults_ignoreTrailEndSpaces() {
		String moviename = properties.getProperty("moviename");
		String movienameWithSpaces = " " + moviename + " ";
		Response response = RestAssured.given().params("api_key", token, "query", movienameWithSpaces)
				.get();
		JsonPath jsonpath = response.jsonPath();
		Assert.assertEquals(200, response.getStatusCode());
		List<String> l = jsonpath.getList("results.title");
		
		for (String s : l) {
			Assert.assertEquals(s.contains(moviename), true);
		}

	}

	/***
	 * Verify user is able to fetch all the paginated results when queried with
	 * movie name and page The below testcase counts the total_results and
	 * paginate the end point, count the results in each page and assert it with
	 * the total_results from the response
	 * 
	 * @throws IOException
	 */

	@Test()
	public void verifymovieresults_paginatedResults_matchTotalResults() {
		String moviename = properties.getProperty("moviename");
		Response response = RestAssured.given().params("api_key", token, "query", moviename)
				.get();
		int total_pages = response.path("total_pages");
		int total_results = response.path("total_results");
		int current_pagecount = 0;

		for (int i = 1; i <= total_pages; i++) {
			Response response2 = RestAssured.given()
					.params("api_key", token, "query", moviename, "page", i)
					.get();
			JsonPath jsonpath2 = response2.jsonPath();
			List<String> l2 = jsonpath2.getList("results.title");
			current_pagecount = current_pagecount + l2.size();
		}
		Assert.assertEquals(current_pagecount, total_results);
	}

	/**
	 * Verify no results are obtained for the searched movie title that does not
	 * exist
	 */
	@Test()
	public void verifyMovieResults_MovieNotExists() {
		String moviename = properties.getProperty("Nonexisting_moviename");
		Response response = RestAssured.given().params("api_key", token, "query", moviename)
				.get();
		int total_results = response.path("total_results");
		Assert.assertEquals(total_results, 0);
		Assert.assertEquals(200, response.getStatusCode());
	}

	/**
	 * Performance test Verify the time taking to fetch all the expected
	 * total_results for the searched movie title when there are 1000 (maximum)
	 * pages. We can use the Thread to count the time and assert based on the
	 * SLA.
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
		String moviename = properties.getProperty("moviename");
		Response response = RestAssured.given().params("query", moviename)
				.get();
		Assert.assertEquals(response.getStatusCode(), 401);
	}

	/**
	 * Verify user with Invalid token key provided and queried with the movie
	 * title, the response code is 401 unauthorized
	 **/
	@Test()
	public void verifyMovieResults_InvalidAPIkey() {
		String moviename = properties.getProperty("moviename");
		Response response = RestAssured.given()
				.params("api_key", "Invalid Token", "query", moviename).get();
		Assert.assertEquals(response.getStatusCode(), 401);
	}

	/**
	 * Verify user is able to fetch all the expected total_results for the
	 * searched movie title. Verify with optional parameters include_adult,
	 * region, year and primary_release_year,page and language. We should get
	 * the correct movie titles as expected.
	 *
	 * @throws IOException
	 */
	@Test()
	public void verifyMovieResults_optionalParams() {
		String moviename = properties.getProperty("moviename");
		Response response = RestAssured.given().params("api_key", token,
				"query", moviename, "page", 2, "include_adult", true).get();
		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonpath = response.jsonPath();
		List<Boolean> l = jsonpath.getList("results.adult");
		
		for (Boolean s : l) {
			Assert.assertFalse(s);
		}
	}

	/**
	 * Verify user is able to fetch the expected total_results for the optional
	 * parameters combination with query, primary_release_year and year.
	 */
	@Test()
	public void verifyMovieResults_fewoptionalParams() {
		String moviename = properties.getProperty("moviename");
		Response response = RestAssured.given().params("api_key", token,
				"query", moviename, "primary_release_year", 2016, "year", 2016)
				.get();
		Assert.assertEquals(response.getStatusCode(), 200);

		JsonPath jsonpath = response.jsonPath();
		List<String> l = jsonpath.getList("results.release_date");

		for (String s : l) {
			Assert.assertEquals(s.contains("2016"), true);
		}
	}

	/**
	 * Verify user is able to fetch all the expected total_results for the
	 * searched movie title. Verify error thrown for incorrect formats submitted
	 * in the optional parameters like language and region.
	 *
	 * @throws IOException
	 */
	@Test(enabled=false)
	public void verifyMovieResults_ErroroptionalParams() {
		String moviename = properties.getProperty("moviename");
		Response response = RestAssured.given().params("api_key", token,
				"query", moviename, "language", "en", "page", 1, "year", 2016)
				.get();
		Assert.assertEquals(200, response.getStatusCode());
		JsonPath jsonpath = response.jsonPath();
		List<String> l = jsonpath.getList("results.original_language");
		
		for (String s : l) {
			Assert.assertEquals(s.contains("en"), true);
		}
	}

	/**
	 * Verify user is able to fetch all the expected total_results for the
	 * searched movie title.Verify when the query parameter for title is of
	 * minimum length (1 in this case)
	 *
	 * @throws IOException
	 */
	@Test()
	public void verifyMovieResults_minTitleSearch() {
		String moviename = properties.getProperty("moviename");
		Response response = RestAssured.given()
				.params("api_key", token, "query", moviename, "page", 1).get();
		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonpath = response.jsonPath();
		List<String> l = jsonpath.getList("results.id");
		Assert.assertEquals(l.size(), 20);
	}

	/**
	 * Verify user is able to request for resource that does not exist and
	 * get404 error with message
	 */
	@Test()
	public void verifyMovieResults_resourceNotFound() {
		String inValidURI = "https://api.themoviedb.org/3/search/movieinvalid";
		Response response = RestAssured.given().baseUri(inValidURI)
				.params("api_key", token, "query", "Saving the Titanic", "page",
						1)
				.get();
		Assert.assertEquals(response.getStatusCode(), 404);
	}

	/**
	 * Verify user is able to fetch all the expected total_results for the
	 * searched movie title with only one result
	 * 
	 * @throws IOException
	 */
	@Test()
	public void verifyMovieResults_OneResult() {
		Response response = RestAssured.given().params("api_key", token,
				"query", "Saving the Titanic", "page", 1).get();
		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonpath = response.jsonPath();
		List<String> l = jsonpath.getList("results.id");
		Assert.assertEquals(l.size(), 1);
	}

	/**
	 * Verify user is able to fetch all the expected total_results for the
	 * searched movie title with proper error message when queried for an
	 * invalid or incorrect page number that does not exist.
	 * 
	 * @throws IOException
	 */
	@Test()
	public void verifyMovieResults_PageDoesNotExists() {
		String moviename = properties.getProperty("moviename");
		Response response = RestAssured.given()
				.params("api_key", token, "query", moviename, "page", 999)
				.get();
		Assert.assertEquals(response.getStatusCode(), 422);
	}

}