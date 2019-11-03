import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BaseTest {
	/**
	 * Get the token from the input file
	 * @param file - properties file name
	 * @return - string token
	 */
	public static String getToken(String file) {
		Properties properties = loadProperties(file);
		String token = properties.getProperty("token");
		return token;
	}

	/**
	 * Get the token from the input file
	 * @param file - properties file name
	 * @return - Properties properties
	 */
	public static Properties loadProperties(String file)  {
		Properties properties = new Properties();
		try {
			InputStream input = new FileInputStream("src/" + file + ".properties");
			properties.load(input);
		}
		catch(FileNotFoundException e) {
			System.out.println("File not found");
			e.printStackTrace();
		}
		catch(IOException e) {
			System.out.println("Property not found");
			e.printStackTrace();
		}
		return properties;
	}

	/**
	 * get the base url from the input file
	 * @param properties file
	 * Exception
	 */
	public static String getBaseUrl(String file){
		Properties properties = loadProperties(file);
		String url = properties.getProperty("url");
		return url;
	}
}
