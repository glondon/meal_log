import java.util.Scanner;
import java.sql.*;
import java.util.Arrays;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;
import java.time.LocalDate;

public class MealLog
{

	public Connection db;
	private Statement stmt;
	private ResultSet rs;

	private static final String[] MEALS = {"breakfast", "lunch", "dinner"};
	private static final String[] RESULT = {"win", "tie", "loss"};
	private static final String[] SIZE = {"pass", "fail"};
	private static final String MENU_TBL = "menu";
	private static final String ACTIONS_TBL = "actions";
	private static final String DAILY_TBL = "daily";
	private static final String MEALS_TBL = "meals";
	private static final String WHYS_TBL = "whys";
	private static final String WEIGHT_TBL = "weight";

	public MealLog()
	{
		try
		{
			Class.forName("com.mysql.cj.jdbc.Driver");
			db = DriverManager.getConnection("jdbc:mysql://localhost:3306/meal_log", "root", "");
			stmt = db.createStatement();
		}
		catch(Exception e)
		{
			System.out.println("DB Error: " + e);
		}
			
	}

	private void getWhys()
	{
		String query = "SELECT why FROM " + WHYS_TBL + " ORDER BY RAND()";
		System.out.println("Reasons to lose weight (ENTER to continue 'q' to quit)\n");
		String today = this.formattedDate();
		String actionQuery = "INSERT INTO actions (viewed_whys) VALUES (?)";
		
		int counter = 1;
		String next;

		try
		{
			rs = stmt.executeQuery(query);
			while(rs.next())
			{
				System.out.println(counter + ". " + rs.getString(1));
				counter++;
				next = System.console().readLine();
				if("q".equals(next))
					break;
			}	

			rs.close();

			//insert last viewed into action table
			PreparedStatement stmt = db.prepareStatement(actionQuery);
			stmt.setString(1, today);
			stmt.execute();

		}  
		catch(SQLException e)
		{
			System.out.println("DB Error: " + e);
		}

	}

	private void menu()
	{
		String q = "SELECT * FROM " + MENU_TBL;

		try{
			rs = stmt.executeQuery(q);
			while(rs.next())
				System.out.println(rs.getInt(1) + ". " + rs.getString(2));	

			rs.close();
		}
		catch(SQLException e){
			System.out.println("DB Error: " + e);
		}
	}

	private static int validateInt(String s)
	{
		int n = -1;
		try{
			n = Integer.parseInt(s);
			return n;
		}
		catch(NumberFormatException e){
			return n;
		}
	}

	private static String validateDate(String s)
	{
		Date d;
		String sd = "";
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		try{
			d =	df.parse(s);
			return df.format(d);
		}
		catch(ParseException e){
			return sd;
		}
	}

	private void logWeight()
	{
		System.out.println("\nEnter weight and date (0 for today) (comma separated):\n");
		Scanner r = new Scanner(System.in);
		String vals = r.nextLine();
		String[] e = vals.split(",");
		if(e.length == 2){
			String w = e[0].trim();
			String d = e[1].trim();
			boolean pass = true;
			int iw = validateInt(w);
			if(iw != -1){
				if(iw > 250 || iw < 0){
					System.out.println(iw + " is an invalid weight");
					pass = false;
				}

				String sd = "";
				if(d.equals("0"))
					sd = this.formattedDate();
				else
					sd = validateDate(d);
				
				if(sd != ""){
					if(pass){
						int last = this.getLastWeight();
						try{
							System.out.println("Inserting weight...");
							String q = "INSERT INTO " + WEIGHT_TBL + " (pounds, date_w) VALUES (?, ?)";
							PreparedStatement stmt = db.prepareStatement(q);
								stmt.setInt(1, iw);
								stmt.setString(2, sd);
								stmt.execute();
								System.out.println("weight successfully logged");
				
								if(last != 0){
									int diff = 0;
									if(last > iw){
										diff = last - iw;
										System.out.println("\nCongrats, you lost " + diff + " pounds");
									}
									else{
										diff = iw - last;
										System.out.println("\nSorry, you gained " + diff + " pounds");
									}
									System.out.println("Last weight: " + last);
								}
						}
						catch(SQLException ex){
							System.out.println("DB insert error: " + ex);
						}
					}
				}
				else
					System.out.println(d + " not a valid date");
			}
			else
				System.out.println(w + " not a valid weight");
			
		}
		else
			System.out.println("\nOnly 2 values can be entered (weight and date)\n");
		
	}

	private int getLastWeight()
	{
		String q = "SELECT pounds FROM " + WEIGHT_TBL + " ORDER BY id DESC LIMIT 1";
		int toReturn = 0;
		try{
			rs = stmt.executeQuery(q);
			while(rs.next())
				toReturn = rs.getInt("pounds");

			return toReturn;
		}
		catch(SQLException e){
			return toReturn;
		}
	}

	private void logMeal()
	{
		System.out.println("\nEnter meal period, (win/tie/loss), size (pass/fail), & date (0 for today)\n(',' separated):\n");

		Scanner mealRead = new Scanner(System.in);
		String mealValue = mealRead.nextLine();

		String[] ent = mealValue.split(",");

		if(ent.length == 4){
			String meal = ent[0].trim();
			String pass = ent[1].trim();
			String size = ent[2].trim();
			String date = ent[3].trim();
			boolean mealValidated = true;
			boolean passValidated = true;
			boolean sizeValidated = true;

			if(!Arrays.asList(MEALS).contains(meal.toLowerCase()))
				mealValidated = false;
			if(!Arrays.asList(SIZE).contains(size.toLowerCase()))
				sizeValidated = false;
			if(!Arrays.asList(RESULT).contains(pass.toLowerCase()))
				passValidated = false;

			if(mealValidated && passValidated && sizeValidated)
			{
				String sd = "";
				if(date.equals("0"))
					sd = this.formattedDate();
				else
					sd = validateDate(date);

				if(sd != ""){
					if(!this.mealExists(meal, sd)){
						try{
							System.out.println("Inserting meal...");
							String query = "INSERT INTO " + MEALS_TBL + " (time, result, meal_size, date_consumed) VALUES (?, ?, ?, ?)";
							PreparedStatement stmt = db.prepareStatement(query);
							stmt.setString(1, meal);
							stmt.setString(2, pass);
							stmt.setString(3, size);
							stmt.setString(4, sd);
							stmt.execute();
							System.out.println("Meal successfully logged");
						}
						catch(SQLException e){
							System.out.println("DB insert error: " + e);
						}
					}
					else
						System.out.println("Meal already logged for that meal period");
				}
				else
					System.out.println("Date validation failed");
			}
			else
			{
				if(!mealValidated)
					System.out.println(meal + " is not a valid meal period");
				if(!sizeValidated)
					System.out.println(size + " is not a valid pass/fail value");
				if(!passValidated)
					System.out.println(pass + " is not a valid pass/fail option");
			}

		}
		else
			System.out.println("Error: 3 values must be entered");

	}

	private boolean mealExists(String m, String d)
	{
		boolean exists = false;
		String q = "SELECT * FROM " + MEALS_TBL + " WHERE time = '" + m + "' AND date_consumed = '" + d + "' LIMIT 1";
		try{
			rs = stmt.executeQuery(q);
			while(rs.next())
				exists = true;

			return exists;
		}
		catch(SQLException e){
			return exists;
		}
	}

	private void logDaily()
	{
		System.out.println("Enter 1 or 0 (true/false) - exercised, alcohol, sugar, healthy food, & qty/size (comma separated):\n");

		Scanner entered = new Scanner(System.in);
		String values = entered.nextLine();
		String[] ent = values.split(",");

		if(ent.length != 5){
			System.out.println("5 values must be entered\n");
			return;
		}
		
		String ex = ent[0].trim();
		String al = ent[1].trim();
		String su = ent[2].trim();
		String mp = ent[3].trim();
		String qs = ent[4].trim();

		int exercised = validateInt(ex);
		int alcohol = validateInt(al);
		int sugar = validateInt(su);
		int meals = validateInt(mp);
		int size = validateInt(qs);

		if(exercised == -1 || alcohol == -1 || sugar == -1 || meals == -1 || size == -1){
			System.out.println("Invalid integer entered");
			return;	
		}

		boolean exPass = true;
		boolean alPass = true;
		boolean suPass = true;
		boolean mpPass = true;
		boolean qsPass = true;

		if(exercised < 0 || exercised > 1) exPass = false;
		if(alcohol < 0 || alcohol > 1) alPass = false;
		if(sugar < 0 || sugar > 1) suPass = false;
		if(meals < 0 || meals > 1) mpPass = false;
		if(size < 0 || size > 1) qsPass = false;

		if(!exPass || !alPass || !suPass || !mpPass || !qsPass){
			if(!exPass) System.out.println(exercised + " is not an allowed exercised value");
			if(!alPass) System.out.println(alcohol + " is not an allowed alcohol value");
			if(!suPass) System.out.println(sugar + " is not an allowed sugar value");
			if(!mpPass) System.out.println(meals + " is not an allowed meals passed value");
			if(!qsPass) System.out.println(size + " is not an allowed qty passed value");
			return;
		}
					
		String saveDate = this.formattedDate();
		String query = "INSERT INTO " + DAILY_TBL + " (exercised, alcohol, sugar, meals_passed, qty_passed, date_affected) VALUES (?, ?, ?, ?, ?, ?)";

		try
		{
			System.out.println("Inserting result...");
			PreparedStatement stmt = db.prepareStatement(query);
			stmt.setInt(1, exercised);
			stmt.setInt(2, alcohol);
			stmt.setInt(3, sugar);
			stmt.setInt(4, meals);
			stmt.setInt(5, size);
			stmt.setString(6, saveDate);
			stmt.execute();
			System.out.println("Daily result successfully logged");
		}
		catch(SQLException e)
		{
			System.out.println("Problem inserting data: " + e);
		}
	}

	private String formattedDate()
	{
		//TODO will allow DateTimes to be passed in - for now return today's date only
		Date today = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		return df.format(today);
	}

	private void viewWeightLog()
	{
		LocalDate t = LocalDate.now();
		String m = t.withDayOfMonth(1).toString();
		String q = "SELECT * FROM " + WEIGHT_TBL + " WHERE date_w >= '" + m + "' ORDER BY date_w DESC";
		System.out.println("Logged weights for month: " + m + "\n");
		try{
			rs = stmt.executeQuery(q);
			int c = 0;
			while(rs.next()){
				if(c == 0)
					System.out.printf("%-3s %-8s", "WEIGHT", "DATE\n");

				System.out.printf("%-3s %-8s", rs.getInt("pounds") + "lbs", rs.getDate("date_w") + "\n");
				c++;
			}

			System.out.println("\nTotal logged: " + c);
		}
		catch(SQLException e){
			System.out.println("Problem accessing data: " + e);
		}
	}

	private void viewMeals()
	{
		//TODO set up for today, then a specific date
		LocalDate today = LocalDate.now();
		String dateParam = today.withDayOfMonth(1).toString();
		String query = "SELECT * FROM " + MEALS_TBL + " WHERE date_consumed >= '" + dateParam + "' ORDER BY date_consumed DESC";

		System.out.println("Meals eaten since " + dateParam);

		try
		{
			rs = stmt.executeQuery(query);
			int count = 0;
			int w = 0;
			int t = 0;
			int l = 0;
			int p = 0;
			int f = 0;
			int b = 0;
			int lu = 0;
			int d = 0;

			while(rs.next()){

				if(count == 0)
					System.out.printf("%-3s %-10s %-7s %-5s %-5s %n", "ID", "MEAL", "RESULT", "SIZE", "DATE");

				if(rs.getString("result").equals("win")) w++;
				if(rs.getString("result").equals("tie")) t++;
				if(rs.getString("result").equals("loss")) l++;
				if(rs.getString("meal_size").equals("pass")) p++;
				if(rs.getString("meal_size").equals("fail")) f++;
				if(rs.getString("time").equals("breakfast")) b++;
				if(rs.getString("time").equals("lunch")) lu++;
				if(rs.getString("time").equals("dinner")) d++;

				System.out.printf("%-3d %-10s %-7s %-5s %tF %n", 
					rs.getInt("id"), rs.getString("time"), rs.getString("result"), rs.getString("meal_size"), rs.getDate("date_consumed"));

				count++;
			}
				
			if(count > 0){
				System.out.println("\nWINS: " + w + " TIES: " + t + " LOSSES: " + l);
				System.out.println("SIZE PASS: " + p + " SIZE FAIL: " + f);
				System.out.println("BREAKFAST: " + b + " LUNCH: " + lu + " DINNER: " + d);
				System.out.println("\n" + count + " results found");
			}
			else
				System.out.println("No results");

			rs.close();
		}
		catch(SQLException e){
			System.out.println("DB error: " + e);
		}
	}

	private void viewDaily()
	{
		//Start off pulling monthly results
		LocalDate today = LocalDate.now();
		String dateParam = today.withDayOfMonth(1).toString();
		String query = "SELECT * FROM " + DAILY_TBL + " WHERE date_affected >= '" + dateParam + "'";

		try
		{
			System.out.println("Daily results since: " + dateParam + "\n");

			rs = stmt.executeQuery(query);
			int count = 0;
			int mePass = 0;
			int meFail = 0;
			int exPass = 0;
			int exFail = 0;
			int alPass = 0;
			int alFail = 0;
			int suPass = 0;
			int suFail = 0;
			int qsPass = 0;
			int qsFail = 0;

			while(rs.next())
			{
				if(rs.getInt("meals_passed") == 1)
					mePass++;
				else
					meFail++;

				if(rs.getInt("qty_passed") == 1)
					qsPass++;
				else
					qsFail++;

				if(rs.getInt("exercised") == 1)
					exPass++;
				else
					exFail++;

				if(rs.getInt("alcohol") == 1)
					alFail++;
				else
					alPass++;

				if(rs.getInt("sugar") == 1)
					suFail++;
				else
					suPass++;

				count++;
			}

			
			if(count > 0)
			{
				//Show results
				System.out.println("Ate Healthly: " + mePass);
				System.out.println("Ate Poorly: " + meFail);
				System.out.println("Ate Good Sizes: " + qsPass);
				System.out.println("Ate Poor Sizes: " + qsFail);
				System.out.println("Exercised: " + exPass);
				System.out.println("Lazy:" + exFail);
				System.out.println("Drank: " + alFail);
				System.out.println("Sober: " + alPass);
				System.out.println("Sugar Free: " + suPass);
				System.out.println("Sugar Fails: " + suFail);
				System.out.println("\n" + count + " days logged");
			}
			else
				System.out.println("No results");

			rs.close();
		}
		catch(SQLException e)
		{
			System.out.println("Problem accessing data: " + e);
		}
	}

	private void checkLastWeight()
	{
		String t = this.formattedDate();
		String q = "SELECT date_w FROM " + WEIGHT_TBL + " ORDER By date_w DESC LIMIT 1";
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String last = "";
		try{
			rs = stmt.executeQuery(q);
			while(rs.next())
				last = rs.getString("date_w");
			
			if(last != ""){
				try{
					Date d1 = df.parse(t);
					Date d2 = df.parse(last);
					long diff = d1.getTime() - d2.getTime();
					float r = (diff / (1000 * 60 * 60 * 24));
					if(r >= 7)
						System.out.println("\nWARNING: It's been " + r + " days since last weight entered!\n");
				}
				catch(ParseException e){
					e.printStackTrace();
				}
			}
		}
		catch(SQLException e){
			System.out.println("Problem accessing data: " + e);
		}
	}

	private void checkLastWhys()
	{
		String today = this.formattedDate();
		String query = "SELECT viewed_whys FROM " + ACTIONS_TBL + " ORDER BY viewed_whys DESC LIMIT 1";
		int i = 0;
		String lastViewed = "";
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		try
		{
			rs = stmt.executeQuery(query);
			while(rs.next()){
				lastViewed = rs.getString("viewed_whys");
				i++;
			}

			if(i == 1 && lastViewed != "")
			{
				try
				{
					Date date1 = df.parse(today);
					Date date2 = df.parse(lastViewed);
					long diff = date1.getTime() - date2.getTime();
					float result = (diff / (1000 * 60 * 60 * 24));

					if(result >= 7)
						System.out.println("\nWARNING: It's been " + result + " days since WHY's viewed!\n");

				}
				catch(ParseException e){
					e.printStackTrace();
				}
			}
			else
				System.out.println("Something wrong with checkLastWhys() query");
		}
		catch(SQLException e){
			System.out.println("Problem access data: " + e);
		}
	}

	public static void main(String[] args)
	{
		MealLog m = new MealLog();
		Scanner read = new Scanner(System.in);

		boolean stop = false;

		m.menu();
		m.checkLastWhys();
		m.checkLastWeight();

		while(stop == false)
		{
			System.out.println("\nSelect an option (3 for menu)\n");
			String value = read.next();

			if(value != "")
			{
				int entered = validateInt(value);
				if(entered != -1){
					switch(entered)
					{
						case 1:
							System.out.println("Exiting Meal Log");
							stop = true;
							break;
						case 2:
							m.getWhys();
							break;
						case 3:
							m.menu();
							break;
						case 4:
							m.logMeal();
							break;
						case 5:
							m.viewMeals();
							break;
						case 6:
							m.logDaily();
							break;
						case 7:
							m.viewDaily();
							break;
						case 8:
							m.logWeight();
							break;
						case 9:
							m.viewWeightLog();
							break;
						default:
							System.out.println("Not a valid entry");
							break;
					}
				}
				else
					System.out.println(value + " not a valid integer");
			}
		}
		
		try{
			m.db.close();
		}
		catch(SQLException e){
			System.out.println("DB Error: " + e);
		}
		
		System.exit(0);	
	}
}