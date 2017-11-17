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

	public MealLog()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
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
		String query = "SELECT why FROM whys ORDER BY why";
		System.out.println("Reasons to lose weight\n");
		
		int counter = 1;

		try
		{
			rs = stmt.executeQuery(query);
			while(rs.next())
			{
				System.out.println(counter + ". " + rs.getString(1));
				System.out.println("-------------------------------");
				counter++;
			}
				

			rs.close();

		}  
		catch(SQLException e)
		{
			System.out.println("DB Error: " + e);
		}

	}

	private void menu()
	{
		String[] menu = {
			"1. Close application",
			"2. View my whys",
			"3. View menu",
			"4. Log a meal",
			"5. View meals",
			"6. Log daily results"
		};

		for(int i = 0; i < menu.length; i++)
			System.out.println(menu[i]);
	}

	private void logMeal()
	{
		System.out.println("\nEnter meal period, (win/tie/loss), & date (separated by commas):\n");

		Scanner mealRead = new Scanner(System.in);
		String mealValue = mealRead.nextLine();

		String[] ent = mealValue.split(",");

		if(ent.length == 3){
			String meal = ent[0].trim();
			String pass = ent[1].trim();
			String date = ent[2].trim();

			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Date pDate;
			boolean mealValidated = true;
			boolean passValidated = true;

			if(!Arrays.asList(MEALS).contains(meal.toLowerCase()))
				mealValidated = false;
			if(!Arrays.asList(RESULT).contains(pass.toLowerCase()))
				passValidated = false;

			if(mealValidated && passValidated)
			{
				try{
					pDate = df.parse(date);
					String saveDate = df.format(pDate);

					try{
						String query = "INSERT INTO meals (time, result, date_consumed) VALUES (?, ?, ?)";

						PreparedStatement stmt = db.prepareStatement(query);
						stmt.setString(1, meal);
						stmt.setString(2, pass);
						stmt.setString(3, saveDate);
						stmt.execute();

						System.out.println("Meal successfully logged");
					}
					catch(SQLException e){
						System.out.println("DB insert error: " + e);
					}
				}
				catch(ParseException e){
					System.out.println("Date validation failed: " + e);
				}	
			}
			else
			{
				if(!mealValidated)
					System.out.println(meal + " is not a valid meal period");
				if(!passValidated)
					System.out.println(pass + " is not a valid pass/fail option");
			}

		}
		else
			System.out.println("Error: 3 values must be entered");

	}

	private void logDaily()
	{
		System.out.println("Enter 1 or 0 (true/false) - daily Exercise, alcohol, & sugar (comma separated):\n");

		Scanner entered = new Scanner(System.in);
		String values = entered.nextLine();
		String[] ent = values.split(",");

		if(ent.length == 3)
		{
			String ex = ent[0].trim();
			String al = ent[1].trim();
			String su = ent[2].trim();

			try
			{
				int exercised = Integer.parseInt(ex);
				int alcohol = Integer.parseInt(al);
				int sugar = Integer.parseInt(su);
				boolean exPass = true;
				boolean alPass = true;
				boolean suPass = true;

				if(exercised != 0 || exercised != 1) exPass = false;
				if(alcohol != 0 || alcohol != 1) alPass = false;
				if(sugar != 0 || sugar != 1) suPass = false;

				//if(exPass && alPass && suPass)
				//{
					Date today = new Date();
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					String saveDate = df.format(today);
					String query = "INSERT INTO daily (exercised, alcohol, sugar, date_affected) VALUES (?, ?, ?, ?)";

					try
					{
						PreparedStatement stmt = db.prepareStatement(query);
						stmt.setInt(1, exercised);
						stmt.setInt(2, alcohol);
						stmt.setInt(3, sugar);
						stmt.setString(4, saveDate);
						stmt.execute();

						System.out.println("Daily result successfully logged");
					}
					catch(SQLException e)
					{
						System.out.println("Problem inserting data: " + e);
					}
				//}
				//else
				//{
				//	if(!exPass) System.out.println(exercised + " is not an allowed exercised value");
				//	if(!alPass) System.out.println(alcohol + " is not an allowed alcohol value");
				//	if(!suPass) System.out.println(sugar + " is not an allowed sugar value");
				//}
			}
			catch(NumberFormatException e)
			{
				System.out.println("Invalid integer entered: " + e);
			}
		}
		else
			System.out.println("Only 3 values can be entered\n");
	}

	private void viewMeals()
	{
		//TODO set up for today, then a specific date
		LocalDate today = LocalDate.now();
		String dateParam = today.withDayOfMonth(1).toString();
		String query = "SELECT * FROM meals WHERE date_consumed >= '" + dateParam + "' ORDER BY date_consumed DESC";

		System.out.println("Meals eaten since " + dateParam);

		try
		{
			rs = stmt.executeQuery(query);
			int count = 0;

			while(rs.next()){

				if(count == 0)
					System.out.printf("%-3s %-8s %-9s %-7s %-6s %n", "ID", "MEAL", "RESULT", "SUGAR", "DATE");

				System.out.printf("%-2d  %-8s  %-8s  %-6s %tF %n", 
					rs.getInt("id"), rs.getString("time"), rs.getString("result"), rs.getString("sugar"), rs.getDate("date_consumed"));

				count++;
			}
				
			if(count > 0)
				System.out.println("\n" + count + " results found");
			else
				System.out.println("No results");

			rs.close();
		}
		catch(SQLException e){
			System.out.println("DB error: " + e);
		}
	}

	public static void main(String[] args)
	{
		MealLog m = new MealLog();
		Scanner read = new Scanner(System.in);

		boolean stop = false;

		m.menu();

		while(stop == false)
		{
			System.out.println("\nSelect an option\n");
			String value = read.next();

			if(value != "")
			{
				try
				{
					
					int entered = Integer.parseInt(value);

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
						default:
							System.out.println("Not a valid entry");
							break;
					}
				}
				catch(NumberFormatException e)
				{
					System.out.println(value + " not a valid integer: " + e);
				}
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