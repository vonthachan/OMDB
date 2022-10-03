package OMDB;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.File;

public class MovieDriver {

	/**
	 * for selecting some data
	 */
	public static ResultSet dbSelect(String sqlQueryStr) {

		Connection db_connection = null;
		ResultSet result_set = null;
		try {

			// Step 1: Get the connection object for the database
			String url = "jdbc:mysql://localhost:3306/omdb";
			String user = "root";
			String password = "";
			db_connection = DriverManager.getConnection(url, user, password);
			System.out.println("Success: Connection established");

			// Step 2: Create a statement object
			Statement statement_object = db_connection.createStatement();

			// Step 3: Execute SQL query
			// Set the query string you want to run on the database
			// If this query is not running in PhpMyAdmin, then it will not run here
			result_set = statement_object.executeQuery(sqlQueryStr);

			// Step 4: Process the result set
			// There are many methods for processing the ResultSet
			// See https://docs.oracle.com/javase/7/docs/api/java/sql/ResultSet.html
			/*while (result_set.next()) {
				int id = result_set.getInt("movie_id");
				String native_name = result_set.getString("native_name");
				String english_name = result_set.getString("english_name");
				String year_made = result_set.getString("year_made");

				System.out.println(id);
				System.out.println(native_name);
				System.out.println(english_name);
				System.out.println(year_made);

			}*/ // end while

		} // end try

		catch (Exception ex) {
			ex.printStackTrace();
		} // end catch
		
		return result_set;

	} // end dbQuery method

	//Take input from the user and add a movie to the DB
	public static void dbInsert(String nativeName, String englishName, int yearMade) {

		Connection db_connection = null;
		ResultSet result_set = null;
		try {

			// Step 1: Get the connection object for the database
			String url = "jdbc:mysql://localhost:3306/omdb";
			String user = "root";
			String password = "";
			db_connection = DriverManager.getConnection(url, user, password);
			
			System.out.println("Success: Connection established");

			// Step 2: Create a statement object
			Statement statement_object = db_connection.createStatement();

			//Step 4: Execute the query
			String sql_query_str = "INSERT INTO movies (native_name, english_name, year_made) VALUES ('" + nativeName + "', '" + englishName + "', " + yearMade + ")";
			statement_object.executeUpdate(sql_query_str);
			
			String sql_query_str_select = "SELECT movie_id FROM movies WHERE NOT EXISTS (SELECT movie_id FROM movie_numbers WHERE movies.movie_id = movie_numbers.movie_id) ORDER BY movie_id DESC LIMIT 1";
			result_set = statement_object.executeQuery(sql_query_str_select);
			result_set.next();
			
			int movieId = result_set.getInt("movie_id");
			
			//Get length
			int length = API.getLength(nativeName);
			
			//Get base chars
			String[] baseCharsArray = API.getBaseChars(nativeName);
			
			String baseChars = baseCharsArray[0];
			
			for(int i = 1; i < baseCharsArray.length; i++) {
				baseChars += ", " + baseCharsArray[i];
			}
			
			String sql_query_str_length = "INSERT INTO movie_numbers (movie_id, length, base_chars) VALUES ('" + movieId + "', '" + length + "', '" + baseChars + "')";
			statement_object.executeUpdate(sql_query_str_length);
			System.out.println("inserted id: " + movieId + " length:" + length + "base chars: " + baseChars);

		} // end try

		catch (Exception ex) {
			ex.printStackTrace();
		} // end catch

	} // end dbInsert method

	//Take input from the user and update a movie in the DB
	public static void dbUpdate(Scanner scnr, int movieID) {

		Connection db_connection = null;
		ResultSet result_set = null;
		try {

			// Step 1: Get the connection object for the database
			String url = "jdbc:mysql://localhost:3306/omdb";
			String user = "root";
			String password = "";
			db_connection = DriverManager.getConnection(url, user, password);
			System.out.println("Success: Connection established");

			// Step 2: Create a statement object
			Statement statement_object = db_connection.createStatement();

			// Step 3: Get the input from the user
			ArrayList<String> updates = new ArrayList<String>();

			
			//Ask user if the native language title is to be updated and to what
			System.out.print("Would you like to update the native language title? y/n: ");
			if(scnr.nextLine().equals("y")){
				System.out.print("Enter the new native language name: ");
				updates.add("`native_name` = '" + scnr.nextLine() + "'");
			}
			//Ask user if the English name is to be updated and to what
			System.out.print("Would you like to update the English name? y/n: ");
			if(scnr.nextLine().equals("y")){
				System.out.print("Enter the new English name: ");
				updates.add("`english_name` = '" + scnr.nextLine() + "'");
			}
			//Ask user if the year made is to be updated and to what
			System.out.print("Would you like to update the year made? y/n: ");
			if(scnr.nextLine().equals("y")){
				System.out.print("Enter the new year made: ");
				updates.add("`year_made` = " + scnr.nextInt());
			}

			//Step 4: Build the query string
			String sql_query_str = "";
			String sql_query_str_length = "";
			String sql_query_str_select = "";
			String nativeName = "";
			int length;
			
			switch(updates.size()){
				case 1:
					sql_query_str = "UPDATE `movies` SET " + updates.get(0) + " WHERE `movie_ID` = " + movieID;
					
					//Step 5: Execute the query
					statement_object.executeUpdate(sql_query_str);
					
					// Get the native_name so we can update the length
					sql_query_str_select = "SELECT `native_name` FROM `movies` WHERE `movie_id` = " + movieID;
					result_set = statement_object.executeQuery(sql_query_str_select);
					result_set.next();
					nativeName = result_set.getString("native_name");
					
					// Get the length
					length = API.getLength(nativeName);		
					
					//Get base chars
					String[] baseCharsArray = API.getBaseChars(nativeName);
					
					String baseChars = baseCharsArray[0];
					
					for(int i = 1; i < baseCharsArray.length; i++) {
						baseChars += ", " + baseCharsArray[i];
					}
					
					
					
					// Update the length, update the base_chars
					sql_query_str_length = "UPDATE `movie_numbers` SET `length` = " + length + ", `base_chars` = '" + baseChars + "' WHERE `movie_ID` = " + movieID;
					statement_object.executeUpdate(sql_query_str_length);
					
					System.out.println("updated id: " + movieID);
					
					break;
				case 2:
					sql_query_str = "UPDATE `movies` SET " + updates.get(0) + ", " + updates.get(1) + " WHERE `movie_ID` = " + movieID;
					
					//Step 5: Execute the query
					statement_object.executeUpdate(sql_query_str);
					
					// Get the native_name so we can update the length
					sql_query_str_select = "SELECT `native_name` FROM `movies` WHERE `movie_id` = " + movieID;
					result_set = statement_object.executeQuery(sql_query_str_select);
					result_set.next();
					nativeName = result_set.getString("native_name");
					
					// Get the length
					length = API.getLength(nativeName);	
					
					//Get base chars
					String[] baseCharsArrayTwo = API.getBaseChars(nativeName);
					
					String baseCharsTwo = baseCharsArrayTwo[0];
					
					for(int i = 1; i < baseCharsArrayTwo.length; i++) {
						baseCharsTwo += ", " + baseCharsArrayTwo[i];
					}
					
					
					// Update the length
					sql_query_str_length = "UPDATE `movie_numbers` SET `length` = " + length + ", `base_chars` = '" + baseCharsTwo +   "' WHERE `movie_ID` = " + movieID;
					statement_object.executeUpdate(sql_query_str_length);
					
					System.out.println("updated id: " + movieID);
					
					break;
				case 3:
					sql_query_str = "UPDATE `movies` SET " + updates.get(0) + ", " + updates.get(1) + ", " + updates.get(2) + " WHERE `movie_ID` = " + movieID;
					
					//Step 5: Execute the query
					statement_object.executeUpdate(sql_query_str);
					
					// Get the native_name so we can update the length
					sql_query_str_select = "SELECT `native_name` FROM `movies` WHERE `movie_id` = " + movieID;
					result_set = statement_object.executeQuery(sql_query_str_select);
					result_set.next();
					nativeName = result_set.getString("native_name");
					
					// Get the length
					length = API.getLength(nativeName);	
					
					//Get base chars
					String[] baseCharsArrayThree = API.getBaseChars(nativeName);
					
					String baseCharsThree = baseCharsArrayThree[0];
					
					for(int i = 1; i < baseCharsArrayThree.length; i++) {
						baseCharsThree += ", " + baseCharsArrayThree[i];
					}
					
					// Update the length
					sql_query_str_length = "UPDATE `movie_numbers` SET `length` = " + length + ", `base_chars` = '" + baseCharsThree +  "' WHERE `movie_ID` = " + movieID;
					statement_object.executeUpdate(sql_query_str_length);
					
					System.out.println("updated id: " + movieID);
					
					break;
			}


			
			
		} // end try

		catch (Exception ex) {
			ex.printStackTrace();
		}//end catch

	}

	//Take input from the user and delete a movie entry in the DB
	public static void dbDelete(int movieID) {
		Connection db_connection = null;
		try {

			// Step 1: Get the connection object for the database
			String url = "jdbc:mysql://localhost:3306/omdb";
			String user = "root";
			String password = "";
			db_connection = DriverManager.getConnection(url, user, password);
			System.out.println("Success: Connection established");

			// Step 2: Create a statement object
			Statement statement_object = db_connection.createStatement();			

			//Step 3: Execute the query
			
			String sql_query_str = "DELETE FROM movies WHERE movie_id = " + movieID + ";";
			statement_object.executeUpdate(sql_query_str);
			
			sql_query_str = "DELETE FROM movie_numbers WHERE movie_id = " + movieID + ";";
			statement_object.executeUpdate(sql_query_str);
			
			System.out.println("deleted id: " + movieID);
			

		} // end try

		catch (Exception ex) {
			ex.printStackTrace();
		} // end catch
		

	}

	public static void baseCharGame(Scanner scnr) {
		
        Connection db_connection = null;
        String[] baseChars = null;
        int lengthOfString = 0;
        int count = 0;
        String[] name_list;
        
        //Get CSV string of movies
        System.out.println("Enter string of movies separated by commas:");
		String movie_string = scnr.nextLine();
		if(movie_string == "") {
			System.out.println("Movie string not entered.");
			return;
		}
		else {
        name_list = movie_string.split(",");
		}
		
        try {
            // Step 1: Get the connection object for the database
            String url = "jdbc:mysql://localhost/omdb";
            String user = "root";
            String password = "";
            db_connection = DriverManager.getConnection(url, user, password);
            System.out.println("Success: Connection established\n");

            // Step 2: Create a statement object
            Statement statement_object = db_connection.createStatement();
            
            // Step 4: Get the length of the string nativeName so we can query the db for
            // movies
            // with a native name of the same length
            // Iterates through array of names
            System.out.println("No.\tinput String\t Matches");

            for(int i = 0; i < name_list.length; i++) {
            	lengthOfString = API.getLength(name_list[i]);
                baseChars = API.getBaseChars(name_list[i]);
                String selectQuery = "SELECT native_name FROM movies, movie_numbers WHERE movie_numbers.length = "
                        + lengthOfString + " AND movies.movie_ID = movie_numbers.movie_ID";
                for (int j = 0; j < baseChars.length; j++) {
                    selectQuery = selectQuery + " AND movies.native_name LIKE '%" + baseChars[j] + "%'";
                }
                ResultSet results = statement_object.executeQuery(selectQuery);
                String matches = "";
                while (results.next()) {
                    String native_name = results.getString("native_name");
                    matches += native_name.trim() + ", ";
            	}
                if(matches != "") {
                	matches = matches.substring(0, matches.lastIndexOf(","));
                }
                else {
                	matches = "---";
                }
                results.close();
                System.out.println(++count + "\t" + name_list[i] + "\t\t" + matches);
            }
        } //end try
		
		catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println();
    }

	public static void baseCharGame2(String[] input_str) {

		Connection db_connection = null;
		// String[] baseChars = null;
		int lengthOfString = 0;
		int count = 0;

		try {
			// Step 1: Get the connection object for the database
			String url = "jdbc:mysql://localhost/omdb";
			String user = "root";
			String password = "";
			db_connection = DriverManager.getConnection(url, user, password);
			System.out.println("Success: Connection established\n");

			// Step 2: Create a statement object
			Statement statement_object = db_connection.createStatement();

			// Step 4: Get the length of the string nativeName so we can query the db for
			// movies
			// with a native name of the same length
			// Iterates through array of names
			// System.out.println("No.\tinput String\t Matches");

			for (int i = 0; i < input_str.length; i++) {
				// Get length of string so we can query db for movies
				lengthOfString = API.getLength(input_str[i]);

				// Get the chars from the strings entered to get base chars after
				// Declare a string array to hold characters from each string and pull base
				// chars from API
				String[] input_strChars = API.getBaseChars(input_str[i]);

				// Pulls the baseChars from the array above and puts them into a plain string
				// separated by space and commas
				// to match against the letters
				// in base_chars column in DB

				String baseChars = input_strChars[0];
				for (int g = 1; g < input_strChars.length; g++) {
					baseChars += ", " + input_strChars[g];
				}

				String getMovieMatchesQuery = "SELECT `native_name` FROM `movie_numbers`, `movies` WHERE movies.movie_id = movie_numbers.movie_id "
						+ "AND movie_numbers.length = " + lengthOfString + " AND movie_numbers.base_chars = '"
						+ baseChars + "'";

				// System.out.println(getMovieMatchesQuery);
				ResultSet matchResults = statement_object.executeQuery(getMovieMatchesQuery);

				String matches = "";
				while (matchResults.next()) {
					String native_name = matchResults.getString("native_name");
					matches += native_name.trim() + ", ";
				}
				if (matches != "") {
					matches = matches.substring(0, matches.lastIndexOf(","));
				} else {
					matches = "---";
				}
				matchResults.close();
				System.out.println(++count + "\t" + input_str[i] + "\t\t" + matches);
			}
		} // end try

		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println();
	} // end baseCharsGame2( ) method
	
	public static void updateBaseChars() throws UnsupportedEncodingException{
		Connection db_connection = null;
		try {
			// Get connection to database
			String url = "jdbc:mysql://localhost:3306/omdb";
			String user = "root";
			String password = "";
			db_connection = DriverManager.getConnection(url, user, password);
			System.out.println("Success: Connection established");
			Statement statement_object = db_connection.createStatement();
			String sqlQueryStr = "SELECT native_name, movies.movie_id, movie_numbers.movie_id FROM movies LEFT OUTER JOIN movie_numbers ON movies.movie_id = movie_numbers.movie_id";
			ResultSet resultSet = statement_object.executeQuery(sqlQueryStr);
			
			// Remove entries from movie numbers table that do not match id numbers in the movies table.
			String deleteNumID = "DELETE FROM movie_numbers WHERE movie_id NOT IN (SELECT movie_id FROM movies)";
			Statement deleteStatement = db_connection.createStatement();
			deleteStatement.execute(deleteNumID);
			System.out.println("Removed non matching entries from movie numbers table.");
			System.out.println("Updating base characters of movie entries...");
			
			while (resultSet.next()) {
				
				String nativeName = resultSet.getString("native_name");
				int movieid = resultSet.getInt("movies.movie_id");
				int movienumberid=resultSet.getInt("movie_numbers.movie_id");
				String[] baseCharsArray = API.getBaseChars(nativeName);
				
				String baseChars = baseCharsArray[0].replaceAll("'", "''");
				for(int i = 1; i < baseCharsArray.length; i++) {
					baseChars += ", " + baseCharsArray[i].replaceAll("'", "''");
				}

				Statement updateStatement = db_connection.createStatement();
				
				//if movie number table contains movie id then update
				if (movieid == movienumberid) {
					String sql_query_update = "UPDATE `movie_numbers`, `movies` SET base_chars = '"+ baseChars + "' WHERE  movie_numbers.movie_id = " + movieid;
					updateStatement.executeUpdate(sql_query_update);
					System.out.println("updated id: " + movieid + " base chars: " + baseChars);
				}
				//if id not in movie number table then insert new entry
				else {
					String sql_query_insert = "INSERT INTO `movie_numbers` (`movie_id`,`base_chars`) VALUES(" + movieid + ", '" + baseChars + "')";
					updateStatement.executeUpdate(sql_query_insert);
					System.out.println("inserted id: " + movieid + " base chars: '" + baseChars + "'");
				}
			}
			
			System.out.println("Done");
			 // end while
		} // end try

		catch (Exception ex) {
			ex.printStackTrace();
		} // end catch
	} //end updateBaseChars()

	public static void updateLength() throws UnsupportedEncodingException{
		Connection db_connection = null;
		try {
			// Get connection to database
			String url = "jdbc:mysql://localhost:3306/omdb";
			String user = "root";
			String password = "";
			db_connection = DriverManager.getConnection(url, user, password);
			System.out.println("Success: Connection established");
			Statement statement_object = db_connection.createStatement();
			String sqlQueryStr = "SELECT native_name, movies.movie_id, movie_numbers.movie_id FROM movies LEFT OUTER JOIN movie_numbers ON movies.movie_id = movie_numbers.movie_id";
			ResultSet resultSet = statement_object.executeQuery(sqlQueryStr);
			
			// Remove entries from movie numbers table that do not match id numbers in the movies table.
			String deleteNumID = "DELETE FROM movie_numbers WHERE movie_id NOT IN (SELECT movie_id FROM movies)";
			Statement deleteStatement = db_connection.createStatement();
			deleteStatement.execute(deleteNumID);
			System.out.println("Removed non matching entries from movie numbers table.");
			System.out.println("Updating lengths of DB...");
			
			while (resultSet.next()) {
				
				String nativeName = resultSet.getString("native_name");
				//nativeName = nativeName.replaceAll("\\s", ""); //Problem?
				int movieid = resultSet.getInt("movies.movie_id");
				int movienumberid=resultSet.getInt("movie_numbers.movie_id");
				int length = API.getLength(nativeName);				
				Statement updateStatement = db_connection.createStatement();
				
				//if movie number table contains movie id then update
				if (movieid == movienumberid) {
					String sql_query_update = "UPDATE `movie_numbers`, `movies` SET `length`='"+ length + "' WHERE  movie_numbers.movie_id = " + movieid;
					updateStatement.executeUpdate(sql_query_update);
					System.out.println("updated id: " + movieid + " length: " + length);
				}
				//if id not in movie number table then insert new entry
				else {
					String sql_query_insert = "INSERT INTO `movie_numbers` (`movie_id`,`length`) VALUES("+ movieid +"," + length + ")";
					updateStatement.executeUpdate(sql_query_insert);
					System.out.println("inserted id: " + movieid + " length:" + length);
				}
			}
			
			System.out.println("Done");
			 // end while
		} // end try

		catch (Exception ex) {
			ex.printStackTrace();
		} // end catch
	} //end updateBaseChars()

	enum ToDo {INSERT, IGNORE, INCONCLUSIVE} //enum to be used for the below method

	public static void process_mpr_data() {
		Connection db_connection = null;
		try {
			// Get connection to database
			String url = "jdbc:mysql://localhost:3306/omdb";
			String user = "root";
			String password = "";
			db_connection = DriverManager.getConnection(url, user, password);
			System.out.println("Success: Connection established");
			Statement mpr_statement = db_connection.createStatement();
			String sqlQueryStr = "SELECT * from mpr_test_data";
			ResultSet mpr_set = mpr_statement.executeQuery(sqlQueryStr); //storing the mpr_test_data in full in this set

			Statement movie_statement = db_connection.createStatement(); //multiple statements are needed to preserve ResultSets
			Statement people_statement = db_connection.createStatement();
			Statement role_statement = db_connection.createStatement();

			ToDo m = ToDo.IGNORE; //baseline set the enums to IGNORE
			ToDo p = ToDo.IGNORE;
			ToDo r = ToDo.IGNORE;

			while(mpr_set.next()) {
				String native_name = mpr_set.getString("native_name").replaceAll("'", "''"); //pulling attributes to be used later
				int year_made = mpr_set.getInt("year_made");
				String stage_name = mpr_set.getString("stage_name").replaceAll("'", "''");
				String role = mpr_set.getString("role").replaceAll("'", "''");
				String screen_name = mpr_set.getString("screen_name").replaceAll("'", "''");
				int mpr_id = mpr_set.getInt("id");

				ResultSet movie_set = movie_statement.executeQuery("SELECT * FROM `movies` WHERE native_name = '" + native_name + "' and year_made = " + year_made);
				int movie_count = 0; //creating set of movie(s) with same native_name and year_made
				while(movie_count < 2 && movie_set.next()) { //checking how many rows in the result set, stop if more than 1
					movie_count++;
				}

				if(movie_count == 0) { //no results returned from query means no matching movie in DB, must be inserted
					m = ToDo.INSERT;
				}
				else if(movie_count == 1) { //found unique movie, no insert needed
					m = ToDo.IGNORE;
				}
				else if(movie_count > 1) { //more than one movie in DB matches native_name/year_made, unique movie not found
					m = ToDo.INCONCLUSIVE;
				}


				ResultSet people_set = people_statement.executeQuery("SELECT * FROM `people` WHERE stage_name = '" + stage_name + "'");
				int people_count = 0; //creating set of people with same stage_name
				while(people_count < 2 && people_set.next()) { //checking how many rows in the result set, stop if more than 1
					people_count++;
				}

				if(people_count == 0) { //no results from query means no matching person in DB, must be inserted
					p = ToDo.INSERT;
				}
				else if(people_count == 1) { //found unique person, no insert needed
					p = ToDo.IGNORE;
				}
				else if(people_count > 1) { //more than one person in DB matches stage name, unique person not found
					p = ToDo.INCONCLUSIVE;
				}


				ResultSet role_set = role_statement.executeQuery("SELECT movie_people.role, movie_people.screen_name " + 
				"FROM `movie_people`, `movies`, `people` WHERE movies.movie_id = movie_people.movie_id AND people.people_id = movie_people.people_id " + 
				"AND movies.native_name = '" + native_name + "' AND people.stage_name = '" + stage_name + "' AND movie_people.role = '" + role + 
				"' AND movie_people.screen_name = '" + screen_name + "'"); //creating set of movie_people (role) with with same relevant data
				int role_count = 0;
				while(role_count < 2 && role_set.next()) { //no results from query means no matching role in DB
					role_count++;
				}

				if(role_count == 0) { //no result from query means no matching role in DB, must be inserted
					r = ToDo.INSERT;
				}
				else if(role_count == 1) { //found unique role, no insert needed
					r = ToDo.IGNORE;
				}

			

				if(m == ToDo.INCONCLUSIVE || p == ToDo.INCONCLUSIVE) { //if m or p is inconclusive, no change made
					movie_statement.executeUpdate("UPDATE `mpr_test_data` SET execution_status = 'M, P, R ignored; no unique tuple can not be identified'" + 
					" WHERE id = " + mpr_id);
				}

				else if(m == ToDo.IGNORE && p == ToDo.IGNORE && r == ToDo.IGNORE) { //if m, p, and r exist already, no change made
					movie_statement.executeUpdate("UPDATE `mpr_test_data` SET execution_status = 'M, P, R ignored' WHERE id = " + mpr_id);
				}

				else if(m == ToDo.IGNORE && p == ToDo.IGNORE && r == ToDo.INSERT) { //m and p exist, but no r, r inserted
					movie_set = movie_statement.executeQuery("SELECT * FROM `movies` WHERE native_name = '" + native_name + "' AND year_made = " + year_made);
					movie_set.next();
					int movie_id = movie_set.getInt("movie_id");

					people_set = people_statement.executeQuery("SELECT * FROM `people` WHERE stage_name = '" + stage_name + "'");
					people_set.next();
					int people_id = people_set.getInt("people_id");

					movie_statement.executeUpdate("INSERT INTO `movie_people` (movie_id, people_id, role, screen_name)" + 
					" VALUES (" + movie_id + ", " + people_id + ", '" + role + "', '" + screen_name + "')");

					movie_statement.executeUpdate("UPDATE `mpr_test_data` SET execution_status = 'M, P ignored; R created' WHERE id = " + mpr_id);
				}

				else if(m == ToDo.IGNORE && p == ToDo.INSERT) { //m in DB but no p, p and r inserted
					people_statement.executeUpdate("INSERT INTO `people` (stage_name, first_name, middle_name, last_name, gender, image_name)" +
					"VALUES ('" + stage_name + "', '', '', '', '', '')");

					movie_set = movie_statement.executeQuery("SELECT * FROM `movies` WHERE native_name = '" + native_name + "' AND year_made = " + year_made);
					movie_set.next();
					int movie_id = movie_set.getInt("movie_id");
					
					people_set = people_statement.executeQuery("SELECT * FROM `people` WHERE stage_name = '" + stage_name + "'");
					people_set.next();
					int people_id = people_set.getInt("people_id");

					movie_statement.executeUpdate("INSERT INTO `movie_people` (movie_id, people_id, role, screen_name)" + 
					" VALUES (" + movie_id + ", " + people_id + ", '" + role + "', '" + screen_name + "')");

					movie_statement.executeUpdate("UPDATE `mpr_test_data` SET execution_status = 'M ignored; P, R created' WHERE id = " + mpr_id);
				}

				else if(m == ToDo.INSERT && p == ToDo.IGNORE) { //no m but p in DB, m and r inserted
					MovieDriver.dbInsert(native_name, "", year_made);

					movie_set = movie_statement.executeQuery("SELECT * FROM `movies` WHERE native_name = '" + native_name + "' AND year_made = " + year_made);
					movie_set.next();
					int movie_id = movie_set.getInt("movie_id");
					
					people_set = people_statement.executeQuery("SELECT * FROM `people` WHERE stage_name = '" + stage_name + "'");
					people_set.next();
					int people_id = people_set.getInt("people_id");

					movie_statement.executeUpdate("INSERT INTO `movie_people` (movie_id, people_id, role, screen_name)" + 
					" VALUES (" + movie_id + ", " + people_id + ", '" + role + "', '" + screen_name + "')");

					movie_statement.executeUpdate("UPDATE `mpr_test_data` SET execution_status = 'P ignored; M, R created' WHERE id = " + mpr_id);
				}

				else if(m == ToDo.INSERT && p == ToDo.INSERT) { //m, p, and r inserted
					MovieDriver.dbInsert(native_name, "", year_made);
					people_statement.executeUpdate("INSERT INTO `people` (stage_name, first_name, middle_name, last_name, gender, image_name)" +
					"VALUES ('" + stage_name + "', '', '', '', '', '')");

					movie_set = movie_statement.executeQuery("SELECT * FROM `movies` WHERE native_name = '" + native_name + "' AND year_made = " + year_made);
					movie_set.next();
					int movie_id = movie_set.getInt("movie_id");
					
					people_set = people_statement.executeQuery("SELECT * FROM `people` WHERE stage_name = '" + stage_name + "'");
					people_set.next();
					int people_id = people_set.getInt("people_id");

					movie_statement.executeUpdate("INSERT INTO `movie_people` (movie_id, people_id, role, screen_name)" + 
					" VALUES (" + movie_id + ", " + people_id + ", '" + role + "', '" + screen_name + "')");
					
					movie_statement.executeUpdate("UPDATE `mpr_test_data` SET execution_status = 'M, P, R created' WHERE id = " + mpr_id);
				}
				
			} //end while

			System.out.println("\nData import complete\n");
		} // end try

		catch (Exception ex) {
				ex.printStackTrace();
		} // end catch
	}

	
	public static void baseCharReport() {
        Connection db_connection = null;
        File outputFile;
        PrintWriter output = null;

        try {

            // Create text file all_movies.txt
            outputFile = new File("all_movies.txt");
            if (outputFile.exists()) { // For retests
                outputFile.delete();
            }

            try {
                output = new PrintWriter(outputFile);
            } catch (Exception x) {
                System.err.format("Exception: %s%n", x);
                System.exit(0);
            }

            // Step 1: Get the connection object for the database
            String url = "jdbc:mysql://localhost:3306/omdb";
            String user = "root";
            String password = "";
            db_connection = DriverManager.getConnection(url, user, password);
            System.out.println("Success: Connection established");

            // Step 2: Create a statement object
            Statement movies_statement = db_connection.createStatement();
            Statement matches_statement = db_connection.createStatement();

            // Step 3: Execute SQL query
            // Set the query string you want to run on the database
            // If this query is not running in PhpMyAdmin, then it will not run here
            ResultSet movies_set = movies_statement.executeQuery(
                    "SELECT movies.movie_id, movies.native_name, movie_numbers.length, movie_numbers.base_chars "
                            + "FROM `movies`, `movie_numbers` WHERE movies.movie_id = movie_numbers.movie_id;");

            while (movies_set.next()) {
                int movie_id = movies_set.getInt("movie_id");
                String native_name = movies_set.getString("native_name");
                String baseChars = movies_set.getString("base_chars").replaceAll("'", "''"); // double apostrophes because SQL 
                //only recognizes 2 as one otherwise thinks it's the end of the string. 
                int length = movies_set.getInt("length");

                // build the sql query to pull matches based on "field" above from DB resultSet
                String matches_query = "SELECT movies.native_name, movie_numbers.movie_id, movie_numbers.base_chars FROM `movies`, `movie_numbers` "
                        + "WHERE movies.movie_id = movie_numbers.movie_id AND movies.movie_id !=" + movie_id
                        + " AND movie_numbers.length = " + length + " AND movie_numbers.base_chars = '" + baseChars
                        + "';";

                // call the query to store matches in result set

                ResultSet matches_set = matches_statement.executeQuery(matches_query);

                String matches = "";

                if (matches_set.next()) {
                    matches = "[" + matches_set.getString("movie_id") + "] " + matches_set.getString("native_name");
                } else {
                    matches = "no matches";
                }

                while (matches_set.next()) {
                    matches += ", " + "[" + matches_set.getString("movie_id") + "] " + matches_set.getString("native_name");
                }

                output.println("[" + movie_id + "] " + native_name + " || " + matches);

            }
            output.flush();

        } // end try

        catch (Exception ex) {
            ex.printStackTrace();
        } // end catch

    }// end baseCharReport()

	public static void main(String[] args) throws UnsupportedEncodingException {
        
		Scanner scnr = new Scanner(System.in); int menuSelection = -1;
		
		do {
		
		do { // Get menu selection, if it is not a valid selection display error and
		//wait for // valid selection
		
		try {
		
		System.out.println("\n\n---Enter a selection---");
		System.out.println("1 : Insert movie");
		System.out.println("2 : Update movie");
		System.out.println("3 : Delete movie");
		System.out.println("4 : Update movie name length");
		System.out.println("5 : Base characters matching game");
		System.out.println("6 : Process mpr data");
		System.out.println("7 : Base characters 2 matching game");
		System.out.println("8 : Update base characters");
		System.out.println("9 : Get a Base Characters Report");
		System.out.println("10 : Exit program");
		
		menuSelection = scnr.nextInt(); scnr.nextLine();
		
		} catch (Exception e) { System.out.println("Error encountered");
		scnr.close(); e.printStackTrace(); System.exit(0);
		
		} } while (menuSelection < 1 || menuSelection > 10);
		
		try { switch (menuSelection) { case 1:
		System.out.print("\nEnter the movie name in its native language: "); String
		nativeName = scnr.nextLine();
		System.out.print("\nEnter the movie name in English: "); String englishName =
		scnr.nextLine(); System.out.print("\nEnter the year the movie was made: ");
		int yearMade = scnr.nextInt(); MovieDriver.dbInsert(nativeName, englishName,
		yearMade); break;
		
		case 2:
		System.out.print("\nEnter the ID for the movie you would like to update: ");
		int upMovieID = scnr.nextInt(); scnr.nextLine(); MovieDriver.dbUpdate(scnr,
		upMovieID); break;
		
		case 3:
		System.out.print("Enter the ID for the movie you would like to delete: ");
		int delMovieID = scnr.nextInt(); scnr.nextLine();
		MovieDriver.dbDelete(delMovieID); break;
		
		case 4: MovieDriver.updateLength(); break;
		
		case 5: baseCharGame(scnr); break;
		
		case 6: process_mpr_data(); break;
		
		case 7: // Stuff to get input_str arrary for baseCharsGame2
			  Scanner inputStringScan = new Scanner(System.in);
			  String[] input_str;

			  System.out.println("Please enter a string of movie names.");
			  String nameString = inputStringScan.nextLine();

			  input_str = nameString.split(",");
			  for(int i = 0; i < input_str.length; i++) {
				  input_str[i] = input_str[i].trim();
			  }

			  baseCharGame2(input_str); break;
			  
			
		case 8: updateBaseChars(); break;
		
		case 9: baseCharReport(); break;
		
		case 10: System.out.println("Exiting"); scnr.close(); System.exit(0); return;
		
	   
		}
		
		} catch (Exception e) { e.printStackTrace(); }
		
		} while (menuSelection != 10);
		
		scnr.close();
	   
  }

} // end class